package cl.actividad.recordatorios.data

import cl.actividad.recordatorios.model.Recordatorio

interface RecordatoriosRepository {

    suspend fun obtenerTodos(): List<Recordatorio>

    suspend fun obtenerPorId(id: Long): Recordatorio?

    suspend fun guardar(recordatorio: Recordatorio)
}
