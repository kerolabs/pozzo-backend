#!/bin/bash
# Prepares an Ubuntu 24.04 instance of Oracle Cloud (Always Free, 1 GB) to run the Pozzo backend behind Caddy,
# deployed by the Deploy workflow of GitHub Actions. It can run again: every step checks what is already done.
#
# Before running it, as the admin user:
#   sudo useradd --create-home --shell /bin/bash deploy
#   sudo useradd --system --no-create-home --shell /usr/sbin/nologin pozzo
#   sudo install -d -m 700 -o deploy -g deploy /home/deploy/.ssh
# and open TCP 80 and 443 in the Security List of the subnet.
#
# Usage, from the repository folder on the machine with SSH access (the status page travels with it):
#   { printf 'STATUS_SERVER=%q\n' "$(cat deploy/status/status_server.py)";
#     printf 'STATUS_PAGE=%q\n' "$(cat deploy/status/index.html)";
#     printf 'STATUS_LOGIN=%q\n' "$(cat deploy/status/login.html)"; cat deploy/oracle-setup.sh; } |
#     ssh ubuntu@<ip> 'sudo bash -s -- <domain> "<public deploy key>" <status user> "<password hash>"'
# <domain> may list several names, e.g. "api-kerolabs.duckdns.org, 147-5-100-8.sslip.io".
# The password hash comes from: python3 deploy/status/status_server.py hash-password
set -euo pipefail
DOMAIN="${1:?The domain is required, e.g. api-kerolabs.duckdns.org}"
DEPLOY_PUBLIC_KEY="${2:?The public deploy key is required}"
STATUS_USER="${3:?The user of the status page is required}"
STATUS_PASSWORD_HASH="${4:?The PBKDF2 hash of its password is required (status_server.py hash-password)}"
# The status page itself travels in the environment: STATUS_SERVER, STATUS_PAGE and STATUS_LOGIN (see above).
: "${STATUS_SERVER:?}" "${STATUS_PAGE:?}" "${STATUS_LOGIN:?}"
export DEBIAN_FRONTEND=noninteractive

echo "== 1. Swap of 2 GB, so Java does not run out of memory on 1 GB"
if [ ! -f /swapfile ]; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile >/dev/null
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
echo 'vm.swappiness=10' > /etc/sysctl.d/90-pozzo-swap.conf
sysctl -q -p /etc/sysctl.d/90-pozzo-swap.conf
systemctl disable --now fwupd.service >/dev/null 2>&1 || true

echo "== 2. Ports 80 and 443 in the firewall of the server (Oracle images only open 22)"
for port in 80 443; do
  if ! iptables -C INPUT -p tcp -m state --state NEW -m tcp --dport $port -j ACCEPT 2>/dev/null; then
    line=$(iptables -L INPUT --line-numbers | awk '/REJECT/ {print $1; exit}')
    iptables -I INPUT "${line:-1}" -p tcp -m state --state NEW -m tcp --dport $port -j ACCEPT
  fi
done
if command -v netfilter-persistent >/dev/null; then
  netfilter-persistent save >/dev/null
else
  iptables-save > /etc/iptables/rules.v4
fi

echo "== 3. Java 21, curl and Caddy"
apt-get update -qq
apt-get install -y -qq openjdk-21-jre-headless curl gnupg debian-keyring debian-archive-keyring apt-transport-https >/dev/null
if [ ! -f /usr/share/keyrings/caddy-stable-archive-keyring.gpg ]; then
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' > /etc/apt/sources.list.d/caddy-stable.list
  apt-get update -qq
fi
apt-get install -y -qq caddy >/dev/null
java -version 2>&1 | head -1
caddy version

echo "== 4. Folders: incoming jars from GitHub, releases only root can change, settings only pozzo reads"
install -d -m 755 -o root -g root /opt/pozzo /opt/pozzo/releases
install -d -m 750 -o deploy -g deploy /opt/pozzo/incoming
id pozzostatus >/dev/null 2>&1 || useradd --system --no-create-home --shell /usr/sbin/nologin \
  --groups systemd-journal pozzostatus
install -d -m 2750 -o deploy -g pozzostatus /opt/pozzo/status
install -d -m 750 -o root -g pozzo /etc/pozzo
if [ ! -f /etc/pozzo/pozzo.env ]; then
  cat > /etc/pozzo/pozzo.env <<'ENV'
# Settings of the Pozzo backend on this server. Copy the values from the Environment tab of the Render service.
# Only root and the pozzo service can read this file. Edit it with: sudo nano /etc/pozzo/pozzo.env
# then apply it with: sudo systemctl restart pozzo

# Database (Supabase, session pooler)
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=

# Signing key of the sessions; use the same as Render so sessions keep working when the app switches
JWT_SECRET=

# SMS with SMS Gate
SMS_PROVIDER=smsgate
SMSGATE_USERNAME=
SMSGATE_PASSWORD=
SMS_TEST_NUMBERS=
SMS_TEST_CODE=

