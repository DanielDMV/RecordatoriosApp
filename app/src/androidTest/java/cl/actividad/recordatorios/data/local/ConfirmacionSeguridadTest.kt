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
class ConfirmacionSeguridadTest {

    private fun crearBase(): RecordatoriosDatabase {
        val context =
            ApplicationProvider.getApplicationContext<Context>()

        return Room.inMemoryDatabaseBuilder(
            context,
            RecordatoriosDatabase::class.java
        ).build()
    }

    private fun insertarRecordatorio(
        database: RecordatoriosDatabase
    ) {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO recordatorios (
                id, titulo, descripcion, categoriaId,
                categoriaNombre, prioridad, estado,
                fechaAviso, ubicacion, fechaCreacion,
                estadoSincronizacion, remoteId,
                fechaModificacion
            ) VALUES (
                10, 'Prueba seguridad', NULL,
                1, 'Estudios', 'Alta', 'Pendiente',
                NULL, NULL, 1000, 'PENDIENTE', NULL, 0
            )
            """.trimIndent()
        )
    }

    @Test
    fun respuestaTardiaNoConfirmaOperacion() = runBlocking {
        val database = crearBase()

        try {
            insertarRecordatorio(database)

            val dao = database.operacionPendienteDao()

            dao.insertar(
                OperacionPendienteEntity(
                    operationId = "crear-tardia",
                    recordatorioId = 10L,
                    tipo = "CREAR",
                    creadaEn = 1000L
                )
            )

            assertEquals(
                1,
                dao.adquirirOperacion(
                    "crear-tardia",
                    2000L,
                    5000L,
                    "token-original"
                )
            )

            val confirmador = ConfirmadorCreacionLocal(database)

            // El bloqueo vence exactamente en 5000.
            assertFalse(
                confirmador.confirmar(
                    operationId = "crear-tardia",
                    tokenBloqueo = "token-original",
                    remoteId = "remoto-tardio",
                    estadoFinal = "SINCRONIZADO",
                    ahora = 5000L
                )
            )

            val recordatorio =
                database.recordatorioDao().obtenerPorId(10L)!!

            assertNull(recordatorio.remoteId)
            assertEquals(
                "PENDIENTE",
                recordatorio.estadoSincronizacion
            )

            // La operación no debe desaparecer.
            assertNotNull(
                dao.obtenerPorOperacion("crear-tardia")
            )

            // El proceso de recuperación la clasifica
            // como resultado incierto.
            assertEquals(
                1,
                dao.recuperarOperacionesVencidas(5000L)
            )

            val recuperada =
                dao.obtenerPorOperacion("crear-tardia")!!

            assertEquals(
                "RESULTADO_INCIERTO",
                recuperada.estadoEnvio
            )
            assertNull(recuperada.tokenBloqueo)

        } finally {
            database.close()
        }
    }

    @Test
    fun errorAlEliminarOperacionRevierteActualizacion() =
        runBlocking {

            val database = crearBase()

            try {
                insertarRecordatorio(database)

                val dao = database.operacionPendienteDao()

                dao.insertar(
                    OperacionPendienteEntity(
                        operationId = "crear-rollback",
                        recordatorioId = 10L,
                        tipo = "CREAR",
                        creadaEn = 1000L
                    )
                )

                assertEquals(
                    1,
                    dao.adquirirOperacion(
                        "crear-rollback",
                        2000L,
                        9000L,
                        "token-valido"
                    )
                )

                // Simular un fallo SQLite justo cuando
                // se intenta eliminar la operación.
                database.openHelper.writableDatabase.execSQL(
                    """
                    CREATE TRIGGER impedir_eliminacion
                    BEFORE DELETE ON operaciones_pendientes
                    BEGIN
                        SELECT RAISE(
                            ABORT,
                            'Fallo de eliminacion simulado'
                        );
                    END
                    """.trimIndent()
                )

                val confirmador =
                    ConfirmadorCreacionLocal(database)

                var falloDetectado = false

                try {
                    confirmador.confirmar(
                        operationId = "crear-rollback",
                        tokenBloqueo = "token-valido",
                        remoteId = "remoto-rollback",
                        estadoFinal = "SINCRONIZADO",
                        ahora = 3000L
                    )
                } catch (e: Exception) {
                    falloDetectado = true
                }

                assertTrue(
                    "Se esperaba un fallo de SQLite",
                    falloDetectado
                )

                // La transacción debe revertir también
                // la actualización del recordatorio.
                val recordatorio =
                    database.recordatorioDao()
                        .obtenerPorId(10L)!!

                assertNull(recordatorio.remoteId)
                assertEquals(
                    "PENDIENTE",
                    recordatorio.estadoSincronizacion
                )
                assertEquals(
                    0L,
                    recordatorio.fechaModificacion
                )

                // La operación continúa disponible
                // con su propietario original.
                val operacion =
                    dao.obtenerPorOperacion("crear-rollback")

                assertNotNull(operacion)
                assertEquals(
                    "EN_PROCESO",
                    operacion!!.estadoEnvio
                )
                assertEquals(
                    "token-valido",
                    operacion.tokenBloqueo
                )

            } finally {
                database.close()
            }
        }
}
