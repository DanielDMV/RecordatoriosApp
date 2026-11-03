package cl.actividad.recordatorios.data

import cl.actividad.recordatorios.domain.ValidadorRecordatorios
import cl.actividad.recordatorios.model.Categoria
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatosSimuladosTest {

    @Test
    fun contieneSeisRecordatoriosConIdentificadoresUnicos() {
        val recordatorios = DatosSimulados.recordatorios

        assertEquals(6, recordatorios.size)
        assertEquals(6, recordatorios.map { it.id }.toSet().size)
    }

    @Test
    fun todosLosRecordatoriosCumplenLasValidaciones() {
        for (recordatorio in DatosSimulados.recordatorios) {
            val errores = ValidadorRecordatorios.validar(recordatorio)

            assertTrue(
                "Recordatorio ${recordatorio.id}: $errores",
                errores.isEmpty()
            )
        }
    }

    @Test
    fun contieneLasTresCategoriasEsperadasSinRepeticiones() {
        val esperadas = setOf(
            Categoria(1, "Estudios"),
            Categoria(2, "Personal"),
            Categoria(3, "Trabajo")
        )

        assertEquals(esperadas, DatosSimulados.categorias)
    }

    @Test
    fun cadaCategoriaTieneLaEtiquetaEsperada() {
        val esperadas = mapOf(
            Categoria(1, "Estudios") to "Actividades académicas",
            Categoria(2, "Personal") to "Tareas personales",
            Categoria(3, "Trabajo") to "Responsabilidades laborales"
        )

        assertEquals(esperadas, DatosSimulados.etiquetasPorCategoria)
        assertEquals(
            DatosSimulados.categorias,
            DatosSimulados.etiquetasPorCategoria.keys
        )
    }
}