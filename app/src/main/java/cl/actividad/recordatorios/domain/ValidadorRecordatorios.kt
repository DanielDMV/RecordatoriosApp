package cl.actividad.recordatorios.domain

import cl.actividad.recordatorios.model.Recordatorio

object ValidadorRecordatorios {

    fun validar(recordatorio: Recordatorio): List<String> = buildList {
        if (recordatorio.id <= 0L) {
            add("El identificador del recordatorio debe ser positivo.")
        }

        if (recordatorio.titulo.isBlank()) {
            add("El título no puede estar vacío.")
        }

        if (recordatorio.categoria.id <= 0) {
            add("El identificador de la categoría debe ser positivo.")
        }

        if (recordatorio.categoria.nombre.isBlank()) {
            add("El nombre de la categoría no puede estar vacío.")
        }
    }
}