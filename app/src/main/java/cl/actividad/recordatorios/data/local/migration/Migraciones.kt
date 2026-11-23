package cl.actividad.recordatorios.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(connection: SQLiteConnection) {

        connection.execSQL(
            """
            ALTER TABLE recordatorios
            ADD COLUMN estadoSincronizacion TEXT
            NOT NULL DEFAULT 'PENDIENTE'
            """.trimIndent()
        )

        connection.execSQL(
            """
            ALTER TABLE recordatorios
            ADD COLUMN remoteId TEXT
            """.trimIndent()
        )

        connection.execSQL(
            """
            ALTER TABLE recordatorios
            ADD COLUMN fechaModificacion INTEGER
            NOT NULL DEFAULT 0
            """.trimIndent()
        )

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS operaciones_pendientes (
                operationId TEXT NOT NULL PRIMARY KEY,
                recordatorioId INTEGER NOT NULL,
                tipo TEXT NOT NULL,
                remoteId TEXT,
                payloadJson TEXT,
                intentos INTEGER NOT NULL,
                creadaEn INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
