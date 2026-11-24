package cl.actividad.recordatorios.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.actividad.recordatorios.data.local.migration.MIGRATION_3_4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordatoriosMigration3To4Test {

    @Test
    fun migracion3a4_preservaDatosEInvalidaBloqueosAntiguos() =
        runBlocking {

            val context =
                ApplicationProvider.getApplicationContext<Context>()

            val nombreBD = "prueba_migracion_v3_v4.db"
            context.deleteDatabase(nombreBD)

            val archivoBD = context.getDatabasePath(nombreBD)
            archivoBD.parentFile?.mkdirs()

            val antigua = SQLiteDatabase.openOrCreateDatabase(
                archivoBD,
                null
            )

            try {
                antigua.execSQL(
                    """
                    CREATE TABLE recordatorios (
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
                        estadoSincronizacion TEXT NOT NULL DEFAULT 'PENDIENTE',
                        remoteId TEXT,
                        fechaModificacion INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                antigua.execSQL(
                    """
                    CREATE TABLE operaciones_pendientes (
                        operationId TEXT NOT NULL,
                        recordatorioId INTEGER NOT NULL,
                        tipo TEXT NOT NULL,
                        remoteId TEXT,
                        payloadJson TEXT,
                        intentos INTEGER NOT NULL,
                        creadaEn INTEGER NOT NULL,
                        estadoEnvio TEXT NOT NULL DEFAULT 'PENDIENTE',
                        ultimoIntentoEn INTEGER,
                        proximoIntentoEn INTEGER,
                        ultimoError TEXT,
                        bloqueadaHasta INTEGER,
                        PRIMARY KEY(operationId)
                    )
                    """.trimIndent()
                )

                antigua.execSQL(
                    """
                    INSERT INTO recordatorios (
                        id, titulo, descripcion, categoriaId,
                        categoriaNombre, prioridad, estado,
                        fechaAviso, ubicacion, fechaCreacion,
                        estadoSincronizacion, remoteId,
                        fechaModificacion
                    ) VALUES (
                        10, 'Registro v3', 'Dato conservado',
                        1, 'Estudios', 'Alta', 'Pendiente',
                        NULL, NULL, 1000,
                        'PENDIENTE', NULL, 0
                    )
                    """.trimIndent()
                )

                antigua.execSQL(
                    """
                    INSERT INTO operaciones_pendientes (
                        operationId, recordatorioId, tipo,
                        remoteId, payloadJson, intentos,
                        creadaEn, estadoEnvio, ultimoIntentoEn,
                        proximoIntentoEn, ultimoError,
                        bloqueadaHasta
                    ) VALUES (
                        'operacion-antigua', 10, 'CREAR',
                        NULL, NULL, 1, 1000,
                        'EN_PROCESO', 1100,
                        NULL, NULL, 5000
                    )
                    """.trimIndent()
                )

                antigua.version = 3

            } finally {
                antigua.close()
            }

            val nueva = Room.databaseBuilder(
                context,
                RecordatoriosDatabase::class.java,
                nombreBD
            )
                .addMigrations(MIGRATION_3_4)
                .build()

            try {
                val recordatorio =
                    nueva.recordatorioDao().obtenerPorId(10L)

                val operacion =
                    nueva.operacionPendienteDao()
                        .obtenerPorOperacion("operacion-antigua")

                assertNotNull(recordatorio)
                assertEquals("Registro v3", recordatorio!!.titulo)

                assertNotNull(operacion)
                assertEquals(10L, operacion!!.recordatorioId)
                assertEquals(1, operacion.intentos)
                assertEquals(
                    "RESULTADO_INCIERTO",
                    operacion.estadoEnvio
                )
                assertNull(operacion.tokenBloqueo)
                assertNull(operacion.bloqueadaHasta)

                assertEquals(
                    4,
                    nueva.openHelper.readableDatabase.version
                )

                nueva.openHelper.readableDatabase
                    .query("PRAGMA integrity_check")
                    .use {
                        assertTrue(it.moveToFirst())
                        assertEquals("ok", it.getString(0))
                    }

            } finally {
                nueva.close()
                context.deleteDatabase(nombreBD)
            }
        }
}
