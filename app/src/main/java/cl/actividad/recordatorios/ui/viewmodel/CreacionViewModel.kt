package cl.actividad.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.actividad.recordatorios.data.RecordatoriosRepository
import cl.actividad.recordatorios.model.Categoria
import cl.actividad.recordatorios.model.EstadoRecordatorio
import cl.actividad.recordatorios.model.Prioridad
import cl.actividad.recordatorios.model.Recordatorio
import cl.actividad.recordatorios.ui.state.CreacionUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreacionViewModel(
    private val repository: RecordatoriosRepository,
    val categoriasDisponibles: List<Categoria>
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreacionUiState())

    val uiState: StateFlow<CreacionUiState> =
        _uiState.asStateFlow()

    fun actualizarTitulo(nuevoTitulo: String) {
        _uiState.value = _uiState.value.copy(
            titulo = nuevoTitulo,
            tituloModificado = true,
            mensajeError = null
        )
    }

    fun actualizarDescripcion(nuevaDescripcion: String) {
        _uiState.value = _uiState.value.copy(
            descripcion = nuevaDescripcion.take(200),
            mensajeError = null
        )
    }

    fun seleccionarCategoria(categoria: Categoria) {
        _uiState.value = _uiState.value.copy(
            categoria = categoria,
            mensajeError = null
        )
    }

    fun seleccionarPrioridad(prioridad: Prioridad) {
        _uiState.value = _uiState.value.copy(
            prioridad = prioridad,
            mensajeError = null
        )
    }

    fun guardarRecordatorio() {
        val estadoActual = _uiState.value

        if (estadoActual.enviando || estadoActual.guardadoExitoso) {
            return
        }

        _uiState.value = estadoActual.copy(
            intentoGuardar = true,
            mensajeError = null
        )

        if (!estadoActual.formularioValido) {
            return
        }

        val categoria = estadoActual.categoria
        val prioridad = estadoActual.prioridad

        if (
            categoria == null ||
            categoria !in categoriasDisponibles ||
            prioridad == null
        ) {
            _uiState.value = _uiState.value.copy(
                mensajeError = "Selecciona una categoría y prioridad válidas."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                enviando = true,
                mensajeError = null
            )

            try {
                delay(500)

                val siguienteId = (
                        repository.obtenerTodos()
                            .maxOfOrNull { it.id } ?: 0L
                        ) + 1L

                val nuevoRecordatorio = Recordatorio(
                    id = siguienteId,
                    titulo = estadoActual.titulo.trim(),
                    descripcion = estadoActual.descripcion
                        .trim()
                        .ifBlank { null },
                    categoria = categoria,
                    prioridad = prioridad,
                    estado = EstadoRecordatorio.Pendiente
                )

                repository.guardar(nuevoRecordatorio)

                _uiState.value = _uiState.value.copy(
                    enviando = false,
                    guardadoExitoso = true
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    enviando = false,
                    mensajeError = e.message
                        ?: "No fue posible guardar el recordatorio."
                )
            }
        }
    }
}
