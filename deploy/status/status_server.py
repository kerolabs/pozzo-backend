#!/usr/bin/env python3
"""Status page of the Pozzo backend on its server: health, deploys, resources and the live log.

It only reads: the health check of the backend, the state of its systemd service, the records the deploy
scripts leave in /opt/pozzo/status, /proc, and the runs of the Deploy workflow on GitHub. Caddy serves it
under /status; it listens only on 127.0.0.1. Standard library only.

Access goes through a login page: the password is checked once against a PBKDF2 hash and the browser keeps a
signed, HttpOnly session cookie for 30 days. To make the hash of a new password:
    python3 status_server.py hash-password
"""
import base64
import getpass
import hashlib
import hmac
import json
import os
import queue
import re
import secrets
import shutil
import subprocess
import sys
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

PORT = int(os.environ.get("STATUS_PORT", "8090"))
HEALTH_URL = os.environ.get("STATUS_HEALTH_URL", "http://127.0.0.1:8080/actuator/health")
SERVICE = os.environ.get("STATUS_SERVICE", "pozzo")
REPOSITORY = os.environ.get("STATUS_REPOSITORY", "kerolabs/pozzo-backend")
WORKFLOW = os.environ.get("STATUS_WORKFLOW", "deploy.yml")
STATUS_DIR = Path(os.environ.get("STATUS_DIR", "/opt/pozzo/status"))
RELEASES_DIR = Path("/opt/pozzo/releases")
CURRENT_JAR = Path("/opt/pozzo/current.jar")
PAGE = Path(__file__).with_name("index.html")
LOGIN_PAGE = Path(__file__).with_name("login.html")
# {"user": "...", "passwordHash": "pbkdf2_sha256$...", "sessionSecret": "..."}, readable only by this service
CONFIG = Path(os.environ.get("STATUS_CONFIG", "/etc/pozzo-status/config.json"))
COOKIE = "pozzo_status"
SESSION_SECONDS = 30 * 24 * 3600
PBKDF2_ITERATIONS = 200_000


def hash_password(password):
    salt = secrets.token_bytes(16)
    digest = hashlib.pbkdf2_hmac("sha256", password.encode(), salt, PBKDF2_ITERATIONS)
    encode = lambda raw: base64.b64encode(raw).decode()  # noqa: E731
    return f"pbkdf2_sha256${PBKDF2_ITERATIONS}${encode(salt)}${encode(digest)}"


def verify_password(password, stored):
    try:
        scheme, iterations, salt, digest = stored.split("$")
        if scheme != "pbkdf2_sha256":
            return False
        actual = hashlib.pbkdf2_hmac("sha256", password.encode(), base64.b64decode(salt), int(iterations))
        return hmac.compare_digest(actual, base64.b64decode(digest))
    except (ValueError, TypeError):
        return False


class Auth:
    """The login of the page: one user, a password hash and a secret to sign the session cookies."""

    def __init__(self):
        config = json.loads(CONFIG.read_text())
        self.user = config["user"]
        self.password_hash = config["passwordHash"]
        self.secret = config["sessionSecret"].encode()
        self._failures = {}
        self._lock = threading.Lock()

    def check(self, user, password):
        return hmac.compare_digest(user.encode(), self.user.encode()) and verify_password(password, self.password_hash)

    def blocked(self, client):
        """Ten wrong passwords in fifteen minutes from one address lock it out for the rest of that window."""
        with self._lock:
            now = time.time()
            recent = [moment for moment in self._failures.get(client, []) if now - moment < 900]
            self._failures[client] = recent
            return len(recent) >= 10

    def failed(self, client):
        with self._lock:
            self._failures.setdefault(client, []).append(time.time())

    def issue(self):
        expires = int(time.time()) + SESSION_SECONDS
        payload = f"{self.user}|{expires}"
        signature = hmac.new(self.secret, payload.encode(), hashlib.sha256).hexdigest()
        return f"{payload}|{signature}"

    def valid(self, cookie_header):
        for part in (cookie_header or "").split(";"):
            name, _, value = part.strip().partition("=")
            if name != COOKIE:
                continue
            try:
                user, expires, signature = urllib.parse.unquote(value).split("|")
            except ValueError:
                return False
            expected = hmac.new(self.secret, f"{user}|{expires}".encode(), hashlib.sha256).hexdigest()
            return (hmac.compare_digest(signature, expected) and user == self.user
                    and expires.isdigit() and int(expires) > time.time())
        return False


