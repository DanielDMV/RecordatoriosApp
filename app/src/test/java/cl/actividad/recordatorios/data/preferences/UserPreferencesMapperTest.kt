
package cl.actividad.recordatorios.data.preferences

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.actividad.recordatorios.model.OrdenRecordatorios
import cl.actividad.recordatorios.model.TemaApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesMapperTest {

    @Test
    fun sinPreferencias_aplicaValoresPredeterminados() {
        val resultado = emptyPreferences().toUserPreferences()

        assertEquals(TemaApp.SISTEMA, resultado.tema)
        assertTrue(resultado.mostrarCompletados)
        assertEquals(
            OrdenRecordatorios.MAS_RECIENTES,
            resultado.orden
        )
    }

    @Test
    fun valoresDesconocidos_aplicaValoresPredeterminados() {
        val preferencias = preferencesOf(
            stringPreferencesKey("tema") to "DESCONOCIDO",
            stringPreferencesKey("orden") to "INVALIDO"
        )

        val resultado = preferencias.toUserPreferences()

        assertEquals(TemaApp.SISTEMA, resultado.tema)
        assertTrue(resultado.mostrarCompletados)
        assertEquals(
            OrdenRecordatorios.MAS_RECIENTES,
            resultado.orden
        )
    }
}
