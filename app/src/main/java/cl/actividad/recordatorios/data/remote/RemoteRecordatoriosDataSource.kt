package cl.actividad.recordatorios.data.remote

import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.Response

sealed interface ResultadoRemoto<out T> {
    data class Exito<T>(val datos: T) : ResultadoRemoto<T>

    data class ErrorHttp(val codigo: Int) :
        ResultadoRemoto<Nothing>

    data object Timeout : ResultadoRemoto<Nothing>

    data object SinConexion : ResultadoRemoto<Nothing>
}

class RemoteRecordatoriosDataSource(
    private val api: RecordatoriosApi
) {

    suspend fun consultar():
            ResultadoRemoto<List<RecordatorioDto>> =
        procesar { api.obtenerRecordatorios() }

    suspend fun crear(
        operationId: String,
        request: CrearRecordatorioRequest
    ): ResultadoRemoto<RecordatorioDto> {

        require(operationId.isNotBlank())

        return procesar {
            api.crearRecordatorio(operationId, request)
        }
    }

    private suspend fun <T : Any> procesar(
        solicitud: suspend () -> Response<T>
    ): ResultadoRemoto<T> {
        return try {
            val respuesta = solicitud()

            if (!respuesta.isSuccessful) {
                ResultadoRemoto.ErrorHttp(respuesta.code())
            } else {
                val datos = respuesta.body()

                if (datos == null) {
                    ResultadoRemoto.ErrorHttp(502)
                } else {
                    ResultadoRemoto.Exito(datos)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {
            ResultadoRemoto.Timeout
        } catch (e: IOException) {
            ResultadoRemoto.SinConexion
        }
    }

    suspend fun consultarOperacion(
        operationId: String
    ): ResultadoRemoto<RecordatorioDto> {

        require(operationId.isNotBlank())

        return procesar {
            api.consultarOperacion(operationId)
        }
    }

}
