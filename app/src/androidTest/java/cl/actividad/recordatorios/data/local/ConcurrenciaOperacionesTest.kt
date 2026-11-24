package cl.actividad.recordatorios.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConcurrenciaOperacionesTest {

    @Test
    fun concurrenciaPropiedadYRecuperacion() = runBlocking {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        val database = Room.inMemoryDatabaseBuilder(
            context,
            RecordatoriosDatabase::class.java
        ).build()

        try {
            val dao = database.operacionPendienteDao()

            dao.insertar(
                OperacionPendienteEntity(
                    operationId = "operacion-concurrente",
                    recordatorioId = 1L,
                    tipo = "CREAR",
                    creadaEn = 1000L
                )
            )

            val ahora = 10000L
            val vence = 15000L

            // Dos propietarios compiten por una operación.
            val resultados = listOf(
                async(Dispatchers.IO) {
                    dao.adquirirOperacion(
                        "operacion-concurrente",
                        ahora,
                        vence,
                        "token-A"
                    )
                },
                async(Dispatchers.IO) {
                    dao.adquirirOperacion(
                        "operacion-concurrente",
                        ahora,
                        vence,
                        "token-B"
                    )
                }
            ).awaitAll()

            assertEquals(1, resultados.sum())
            assertEquals(1, resultados.count { it == 1 })

            val adquirida = dao.obtenerPorOperacion(
                "operacion-concurrente"
            )!!

            assertEquals("EN_PROCESO", adquirida.estadoEnvio)
            assertEquals(vence, adquirida.bloqueadaHasta)

            val propietario = adquirida.tokenBloqueo!!
            val ajeno = if (propietario == "token-A") {
                "token-B"
            } else {
                "token-A"
            }

            // El proceso que perdió no puede registrar intentos.
            assertEquals(
                0,
                dao.registrarIntentoEnvio(
                    "operacion-concurrente",
                    ajeno,
                    11000L
                )
            )

            // El propietario vigente sí puede.
            assertEquals(
                1,
                dao.registrarIntentoEnvio(
                    "operacion-concurrente",
                    propietario,
                    11000L
                )
            )

            val enviada = dao.obtenerPorOperacion(
                "operacion-concurrente"
            )!!

            assertEquals(1, enviada.intentos)
            assertEquals(11000L, enviada.ultimoIntentoEn)

            // Un token ajeno tampoco puede resolver el intento.
            assertEquals(
                0,
                dao.marcarResultadoIncierto(
                    "operacion-concurrente",
                    ajeno,
                    12000L,
                    "Respuesta ajena"
                )
            )

            // Antes del vencimiento, no debe recuperarse.
            assertEquals(
                0,
                dao.recuperarOperacionesVencidas(14000L)
            )

            // Al vencer, se invalida el token.
            assertEquals(
                1,
                dao.recuperarOperacionesVencidas(16000L)
            )

            val recuperada = dao.obtenerPorOperacion(
                "operacion-concurrente"
            )!!

            assertEquals(
                "RESULTADO_INCIERTO",
                recuperada.estadoEnvio
            )

            assertNull(recuperada.tokenBloqueo)
            assertNull(recuperada.bloqueadaHasta)
            assertEquals(1, recuperada.intentos)

            // Ni siquiera el antiguo propietario puede
            // modificar la operación después de vencer.
            assertEquals(
                0,
                dao.registrarIntentoEnvio(
                    "operacion-concurrente",
                    propietario,
                    17000L
                )
            )

            assertEquals(
                0,
                dao.marcarResultadoIncierto(
                    "operacion-concurrente",
                    propietario,
                    17000L,
                    "Respuesta tardía"
                )
            )

            // No se permite un reenvío automático.
            assertEquals(
                0,
                dao.adquirirOperacion(
                    "operacion-concurrente",
                    17000L,
                    22000L,
                    "token-C"
                )
            )

            assertEquals(
                1,
                dao.obtenerOperacionesInciertas().size
            )

        } finally {
            database.close()
        }
    }
}
