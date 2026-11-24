package cl.actividad.recordatorios.data.sync

object PoliticaReintentos {

    private const val ESPERA_BASE_MS = 5_000L
    private const val ESPERA_MAXIMA_MS = 300_000L

    fun calcularProximoIntento(
        ahora: Long,
        intentos: Int
    ): Long {

        require(ahora >= 0L)
        require(intentos >= 0)

        val exponente = intentos.coerceIn(0, 6)

        val factor = 1L shl exponente

        val espera = (ESPERA_BASE_MS * factor)
            .coerceAtMost(ESPERA_MAXIMA_MS)

        return if (ahora > Long.MAX_VALUE - espera) {
            Long.MAX_VALUE
        } else {
            ahora + espera
        }
    }
}