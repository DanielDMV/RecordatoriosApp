package cl.actividad.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.actividad.recordatorios.data.RecordatoriosRepository

class DetalleViewModelFactory(
    private val repository: RecordatoriosRepository,
    private val recordatorioId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        require(
            modelClass.isAssignableFrom(DetalleViewModel::class.java)
        ) {
            "ViewModel no reconocido: ${modelClass.name}"
        }

        return DetalleViewModel(
            repository = repository,
            recordatorioId = recordatorioId
        ) as T
    }
}
