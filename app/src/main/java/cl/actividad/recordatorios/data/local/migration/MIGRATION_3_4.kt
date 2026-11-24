
package cl.actividad.recordatorios.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            ALTER TABLE operaciones_pendientes
            ADD COLUMN tokenBloqueo TEXT
            """.trimIndent()
        )

        // Las operaciones antiguas EN_PROCESO no
        // tienen un propietario verificable.
        db.execSQL(
            """
            UPDATE operaciones_pendientes
            SET estadoEnvio = 'RESULTADO_INCIERTO',
                bloqueadaHasta = NULL,
                tokenBloqueo = NULL
            WHERE estadoEnvio = 'EN_PROCESO'
            """.trimIndent()
        )
    }
}