class GitHubRuns:
    """The latest runs of the Deploy workflow. Asked every 15 s with an ETag: an unchanged answer (304) does
    not count against the 60 requests per hour GitHub allows without a token."""

    def __init__(self):
        self._runs = []
        self._etag = None
        self._error = None
        self._lock = threading.Lock()
        threading.Thread(target=self._loop, daemon=True).start()

    def _loop(self):
        while True:
            self._refresh()
            time.sleep(15)

    def _refresh(self):
        url = f"https://api.github.com/repos/{REPOSITORY}/actions/workflows/{WORKFLOW}/runs?per_page=15"
        request = urllib.request.Request(url, headers={"Accept": "application/vnd.github+json",
                                                       "User-Agent": "pozzo-status"})
        if self._etag:
            request.add_header("If-None-Match", self._etag)
        try:
            with urllib.request.urlopen(request, timeout=10) as response:
                data = json.load(response)
                runs = [{
                    "id": run["id"],
                    "number": run["run_number"],
                    "status": run["status"],
                    "conclusion": run["conclusion"],
                    "commit": run["head_sha"],
                    "title": run.get("display_title") or "",
                    "actor": (run.get("actor") or {}).get("login", ""),
                    "createdAt": run["created_at"],
                    "updatedAt": run["updated_at"],
                    "url": run["html_url"],
                } for run in data.get("workflow_runs", [])]
                with self._lock:
                    self._runs, self._etag, self._error = runs, response.headers.get("ETag"), None
        except urllib.error.HTTPError as error:
            if error.code != 304:
                with self._lock:
                    self._error = f"GitHub answered {error.code}"
        except Exception as error:  # noqa: BLE001 - the page shows it instead of failing
            with self._lock:
                self._error = f"GitHub could not be reached: {error.__class__.__name__}"

    def snapshot(self):
        with self._lock:
            return {"runs": list(self._runs), "error": self._error}


GITHUB = GitHubRuns()


def health():
    started = time.monotonic()
    try:
        with urllib.request.urlopen(HEALTH_URL, timeout=3) as response:
            status = json.load(response).get("status", "UNKNOWN")
    except Exception:  # noqa: BLE001 - down or starting
        status = "DOWN"
    return {"status": status, "millis": round((time.monotonic() - started) * 1000)}


def service_state():
    keys = ["ActiveState", "SubState", "ActiveEnterTimestamp", "NRestarts", "MainPID"]
    output = subprocess.run(["systemctl", "show", SERVICE, "-p", ",".join(keys)],
                            capture_output=True, text=True, timeout=5).stdout
    values = dict(line.split("=", 1) for line in output.splitlines() if "=" in line)
    since = None
    if values.get("ActiveEnterTimestamp"):
        parsed = subprocess.run(["date", "-d", values["ActiveEnterTimestamp"], "+%s"],
                                capture_output=True, text=True, timeout=5).stdout.strip()
        since = int(parsed) if parsed.isdigit() else None
    memory = None
    pid = values.get("MainPID", "0")
    if pid.isdigit() and pid != "0":
        try:
            rss = re.search(r"VmRSS:\s+(\d+)", Path(f"/proc/{pid}/status").read_text())
            memory = int(rss.group(1)) * 1024 if rss else None
        except OSError:
            pass
    return {"active": values.get("ActiveState"), "sub": values.get("SubState"), "since": since,
            "restarts": int(values.get("NRestarts", "0") or 0), "memory": memory}


def resources():
    info = {}
    for line in Path("/proc/meminfo").read_text().splitlines():
        key, value = line.split(":", 1)
        info[key] = int(value.split()[0]) * 1024
    disk = shutil.disk_usage("/")
    load = os.getloadavg()
    uptime = float(Path("/proc/uptime").read_text().split()[0])
    return {
        "memory": {"total": info["MemTotal"], "available": info["MemAvailable"]},
        "swap": {"total": info["SwapTotal"], "used": info["SwapTotal"] - info["SwapFree"]},
        "disk": {"total": disk.total, "used": disk.used},
        "load": [round(value, 2) for value in load],
        "cpus": os.cpu_count(),
        "uptime": round(uptime),
    }


def read_json(path):
    try:
        return json.loads(path.read_text())
    except (OSError, ValueError):
        return None


def deploys():
    history = []
    try:
        for line in (STATUS_DIR / "history.jsonl").read_text().splitlines()[-30:]:
            try:
                history.append(json.loads(line))
            except ValueError:
                pass
    except OSError:
        pass
    current = CURRENT_JAR.resolve().name if CURRENT_JAR.exists() else None
    releases = sorted((path.name for path in RELEASES_DIR.glob("pozzo-*.jar")), reverse=True)
    return {"inProgress": read_json(STATUS_DIR / "current.json"), "history": list(reversed(history)),
            "currentRelease": current, "releases": releases}


