package cl.actividad.recordatorios.domain

import cl.actividad.recordatorios.model.EstadoRecordatorio
import cl.actividad.recordatorios.model.Prioridad
import cl.actividad.recordatorios.model.Recordatorio

object FiltroRecordatorios {

    fun porPrioridad(
        recordatorios: List<Recordatorio>,
        prioridad: Prioridad
    ): List<Recordatorio> =
        recordatorios.filter { it.prioridad == prioridad }

    fun porEstado(
        recordatorios: List<Recordatorio>,
        estado: EstadoRecordatorio
    ): List<Recordatorio> =
        recordatorios.filter { it.estado == estado }
}