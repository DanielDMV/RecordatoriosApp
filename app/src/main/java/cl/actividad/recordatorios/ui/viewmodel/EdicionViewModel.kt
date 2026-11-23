package cl.actividad.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.actividad.recordatorios.data.RecordatoriosRepository
import cl.actividad.recordatorios.model.Categoria
import cl.actividad.recordatorios.model.EstadoRecordatorio
import cl.actividad.recordatorios.model.Prioridad
import cl.actividad.recordatorios.model.Recordatorio
import cl.actividad.recordatorios.ui.state.CreacionUiState
import cl.actividad.recordatorios.ui.state.EdicionUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EdicionViewModel(
    private val repository: RecordatoriosRepository,
    private val recordatorioId: Long,
    val categoriasDisponibles: List<Categoria>
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<EdicionUiState>(EdicionUiState.Cargando)

    val uiState: StateFlow<EdicionUiState> =
        _uiState.asStateFlow()

    private var recordatorioOriginal: Recordatorio? = null

    init {
        cargarRecordatorio()
    }

    fun cargarRecordatorio() {
        viewModelScope.launch {
            _uiState.value = EdicionUiState.Cargando

            try {
                val recordatorio =
                    repository.obtenerPorId(recordatorioId)

                if (recordatorio == null) {
                    recordatorioOriginal = null
                    _uiState.value = EdicionUiState.NoEncontrado
                    return@launch
                }

                recordatorioOriginal = recordatorio

                _uiState.value = EdicionUiState.Formulario(
                    datos = CreacionUiState(
                        titulo = recordatorio.titulo,
                        descripcion = recordatorio.descripcion.orEmpty(),
                        categoria = recordatorio.categoria,
                        prioridad = recordatorio.prioridad
                    ),
                    estado = recordatorio.estado,
                    id = recordatorio.id
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = EdicionUiState.Error(
                    e.message
                        ?: "No fue posible cargar el recordatorio."
                )
            }
        }
    }

    private fun modificarFormulario(
        cambio: (CreacionUiState) -> CreacionUiState
    ) {
        val actual =
            _uiState.value as? EdicionUiState.Formulario
                ?: return

        if (actual.datos.enviando ||
            actual.datos.guardadoExitoso
        ) return

        _uiState.value = actual.copy(
            datos = cambio(actual.datos).copy(
                mensajeError = null
            )
        )
    }

    fun actualizarTitulo(titulo: String) {
        modificarFormulario {
            it.copy(
                titulo = titulo,
                tituloModificado = true
            )
        }
    }

    fun actualizarDescripcion(descripcion: String) {
        modificarFormulario {
            it.copy(
                descripcion = descripcion.take(200)
            )
        }
    }

    fun seleccionarCategoria(categoria: Categoria) {
        modificarFormulario {
            it.copy(categoria = categoria)
        }
    }

    fun seleccionarPrioridad(prioridad: Prioridad) {
        modificarFormulario {
            it.copy(prioridad = prioridad)
        }
    }

    fun seleccionarEstado(estado: EstadoRecordatorio) {
        val actual =
            _uiState.value as? EdicionUiState.Formulario
                ?: return

        if (actual.datos.enviando ||
            actual.datos.guardadoExitoso
        ) return

        _uiState.value = actual.copy(estado = estado)
    }

    fun guardarCambios() {
        val actual =
            _uiState.value as? EdicionUiState.Formulario
                ?: return

        if (actual.datos.enviando ||
            actual.datos.guardadoExitoso
        ) return

        val datos = actual.datos

        if (!datos.formularioValido ||
            datos.categoria !in categoriasDisponibles
        ) {
            _uiState.value = actual.copy(
                datos = datos.copy(
                    intentoGuardar = true,
                    tituloModificado = true,
                    mensajeError =
                        "Revisa los campos antes de guardar."
                )
            )
            return
        }

        val original = recordatorioOriginal ?: return
        val categoria = datos.categoria ?: return
        val prioridad = datos.prioridad ?: return

        _uiState.value = actual.copy(
            datos = datos.copy(
                enviando = true,
                mensajeError = null
            )
        )

        viewModelScope.launch {
            try {
                val actualizado = original.copy(
                    titulo = datos.titulo.trim(),
                    descripcion = datos.descripcion
                        .trim()
                        .ifBlank { null },
                    categoria = categoria,
                    prioridad = prioridad,
                    estado = actual.estado
                )

                val guardado =
                    repository.actualizar(actualizado)

                _uiState.value = if (guardado) {
                    EdicionUiState.Guardado
                } else {
                    EdicionUiState.NoEncontrado
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = actual.copy(
                    datos = datos.copy(
                        enviando = false,
                        mensajeError = e.message
                            ?: "No fue posible actualizar el recordatorio."
                    )
                )
            }
        }
    }
}
