package cl.actividad.recordatorios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.actividad.recordatorios.data.DatosSimulados
import cl.actividad.recordatorios.model.Recordatorio
import cl.actividad.recordatorios.ui.theme.RecordatoriosAppTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import cl.actividad.recordatorios.domain.FiltroRecordatorios
import cl.actividad.recordatorios.model.Prioridad
import cl.actividad.recordatorios.model.EstadoRecordatorio

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RecordatoriosAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaRecordatorios(
                        recordatorios = DatosSimulados.recordatorios,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaRecordatorios(
    recordatorios: List<Recordatorio>,
    modifier: Modifier = Modifier
) {
    var indicePrioridad by rememberSaveable { mutableStateOf(0) }

    val prioridades = listOf<Prioridad?>(
        null,
        Prioridad.Alta,
        Prioridad.Media,
        Prioridad.Baja
    )

    var indiceEstado by rememberSaveable { mutableStateOf(0) }

    val estados = listOf<EstadoRecordatorio?>(
        null,
        EstadoRecordatorio.Pendiente,
        EstadoRecordatorio.EnProgreso,
        EstadoRecordatorio.Completado
    )

    val prioridadSeleccionada = prioridades[indicePrioridad]

    val filtradosPorPrioridad = prioridadSeleccionada?.let { prioridad ->
        FiltroRecordatorios.porPrioridad(recordatorios, prioridad)
    } ?: recordatorios

    val estadoSeleccionado = estados[indiceEstado]

    val visibles = estadoSeleccionado?.let { estado ->
        FiltroRecordatorios.porEstado(filtradosPorPrioridad, estado)
    } ?: filtradosPorPrioridad

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Mis recordatorios",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Prioridad",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    prioridades.forEachIndexed { indice, prioridad ->
                        FilterChip(
                            selected = indicePrioridad == indice,
                            onClick = { indicePrioridad = indice },
                            label = {
                                Text(prioridad?.nombre ?: "Todas")
                            }
                        )
                    }
                }

                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    estados.forEachIndexed { indice, estado ->
                        FilterChip(
                            selected = indiceEstado == indice,
                            onClick = { indiceEstado = indice },
                            label = {
                                Text(estado?.nombre ?: "Todos")
                            }
                        )
                    }
                }

                Text(
                    text = "${visibles.size} de ${recordatorios.size} recordatorios",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (visibles.isEmpty()) {
            item {
                Text("No hay recordatorios con estos filtros.")
            }
        }

        items(
            items = visibles,
            key = { it.id }
        ) { recordatorio ->
            TarjetaRecordatorio(recordatorio = recordatorio)
        }
    }
}

@Composable
fun TarjetaRecordatorio(
    recordatorio: Recordatorio,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = recordatorio.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            recordatorio.descripcion?.let { descripcion ->
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Categoría: ${recordatorio.categoria.nombre}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Prioridad: ${recordatorio.prioridad.nombre}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Estado: ${recordatorio.estado.nombre}",
                style = MaterialTheme.typography.bodyMedium
            )

            recordatorio.fechaAviso?.let { fecha ->
                Text(
                    text = "Aviso: $fecha",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            recordatorio.ubicacion?.let { ubicacion ->
                Text(
                    text = "Ubicación: $ubicacion",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaRecordatoriosPreview() {
    RecordatoriosAppTheme {
        PantallaRecordatorios(
            recordatorios = DatosSimulados.recordatorios
        )
    }
}