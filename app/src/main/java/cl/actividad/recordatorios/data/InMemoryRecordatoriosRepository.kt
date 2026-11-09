package cl.actividad.recordatorios.data

import cl.actividad.recordatorios.model.Recordatorio
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryRecordatoriosRepository(
    recordatoriosIniciales: List<Recordatorio> =
        DatosSimulados.recordatorios
) : RecordatoriosRepository {

    private val mutex = Mutex()

    private val recordatorios =
        recordatoriosIniciales.toMutableList()

    override suspend fun obtenerTodos(): List<Recordatorio> =
        mutex.withLock {
            recordatorios.toList()
        }

    override suspend fun obtenerPorId(id: Long): Recordatorio? =
        mutex.withLock {
            recordatorios.find { it.id == id }
        }

    override suspend fun guardar(recordatorio: Recordatorio) {
        mutex.withLock {
            require(recordatorios.none { it.id == recordatorio.id }) {
                "Ya existe un recordatorio con ese identificador."
            }

            recordatorios.add(recordatorio)
        }
    }
}
