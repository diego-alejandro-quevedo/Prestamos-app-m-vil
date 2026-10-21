package com.example.prestamosapp.data.remote

import com.example.prestamosapp.data.model.Herramienta
import com.example.prestamosapp.data.model.HerramientaRequest
import com.example.prestamosapp.data.model.Prestamo
import com.example.prestamosapp.data.model.PrestamoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Rutas copiadas tal cual del backend (Spring distingue mayúsculas en la URL,
 * y "crearHerranienta" lleva ese typo en el controlador original).
 */
interface PrestamosApi {

    @GET("api/Herramienta/listar")
    suspend fun listarHerramientas(): Response<List<Herramienta>>

    @POST("api/Herramienta/crearHerranienta")
    suspend fun crearHerramienta(@Body request: HerramientaRequest): Response<Herramienta>

    @GET("api/Prestamos/listar")
    suspend fun listarPrestamos(): Response<List<Prestamo>>

    @GET("api/Prestamos/listar/{id}")
    suspend fun obtenerPrestamo(@Path("id") id: Long): Response<Prestamo>

    @POST("api/Prestamos/crearPrestamo")
    suspend fun crearPrestamo(@Body request: PrestamoRequest): Response<Prestamo>
}
