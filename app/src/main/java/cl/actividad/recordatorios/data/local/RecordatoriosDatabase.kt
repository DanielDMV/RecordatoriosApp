package cl.actividad.recordatorios.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RecordatorioEntity::class,
        OperacionPendienteEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class RecordatoriosDatabase : RoomDatabase() {

    abstract fun recordatorioDao(): RecordatorioDao
}
