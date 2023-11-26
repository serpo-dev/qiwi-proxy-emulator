import requests
import signal
import sys
import urllib.parse
import ast
import re
from http.server import BaseHTTPRequestHandler, HTTPServer


class Storage:
    cache = dict()

    def add_to_cache(self):
        pass

    def delete_partner(self):
        pass

    def read_cache(self):
        pass

class ProjectProxy:
    def start_server(self):
        class ProxyHTTPRequestHandler(BaseHTTPRequestHandler):
            protocol_version = "HTTP/1.0"

            def do_GET(self):
                self._handle_request("get", requests.get)

            def do_DELETE(self):
                self._handle_request("delete", requests.delete)

            def do_POST(self):
                self._handle_request("post", requests.post)

            def do_PUT(self):
                self._handle_request("put", requests.put)

            def do_PATCH(self):
                self._handle_request("patch", requests.patch)

            def _handle_request(self, method, requests_func):
                url = self._resolve_url()

                if url is None:
                    self.send_response(404)
                    self.send_header("Content-Type", "application/json")
                    self.end_headers()
                    return

                body = (self.rfile.read(int(self.headers["content-length"]))).decode('UTF-8')
                body = ast.literal_eval(body)
                del body['id']

                headers = dict(self.headers)

                resp = requests_func(url, data=body, headers=headers)

                self.send_response(resp.status_code)
                self.end_headers()
                self.wfile.write(resp.content)

            def _resolve_url(self):
                parts = urllib.parse.urlparse(self.path)

                # get path without uuid in it
                rx = re.compile(r'\b[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}\b')
                parts._replace(path=re.sub(rx, '', parts.path))

                return parts.geturl()


        server_address = ('', 8000)
        self.httpd = HTTPServer(server_address, ProxyHTTPRequestHandler)
        self.httpd.serve_forever()

def exit_now(signum, frame):
    sys.exit(0)

if __name__ == '__main__':
    proxy = ProjectProxy()
    signal.signal(signal.SIGTERM, exit_now)
    proxy.start_server()
