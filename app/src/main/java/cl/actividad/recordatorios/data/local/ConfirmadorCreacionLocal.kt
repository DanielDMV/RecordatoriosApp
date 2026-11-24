package cl.actividad.recordatorios.data.local

import androidx.room.withTransaction

class ConfirmadorCreacionLocal(
    private val database: RecordatoriosDatabase
) {

    suspend fun confirmar(
        operationId: String,
        tokenBloqueo: String,
        remoteId: String,
        estadoFinal: String,
        ahora: Long,
        fechaVersionEsperada: Long? = null
    ): Boolean {

        require(operationId.isNotBlank())
        require(tokenBloqueo.isNotBlank())
        require(remoteId.isNotBlank())
        require(estadoFinal.isNotBlank())

        return database.withTransaction {

            val dao = database.operacionPendienteDao()

            // Solo actualiza un recordatorio cuando existe
            // una operación CREAR con propietario vigente.
            val actualizados = dao.actualizarRecordatorioConfirmado(
                operationId = operationId,
                tokenBloqueo = tokenBloqueo,
                remoteId = remoteId,
                estadoFinal = estadoFinal,
                ahora = ahora,
                fechaVersionEsperada = fechaVersionEsperada
            )

            if (actualizados != 1) {
                return@withTransaction false
            }

            // Retirar la operación dentro de la misma transacción.
            val eliminados = dao.eliminarCreacionConfirmada(
                operationId = operationId,
                tokenBloqueo = tokenBloqueo,
                ahora = ahora
            )

            // Si falla esta eliminación, se revierte también
            // la actualización del recordatorio.
            check(eliminados == 1) {
                "No se pudo retirar la operación confirmada."
            }

            true
        }
    }
}
