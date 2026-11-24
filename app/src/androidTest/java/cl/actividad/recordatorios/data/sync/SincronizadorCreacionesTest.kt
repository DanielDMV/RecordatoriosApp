package cl.actividad.recordatorios.data.sync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.actividad.recordatorios.data.local.OperacionPendienteEntity
import cl.actividad.recordatorios.data.local.RecordatoriosDatabase
import cl.actividad.recordatorios.data.remote.RemoteRecordatoriosDataSource
import cl.actividad.recordatorios.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SincronizadorCreacionesTest {

    @Test
    fun creacionRemotaActualizaRoomSinDuplicar() = runBlocking {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        val database = Room.inMemoryDatabaseBuilder(
            context,
            RecordatoriosDatabase::class.java
        ).build()

        val servidor = MockWebServer()
        servidor.start()

        try {
            val dao = database.operacionPendienteDao()

            database.openHelper.writableDatabase.execSQL(
                """
                INSERT INTO recordatorios (
                    id, titulo, descripcion, categoriaId,
                    categoriaNombre, prioridad, estado,
                    fechaAviso, ubicacion, fechaCreacion,
                    estadoSincronizacion, remoteId,
                    fechaModificacion
                ) VALUES (
                    10, 'Prueba sincronización',
                    'Creado sin conexión',
                    1, 'Estudios', 'Alta', 'Pendiente',
                    NULL, NULL, 1000,
                    'PENDIENTE', NULL, 1000
                )
                """.trimIndent()
            )

            dao.insertar(
                OperacionPendienteEntity(
                    operationId = "operacion-test-10",
                    recordatorioId = 10L,
                    tipo = "CREAR",
                    creadaEn = 1000L
                )
            )

            servidor.enqueue(
                MockResponse()
                    .setResponseCode(201)
                    .addHeader("Content-Type", "application/json")
                    .setBody(
                        """
                        {
                          "id":"remoto-10",
                          "titulo":"Prueba sincronización",
                          "descripcion":"Creado sin conexión",
                          "categoria":"Estudios",
                          "prioridad":"Alta",
                          "estado":"Pendiente"
                        }
                        """.trimIndent()
                    )
            )

            val api = RetrofitClient.crear(
                servidor.url("/").toString()
            )

            val sincronizador = SincronizadorCreaciones(
                database,
                RemoteRecordatoriosDataSource(api)
            )

            assertEquals(1, sincronizador.sincronizar())

            val registro = database.recordatorioDao()
                .obtenerPorId(10L)!!

            assertEquals("remoto-10", registro.remoteId)
            assertEquals(
                "SINCRONIZADO",
                registro.estadoSincronizacion
            )

            assertEquals(1, database.recordatorioDao().contar())
            assertEquals(0, dao.contarPendientes())

            val solicitud = servidor.takeRequest()

            assertEquals("POST", solicitud.method)
            assertEquals(
                "operacion-test-10",
                solicitud.getHeader("Idempotency-Key")
            )

            // Una segunda ejecución no genera otro POST.
            assertEquals(0, sincronizador.sincronizar())
            assertEquals(1, servidor.requestCount)

        } finally {
            servidor.shutdown()
            database.close()
        }
    }
}