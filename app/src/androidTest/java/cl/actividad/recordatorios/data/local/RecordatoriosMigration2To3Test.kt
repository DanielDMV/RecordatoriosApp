package cl.actividad.recordatorios.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.actividad.recordatorios.data.local.migration.MIGRATION_2_3
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordatoriosMigration2To3Test {

    private val nombreBD = "prueba_migracion_room_v2_v3.db"

    @Test
    fun migracion2a3_conservaDatosYClasificaOperaciones() = runBlocking {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        // Trabajar exclusivamente con una base de prueba.
        context.deleteDatabase(nombreBD)

        val archivoBD = context.getDatabasePath(nombreBD)
        archivoBD.parentFile?.mkdirs()

        // Crear la estructura exacta de las tablas Room v2.
        val bdV2 = SQLiteDatabase.openOrCreateDatabase(
            archivoBD,
            null
        )

        try {
            bdV2.execSQL(
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
                    fechaCreacion INTEGER NOT NULL,
                    estadoSincronizacion TEXT NOT NULL
                        DEFAULT 'PENDIENTE',
                    remoteId TEXT,
                    fechaModificacion INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )

            bdV2.execSQL(
                """
                CREATE TABLE IF NOT EXISTS operaciones_pendientes (
                    operationId TEXT NOT NULL,
                    recordatorioId INTEGER NOT NULL,
                    tipo TEXT NOT NULL,
                    remoteId TEXT,
                    payloadJson TEXT,
                    intentos INTEGER NOT NULL,
                    creadaEn INTEGER NOT NULL,
                    PRIMARY KEY(operationId)
                )
                """.trimIndent()
            )

            // Tres recordatorios históricos.
            for (id in 10..12) {
                bdV2.execSQL(
                    """
                    INSERT INTO recordatorios (
                        id, titulo, descripcion,
                        categoriaId, categoriaNombre,
                        prioridad, estado,
                        fechaAviso, ubicacion,
                        fechaCreacion, estadoSincronizacion,
                        remoteId, fechaModificacion
                    ) VALUES (
                        ?, ?, ?, 1, 'Estudios',
                        'Alta', 'Pendiente',
                        NULL, NULL, 1795392000000,
                        'PENDIENTE', NULL, 0
                    )
                    """.trimIndent(),
                    arrayOf<Any>(
                        id,
                        "Recordatorio $id",
                        "Registro histórico $id"
                    )
                )
            }

            // Operación que todavía no tiene intentos.
            bdV2.execSQL(
                """
                INSERT INTO operaciones_pendientes (
                    operationId, recordatorioId,
                    tipo, remoteId, payloadJson,
                    intentos, creadaEn
                ) VALUES (
                    'operacion-10', 10, 'CREAR',
                    NULL, NULL, 0, 1795392000000
                )
                """.trimIndent()
            )

            // Operación con intentos previos registrados.
            bdV2.execSQL(
                """
                INSERT INTO operaciones_pendientes (
                    operationId, recordatorioId,
                    tipo, remoteId, payloadJson,
                    intentos, creadaEn
                ) VALUES (
                    'operacion-11', 11, 'CREAR',
                    NULL, NULL, 2, 1795392000001
                )
                """.trimIndent()
            )

            bdV2.version = 2

        } finally {
            bdV2.close()
        }

        // Abrir la base histórica mediante Room v3.
        val database = Room.databaseBuilder(
            context,
            RecordatoriosDatabase::class.java,
            nombreBD
        )
            .addMigrations(MIGRATION_2_3)
            .build()

        try {
            // La apertura ejecuta y valida la migración.
            val recordatorio10 =
                database.recordatorioDao().obtenerPorId(10L)

            val recordatorio11 =
                database.recordatorioDao().obtenerPorId(11L)

            val recordatorio12 =
                database.recordatorioDao().obtenerPorId(12L)

            assertNotNull(recordatorio10)
            assertNotNull(recordatorio11)
            assertNotNull(recordatorio12)

            assertEquals("Recordatorio 10", recordatorio10!!.titulo)
            assertEquals("Recordatorio 11", recordatorio11!!.titulo)
            assertEquals("Recordatorio 12", recordatorio12!!.titulo)

            assertEquals(
                "Registro histórico 10",
                recordatorio10.descripcion
            )
            assertEquals(
                "Estudios",
                recordatorio11.categoriaNombre
            )
            assertEquals(
                1795392000000L,
                recordatorio12.fechaCreacion
            )

            // Verificar las operaciones originales.
            val operacion10 =
                database.operacionPendienteDao()
                    .obtenerPorOperacion("operacion-10")

            val operacion11 =
                database.operacionPendienteDao()
                    .obtenerPorOperacion("operacion-11")

            assertNotNull(operacion10)
            assertNotNull(operacion11)

            assertEquals(10L, operacion10!!.recordatorioId)
            assertEquals(11L, operacion11!!.recordatorioId)

            assertEquals(0, operacion10.intentos)
            assertEquals(2, operacion11.intentos)

            // Verificar la clasificación histórica.
            assertEquals(
                "PENDIENTE",
                operacion10.estadoEnvio
            )

            assertEquals(
                "RESULTADO_INCIERTO",
                operacion11.estadoEnvio
            )

            // Los campos nuevos no deben inventar datos.
            assertNull(operacion10.ultimoIntentoEn)
            assertNull(operacion10.proximoIntentoEn)
            assertNull(operacion10.ultimoError)
            assertNull(operacion10.bloqueadaHasta)

            assertNull(operacion11.ultimoIntentoEn)
            assertNull(operacion11.proximoIntentoEn)
            assertNull(operacion11.ultimoError)
            assertNull(operacion11.bloqueadaHasta)

            assertEquals(
                2,
                database.operacionPendienteDao().contarPendientes()
            )

            // Verificar versión final.
            assertEquals(
                3,
                database.openHelper.readableDatabase.version
            )

            // Verificar integridad de SQLite.
            val cursor =
                database.openHelper.readableDatabase.query(
                    "PRAGMA integrity_check"
                )

            cursor.use {
                assertTrue(it.moveToFirst())
                assertEquals("ok", it.getString(0))
            }

        } finally {
            database.close()
        }
    }
}
