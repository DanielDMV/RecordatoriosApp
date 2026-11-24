import json
import sqlite3
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import unquote, urlsplit

DB_PATH = Path(__file__).resolve().parent / "recordatorios_api.db"
HOST = "127.0.0.1"
PORT = 8765


def conectar():
    conexion = sqlite3.connect(DB_PATH, timeout=10)
    conexion.row_factory = sqlite3.Row
    return conexion


def inicializar():
    with conectar() as db:
        db.execute("""
            CREATE TABLE IF NOT EXISTS recordatorios (
                id TEXT PRIMARY KEY,
                operation_id TEXT NOT NULL UNIQUE,
                payload TEXT NOT NULL
            )
        """)


def respuesta_recordatorio(fila):
    datos = json.loads(fila["payload"])
    return {"id": fila["id"], **datos}


class Api(BaseHTTPRequestHandler):

    def responder(self, codigo, datos):
        cuerpo = json.dumps(
            datos, ensure_ascii=False
        ).encode("utf-8")

        self.send_response(codigo)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(cuerpo)))
        self.end_headers()
        self.wfile.write(cuerpo)

    def do_GET(self):
        ruta = urlsplit(self.path).path

        if ruta == "/health":
            return self.responder(200, {"estado": "ok"})

        if ruta == "/recordatorios":
            with conectar() as db:
                filas = db.execute(
                    "SELECT * FROM recordatorios ORDER BY rowid"
                ).fetchall()

            return self.responder(
                200, [respuesta_recordatorio(fila) for fila in filas]
            )

        if ruta.startswith("/operaciones/"):
            operation_id = unquote(
                ruta[len("/operaciones/"):]
            )

            if not operation_id or "/" in operation_id:
                return self.responder(400, {"error": "ID invalido"})

            with conectar() as db:
                fila = db.execute(
                    "SELECT * FROM recordatorios WHERE operation_id = ?",
                    (operation_id,)
                ).fetchone()

            if fila is None:
                return self.responder(
                    404, {"error": "Operacion no encontrada"}
                )

            return self.responder(
                200, respuesta_recordatorio(fila)
            )

        return self.responder(404, {"error": "Ruta no encontrada"})

    def do_POST(self):
        if urlsplit(self.path).path != "/recordatorios":
            return self.responder(404, {"error": "Ruta no encontrada"})

        operation_id = self.headers.get("Idempotency-Key", "").strip()

        if not operation_id or len(operation_id) > 128:
            return self.responder(
                400, {"error": "Idempotency-Key requerido"}
            )

        try:
            longitud = int(self.headers.get("Content-Length", "0"))

            if not 0 < longitud <= 65536:
                return self.responder(
                    413, {"error": "Longitud no permitida"}
                )

            datos = json.loads(
                self.rfile.read(longitud).decode("utf-8")
            )

            if not isinstance(datos, dict):
                raise ValueError("Objeto JSON requerido")

            campos = (
                "titulo", "descripcion", "categoria",
                "prioridad", "estado"
            )

            if set(datos) != set(campos):
                raise ValueError("Campos de recordatorio incorrectos")

            if any(
                not isinstance(datos[campo], str)
                or not datos[campo].strip()
                for campo in ("titulo", "categoria", "prioridad", "estado")
            ):
                raise ValueError("Campos obligatorios invalidos")

            if datos["descripcion"] is not None and not isinstance(
                datos["descripcion"], str
            ):
                raise ValueError("Descripcion invalida")

        except (ValueError, UnicodeError, json.JSONDecodeError):
            return self.responder(
                400, {"error": "Solicitud JSON invalida"}
            )

        payload = json.dumps(
            datos, ensure_ascii=False, sort_keys=True
        )

        with conectar() as db:
            db.execute("BEGIN IMMEDIATE")

            anterior = db.execute(
                "SELECT * FROM recordatorios WHERE operation_id = ?",
                (operation_id,)
            ).fetchone()

            if anterior is not None:
                if anterior["payload"] != payload:
                    return self.responder(
                        409, {"error": "Clave usada con otros datos"}
                    )

                return self.responder(
                    200, respuesta_recordatorio(anterior)
                )

            nuevo_id = str(uuid.uuid4())

            db.execute(
                """
                INSERT INTO recordatorios(id, operation_id, payload)
                VALUES (?, ?, ?)
                """,
                (nuevo_id, operation_id, payload)
            )

        return self.responder(201, {"id": nuevo_id, **datos})


if __name__ == "__main__":
    inicializar()
    servidor = ThreadingHTTPServer((HOST, PORT), Api)
    print(f"API disponible en http://{HOST}:{PORT}", flush=True)

    try:
        servidor.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        servidor.server_close()