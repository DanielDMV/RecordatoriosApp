package cl.actividad.recordatorios

import cl.actividad.recordatorios.data.InMemoryRecordatoriosRepository
import cl.actividad.recordatorios.data.RecordatoriosRepository

object AppContainer {

    val recordatoriosRepository: RecordatoriosRepository =
        InMemoryRecordatoriosRepository()
}
