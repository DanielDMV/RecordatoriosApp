
package cl.actividad.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.actividad.recordatorios.data.RecordatoriosRepository

class ListadoViewModelFactory(
    private val repository: RecordatoriosRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        require(
            modelClass.isAssignableFrom(ListadoViewModel::class.java)
        ) {
            "ViewModel no reconocido: ${modelClass.name}"
        }

        return ListadoViewModel(repository) as T
    }
}
