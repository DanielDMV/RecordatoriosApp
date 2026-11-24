
package cl.actividad.recordatorios.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OperacionPendienteDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(
        operacion: OperacionPendienteEntity
    )

    @Query(
        """
        SELECT * FROM operaciones_pendientes
        ORDER BY creadaEn ASC, operationId ASC
        """
    )
    fun observarPendientes(): Flow<List<OperacionPendienteEntity>>

    @Query(
        """
        SELECT * FROM operaciones_pendientes
        ORDER BY creadaEn ASC, operationId ASC
        """
    )
    suspend fun obtenerPendientes(): List<OperacionPendienteEntity>

    @Query(
        """
        SELECT * FROM operaciones_pendientes
        WHERE recordatorioId = :recordatorioId
        ORDER BY creadaEn ASC
        """
    )
    suspend fun obtenerPorRecordatorio(
        recordatorioId: Long
    ): List<OperacionPendienteEntity>

    @Query(
        """
        DELETE FROM operaciones_pendientes
        WHERE operationId = :operationId
        """
    )
    suspend fun eliminarPorOperacion(
        operationId: String
    ): Int

    @Query(
        """
        UPDATE operaciones_pendientes
        SET intentos = intentos + 1
        WHERE operationId = :operationId
        """
    )
    suspend fun incrementarIntentos(
        operationId: String
    ): Int

    @Query(
        """
    SELECT * FROM operaciones_pendientes
    WHERE operationId = :operationId
    LIMIT 1
    """
    )
    suspend fun obtenerPorOperacion(
        operationId: String
    ): OperacionPendienteEntity?

    @Query(
        """
    SELECT COUNT(*) FROM operaciones_pendientes
    """
    )
    suspend fun contarPendientes(): Int

    @Query(
        """
    SELECT COUNT(*) FROM operaciones_pendientes
    WHERE recordatorioId = :recordatorioId
    """
    )
    suspend fun contarPorRecordatorio(
        recordatorioId: Long
    ): Int

}