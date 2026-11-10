package cl.actividad.recordatorios.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.actividad.recordatorios.model.Categoria
import cl.actividad.recordatorios.model.Prioridad
import cl.actividad.recordatorios.ui.state.CreacionUiState

@Composable
fun PantallaCreacion(
    uiState: CreacionUiState,
    categorias: List<Categoria>,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCategoriaChange: (Categoria) -> Unit,
    onPrioridadChange: (Prioridad) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Nuevo recordatorio",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = uiState.titulo,
            onValueChange = onTituloChange,
            label = { Text("Título") },
            singleLine = true,
            enabled = !uiState.enviando,
            isError = uiState.tituloModificado &&
                    !uiState.tituloValido,
            supportingText = {
                if (uiState.tituloModificado &&
                    !uiState.tituloValido
                ) {
                    Text("El título debe contener al menos 4 caracteres.")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.descripcion,
            onValueChange = onDescripcionChange,
            label = { Text("Descripción (opcional)") },
            minLines = 3,
            maxLines = 5,
            enabled = !uiState.enviando,
            isError = !uiState.descripcionValida,
            supportingText = {
                Column {
                    Text(
                        text = "${uiState.descripcion.length}/200 caracteres"
                    )

                    if (uiState.descripcion.length == 200) {
                        Text(
                            text = "Has alcanzado el límite permitido.",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Categoría",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorias.forEach { categoria ->
                FilterChip(
                    selected = uiState.categoria == categoria,
                    onClick = {
                        onCategoriaChange(categoria)
                    },
                    enabled = !uiState.enviando,
                    label = {
                        Text(categoria.nombre)
                    }
                )
            }
        }

        if (uiState.intentoGuardar &&
            !uiState.categoriaValida
        ) {
            Text(
                text = "Selecciona una categoría.",
                color = MaterialTheme.colorScheme.error
            )
        }

        Text(
            text = "Prioridad",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Prioridad.Alta,
                Prioridad.Media,
                Prioridad.Baja
            ).forEach { prioridad ->

                FilterChip(
                    selected = uiState.prioridad == prioridad,
                    onClick = {
                        onPrioridadChange(prioridad)
                    },
                    enabled = !uiState.enviando,
                    label = {
                        Text(prioridad.nombre)
                    }
                )
            }
        }

        if (uiState.intentoGuardar &&
            uiState.prioridad == null
        ) {
            Text(
                text = "Selecciona una prioridad.",
                color = MaterialTheme.colorScheme.error
            )
        }

        uiState.mensajeError?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (uiState.guardadoExitoso) {
            Text(
                text = "Recordatorio guardado correctamente.",
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (uiState.enviando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedButton(
                onClick = onCancelar,
                enabled = !uiState.enviando,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = onGuardar,
                enabled = uiState.puedeGuardar &&
                        !uiState.guardadoExitoso,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (uiState.enviando) {
                        "Guardando..."
                    } else {
                        "Guardar"
                    }
                )
            }
        }
    }
}
