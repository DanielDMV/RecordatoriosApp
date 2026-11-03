package cl.actividad.recordatorios.domain

import cl.actividad.recordatorios.data.DatosSimulados
import cl.actividad.recordatorios.model.EstadoRecordatorio
import cl.actividad.recordatorios.model.Prioridad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltroRecordatoriosTest {

    private val recordatorios = DatosSimulados.recordatorios

    @Test
    fun filtraPorPrioridadConservandoElOrden() {
        val casos = listOf(
            Prioridad.Alta to listOf(1L, 2L),
            Prioridad.Media to listOf(3L, 5L),
            Prioridad.Baja to listOf(4L, 6L)
        )

        for ((prioridad, idsEsperados) in casos) {
            val resultado = FiltroRecordatorios.porPrioridad(
                recordatorios, prioridad
            )

            assertEquals(idsEsperados, resultado.map { it.id })
        }
    }

    @Test
    fun filtraPorEstadoConservandoElOrden() {
        val casos = listOf(
            EstadoRecordatorio.Pendiente to listOf(1L, 3L),
            EstadoRecordatorio.EnProgreso to listOf(2L, 5L),
            EstadoRecordatorio.Completado to listOf(4L, 6L)
        )

        for ((estado, idsEsperados) in casos) {
            val resultado = FiltroRecordatorios.porEstado(
                recordatorios, estado
            )

            assertEquals(idsEsperados, resultado.map { it.id })
        }
    }

    @Test
    fun aceptaUnaListaVacia() {
        assertTrue(
            FiltroRecordatorios.porPrioridad(
                emptyList(), Prioridad.Alta
            ).isEmpty()
        )

        assertTrue(
            FiltroRecordatorios.porEstado(
                emptyList(), EstadoRecordatorio.Pendiente
            ).isEmpty()
        )
    }

    @Test
    fun devuelveListaVaciaCuandoNoHayCoincidencias() {
        val entrada = listOf(
            recordatorios.first().copy(
                prioridad = Prioridad.Baja,
                estado = EstadoRecordatorio.Completado
            )
        )

        assertTrue(
            FiltroRecordatorios.porPrioridad(
                entrada, Prioridad.Alta
            ).isEmpty()
        )

        assertTrue(
            FiltroRecordatorios.porEstado(
                entrada, EstadoRecordatorio.Pendiente
            ).isEmpty()
        )
    }

    @Test
    fun noModificaLaListaOriginal() {
        val entrada = recordatorios.toMutableList()
        val copiaOriginal = entrada.toList()

        FiltroRecordatorios.porPrioridad(entrada, Prioridad.Alta)
        assertEquals(copiaOriginal, entrada)

        FiltroRecordatorios.porEstado(
            entrada, EstadoRecordatorio.Pendiente
        )
        assertEquals(copiaOriginal, entrada)
    }
}