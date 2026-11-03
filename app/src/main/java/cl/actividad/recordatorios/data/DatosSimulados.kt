package cl.actividad.recordatorios.data

import cl.actividad.recordatorios.model.Categoria
import cl.actividad.recordatorios.model.EstadoRecordatorio
import cl.actividad.recordatorios.model.Prioridad
import cl.actividad.recordatorios.model.Recordatorio

object DatosSimulados {

    private val estudios = Categoria(1, "Estudios")
    private val personal = Categoria(2, "Personal")
    private val trabajo = Categoria(3, "Trabajo")

    val recordatorios: List<Recordatorio> = listOf(
        Recordatorio(
            id = 1L,
            titulo = "Estudiar Kotlin",
            descripcion = "Repasar listas, conjuntos y mapas",
            categoria = estudios,
            prioridad = Prioridad.Alta,
            estado = EstadoRecordatorio.Pendiente
        ),
        Recordatorio(
            id = 2L,
            titulo = "Completar actividad de programación",
            categoria = estudios,
            prioridad = Prioridad.Alta,
            estado = EstadoRecordatorio.EnProgreso
        ),
        Recordatorio(
            id = 3L,
            titulo = "Comprar materiales",
            categoria = personal,
            prioridad = Prioridad.Media,
            estado = EstadoRecordatorio.Pendiente,
            ubicacion = "Librería"
        ),
        Recordatorio(
            id = 4L,
            titulo = "Organizar escritorio",
            categoria = personal,
            prioridad = Prioridad.Baja,
            estado = EstadoRecordatorio.Completado
        ),
        Recordatorio(
            id = 5L,
            titulo = "Preparar informe",
            categoria = trabajo,
            prioridad = Prioridad.Media,
            estado = EstadoRecordatorio.EnProgreso
        ),
        Recordatorio(
            id = 6L,
            titulo = "Revisar correo",
            categoria = trabajo,
            prioridad = Prioridad.Baja,
            estado = EstadoRecordatorio.Completado
        )
    )

    val categorias: Set<Categoria> =
        recordatorios.map { it.categoria }.toSet()

    val etiquetasPorCategoria: Map<Categoria, String> = mapOf(
        estudios to "Actividades académicas",
        personal to "Tareas personales",
        trabajo to "Responsabilidades laborales"
    )
}