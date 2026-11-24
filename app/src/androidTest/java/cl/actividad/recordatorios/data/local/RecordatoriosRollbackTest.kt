package cl.actividad.recordatorios.data.local

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RecordatoriosRollbackTest {

    @Test
    fun errorEnOperacionPendienteRevierteTodaLaTransaccion() =
        runBlocking {

            val context =
                ApplicationProvider.getApplicationContext<Context>()

            val database = Room.inMemoryDatabaseBuilder(
                context,
                RecordatoriosDatabase::class.java
            ).build()

            try {
                val recordatorioDao =
                    database.recordatorioDao()

                val operacionDao =
                    database.operacionPendienteDao()

                val operationId =
                    UUID.randomUUID().toString()

                var errorDetectado = false

                try {
                    database.withTransaction {

                        val recordatorio =
                            RecordatorioEntity(
                                id = 0L,
                                titulo = "Prueba rollback",
                                descripcion = "Registro de prueba",
                                categoriaId = 1,
                                categoriaNombre = "Estudios",
                                prioridad = "Alta",
                                estado = "Pendiente",
                                fechaAviso = null,
                                ubicacion = null,
                                fechaCreacion =
                                    System.currentTimeMillis()
                            )

                        val idLocal =
                            recordatorioDao.insertar(recordatorio)

                        val operacion =
                            OperacionPendienteEntity(
                                operationId = operationId,
                                recordatorioId = idLocal,
                                tipo = "CREAR",
                                remoteId = null,
                                payloadJson = null,
                                intentos = 0,
                                creadaEn =
                                    System.currentTimeMillis()
                            )

                        // Primera inserción válida.
                        operacionDao.insertar(operacion)

                        // Segunda inserción con la misma clave primaria.
                        // Se espera una excepción de SQLite.
                        operacionDao.insertar(operacion)
                    }

                } catch (e: Exception) {
                    errorDetectado = true
                }

                assertTrue(
                    "Se esperaba un error por clave duplicada.",
                    errorDetectado
                )

                // Ningún registro debe quedar guardado.
                assertEquals(
                    0,
                    recordatorioDao.contar()
                )

                // La operación pendiente también debe revertirse.
                assertEquals(
                    0,
                    operacionDao.contarPendientes()
                )

            } finally {
                database.close()
            }
        }
}
