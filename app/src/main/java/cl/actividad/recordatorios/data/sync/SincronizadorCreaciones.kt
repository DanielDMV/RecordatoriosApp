package cl.actividad.recordatorios.data.sync

import cl.actividad.recordatorios.data.local.ConfirmadorCreacionLocal
import cl.actividad.recordatorios.data.local.RecordatoriosDatabase
import cl.actividad.recordatorios.data.remote.CrearRecordatorioRequest
import cl.actividad.recordatorios.data.remote.RemoteRecordatoriosDataSource
import cl.actividad.recordatorios.data.remote.ResultadoRemoto
import java.util.UUID

class SincronizadorCreaciones(
    private val database: RecordatoriosDatabase,
    private val remoto: RemoteRecordatoriosDataSource
) {

    private val operaciones = database.operacionPendienteDao()
    private val recordatorios = database.recordatorioDao()
    private val confirmador = ConfirmadorCreacionLocal(database)

    suspend fun sincronizar(): Int {

        val pendientes = operaciones.obtenerPendientes()
            .filter {
                it.tipo == TipoOperacionPendiente.CREAR.name &&
                        it.estadoEnvio == "PENDIENTE"
            }

        var confirmadas = 0

        for (operacion in pendientes) {

            val ahora = System.currentTimeMillis()
            val token = UUID.randomUUID().toString()
            val vencimiento = ahora + 60_000L

            val adquirida = operaciones.adquirirOperacion(
                operationId = operacion.operationId,
                ahora = ahora,
                bloqueadaHasta = vencimiento,
                tokenBloqueo = token
            )

            if (adquirida != 1) continue

            val recordatorio =
                recordatorios.obtenerPorId(operacion.recordatorioId)

            if (recordatorio == null) {
                operaciones.marcarResultadoIncierto(
                    operacion.operationId,
                    token,
                    System.currentTimeMillis(),
                    "Recordatorio local no encontrado"
                )
                continue
            }

            val versionLocal = recordatorio.fechaModificacion

            val intentoRegistrado = operaciones.registrarIntentoEnvio(
                operationId = operacion.operationId,
                tokenBloqueo = token,
                ahora = System.currentTimeMillis()
            )

            if (intentoRegistrado != 1) continue

            val resultado = remoto.crear(
                operationId = operacion.operationId,
                request = CrearRecordatorioRequest(
                    titulo = recordatorio.titulo,
                    descripcion = recordatorio.descripcion,
                    categoria = recordatorio.categoriaNombre,
                    prioridad = recordatorio.prioridad,
                    estado = recordatorio.estado
                )
            )

            when (resultado) {

                is ResultadoRemoto.Exito -> {

                    val remoteId = resultado.datos.id

                    if (remoteId.isNotBlank()) {

                        val confirmado = confirmador.confirmar(
                            operationId = operacion.operationId,
                            tokenBloqueo = token,
                            remoteId = remoteId,
                            estadoFinal =
                                EstadoSincronizacion.SINCRONIZADO.name,
                            ahora = System.currentTimeMillis(),
                            fechaVersionEsperada = versionLocal
                        )

                        if (confirmado) {
                            confirmadas++
                            continue
                        }
                    }

                    operaciones.marcarResultadoIncierto(
                        operacion.operationId,
                        token,
                        System.currentTimeMillis(),
                        "Confirmación remota no aplicada localmente"
                    )
                }

                is ResultadoRemoto.ErrorHttp -> {
                    operaciones.marcarResultadoIncierto(
                        operacion.operationId,
                        token,
                        System.currentTimeMillis(),
                        "Error HTTP ${resultado.codigo}"
                    )
                }

                ResultadoRemoto.Timeout -> {
                    operaciones.marcarResultadoIncierto(
                        operacion.operationId,
                        token,
                        System.currentTimeMillis(),
                        "Tiempo de espera agotado"
                    )
                }

                ResultadoRemoto.SinConexion -> {
                    operaciones.marcarResultadoIncierto(
                        operacion.operationId,
                        token,
                        System.currentTimeMillis(),
                        "Error de comunicación"
                    )
                }
            }
        }

        return confirmadas
    }
}