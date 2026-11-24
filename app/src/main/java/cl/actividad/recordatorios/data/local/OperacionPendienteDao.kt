
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


    // Adquirir una operación mediante una actualización atómica.
    // Solo una ejecución puede cambiarla de PENDIENTE a EN_PROCESO.

    @Query(
        """
    UPDATE operaciones_pendientes
    SET estadoEnvio = 'EN_PROCESO',
        tokenBloqueo = :tokenBloqueo,
        bloqueadaHasta = :bloqueadaHasta
    WHERE operationId = :operationId
      AND estadoEnvio = 'PENDIENTE'
      AND tokenBloqueo IS NULL
      AND (
          proximoIntentoEn IS NULL
          OR proximoIntentoEn <= :ahora
      )
      AND :bloqueadaHasta > :ahora
      AND LENGTH(:tokenBloqueo) > 0
    """
    )
    suspend fun adquirirOperacion(
        operationId: String,
        ahora: Long,
        bloqueadaHasta: Long,
        tokenBloqueo: String
    ): Int

    // Registrar el inicio de un intento de transmisión.
    // Este método se llamará antes de efectuar una solicitud HTTP.

    @Query(
        """
    UPDATE operaciones_pendientes
    SET intentos = intentos + 1,
        ultimoIntentoEn = :ahora
    WHERE operationId = :operationId
      AND estadoEnvio = 'EN_PROCESO'
      AND tokenBloqueo = :tokenBloqueo
      AND bloqueadaHasta IS NOT NULL
      AND bloqueadaHasta > :ahora
    """
    )
    suspend fun registrarIntentoEnvio(
        operationId: String,
        tokenBloqueo: String,
        ahora: Long
    ): Int


    // Recuperar operaciones cuyo bloqueo ha vencido.
    // No retornan a PENDIENTE: requieren reconciliación.

    @Query(
        """
    UPDATE operaciones_pendientes
    SET estadoEnvio = 'RESULTADO_INCIERTO',
        bloqueadaHasta = NULL,
        tokenBloqueo = NULL
    WHERE estadoEnvio = 'EN_PROCESO'
      AND bloqueadaHasta IS NOT NULL
      AND bloqueadaHasta <= :ahora
    """
    )
    suspend fun recuperarOperacionesVencidas(
        ahora: Long
    ): Int

    // Consultar operaciones que requieren reconciliación.
    @Query(
        """
        SELECT * FROM operaciones_pendientes
        WHERE estadoEnvio = 'RESULTADO_INCIERTO'
        ORDER BY creadaEn ASC, operationId ASC
        """
    )
    suspend fun obtenerOperacionesInciertas():
            List<OperacionPendienteEntity>

    @Query(
        """
    UPDATE operaciones_pendientes
    SET estadoEnvio = 'RESULTADO_INCIERTO',
        bloqueadaHasta = NULL,
        tokenBloqueo = NULL,
        ultimoError = :motivo
    WHERE operationId = :operationId
      AND estadoEnvio = 'EN_PROCESO'
      AND tokenBloqueo = :tokenBloqueo
      AND bloqueadaHasta IS NOT NULL
      AND bloqueadaHasta > :ahora
    """
    )
    suspend fun marcarResultadoIncierto(
        operationId: String,
        tokenBloqueo: String,
        ahora: Long,
        motivo: String
    ): Int

}