package cl.actividad.recordatorios.model

data class Recordatorio(
    val id: Long,
    val titulo: String,
    val descripcion: String? = null,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: EstadoRecordatorio,
    val fechaAviso: String? = null,
    val ubicacion: String? = null
)
