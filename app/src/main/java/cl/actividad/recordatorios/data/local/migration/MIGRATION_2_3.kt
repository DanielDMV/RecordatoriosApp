package cl.actividad.recordatorios.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            ALTER TABLE operaciones_pendientes
            ADD COLUMN estadoEnvio TEXT NOT NULL DEFAULT 'PENDIENTE'
            """.trimIndent()
        )

        db.execSQL(
            """
            ALTER TABLE operaciones_pendientes
            ADD COLUMN ultimoIntentoEn INTEGER
            """.trimIndent()
        )

        db.execSQL(
            """
            ALTER TABLE operaciones_pendientes
            ADD COLUMN proximoIntentoEn INTEGER
            """.trimIndent()
        )

        db.execSQL(
            """
            ALTER TABLE operaciones_pendientes
            ADD COLUMN ultimoError TEXT
            """.trimIndent()
        )

        db.execSQL(
            """
            ALTER TABLE operaciones_pendientes
            ADD COLUMN bloqueadaHasta INTEGER
            """.trimIndent()
        )

        // Una operación con intentos previos requiere
        // reconciliación antes de un nuevo envío.
        db.execSQL(
            """
            UPDATE operaciones_pendientes
            SET estadoEnvio = 'RESULTADO_INCIERTO'
            WHERE intentos > 0
            """.trimIndent()
        )
    }
}
