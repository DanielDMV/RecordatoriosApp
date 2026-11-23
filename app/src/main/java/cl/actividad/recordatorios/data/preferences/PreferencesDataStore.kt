package cl.actividad.recordatorios.data.preferences

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "recordatorios_preferences"
)
