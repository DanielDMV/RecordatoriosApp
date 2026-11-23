package cl.actividad.recordatorios.data.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.actividad.recordatorios.model.OrdenRecordatorios
import cl.actividad.recordatorios.model.TemaApp
import cl.actividad.recordatorios.model.UserPreferences

internal fun Preferences.toUserPreferences(): UserPreferences {

    val temaKey = stringPreferencesKey("tema")
    val completadosKey = booleanPreferencesKey("mostrar_completados")
    val ordenKey = stringPreferencesKey("orden")

    val tema = TemaApp.entries.firstOrNull {
        it.name == this[temaKey]
    } ?: TemaApp.SISTEMA

    val orden = OrdenRecordatorios.entries.firstOrNull {
        it.name == this[ordenKey]
    } ?: OrdenRecordatorios.MAS_RECIENTES

    return UserPreferences(
        tema = tema,
        mostrarCompletados = this[completadosKey] ?: true,
        orden = orden
    )
}
