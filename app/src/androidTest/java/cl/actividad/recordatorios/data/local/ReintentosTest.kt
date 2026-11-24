package cl.actividad.recordatorios.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.actividad.recordatorios.data.sync.PoliticaReintentos
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReintentosTest {

    @Test
    fun reprogramacionRespetaPropiedadYEspera() = runBlocking {

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
                    operationId = "crear-reintento",
                    recordatorioId = 1L,
                    tipo = "CREAR",
                    creadaEn = 1000L
                )
            )

            assertEquals(
                1,
                dao.adquirirOperacion(
                    "crear-reintento",
                    2000L,
                    10000L,
                    "token-A"
                )
            )

            val proximo =
                PoliticaReintentos.calcularProximoIntento(
                    ahora = 3000L,
                    intentos = 0
                )

            assertEquals(8000L, proximo)

            // Un propietario ajeno no puede reprogramar.
            assertEquals(
                0,
                dao.reprogramarAntesDeEnviar(
                    "crear-reintento",
                    "token-ajeno",
                    3000L,
                    proximo,
                    "Sin conectividad"
                )
            )

            // El propietario vigente puede reprogramar.
            assertEquals(
                1,
                dao.reprogramarAntesDeEnviar(
                    "crear-reintento",
                    "token-A",
                    3000L,
                    proximo,
                    "Sin conectividad"
                )
            )

            val pendiente =
                dao.obtenerPorOperacion("crear-reintento")!!

            assertEquals("PENDIENTE", pendiente.estadoEnvio)
            assertNull(pendiente.tokenBloqueo)
            assertEquals(0, pendiente.intentos)
            assertEquals(proximo, pendiente.proximoIntentoEn)

            // No puede adquirirse antes de su fecha.
            assertEquals(
                0,
                dao.adquirirOperacion(
                    "crear-reintento",
                    7000L,
                    12000L,
                    "token-B"
                )
            )

            // Se adquiere cuando corresponde.
            assertEquals(
                1,
                dao.adquirirOperacion(
                    "crear-reintento",
                    8000L,
                    13000L,
                    "token-B"
                )
            )

        } finally {
            database.close()
        }
    }
}
