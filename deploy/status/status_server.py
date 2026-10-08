#!/usr/bin/env python3
"""Status page of the Pozzo backend on its server: health, deploys, resources and the live log.

It only reads: the health check of the backend, the state of its systemd service, the records the deploy
scripts leave in /opt/pozzo/status, /proc, and the runs of the Deploy workflow on GitHub. Caddy serves it
under /status behind a password; it listens only on 127.0.0.1. Standard library only.
"""
import json
import os
import queue
import re
import shutil
import subprocess
import threading
import time
import urllib.error
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


class Handler(BaseHTTPRequestHandler):
    server_version = "pozzo-status"

    def do_GET(self):
        path = self.path.split("?", 1)[0].rstrip("/")
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
        self.end_headers()
        self.wfile.write(body)

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
    ThreadingHTTPServer(("127.0.0.1", PORT), Handler).serve_forever()
