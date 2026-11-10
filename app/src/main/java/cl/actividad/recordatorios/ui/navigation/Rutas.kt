package cl.actividad.recordatorios.ui.navigation

object Rutas {
    const val LISTADO = "listado"
    const val CREACION = "creacion"
    const val DETALLE = "detalle/{recordatorioId}"

    fun detalle(recordatorioId: Long): String {
        return "detalle/$recordatorioId"
    }
}