# Email with Brevo (account recovery)
EMAIL_PROVIDER=brevo
BREVO_API_KEY=
EMAIL_FROM_ADDRESS=

# Push notifications with Firebase Cloud Messaging (the service account JSON in Base64)
PUSH_PROVIDER=fcm
FCM_SERVICE_ACCOUNT=

# Profile photos and receipt images (Supabase Storage)
SUPABASE_URL=
SUPABASE_SECRET_KEY=
ENV
fi
chown root:pozzo /etc/pozzo/pozzo.env
chmod 640 /etc/pozzo/pozzo.env

echo "== 5. The pozzo service: Java with a heap sized for 1 GB, only reachable through Caddy"
cat > /etc/systemd/system/pozzo.service <<'UNIT'
[Unit]
Description=Pozzo RESTful services
After=network-online.target
Wants=network-online.target
# Nothing to start until the first deploy has put a release in place
ConditionPathExists=/opt/pozzo/current.jar

[Service]
User=pozzo
Group=pozzo
Environment=PORT=8080 SERVER_ADDRESS=127.0.0.1
EnvironmentFile=/etc/pozzo/pozzo.env
ExecStart=/usr/bin/java -Xms128m -Xmx320m -XX:MaxMetaspaceSize=200m -XX:ReservedCodeCacheSize=64m \
  -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError \
  -Djava.security.egd=file:/dev/./urandom -jar /opt/pozzo/current.jar
Restart=always
RestartSec=10
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true

