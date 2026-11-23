package cl.actividad.recordatorios.data.local

import cl.actividad.recordatorios.model.Categoria
import cl.actividad.recordatorios.model.EstadoRecordatorio
import cl.actividad.recordatorios.model.Prioridad
import cl.actividad.recordatorios.model.Recordatorio

fun Recordatorio.toEntity(): RecordatorioEntity =
    RecordatorioEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        categoriaId = categoria.id,
        categoriaNombre = categoria.nombre,
        prioridad = prioridad.nombre,
        estado = estado.nombre,
        fechaAviso = fechaAviso,
        ubicacion = ubicacion,
        fechaCreacion = fechaCreacion
    )

fun RecordatorioEntity.toDomain(): Recordatorio =
    Recordatorio(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = Categoria(
            id = categoriaId,
            nombre = categoriaNombre
        ),
        prioridad = when (prioridad) {
            Prioridad.Alta.nombre -> Prioridad.Alta
            Prioridad.Media.nombre -> Prioridad.Media
            Prioridad.Baja.nombre -> Prioridad.Baja
            else -> error("Prioridad desconocida: $prioridad")
        },
        estado = when (estado) {
            EstadoRecordatorio.Pendiente.nombre ->
                EstadoRecordatorio.Pendiente

            EstadoRecordatorio.EnProgreso.nombre ->
                EstadoRecordatorio.EnProgreso

            EstadoRecordatorio.Completado.nombre ->
                EstadoRecordatorio.Completado

            else -> error("Estado desconocido: $estado")
        },
        fechaAviso = fechaAviso,
        ubicacion = ubicacion,
        fechaCreacion = fechaCreacion
    )
