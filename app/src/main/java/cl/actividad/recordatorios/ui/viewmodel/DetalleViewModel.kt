package cl.actividad.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.actividad.recordatorios.data.RecordatoriosRepository
import cl.actividad.recordatorios.ui.state.DetalleUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val repository: RecordatoriosRepository,
    private val recordatorioId: Long
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<DetalleUiState>(DetalleUiState.Cargando)

    val uiState: StateFlow<DetalleUiState> =
        _uiState.asStateFlow()

    init {
        cargarDetalle()
    }

    fun cargarDetalle() {
        viewModelScope.launch {
            _uiState.value = DetalleUiState.Cargando

            try {
                val recordatorio =
                    repository.obtenerPorId(recordatorioId)

                _uiState.value = if (recordatorio != null) {
                    DetalleUiState.ConDatos(recordatorio)
                } else {
                    DetalleUiState.NoEncontrado
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.value = DetalleUiState.Error(
                    e.message
                        ?: "No fue posible consultar el recordatorio."
                )
            }
        }
    }
}

