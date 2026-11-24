package cl.actividad.recordatorios.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {

    @Query("SELECT * FROM recordatorios ORDER BY fechaCreacion DESC, id DESC")
    fun observarTodos(): Flow<List<RecordatorioEntity>>

    @Query("SELECT * FROM recordatorios ORDER BY fechaCreacion DESC, id DESC")
    suspend fun obtenerTodos(): List<RecordatorioEntity>

    @Query("SELECT * FROM recordatorios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): RecordatorioEntity?

    @Query("SELECT * FROM recordatorios WHERE estado = :estado ORDER BY fechaCreacion DESC")
    fun observarPorEstado(
        estado: String
    ): Flow<List<RecordatorioEntity>>

    @Insert
    suspend fun insertar(
        recordatorio: RecordatorioEntity
    ): Long

    @Update
    suspend fun actualizar(
        recordatorio: RecordatorioEntity
    ): Int

    @Delete
    suspend fun eliminar(
        recordatorio: RecordatorioEntity
    ): Int

    @Query("DELETE FROM recordatorios WHERE id = :id")
    suspend fun eliminarPorId(id: Long): Int

    @Query("SELECT COUNT(*) FROM recordatorios")
    suspend fun contar(): Int

    @Query("""
    SELECT * FROM recordatorios
    WHERE remoteId = :remoteId
    LIMIT 1
""")
    suspend fun obtenerPorRemoteId(
        remoteId: String
    ): RecordatorioEntity?
}
