package cl.actividad.recordatorios.data

import cl.actividad.recordatorios.model.Categoria
import cl.actividad.recordatorios.model.EstadoRecordatorio
import cl.actividad.recordatorios.model.Prioridad
import cl.actividad.recordatorios.model.Recordatorio
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class InMemoryRecordatoriosRepositoryTest {

    private fun crearRecordatorio(id: Long) = Recordatorio(
        id = id,
        titulo = "Recordatorio de prueba",
        categoria = Categoria(1, "Estudios"),
        prioridad = Prioridad.Alta,
        estado = EstadoRecordatorio.Pendiente
    )

    @Test
    fun obtieneLosSeisRecordatoriosIniciales() = runBlocking {
        val repositorio = InMemoryRecordatoriosRepository()

        val resultado = repositorio.obtenerTodos()

        assertEquals(6, resultado.size)
    }

    @Test
    fun obtieneRecordatorioPorIdentificador() = runBlocking {
        val repositorio = InMemoryRecordatoriosRepository()

        val resultado = repositorio.obtenerPorId(1L)

        assertNotNull(resultado)
        assertEquals("Estudiar Kotlin", resultado?.titulo)
        assertNull(repositorio.obtenerPorId(999L))
    }

    @Test
    fun guardaNuevoRecordatorio() = runBlocking {
        val repositorio = InMemoryRecordatoriosRepository()
        val nuevo = crearRecordatorio(100L)

        repositorio.guardar(nuevo)

        assertEquals(7, repositorio.obtenerTodos().size)
        assertEquals(nuevo, repositorio.obtenerPorId(100L))
    }

    @Test
    fun rechazaIdentificadorDuplicado() = runBlocking {
        val repositorio = InMemoryRecordatoriosRepository()
        val duplicado = crearRecordatorio(1L)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                repositorio.guardar(duplicado)
            }
        }

        assertEquals(6, repositorio.obtenerTodos().size)
    }
}
