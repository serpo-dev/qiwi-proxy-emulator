# кэш - просто глобальная переменная
# map{
#     qiwi-hackathon.free.beeceptor.com: [
#         request.path + base64(A(request.body)): resp obj (заголовки от тестового контура, полный ответ от тестового контура),
#     ],
#     sberbank_example: [
#         ...
#     ]
# }
#
# class внутри которого лежит переменная кэша
# и у класса функции - добавить, удалить по партнеру, читать
#
# A: функция, которая парсит тело
# составляет набор пар ключ-значения (array[string], string - key:value, key.subkey:value)
# сортировать набор лексографически (сортировать массив с предыдщего шага)
# склеить массив с каким-то сепаратов (запятая, пробел)
#
# проблема функции A: не отсеивает ключи идемпотентности (то есть надо бы еще из пар ключ значение убирать id)


import requests
import signal
import sys
import urllib.parse
import re
from http.server import BaseHTTPRequestHandler, HTTPServer


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
                print(url)

                if url is None:
                    self.send_response(404)
                    self.send_header("Content-Type", "application/json")
                    self.end_headers()
                    return

                body = self.rfile.read(int(self.headers["content-length"]))
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