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
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.actividad.recordatorios.ui.state.ListadoUiState
import cl.actividad.recordatorios.ui.viewmodel.ListadoViewModel
import cl.actividad.recordatorios.ui.viewmodel.ListadoViewModelFactory
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.actividad.recordatorios.ui.navigation.Rutas
import cl.actividad.recordatorios.ui.screen.PantallaCreacion
import cl.actividad.recordatorios.ui.viewmodel.CreacionViewModel
import cl.actividad.recordatorios.ui.viewmodel.CreacionViewModelFactory
import androidx.compose.foundation.clickable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import cl.actividad.recordatorios.ui.screen.PantallaDetalle
import cl.actividad.recordatorios.ui.viewmodel.DetalleViewModel
import cl.actividad.recordatorios.ui.viewmodel.DetalleViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RecordatoriosAppTheme {

                val navController = rememberNavController()
                val repository = AppContainer.recordatoriosRepository

                val listadoViewModel: ListadoViewModel = viewModel(
                    factory = ListadoViewModelFactory(repository)
                )

                val uiState by listadoViewModel.uiState
                    .collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = Rutas.LISTADO,
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable(Rutas.LISTADO) {

                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                ControlesPrueba(
                                    onCargar = {
                                        listadoViewModel.cargarRecordatorios()
                                    },
                                    onVacio = {
                                        listadoViewModel.simularEstadoVacio()
                                    },
                                    onError = {
                                        listadoViewModel.simularError()
                                    }
                                )

                                ContenidoListado(
                                    uiState = uiState,
                                    onReintentar = {
                                        listadoViewModel.cargarRecordatorios()
                                    },
                                    onCrear = {
                                        navController.navigate(Rutas.CREACION)
                                    },
                                    onDetalle = { recordatorioId ->
                                        navController.navigate(
                                            Rutas.detalle(recordatorioId)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        composable(Rutas.CREACION) { entry ->

                            val creacionViewModel: CreacionViewModel =
                                viewModel(
                                    viewModelStoreOwner = entry,
                                    factory = CreacionViewModelFactory(
                                        repository = repository,
                                        categorias = DatosSimulados
                                            .categorias.toList()
                                    )
                                )

                            val creacionState by creacionViewModel.uiState
                                .collectAsStateWithLifecycle()

                            LaunchedEffect(
                                creacionState.guardadoExitoso
                            ) {
                                if (creacionState.guardadoExitoso) {
                                    listadoViewModel.cargarRecordatorios()
                                    navController.popBackStack()
                                }
                            }

                            PantallaCreacion(
                                uiState = creacionState,
                                categorias = creacionViewModel
                                    .categoriasDisponibles,
                                onTituloChange = {
                                    creacionViewModel.actualizarTitulo(it)
                                },
                                onDescripcionChange = {
                                    creacionViewModel.actualizarDescripcion(it)
                                },
                                onCategoriaChange = {
                                    creacionViewModel.seleccionarCategoria(it)
                                },
                                onPrioridadChange = {
                                    creacionViewModel.seleccionarPrioridad(it)
                                },
                                onGuardar = {
                                    creacionViewModel.guardarRecordatorio()
                                },
                                onCancelar = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = Rutas.DETALLE,
                            arguments = listOf(
                                navArgument("recordatorioId") {
                                    type = NavType.LongType
                                }
                            )
                        ) { entry ->

                            val recordatorioId =
                                entry.arguments?.getLong("recordatorioId") ?: 0L

                            val detalleViewModel: DetalleViewModel = viewModel(
                                viewModelStoreOwner = entry,
                                factory = DetalleViewModelFactory(
                                    repository = repository,
                                    recordatorioId = recordatorioId
                                )
                            )

                            val detalleState by detalleViewModel.uiState
                                .collectAsStateWithLifecycle()

                            PantallaDetalle(
                                uiState = detalleState,
                                onVolver = {
                                    navController.popBackStack()
                                },
                                onReintentar = {
                                    detalleViewModel.cargarDetalle()
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}



@Composable
fun ControlesPrueba(
    onCargar: () -> Unit,
    onVacio: () -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Pruebas de estados",
            style = MaterialTheme.typography.labelMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedButton(onClick = onCargar) {
                Text("Cargar")
            }

            OutlinedButton(onClick = onVacio) {
                Text("Vacío")
            }

            OutlinedButton(onClick = onError) {
                Text("Error")
            }
        }
    }
}



@Composable
fun ContenidoListado(
    uiState: ListadoUiState,
    onReintentar: () -> Unit,
    onCrear: () -> Unit,
    onDetalle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {

        ListadoUiState.Cargando -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()

                    Text(
                        text = "Cargando recordatorios...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }


        ListadoUiState.Vacio -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Todavía no existen recordatorios.")

                    Text("Puedes crear uno desde el formulario.")

                    Button(onClick = onCrear) {
                        Text("Crear recordatorio")
                    }
                }
            }
        }

        is ListadoUiState.ConDatos -> {
            PantallaRecordatorios(
                recordatorios = uiState.recordatorios,
                onCrear = onCrear,
                onDetalle = onDetalle,
                modifier = modifier
            )
        }

        is ListadoUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(uiState.mensaje)

                    Button(onClick = onReintentar) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaRecordatorios(
    recordatorios: List<Recordatorio>,
    modifier: Modifier = Modifier,
    onCrear: () -> Unit = {},
    onDetalle: (Long) -> Unit = {}
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

                Button(
                    onClick = onCrear,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear recordatorio")
                }

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

            TarjetaRecordatorio(
                recordatorio = recordatorio,
                onClick = {
                    onDetalle(recordatorio.id)
                }
            )
        }
    }
}

@Composable
fun TarjetaRecordatorio(
    recordatorio: Recordatorio,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
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