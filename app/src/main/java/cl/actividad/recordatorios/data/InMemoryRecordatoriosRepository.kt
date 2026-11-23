package cl.actividad.recordatorios.data

import cl.actividad.recordatorios.model.Recordatorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryRecordatoriosRepository(
    recordatoriosIniciales: List<Recordatorio> =
        DatosSimulados.recordatorios
) : RecordatoriosRepository {

    private val mutex = Mutex()

    private val recordatorios =
        recordatoriosIniciales.toMutableList()

    private val _recordatoriosFlow =
        MutableStateFlow(recordatorios.toList())

    override fun observarTodos(): Flow<List<Recordatorio>> =
        _recordatoriosFlow.asStateFlow()

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
            _recordatoriosFlow.value = recordatorios.toList()
        }
    }

    override suspend fun crear(recordatorio: Recordatorio): Long =
        mutex.withLock {
            val siguienteId =
                (recordatorios.maxOfOrNull { it.id } ?: 0L) + 1L

            recordatorios.add(
                recordatorio.copy(id = siguienteId)
            )

            _recordatoriosFlow.value = recordatorios.toList()

            siguienteId
        }


    override suspend fun actualizar(
        recordatorio: Recordatorio
    ): Boolean = mutex.withLock {

        val indice = recordatorios.indexOfFirst {
            it.id == recordatorio.id
        }

        if (indice == -1) {
            false
        } else {
            recordatorios[indice] = recordatorio

            _recordatoriosFlow.value = recordatorios.toList()

            true
        }
    }

    override suspend fun eliminarPorId(
        id: Long
    ): Boolean = mutex.withLock {

        val eliminado = recordatorios.removeAll {
            it.id == id
        }

        if (eliminado) {
            _recordatoriosFlow.value = recordatorios.toList()
        }

        eliminado
    }

}