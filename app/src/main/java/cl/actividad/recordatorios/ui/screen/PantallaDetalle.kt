
package cl.actividad.recordatorios.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.actividad.recordatorios.ui.state.DetalleUiState

@Composable
fun PantallaDetalle(
    uiState: DetalleUiState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Detalle del recordatorio",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedButton(onClick = onVolver) {
            Text("Volver al listado")
        }

        when (uiState) {

            DetalleUiState.Cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            DetalleUiState.NoEncontrado -> {
                Text(
                    text = "El recordatorio solicitado no existe.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is DetalleUiState.Error -> {
                Text(
                    text = uiState.mensaje,
                    color = MaterialTheme.colorScheme.error
                )

                Button(onClick = onReintentar) {
                    Text("Reintentar")
                }
            }

            is DetalleUiState.ConDatos -> {

                val recordatorio = uiState.recordatorio

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = recordatorio.titulo,
                            style =
                                MaterialTheme.typography.titleLarge
                        )

                        recordatorio.descripcion?.let {
                            Text(it)
                        }

                        Text(
                            "Categoría: ${recordatorio.categoria.nombre}"
                        )

                        Text(
                            "Prioridad: ${recordatorio.prioridad.nombre}"
                        )

                        Text(
                            "Estado: ${recordatorio.estado.nombre}"
                        )

                        recordatorio.fechaAviso?.let {
                            Text("Fecha de aviso: $it")
                        }

                        recordatorio.ubicacion?.let {
                            Text("Ubicación: $it")
                        }

                        Text(
                            text = "Identificador: ${recordatorio.id}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
