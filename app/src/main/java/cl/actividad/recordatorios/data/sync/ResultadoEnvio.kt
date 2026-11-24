package cl.actividad.recordatorios.data.sync

sealed interface ResultadoEnvio {

    data class Confirmado(
        val remoteId: String?
    ) : ResultadoEnvio

    data class Reintentable(
        val motivo: String
    ) : ResultadoEnvio

    data class Incierto(
        val motivo: String
    ) : ResultadoEnvio

    data class Permanente(
        val motivo: String
    ) : ResultadoEnvio
}
