package cl.actividad.recordatorios.data.sync

import androidx.room.withTransaction
import cl.actividad.recordatorios.data.local.RecordatorioEntity
import cl.actividad.recordatorios.data.local.RecordatoriosDatabase
import cl.actividad.recordatorios.data.remote.RecordatorioDto
import cl.actividad.recordatorios.data.remote.RemoteRecordatoriosDataSource
import cl.actividad.recordatorios.data.remote.ResultadoRemoto
import cl.actividad.recordatorios.model.EstadoRecordatorio
import cl.actividad.recordatorios.model.Prioridad

sealed interface ResultadoImportacion {
    data class Correcto(
        val incorporados: Int,
        val actualizados: Int,
        val ignorados: Int
    ) : ResultadoImportacion

    data class Error(val mensaje: String) : ResultadoImportacion
}

class ImportadorRecordatoriosRemotos(
    private val database: RecordatoriosDatabase,
    private val remoto: RemoteRecordatoriosDataSource
) {

    suspend fun importar(): ResultadoImportacion {

        val respuesta = remoto.consultar()

        val registros = when (respuesta) {
            is ResultadoRemoto.Exito -> respuesta.datos

            is ResultadoRemoto.ErrorHttp ->
                return ResultadoImportacion.Error(
                    "Error HTTP ${respuesta.codigo}"
                )

            ResultadoRemoto.Timeout ->
                return ResultadoImportacion.Error(
                    "Tiempo de espera agotado"
                )

            ResultadoRemoto.SinConexion ->
                return ResultadoImportacion.Error(
                    "Sin conexión con el servidor"
                )
        }

        return database.withTransaction {

            val dao = database.recordatorioDao()
            val operaciones = database.operacionPendienteDao()

            var incorporados = 0
            var actualizados = 0
            var ignorados = 0

            for (dto in registros.distinctBy { it.id }) {

                if (!esValido(dto)) {
                    ignorados++
                    continue
                }

                val existente = dao.obtenerPorRemoteId(dto.id)

                if (existente == null) {

                    val ahora = System.currentTimeMillis()

                    dao.insertar(
                        RecordatorioEntity(
                            id = 0L,
                            titulo = dto.titulo,
                            descripcion = dto.descripcion,
                            categoriaId = 0,
                            categoriaNombre = dto.categoria,
                            prioridad = dto.prioridad,
                            estado = dto.estado,
                            fechaAviso = null,
                            ubicacion = null,
                            fechaCreacion = ahora,
                            estadoSincronizacion =
                                EstadoSincronizacion.SINCRONIZADO.name,
                            remoteId = dto.id,
                            fechaModificacion = ahora
                        )
                    )

                    incorporados++

                } else {

                    val tieneOperaciones =
                        operaciones.obtenerPorRecordatorio(
                            existente.id
                        ).isNotEmpty()

                    if (
                        tieneOperaciones ||
                        existente.estadoSincronizacion !=
                        EstadoSincronizacion.SINCRONIZADO.name
                    ) {
                        ignorados++
                        continue
                    }

                    // Mantener el identificador local y los
                    // campos que el contrato remoto no contiene.
                    dao.actualizar(
                        existente.copy(
                            titulo = dto.titulo,
                            descripcion = dto.descripcion,
                            categoriaNombre = dto.categoria,
                            prioridad = dto.prioridad,
                            estado = dto.estado
                        )
                    )

                    actualizados++
                }
            }

            ResultadoImportacion.Correcto(
                incorporados = incorporados,
                actualizados = actualizados,
                ignorados = ignorados
            )
        }
    }


    private fun esValido(dto: RecordatorioDto): Boolean {

        val prioridadesValidas = setOf(
            Prioridad.Alta.nombre,
            Prioridad.Media.nombre,
            Prioridad.Baja.nombre
        )

        val estadosValidos = setOf(
            EstadoRecordatorio.Pendiente.nombre,
            EstadoRecordatorio.EnProgreso.nombre,
            EstadoRecordatorio.Completado.nombre
        )

        return dto.id.isNotBlank() &&
                dto.titulo.isNotBlank() &&
                dto.categoria.isNotBlank() &&
                dto.prioridad in prioridadesValidas &&
                dto.estado in estadosValidos
    }

}