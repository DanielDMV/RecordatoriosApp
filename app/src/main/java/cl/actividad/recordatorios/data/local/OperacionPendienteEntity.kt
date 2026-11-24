package cl.actividad.recordatorios.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "operaciones_pendientes")
data class OperacionPendienteEntity(
    @PrimaryKey
    val operationId: String,

    val recordatorioId: Long,

    val tipo: String,

    val remoteId: String? = null,

    val payloadJson: String? = null,

    val intentos: Int = 0,

    val creadaEn: Long,

    @ColumnInfo(defaultValue = "'PENDIENTE'")

    val estadoEnvio: String = "PENDIENTE",

    val ultimoIntentoEn: Long? = null,

    val proximoIntentoEn: Long? = null,

    val ultimoError: String? = null,

    val bloqueadaHasta: Long? = null

)
