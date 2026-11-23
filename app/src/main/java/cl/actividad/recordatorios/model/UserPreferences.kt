package cl.actividad.recordatorios.model

enum class TemaApp {
    SISTEMA,
    CLARO,
    OSCURO
}

enum class OrdenRecordatorios {
    MAS_RECIENTES,
    PRIORIDAD
}

data class UserPreferences(
    val tema: TemaApp = TemaApp.SISTEMA,
    val mostrarCompletados: Boolean = true,
    val orden: OrdenRecordatorios = OrdenRecordatorios.MAS_RECIENTES
)