AUTH = None  # loaded at start, so a broken config stops the service instead of opening the page


class Handler(BaseHTTPRequestHandler):
    server_version = "pozzo-status"

    def _client(self):
        # Caddy, the only way in, puts the address of the browser first in X-Forwarded-For.
        return (self.headers.get("X-Forwarded-For") or self.client_address[0]).split(",")[0].strip()

    def do_POST(self):
        if self.path.split("?", 1)[0].rstrip("/") != "/status/login":
            self._send(404, b"Not found", "text/plain")
            return
        client = self._client()
        if AUTH.blocked(client):
            self._redirect("/status/login?error=blocked")
            return
        length = min(int(self.headers.get("Content-Length") or 0), 4096)
        form = urllib.parse.parse_qs(self.rfile.read(length).decode(errors="replace"))
        user = form.get("user", [""])[0]
        password = form.get("password", [""])[0]
        if not AUTH.check(user, password):
            AUTH.failed(client)
            time.sleep(1)
            self._redirect("/status/login?error=1")
            return
        cookie = (f"{COOKIE}={urllib.parse.quote(AUTH.issue())}; Path=/status; Max-Age={SESSION_SECONDS}; "
                  "HttpOnly; Secure; SameSite=Strict")
        self._redirect("/status", cookie)

    def do_GET(self):
        path = self.path.split("?", 1)[0].rstrip("/")
        if path == "/status/login":
            self._send(200, LOGIN_PAGE.read_bytes(), "text/html; charset=utf-8")
            return
        if path == "/status/logout":
            self._redirect("/status/login", f"{COOKIE}=; Path=/status; Max-Age=0; HttpOnly; Secure; SameSite=Strict")
            return
        if not AUTH.valid(self.headers.get("Cookie")):
            if path.startswith("/status/api/"):
                self._send(401, b'{"error":"login required"}', "application/json")
            else:
                self._redirect("/status/login")
            return
        if path in ("/status", ""):
            self._send(200, PAGE.read_bytes(), "text/html; charset=utf-8")
        elif path == "/status/api/summary":
            body = {"health": health(), "service": service_state(), "resources": resources(),
                    "deploys": deploys(), "github": GITHUB.snapshot(), "repository": REPOSITORY,
                    "now": int(time.time())}
            self._send(200, json.dumps(body).encode(), "application/json")
        elif path == "/status/api/logs":
            self._stream_logs()
        else:
            self._send(404, b"Not found", "text/plain")

    def _send(self, status, body, content_type):
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("X-Frame-Options", "DENY")
        self.send_header("Referrer-Policy", "no-referrer")
        self.end_headers()
        self.wfile.write(body)

    def _redirect(self, location, cookie=None):
        self.send_response(303)
        self.send_header("Location", location)
        self.send_header("Cache-Control", "no-store")
        if cookie:
            self.send_header("Set-Cookie", cookie)
        self.send_header("Content-Length", "0")
        self.end_headers()

    def _stream_logs(self):
        """Server-sent events with the log of the service: the last 300 lines, then each new one."""
        self.send_response(200)
        self.send_header("Content-Type", "text/event-stream")
        self.send_header("Cache-Control", "no-store")
        self.send_header("X-Accel-Buffering", "no")
        self.end_headers()
        process = subprocess.Popen(["journalctl", "-u", SERVICE, "-f", "-n", "300", "-o", "short-iso",
                                    "--no-hostname"], stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
                                   text=True, bufsize=1)
        lines = queue.Queue()

        def read():
            for line in process.stdout:
                lines.put(line.rstrip())
            lines.put(None)

        threading.Thread(target=read, daemon=True).start()
        try:
            while True:
                try:
                    line = lines.get(timeout=15)
                except queue.Empty:
                    # A comment every 15 s keeps the connection open and finds out when the page was closed.
                    self.wfile.write(b": ping\n\n")
                    self.wfile.flush()
                    continue
                if line is None:
                    break
                self.wfile.write(f"data: {json.dumps(line)}\n\n".encode())
                self.wfile.flush()
        except (BrokenPipeError, ConnectionResetError):
            pass
        finally:
            process.kill()

    def log_message(self, *args):
        pass


if __name__ == "__main__":
    if sys.argv[1:] == ["hash-password"]:
        # Reads the password from the terminal, or from standard input when it is piped.
        password = getpass.getpass("Password: ") if sys.stdin.isatty() else sys.stdin.readline().rstrip("\n")
        print(hash_password(password))
        sys.exit(0)
    AUTH = Auth()
    ThreadingHTTPServer(("127.0.0.1", PORT), Handler).serve_forever()
