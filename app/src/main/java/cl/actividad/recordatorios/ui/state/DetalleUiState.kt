package cl.actividad.recordatorios.ui.state

import cl.actividad.recordatorios.model.Recordatorio

sealed interface DetalleUiState {

    data object Cargando : DetalleUiState

    data class ConDatos(
        val recordatorio: Recordatorio
    ) : DetalleUiState

    data object NoEncontrado : DetalleUiState

    data class Error(
        val mensaje: String
    ) : DetalleUiState
}


