package cl.actividad.recordatorios

import android.content.Context
import cl.actividad.recordatorios.data.InMemoryRecordatoriosRepository
import cl.actividad.recordatorios.data.RecordatoriosRepository
import cl.actividad.recordatorios.data.preferences.DataStorePreferencesRepository
import cl.actividad.recordatorios.data.preferences.UserPreferencesRepository

object AppContainer {

    val recordatoriosRepository: RecordatoriosRepository =
        InMemoryRecordatoriosRepository()

    private var _preferencesRepository: UserPreferencesRepository? = null

    val preferencesRepository: UserPreferencesRepository
        get() = checkNotNull(_preferencesRepository) {
            "AppContainer debe inicializarse antes de acceder a DataStore."
        }

    fun initialize(context: Context) {
        if (_preferencesRepository == null) {
            _preferencesRepository = DataStorePreferencesRepository(
                context.applicationContext
            )
        }
    }
}
