package cl.actividad.recordatorios.data.local

import cl.actividad.recordatorios.model.Categoria
import cl.actividad.recordatorios.model.EstadoRecordatorio
import cl.actividad.recordatorios.model.Prioridad
import cl.actividad.recordatorios.model.Recordatorio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class RecordatorioMapperTest {

    @Test
    fun convertirDominioAEntidad_conservaTodosLosCampos() {
        val recordatorio = Recordatorio(
            id = 10L,
            titulo = "Preparar evaluación",
            descripcion = "Repasar módulo 3",
            categoria = Categoria(1, "Estudios"),
            prioridad = Prioridad.Alta,
            estado = EstadoRecordatorio.Pendiente,
            fechaAviso = "2026-11-30",
            ubicacion = "Casa",
            fechaCreacion = 1795392000000L
        )

        val entidad = recordatorio.toEntity()

        assertEquals(recordatorio.id, entidad.id)
        assertEquals(recordatorio.titulo, entidad.titulo)
        assertEquals(recordatorio.descripcion, entidad.descripcion)
        assertEquals(recordatorio.categoria.id, entidad.categoriaId)
        assertEquals(recordatorio.categoria.nombre, entidad.categoriaNombre)
        assertEquals(recordatorio.prioridad.nombre, entidad.prioridad)
        assertEquals(recordatorio.estado.nombre, entidad.estado)
        assertEquals(recordatorio.fechaAviso, entidad.fechaAviso)
        assertEquals(recordatorio.ubicacion, entidad.ubicacion)
        assertEquals(recordatorio.fechaCreacion, entidad.fechaCreacion)
    }

    @Test
    fun convertirEntidadADominio_conservaValoresOpcionalesNulos() {
        val entidad = RecordatorioEntity(
            id = 20L,
            titulo = "Comprar materiales",
            descripcion = null,
            categoriaId = 2,
            categoriaNombre = "Personal",
            prioridad = Prioridad.Media.nombre,
            estado = EstadoRecordatorio.EnProgreso.nombre,
            fechaAviso = null,
            ubicacion = null,
            fechaCreacion = 1795392000000L
        )

        val resultado = entidad.toDomain()

        assertEquals(entidad.id, resultado.id)
        assertEquals(entidad.titulo, resultado.titulo)
        assertEquals(Prioridad.Media, resultado.prioridad)
        assertEquals(
            EstadoRecordatorio.EnProgreso,
            resultado.estado
        )
        assertNull(resultado.descripcion)
        assertNull(resultado.fechaAviso)
        assertNull(resultado.ubicacion)
        assertEquals(entidad.fechaCreacion, resultado.fechaCreacion)
    }

    @Test
    fun conversionDeIdaYVuelta_conservaElRecordatorio() {
        val original = Recordatorio(
            id = 30L,
            titulo = "Revisar informe",
            categoria = Categoria(3, "Trabajo"),
            prioridad = Prioridad.Baja,
            estado = EstadoRecordatorio.Completado,
            fechaCreacion = 1795392000000L
        )

        val recuperado = original.toEntity().toDomain()

        assertEquals(original, recuperado)
    }

    @Test
    fun prioridadDesconocida_generaErrorControlado() {
        val entidad = RecordatorioEntity(
            id = 40L,
            titulo = "Prueba",
            descripcion = null,
            categoriaId = 1,
            categoriaNombre = "Estudios",
            prioridad = "DESCONOCIDA",
            estado = EstadoRecordatorio.Pendiente.nombre,
            fechaAviso = null,
            ubicacion = null,
            fechaCreacion = 1795392000000L
        )

        assertThrows(IllegalStateException::class.java) {
            entidad.toDomain()
        }
    }
}
