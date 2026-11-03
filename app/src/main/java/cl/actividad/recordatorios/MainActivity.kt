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
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Mis recordatorios",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${recordatorios.size} recordatorios",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(
            items = recordatorios,
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