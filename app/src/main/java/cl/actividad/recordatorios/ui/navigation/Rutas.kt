package cl.actividad.recordatorios.ui.navigation

object Rutas {

    const val LISTADO = "listado"

    const val CREACION = "creacion"

    const val DETALLE = "detalle/{recordatorioId}"

    const val EDICION = "edicion/{recordatorioId}"

    fun detalle(recordatorioId: Long): String {
        return "detalle/$recordatorioId"
    }

    fun edicion(recordatorioId: Long): String {
        return "edicion/$recordatorioId"
    }
}