package cl.actividad.recordatorios.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operaciones_pendientes")
data class OperacionPendienteEntity(
    @PrimaryKey
    val operationId: String,

    val recordatorioId: Long,

    val tipo: String,

    val remoteId: String? = null,

    val payloadJson: String? = null,

    val intentos: Int = 0,

    val creadaEn: Long
)
