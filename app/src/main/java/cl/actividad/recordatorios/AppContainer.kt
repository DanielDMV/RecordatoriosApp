package cl.actividad.recordatorios

import android.content.Context
import androidx.room.Room
import cl.actividad.recordatorios.data.RecordatoriosRepository
import cl.actividad.recordatorios.data.local.RecordatoriosDatabase
import cl.actividad.recordatorios.data.local.RoomRecordatoriosRepository
import cl.actividad.recordatorios.data.preferences.DataStorePreferencesRepository
import cl.actividad.recordatorios.data.preferences.UserPreferencesRepository
import cl.actividad.recordatorios.data.local.migration.MIGRATION_1_2
import cl.actividad.recordatorios.data.local.migration.MIGRATION_2_3
import cl.actividad.recordatorios.data.local.migration.MIGRATION_3_4

object AppContainer {

    private var database: RecordatoriosDatabase? = null

    private var _recordatoriosRepository: RecordatoriosRepository? = null

    private var _preferencesRepository: UserPreferencesRepository? = null

    val recordatoriosRepository: RecordatoriosRepository
        get() = checkNotNull(_recordatoriosRepository) {
            "El repositorio de recordatorios no esta inicializado."
        }

    val preferencesRepository: UserPreferencesRepository
        get() = checkNotNull(_preferencesRepository) {
            "El repositorio de preferencias no esta inicializado."
        }

    @Synchronized
    fun initialize(context: Context) {

        val appContext = context.applicationContext

        if (database == null) {
            database = Room.databaseBuilder(
                appContext,
                RecordatoriosDatabase::class.java,
                "recordatorios.db"
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4
                )
                .build()
        }

        if (_recordatoriosRepository == null) {
            _recordatoriosRepository = RoomRecordatoriosRepository(
                database = checkNotNull(database)
            )
        }

        if (_preferencesRepository == null) {
            _preferencesRepository = DataStorePreferencesRepository(
                appContext
            )
        }
    }
}
