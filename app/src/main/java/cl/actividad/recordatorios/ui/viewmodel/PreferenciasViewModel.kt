package cl.actividad.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.actividad.recordatorios.data.preferences.UserPreferencesRepository
import cl.actividad.recordatorios.model.OrdenRecordatorios
import cl.actividad.recordatorios.model.TemaApp
import cl.actividad.recordatorios.model.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferenciasViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    val preferencias: StateFlow<UserPreferences> =
        repository.preferencias.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    fun cambiarTema(tema: TemaApp) {
        viewModelScope.launch {
            repository.cambiarTema(tema)
        }
    }

    fun cambiarMostrarCompletados(mostrar: Boolean) {
        viewModelScope.launch {
            repository.cambiarMostrarCompletados(mostrar)
        }
    }

    fun cambiarOrden(orden: OrdenRecordatorios) {
        viewModelScope.launch {
            repository.cambiarOrden(orden)
        }
    }
}
