package cl.actividad.recordatorios.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.actividad.recordatorios.model.OrdenRecordatorios
import cl.actividad.recordatorios.model.TemaApp
import cl.actividad.recordatorios.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException

class DataStorePreferencesRepository(
    private val context: Context
) : UserPreferencesRepository {

    private object Keys {
        val TEMA = stringPreferencesKey("tema")
        val MOSTRAR_COMPLETADOS =
            booleanPreferencesKey("mostrar_completados")
        val ORDEN = stringPreferencesKey("orden")
    }

    override val preferencias: Flow<UserPreferences> =
        context.userPreferencesDataStore.data
            .toUserPreferencesFlow()

    override suspend fun cambiarTema(tema: TemaApp) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.TEMA] = tema.name
        }
    }

    override suspend fun cambiarMostrarCompletados(
        mostrar: Boolean
    ) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.MOSTRAR_COMPLETADOS] = mostrar
        }
    }

    override suspend fun cambiarOrden(
        orden: OrdenRecordatorios
    ) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.ORDEN] = orden.name
        }
    }
}
