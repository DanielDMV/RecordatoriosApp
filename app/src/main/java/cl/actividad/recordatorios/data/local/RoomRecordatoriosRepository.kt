package cl.actividad.recordatorios.data.local

import cl.actividad.recordatorios.data.RecordatoriosRepository
import cl.actividad.recordatorios.model.Recordatorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRecordatoriosRepository(
    private val dao: RecordatorioDao
) : RecordatoriosRepository {

    override suspend fun obtenerTodos(): List<Recordatorio> {
        return dao.obtenerTodos().map { entidad ->
            entidad.toDomain()
        }
    }

    override suspend fun obtenerPorId(id: Long): Recordatorio? {
        return dao.obtenerPorId(id)?.toDomain()
    }

    override suspend fun guardar(recordatorio: Recordatorio) {
        dao.insertar(recordatorio.toEntity())
    }

    override suspend fun actualizar(
        recordatorio: Recordatorio
    ): Boolean {
        return dao.actualizar(recordatorio.toEntity()) > 0
    }

    override suspend fun eliminarPorId(id: Long): Boolean {
        return dao.eliminarPorId(id) > 0
    }

    override suspend fun crear(recordatorio: Recordatorio): Long {
        val entidad = recordatorio
            .copy(id = 0L)
            .toEntity()

        return dao.insertar(entidad)
    }

    override fun observarTodos(): Flow<List<Recordatorio>> =
        dao.observarTodos().map { entidades ->
            entidades.map { entidad ->
                entidad.toDomain()
            }
        }

}
