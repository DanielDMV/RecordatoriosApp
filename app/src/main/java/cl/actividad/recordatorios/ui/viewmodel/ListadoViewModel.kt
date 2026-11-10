package cl.actividad.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.actividad.recordatorios.data.RecordatoriosRepository
import cl.actividad.recordatorios.ui.state.ListadoUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListadoViewModel(
    private val repository: RecordatoriosRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ListadoUiState>(ListadoUiState.Cargando)

    val uiState: StateFlow<ListadoUiState> =
        _uiState.asStateFlow()

    private var cargaJob: Job? = null

    init {
        cargarRecordatorios()
    }

    fun cargarRecordatorios() {
        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {
            _uiState.value = ListadoUiState.Cargando

            try {
                // Demora simulada para comprobar el estado de carga.
                delay(3000)

                val recordatorios = repository.obtenerTodos()

                _uiState.value = if (recordatorios.isEmpty()) {
                    ListadoUiState.Vacio
                } else {
                    ListadoUiState.ConDatos(recordatorios)
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.value = ListadoUiState.Error(
                    e.message
                        ?: "No fue posible cargar los recordatorios."
                )
            }
        }
    }

    fun simularEstadoVacio() {
        cargaJob?.cancel()
        _uiState.value = ListadoUiState.Vacio
    }

    fun simularError() {
        cargaJob?.cancel()
        _uiState.value = ListadoUiState.Error(
            "No fue posible cargar los recordatorios."
        )
    }
}
