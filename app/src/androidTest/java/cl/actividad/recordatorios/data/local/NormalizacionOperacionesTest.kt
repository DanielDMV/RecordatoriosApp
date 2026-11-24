package cl.actividad.recordatorios.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class NormalizacionOperacionesTest {

    @Test
    fun normalizacionEsIdempotente() = runBlocking {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        val database = Room.inMemoryDatabaseBuilder(
            context,
            RecordatoriosDatabase::class.java
        ).build()

        try {
            val recordatoriosDao = database.recordatorioDao()
            val operacionesDao = database.operacionPendienteDao()

            val repository =
                RoomRecordatoriosRepository(database)

            val fecha = System.currentTimeMillis()

            // Se insertan directamente para simular
            // registros anteriores a la cola.
            val idA = recordatoriosDao.insertar(
                crearEntidad("Prueba A", fecha)
            )

            val idB = recordatoriosDao.insertar(
                crearEntidad("Prueba B", fecha + 1)
            )

            assertEquals(2, recordatoriosDao.contar())
            assertEquals(0, operacionesDao.contarPendientes())

            // Primera ejecución: crea dos operaciones.
            val primera = repository.normalizarOperacionesPendientes()

            assertEquals(2, primera)
            assertEquals(2, operacionesDao.contarPendientes())

            val operacionesPrimera =
                operacionesDao.obtenerPendientes()

            assertEquals(
                setOf(idA, idB),
                operacionesPrimera.map { it.recordatorioId }.toSet()
            )

            assertEquals(
                2,
                operacionesPrimera.count { it.tipo == "CREAR" }
            )

            val uuidsOriginales =
                operacionesPrimera.map { it.operationId }.toSet()

            // Segunda ejecución: no debe duplicar.
            val segunda = repository.normalizarOperacionesPendientes()

            assertEquals(0, segunda)
            assertEquals(2, operacionesDao.contarPendientes())

            val uuidsFinales =
                operacionesDao.obtenerPendientes()
                    .map { it.operationId }
                    .toSet()

            assertEquals(uuidsOriginales, uuidsFinales)

            // Los registros originales siguen existiendo.
            assertNotNull(recordatoriosDao.obtenerPorId(idA))
            assertNotNull(recordatoriosDao.obtenerPorId(idB))

            // La normalización no altera registros inexistentes.
            assertNull(recordatoriosDao.obtenerPorId(999L))

        } finally {
            database.close()
        }
    }

    private fun crearEntidad(
        titulo: String,
        fecha: Long
    ): RecordatorioEntity =
        RecordatorioEntity(
            id = 0L,
            titulo = titulo,
            descripcion = null,
            categoriaId = 1,
            categoriaNombre = "Estudios",
            prioridad = "Alta",
            estado = "Pendiente",
            fechaAviso = null,
            ubicacion = null,
            fechaCreacion = fecha
        )
}
