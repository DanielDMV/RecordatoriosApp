package cl.actividad.recordatorios.ui.state

import cl.actividad.recordatorios.model.EstadoRecordatorio

sealed interface EdicionUiState {

    data object Cargando : EdicionUiState

    data class Formulario(
        val datos: CreacionUiState,
        val estado: EstadoRecordatorio,
        val id: Long
    ) : EdicionUiState

    data object Guardado : EdicionUiState

    data object NoEncontrado : EdicionUiState

    data class Error(
        val mensaje: String
    ) : EdicionUiState
}
