package cl.actividad.recordatorios.data.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RetrofitIntegrationTest {

    @Test
    fun consultaCreacionYErrorHttp() = runBlocking {

        val servidor = MockWebServer()
        servidor.start()

        try {
            val api = RetrofitClient.crear(
                servidor.url("/").toString()
            )

            val remoto = RemoteRecordatoriosDataSource(api)

            // Consulta exitosa.
            servidor.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        [{
                          "id":"remoto-1",
                          "titulo":"Recordatorio remoto",
                          "descripcion":null,
                          "categoria":"Trabajo",
                          "prioridad":"Alta",
                          "estado":"Pendiente"
                        }]
                        """.trimIndent()
                    )
                    .addHeader("Content-Type", "application/json")
            )

            val consulta = remoto.consultar()

            assertTrue(consulta is ResultadoRemoto.Exito)

            val registros =
                (consulta as ResultadoRemoto.Exito).datos

            assertEquals(1, registros.size)
            assertEquals("remoto-1", registros.first().id)

            // Creación exitosa.
            servidor.enqueue(
                MockResponse()
                    .setResponseCode(201)
                    .setBody(
                        """
                        {
                          "id":"remoto-2",
                          "titulo":"Nuevo recordatorio",
                          "descripcion":null,
                          "categoria":"Personal",
                          "prioridad":"Alta",
                          "estado":"Pendiente"
                        }
                        """.trimIndent()
                    )
                    .addHeader("Content-Type", "application/json")
            )

            val creacion = remoto.crear(
                operationId = "operacion-123",
                request = CrearRecordatorioRequest(
                    titulo = "Nuevo recordatorio",
                    descripcion = null,
                    categoria = "Personal",
                    prioridad = "Alta",
                    estado = "Pendiente"
                )
            )

            assertTrue(creacion is ResultadoRemoto.Exito)

            val peticionConsulta =
                servidor.takeRequest()

            assertEquals("GET", peticionConsulta.method)

            val peticionCreacion =
                servidor.takeRequest()

            assertEquals("POST", peticionCreacion.method)

            assertEquals(
                "/recordatorios",
                peticionCreacion.path
            )

            assertEquals(
                "operacion-123",
                peticionCreacion.getHeader("Idempotency-Key")
            )

            // Error HTTP controlado.
            servidor.enqueue(
                MockResponse().setResponseCode(500)
            )

            val error = remoto.consultar()

            assertEquals(
                500,
                (error as ResultadoRemoto.ErrorHttp).codigo
            )

        } finally {
            servidor.shutdown()
        }
    }
}
