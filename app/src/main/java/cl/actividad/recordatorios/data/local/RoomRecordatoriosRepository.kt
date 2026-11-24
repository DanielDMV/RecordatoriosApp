package cl.actividad.recordatorios.data.local

import cl.actividad.recordatorios.data.RecordatoriosRepository
import cl.actividad.recordatorios.model.Recordatorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction
import cl.actividad.recordatorios.data.sync.TipoOperacionPendiente
import java.util.UUID

class RoomRecordatoriosRepository(
    private val database: RecordatoriosDatabase
) : RecordatoriosRepository {

    private val dao: RecordatorioDao =
        database.recordatorioDao()

    private val operacionesDao: OperacionPendienteDao =
        database.operacionPendienteDao()


    override suspend fun obtenerTodos(): List<Recordatorio> {
        return dao.obtenerTodos().map { entidad ->
            entidad.toDomain()
        }
    }

    override suspend fun obtenerPorId(id: Long): Recordatorio? {
        return dao.obtenerPorId(id)?.toDomain()
    }

    override suspend fun guardar(recordatorio: Recordatorio) {
        insertarConOperacionPendiente(recordatorio)
    }

    override suspend fun actualizar(
        recordatorio: Recordatorio
    ): Boolean {
        return actualizarConOperacionPendiente(recordatorio)
    }

    override suspend fun eliminarPorId(
        id: Long
    ): Boolean {
        return eliminarConOperacionPendiente(id)
    }

    override suspend fun crear(
        recordatorio: Recordatorio
    ): Long {

        return insertarConOperacionPendiente(
            recordatorio.copy(id = 0L)
        )
    }
    private suspend fun insertarConOperacionPendiente(
        recordatorio: Recordatorio
    ): Long {

        return database.withTransaction {

            val fechaActual = System.currentTimeMillis()

            val entidad = recordatorio.toEntity().copy(
                estadoSincronizacion = "PENDIENTE",
                fechaModificacion = fechaActual
            )

            val idLocal = dao.insertar(entidad)

            val operacion = OperacionPendienteEntity(
                operationId = UUID.randomUUID().toString(),
                recordatorioId = idLocal,
                tipo = TipoOperacionPendiente.CREAR.name,
                remoteId = null,
                payloadJson = null,
                intentos = 0,
                creadaEn = fechaActual
            )

            operacionesDao.insertar(operacion)

            idLocal
        }
    }

    private suspend fun actualizarConOperacionPendiente(
        recordatorio: Recordatorio
    ): Boolean {

        return database.withTransaction {

            val entidadAnterior =
                dao.obtenerPorId(recordatorio.id)
                    ?: return@withTransaction false

            val fechaActual = System.currentTimeMillis()

            val entidadActualizada =
                recordatorio.toEntity(entidadAnterior).copy(
                    estadoSincronizacion = "PENDIENTE",
                    fechaModificacion = fechaActual
                )

            val filasActualizadas =
                dao.actualizar(entidadActualizada)

            if (filasActualizadas == 0) {
                return@withTransaction false
            }

            val operacionesExistentes =
                operacionesDao.obtenerPorRecordatorio(
                    recordatorio.id
                )

            val tieneCreacionPendiente =
                operacionesExistentes.any {
                    it.tipo == TipoOperacionPendiente.CREAR.name
                }

            if (!tieneCreacionPendiente) {

                operacionesDao.insertar(
                    OperacionPendienteEntity(
                        operationId = UUID.randomUUID().toString(),
                        recordatorioId = recordatorio.id,
                        tipo = TipoOperacionPendiente.ACTUALIZAR.name,
                        remoteId = entidadAnterior.remoteId,
                        payloadJson = null,
                        intentos = 0,
                        creadaEn = fechaActual
                    )
                )
            }

            true
        }
    }

    private suspend fun eliminarConOperacionPendiente(
        recordatorioId: Long
    ): Boolean {

        return database.withTransaction {

            val entidadAnterior =
                dao.obtenerPorId(recordatorioId)
                    ?: return@withTransaction false

            val operacionesExistentes =
                operacionesDao.obtenerPorRecordatorio(
                    recordatorioId
                )

            val creacionPendiente =
                operacionesExistentes.firstOrNull {
                    it.tipo == TipoOperacionPendiente.CREAR.name
                }

            if (
                creacionPendiente != null &&
                entidadAnterior.remoteId == null &&
                creacionPendiente.intentos == 0
            ) {
                // Creación exclusivamente local y nunca enviada.
                val eliminadas =
                    dao.eliminarPorId(recordatorioId)

                if (eliminadas == 0) {
                    return@withTransaction false
                }

                val operacionesEliminadas =
                    operacionesDao.eliminarPorOperacion(
                        creacionPendiente.operationId
                    )

                check(operacionesEliminadas == 1) {
                    "No fue posible eliminar la operación pendiente."
                }

                true

            } else if (
                entidadAnterior.remoteId == null &&
                operacionesExistentes.isEmpty()
            ) {
                // Registro histórico que nunca fue sincronizado.
                dao.eliminarPorId(recordatorioId) > 0

            } else {
                // Se conserva la referencia para eliminar remotamente.
                val fechaActual = System.currentTimeMillis()

                operacionesDao.insertar(
                    OperacionPendienteEntity(
                        operationId = UUID.randomUUID().toString(),
                        recordatorioId = recordatorioId,
                        tipo = TipoOperacionPendiente.ELIMINAR.name,
                        remoteId = entidadAnterior.remoteId,
                        payloadJson = null,
                        intentos = 0,
                        creadaEn = fechaActual
                    )
                )

                dao.eliminarPorId(recordatorioId) > 0
            }
        }
    }

    suspend fun normalizarOperacionesPendientes(): Int {

        return database.withTransaction {

            val recordatorios = dao.obtenerTodos()

            val operaciones = operacionesDao.obtenerPendientes()

            val idsConOperaciones = operaciones
                .map { it.recordatorioId }
                .toSet()

            var operacionesCreadas = 0

            recordatorios.forEach { recordatorio ->

                val requiereCreacion =
                    recordatorio.remoteId == null &&
                            recordatorio.id !in idsConOperaciones

                if (requiereCreacion) {

                    operacionesDao.insertar(
                        OperacionPendienteEntity(
                            operationId =
                                UUID.randomUUID().toString(),
                            recordatorioId = recordatorio.id,
                            tipo =
                                TipoOperacionPendiente.CREAR.name,
                            remoteId = null,
                            payloadJson = null,
                            intentos = 0,
                            creadaEn =
                                System.currentTimeMillis()
                        )
                    )

                    operacionesCreadas++
                }
            }

            operacionesCreadas
        }
    }

    override fun observarTodos(): Flow<List<Recordatorio>> =
        dao.observarTodos().map { entidades ->
            entidades.map { entidad ->
                entidad.toDomain()
            }
        }

}
