package cl.actividad.recordatorios.data

import cl.actividad.recordatorios.model.Recordatorio
import kotlinx.coroutines.flow.Flow

interface RecordatoriosRepository {

    fun observarTodos(): Flow<List<Recordatorio>>

    suspend fun obtenerTodos(): List<Recordatorio>

    suspend fun obtenerPorId(id: Long): Recordatorio?

    suspend fun guardar(recordatorio: Recordatorio)

    suspend fun crear(recordatorio: Recordatorio): Long

    suspend fun actualizar(recordatorio: Recordatorio): Boolean

    suspend fun eliminarPorId(id: Long): Boolean
}