[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable pozzo.service >/dev/null 2>&1

echo "== 6. Deploy scripts: GitHub only hands over a jar; root checks it, switches and rolls back"
cat > /usr/local/bin/pozzo-deploy <<'SCRIPT'
#!/bin/bash
# The only command the GitHub deploy key may run (forced in authorized_keys of deploy).
# It reads the new jar from standard input and asks pozzo-activate to put it in place. The workflow may
# send "<run id> <commit>" as its command, which is kept in the records of the status page.
set -euo pipefail
umask 027
run_id=""; commit=""
if [[ "${SSH_ORIGINAL_COMMAND:-}" =~ ^([0-9]{1,20})\ ([0-9a-f]{40})$ ]]; then
  run_id="${BASH_REMATCH[1]}"; commit="${BASH_REMATCH[2]}"
fi
name="pozzo-$(date -u +%Y%m%d%H%M%S).jar"
incoming="/opt/pozzo/incoming/$name"
status=/opt/pozzo/status/current.json
printf '{"state":"receiving","release":"%s","runId":"%s","commit":"%s","receivedAt":%s}\n' \
  "$name" "$run_id" "$commit" "$(date +%s)" > "$status"
head -c 200000000 > "$incoming"
size=$(stat -c %s "$incoming")
if [ "$size" -lt 1000000 ] || [ "$(head -c 2 "$incoming")" != "PK" ]; then
  rm -f "$incoming" "$status"
  echo "What arrived is not a jar ($size bytes)" >&2
  exit 1
fi
echo "Received $name ($((size / 1024 / 1024)) MB)"
exec sudo /usr/local/bin/pozzo-activate "$name" "$run_id" "$commit"
SCRIPT
cat > /usr/local/bin/pozzo-activate <<'SCRIPT'
#!/bin/bash
# Run by root through sudo: copies a received jar where only root can write, restarts the service and waits
# until it answers healthy. If it does not come up, it goes back to the previous release.
set -euo pipefail
name="${1:-}"; run_id="${2:-}"; commit="${3:-}"
[[ "$name" =~ ^pozzo-[0-9]{14}\.jar$ ]] || { echo "Invalid release name" >&2; exit 2; }
[[ "$run_id" =~ ^[0-9]{0,20}$ && "$commit" =~ ^([0-9a-f]{40})?$ ]] || { echo "Invalid run or commit" >&2; exit 2; }
status_dir=/opt/pozzo/status
received_at=$(date +%s)

# Records for the status page: the stage of the deploy in progress, and one line per deploy when it ends.
record() {
  printf '{"state":"%s","release":"%s","runId":"%s","commit":"%s","receivedAt":%s,"startedAt":%s}\n' \
    "$1" "$name" "$run_id" "$commit" "$received_at" "${started_at:-null}" > "$status_dir/current.json"
}
finish() {
  printf '{"state":"%s","release":"%s","runId":"%s","commit":"%s","receivedAt":%s,"startedAt":%s,"finishedAt":%s,"startSeconds":%s}\n' \
    "$1" "$name" "$run_id" "$commit" "$received_at" "$started_at" "$(date +%s)" "$(( $(date +%s) - started_at ))" \
    >> "$status_dir/history.jsonl"
  rm -f "$status_dir/current.json"
}
incoming="/opt/pozzo/incoming/$name"
release="/opt/pozzo/releases/$name"
[ -f "$incoming" ] || { echo "Release not found" >&2; exit 2; }
install -m 644 -o root -g root "$incoming" "$release"
rm -f "$incoming"
previous=$(readlink -f /opt/pozzo/current.jar 2>/dev/null || true)

healthy() {
  # A small CPU takes a while to start Spring: up to five minutes
  for _ in $(seq 1 60); do
    sleep 5
    if curl -fsS --max-time 4 http://127.0.0.1:8080/actuator/health 2>/dev/null | grep -q '"status":"UP"'; then
      return 0
    fi
  done
  return 1
}

ln -sfn "$release" /opt/pozzo/current.jar
started_at=$(date +%s)
record starting
since=$(date '+%Y-%m-%d %H:%M:%S')
systemctl restart pozzo
echo "Started $name, waiting for /actuator/health. Log of the start:"
# The log of the service goes to whoever deployed (the workflow of GitHub) while it starts
journalctl -u pozzo -f -o cat --since "$since" &
follower=$!
trap 'kill $follower 2>/dev/null || true' EXIT
if healthy; then
  kill $follower 2>/dev/null || true
  echo "UP: $name is serving, after $(( $(date +%s) - started_at )) s"
  finish up
  # Keep the three newest releases, so there is always one to go back to
  ls -1t /opt/pozzo/releases/pozzo-*.jar | tail -n +4 | xargs -r rm -f
  exit 0
fi
kill $follower 2>/dev/null || true
echo "$name did not come up after $(( $(date +%s) - started_at )) s" >&2
if [ -n "$previous" ] && [ -f "$previous" ] && [ "$previous" != "$release" ]; then
  ln -sfn "$previous" /opt/pozzo/current.jar
  systemctl restart pozzo
  echo "Rolled back to $(basename "$previous")" >&2
  finish rolled_back
else
  # Nothing to go back to: stop it instead of restarting it forever on a small CPU
  systemctl stop pozzo
  echo "No previous release to go back to; the service is stopped" >&2
  finish stopped
fi
exit 1
SCRIPT
chown root:root /usr/local/bin/pozzo-deploy /usr/local/bin/pozzo-activate
chmod 755 /usr/local/bin/pozzo-deploy /usr/local/bin/pozzo-activate

echo "== 7. deploy may run pozzo-activate as root, and nothing else"
echo 'deploy ALL=(root) NOPASSWD: /usr/local/bin/pozzo-activate' > /etc/sudoers.d/pozzo-deploy
chmod 440 /etc/sudoers.d/pozzo-deploy
visudo -cqf /etc/sudoers.d/pozzo-deploy

echo "== 8. The GitHub key can only run pozzo-deploy: no shell, no forwarding"
echo "restrict,command=\"/usr/local/bin/pozzo-deploy\" $DEPLOY_PUBLIC_KEY" > /home/deploy/.ssh/authorized_keys
chown deploy:deploy /home/deploy/.ssh/authorized_keys
chmod 600 /home/deploy/.ssh/authorized_keys

echo "== 9. The status page: a small service that only reads, with its own login, served by Caddy under /status"
install -d -m 755 /opt/pozzo-status
install -m 644 /dev/stdin /opt/pozzo-status/status_server.py <<< "$STATUS_SERVER"
install -m 644 /dev/stdin /opt/pozzo-status/index.html <<< "$STATUS_PAGE"
install -m 644 /dev/stdin /opt/pozzo-status/login.html <<< "$STATUS_LOGIN"
# The login: user, password hash and the secret that signs the session cookies. Keeping the secret keeps the
# sessions open across runs of this script; a new password does not need a new secret.
install -d -m 750 -o root -g pozzostatus /etc/pozzo-status
secret=$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["sessionSecret"])' \
  /etc/pozzo-status/config.json 2>/dev/null || python3 -c 'import secrets; print(secrets.token_urlsafe(48))')
python3 -c 'import json,sys; print(json.dumps({"user": sys.argv[1], "passwordHash": sys.argv[2], "sessionSecret": sys.argv[3]}))' \
  "$STATUS_USER" "$STATUS_PASSWORD_HASH" "$secret" | install -m 640 -o root -g pozzostatus /dev/stdin /etc/pozzo-status/config.json
cat > /etc/systemd/system/pozzo-status.service <<'UNIT'
[Unit]
Description=Status page of the Pozzo backend
After=network-online.target

[Service]
User=pozzostatus
Group=pozzostatus
ExecStart=/usr/bin/python3 /opt/pozzo-status/status_server.py
Restart=always
RestartSec=5
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true

[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable pozzo-status.service >/dev/null 2>&1
systemctl restart pozzo-status.service

echo "== 10. Caddy: HTTPS for $DOMAIN in front of the backend and of the status page"
cat > /etc/caddy/Caddyfile <<CADDY
$DOMAIN {
	handle /status* {
		reverse_proxy 127.0.0.1:8090 {
			flush_interval -1
		}
	}
	handle {
		encode gzip
		reverse_proxy 127.0.0.1:8080
	}
}
CADDY
caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile >/dev/null 2>&1
systemctl reload caddy || systemctl restart caddy

echo "== Done"
free -m | sed -n 1,3p
