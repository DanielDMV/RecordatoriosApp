package cl.actividad.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.actividad.recordatorios.data.sync.SincronizadorCreaciones
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SincronizacionViewModel(
    private val sincronizador: SincronizadorCreaciones?
) : ViewModel() {

    private val _mensaje = MutableStateFlow(
        if (sincronizador == null) {
            "Servicio remoto no configurado."
        } else {
            "Sincronizacion disponible."
        }
    )

    val mensaje = _mensaje.asStateFlow()

    private val _procesando = MutableStateFlow(false)
    val procesando = _procesando.asStateFlow()

    fun sincronizar() {
        if (_procesando.value) return

        val servicio = sincronizador ?: run {
            _mensaje.value = "No existe un servidor remoto configurado."
            return
        }

        viewModelScope.launch {
            _procesando.value = true
            _mensaje.value = "Sincronizando recordatorios..."

            try {
                val confirmados = servicio.sincronizar()

                _mensaje.value =
                    "Creaciones confirmadas: $confirmados. " +
                            "Otras operaciones pueden seguir pendientes."

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value =
                    "No se pudo completar la sincronizacion. " +
                            "Los datos locales permanecen guardados."
            } finally {
                _procesando.value = false
            }
        }
    }
}

class SincronizacionViewModelFactory(
    private val sincronizador: SincronizadorCreaciones?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        require(
            modelClass.isAssignableFrom(
                SincronizacionViewModel::class.java
            )
        )

        return SincronizacionViewModel(sincronizador) as T
    }
}
