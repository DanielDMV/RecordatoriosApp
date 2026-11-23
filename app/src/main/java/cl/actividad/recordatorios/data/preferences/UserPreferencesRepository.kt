package cl.actividad.recordatorios.data.preferences

import cl.actividad.recordatorios.model.OrdenRecordatorios
import cl.actividad.recordatorios.model.TemaApp
import cl.actividad.recordatorios.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val preferencias: Flow<UserPreferences>

    suspend fun cambiarTema(tema: TemaApp)

    suspend fun cambiarMostrarCompletados(mostrar: Boolean)

    suspend fun cambiarOrden(orden: OrdenRecordatorios)
}
