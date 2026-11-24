package cl.actividad.recordatorios.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.actividad.recordatorios.data.local.migration.MIGRATION_1_2
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordatoriosMigrationTest {

    private val nombreBD = "prueba_migracion_room.db"

    @Test
    fun migracion1a2_conservaDatosYEstructura() = runBlocking {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        // Eliminar solamente la base aislada de prueba.
        context.deleteDatabase(nombreBD)

        val archivoBD = context.getDatabasePath(nombreBD)

        // Crear la estructura original de la versión 1.
        val bdVersion1 = SQLiteDatabase.openOrCreateDatabase(
            archivoBD,
            null
        )

        try {
            bdVersion1.execSQL(
                """
                CREATE TABLE IF NOT EXISTS recordatorios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    titulo TEXT NOT NULL,
                    descripcion TEXT,
                    categoriaId INTEGER NOT NULL,
                    categoriaNombre TEXT NOT NULL,
                    prioridad TEXT NOT NULL,
                    estado TEXT NOT NULL,
                    fechaAviso TEXT,
                    ubicacion TEXT,
                    fechaCreacion INTEGER NOT NULL
                )
                """.trimIndent()
            )

            bdVersion1.execSQL(
                """
                INSERT INTO recordatorios (
                    id, titulo, descripcion, categoriaId,
                    categoriaNombre, prioridad, estado,
                    fechaAviso, ubicacion, fechaCreacion
                ) VALUES (
                    10, 'Prueba migración', 'Registro original',
                    1, 'Estudios', 'Alta', 'Pendiente',
                    NULL, NULL, 1795392000000
                )
                """.trimIndent()
            )

            bdVersion1.version = 1
        } finally {
            bdVersion1.close()
        }

        // Abrir la misma base con Room versión 2.
        val database = Room.databaseBuilder(
            context,
            RecordatoriosDatabase::class.java,
            nombreBD
        )
            .addMigrations(MIGRATION_1_2)
            .build()

        try {
            val recordatorio =
                database.recordatorioDao().obtenerPorId(10L)

            assertNotNull(recordatorio)
            assertEquals(10L, recordatorio!!.id)
            assertEquals("Prueba migración", recordatorio.titulo)
            assertEquals("Estudios", recordatorio.categoriaNombre)
            assertEquals("Alta", recordatorio.prioridad)
            assertEquals("Pendiente", recordatorio.estado)
            assertEquals(1795392000000L, recordatorio.fechaCreacion)

            // Verificar los valores iniciales de sincronización.
            val cursor = database.openHelper.readableDatabase.query(
                """
                SELECT remoteId, estadoSincronizacion,
                       fechaModificacion
                FROM recordatorios
                WHERE id = 10
                """.trimIndent()
            )

            cursor.use {
                assertTrue(it.moveToFirst())
                assertTrue(it.isNull(0))
                assertEquals("PENDIENTE", it.getString(1))
                assertEquals(0L, it.getLong(2))
            }

            // Comprobar que la nueva tabla existe.
            val tabla = database.openHelper.readableDatabase.query(
                """
                SELECT name FROM sqlite_master
                WHERE type = 'table'
                AND name = 'operaciones_pendientes'
                """.trimIndent()
            )

            tabla.use {
                assertTrue(it.moveToFirst())
            }

        } finally {
            database.close()
        }
    }
}