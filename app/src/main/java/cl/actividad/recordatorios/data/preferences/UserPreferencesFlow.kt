package cl.actividad.recordatorios.data.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import cl.actividad.recordatorios.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

internal fun Flow<Preferences>.toUserPreferencesFlow():
        Flow<UserPreferences> =
    catch { error ->
        if (error is IOException) {
            emit(emptyPreferences())
        } else {
            throw error
        }
    }.map { preferences ->
        preferences.toUserPreferences()
    }
