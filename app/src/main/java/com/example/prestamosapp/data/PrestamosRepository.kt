package com.example.prestamosapp.data

import com.example.prestamosapp.data.model.Herramienta
import com.example.prestamosapp.data.model.HerramientaRequest
import com.example.prestamosapp.data.model.Prestamo
import com.example.prestamosapp.data.model.PrestamoRequest
import com.example.prestamosapp.data.remote.PrestamosApi
import com.example.prestamosapp.data.remote.RetrofitClient
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

sealed interface ApiResult<out T> {
    data class Ok<T>(val data: T) : ApiResult<T>
    data class Fail(val message: String) : ApiResult<Nothing>
}

class PrestamosRepository(
    private val api: PrestamosApi = RetrofitClient.api,
) {
    suspend fun herramientas(): ApiResult<List<Herramienta>> = safeCall { api.listarHerramientas() }

    suspend fun crearHerramienta(request: HerramientaRequest): ApiResult<Herramienta> =
        safeCall { api.crearHerramienta(request) }

    suspend fun prestamos(): ApiResult<List<Prestamo>> = safeCall { api.listarPrestamos() }

    /** GET api/Prestamos/listar/{id} */
    suspend fun prestamoPorId(id: Long): ApiResult<Prestamo> = safeCall { api.obtenerPrestamo(id) }

    suspend fun crearPrestamo(request: PrestamoRequest): ApiResult<Prestamo> =
        safeCall { api.crearPrestamo(request) }

    /**
     * El backend responde errores como texto plano (p. ej. "No hay suficiente stock...")
     * o como JSON de validación; en ambos casos se muestra el cuerpo al usuario.
     */
    private suspend fun <T> safeCall(block: suspend () -> Response<T>): ApiResult<T> = try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) ApiResult.Ok(body) else ApiResult.Fail("El servidor devolvió una respuesta vacía")
        } else if (response.code() == 404) {
            ApiResult.Fail("No se encontró ningún registro con ese ID")
        } else {
            val detail = response.errorBody()?.string()?.trim().orEmpty()
            ApiResult.Fail(if (detail.isNotEmpty()) detail.take(300) else "Error ${response.code()}")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        ApiResult.Fail("Sin conexión o el servidor tardó en responder. Si Render estaba dormido, reintenta en unos segundos.")
    } catch (e: Exception) {
        ApiResult.Fail(e.message ?: "Error inesperado")
    }
}