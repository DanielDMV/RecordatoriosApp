package cl.actividad.recordatorios.ui.state

import cl.actividad.recordatorios.model.Categoria
import cl.actividad.recordatorios.model.Prioridad

data class CreacionUiState(
    val titulo: String = "",
    val tituloModificado: Boolean = false,
    val descripcion: String = "",
    val categoria: Categoria? = null,
    val prioridad: Prioridad? = null,
    val enviando: Boolean = false,
    val intentoGuardar: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val mensajeError: String? = null
) {
    val tituloValido: Boolean
        get() = titulo.trim().length >= 4

    val descripcionValida: Boolean
        get() = descripcion.length <= 200

    val formularioValido: Boolean
        get() = tituloValido &&
                descripcionValida &&
                categoriaValida &&
                prioridad != null

    val puedeGuardar: Boolean
        get() = formularioValido && !enviando

    val categoriaValida: Boolean
        get() = categoria != null &&
                categoria.id > 0 &&
                categoria.nombre.isNotBlank()
}
