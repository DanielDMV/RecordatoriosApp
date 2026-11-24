package cl.actividad.recordatorios.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfirmacionCreacionTest {

    @Test
    fun confirmaCreacionYRechazaTokenAjeno() = runBlocking {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        val database = Room.inMemoryDatabaseBuilder(
            context,
            RecordatoriosDatabase::class.java
        ).build()

        try {
            val sqlite = database.openHelper.writableDatabase

            sqlite.execSQL(
                """
                INSERT INTO recordatorios (
                    id, titulo, descripcion, categoriaId,
                    categoriaNombre, prioridad, estado,
                    fechaAviso, ubicacion, fechaCreacion,
                    estadoSincronizacion, remoteId,
                    fechaModificacion
                ) VALUES (
                    10, 'Prueba confirmación', NULL,
                    1, 'Estudios', 'Alta', 'Pendiente',
                    NULL, NULL, 1000, 'PENDIENTE', NULL, 0
                )
                """.trimIndent()
            )

            val dao = database.operacionPendienteDao()

            dao.insertar(
                OperacionPendienteEntity(
                    operationId = "crear-10",
                    recordatorioId = 10L,
                    tipo = "CREAR",
                    creadaEn = 1000L
                )
            )

            assertEquals(
                1,
                dao.adquirirOperacion(
                    "crear-10",
                    2000L,
                    9000L,
                    "token-correcto"
                )
            )

            val confirmador = ConfirmadorCreacionLocal(database)

            // Un proceso ajeno no puede confirmar.
            assertFalse(
                confirmador.confirmar(
                    operationId = "crear-10",
                    tokenBloqueo = "token-ajeno",
                    remoteId = "remoto-10",
                    estadoFinal = "SINCRONIZADO",
                    ahora = 3000L
                )
            )

            assertNotNull(
                dao.obtenerPorOperacion("crear-10")
            )

            val antes = database.recordatorioDao()
                .obtenerPorId(10L)

            assertNull(antes!!.remoteId)

            // El propietario vigente confirma.
            assertTrue(
                confirmador.confirmar(
                    operationId = "crear-10",
                    tokenBloqueo = "token-correcto",
                    remoteId = "remoto-10",
                    estadoFinal = "SINCRONIZADO",
                    ahora = 4000L
                )
            )

            val despues = database.recordatorioDao()
                .obtenerPorId(10L)

            assertEquals("remoto-10", despues!!.remoteId)
            assertEquals(
                "SINCRONIZADO",
                despues.estadoSincronizacion
            )

            assertNull(
                dao.obtenerPorOperacion("crear-10")
            )

            // Una segunda confirmación no debe tener efecto.
            assertFalse(
                confirmador.confirmar(
                    operationId = "crear-10",
                    tokenBloqueo = "token-correcto",
                    remoteId = "remoto-10",
                    estadoFinal = "SINCRONIZADO",
                    ahora = 5000L
                )
            )

        } finally {
            database.close()
        }
    }
}
