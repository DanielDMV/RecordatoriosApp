package cl.actividad.recordatorios.data.remote

data class RecordatorioDto(
    val id: String,
    val titulo: String,
    val descripcion: String?,
    val categoria: String,
    val prioridad: String,
    val estado: String
)

data class CrearRecordatorioRequest(
    val titulo: String,
    val descripcion: String?,
    val categoria: String,
    val prioridad: String,
    val estado: String
)
