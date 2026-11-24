package cl.actividad.recordatorios.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface RecordatoriosApi {

    @GET("recordatorios")
    suspend fun obtenerRecordatorios():
            Response<List<RecordatorioDto>>

    @POST("recordatorios")
    suspend fun crearRecordatorio(
        @Header("Idempotency-Key") operationId: String,
        @Body request: CrearRecordatorioRequest
    ): Response<RecordatorioDto>
}