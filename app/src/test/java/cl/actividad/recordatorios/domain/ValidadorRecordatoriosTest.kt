package cl.actividad.recordatorios.domain

import cl.actividad.recordatorios.model.Categoria
import cl.actividad.recordatorios.model.EstadoRecordatorio
import cl.actividad.recordatorios.model.Prioridad
import cl.actividad.recordatorios.model.Recordatorio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorRecordatoriosTest {

    private val valido = Recordatorio(
        id = 1L,
        titulo = "Estudiar Kotlin",
        categoria = Categoria(1, "Estudios"),
        prioridad = Prioridad.Alta,
        estado = EstadoRecordatorio.Pendiente
    )

    @Test
    fun aceptaRecordatorioValidoConOpcionalesNulos() {
        assertTrue(ValidadorRecordatorios.validar(valido).isEmpty())
    }

    @Test
    fun rechazaIdentificadorCeroYNegativo() {
        for (id in listOf(0L, -1L)) {
            assertEquals(
                listOf("El identificador del recordatorio debe ser positivo."),
                ValidadorRecordatorios.validar(valido.copy(id = id))
            )
        }
    }

    @Test
    fun rechazaTituloVacioOConEspacios() {
        for (titulo in listOf("", " \t\n")) {
            assertEquals(
                listOf("El título no puede estar vacío."),
                ValidadorRecordatorios.validar(valido.copy(titulo = titulo))
            )
        }
    }

    @Test
    fun rechazaIdentificadorDeCategoriaCeroYNegativo() {
        for (id in listOf(0, -1)) {
            assertEquals(
                listOf("El identificador de la categoría debe ser positivo."),
                ValidadorRecordatorios.validar(
                    valido.copy(categoria = Categoria(id, "Estudios"))
                )
            )
        }
    }

    @Test
    fun rechazaNombreDeCategoriaVacioOConEspacios() {
        for (nombre in listOf("", " \t\n")) {
            assertEquals(
                listOf("El nombre de la categoría no puede estar vacío."),
                ValidadorRecordatorios.validar(
                    valido.copy(categoria = Categoria(1, nombre))
                )
            )
        }
    }

    @Test
    fun devuelveTodosLosErroresJuntos() {
        val invalido = valido.copy(
            id = 0L,
            titulo = "",
            categoria = Categoria(0, "")
        )

        assertEquals(
            listOf(
                "El identificador del recordatorio debe ser positivo.",
                "El título no puede estar vacío.",
                "El identificador de la categoría debe ser positivo.",
                "El nombre de la categoría no puede estar vacío."
            ),
            ValidadorRecordatorios.validar(invalido)
        )
    }
}