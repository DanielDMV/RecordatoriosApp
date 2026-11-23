package cl.actividad.recordatorios.data.preferences

import androidx.datastore.preferences.core.Preferences
import cl.actividad.recordatorios.model.OrdenRecordatorios
import cl.actividad.recordatorios.model.TemaApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class UserPreferencesFlowTest {

    @Test
    fun errorDeLectura_recuperaValoresPredeterminados() = runBlocking {

        val origen: Flow<Preferences> = flow {
            throw IOException("Error de lectura simulado")
        }

        val resultado = origen
            .toUserPreferencesFlow()
            .first()

        assertEquals(TemaApp.SISTEMA, resultado.tema)
        assertTrue(resultado.mostrarCompletados)
        assertEquals(
            OrdenRecordatorios.MAS_RECIENTES,
            resultado.orden
        )
    }
}
