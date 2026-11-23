package cl.actividad.recordatorios.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "recordatorios")
data class RecordatorioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val titulo: String,
    val descripcion: String?,

    val categoriaId: Int,
    val categoriaNombre: String,

    val prioridad: String,
    val estado: String,

    val fechaAviso: String?,
    val ubicacion: String?,

    val fechaCreacion: Long,

@ColumnInfo(defaultValue = "'PENDIENTE'")
    val estadoSincronizacion: String = "PENDIENTE",

    val remoteId: String? = null,

@ColumnInfo(defaultValue = "0")
    val fechaModificacion: Long = 0L
)