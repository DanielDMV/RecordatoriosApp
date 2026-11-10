package cl.actividad.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.actividad.recordatorios.data.RecordatoriosRepository
import cl.actividad.recordatorios.model.Categoria

class CreacionViewModelFactory(
    private val repository: RecordatoriosRepository,
    private val categorias: List<Categoria>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        require(
            modelClass.isAssignableFrom(CreacionViewModel::class.java)
        ) {
            "ViewModel no reconocido: ${modelClass.name}"
        }

        return CreacionViewModel(
            repository = repository,
            categoriasDisponibles = categorias
        ) as T
    }
}
