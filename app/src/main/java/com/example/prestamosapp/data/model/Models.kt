package com.example.prestamosapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Todos los campos tienen valor por defecto para que Kotlin genere un constructor
 * sin argumentos y Gson nunca deje un campo no-nulo en null si el JSON no lo trae.
 *
 * El id acepta varios nombres posibles en el JSON (id, idHerramienta, ...), así que si
 * el backend lo envía con otro nombre la app lo recoge igual.
 */

// GET api/Herramienta/listar
data class Herramienta(
    @SerializedName(
        value = "id",
        alternate = ["idHerramienta", "id_herramienta", "herramientaId", "Id", "ID", "_id"],
    )
    val id: Long? = null,
    val nombre: String = "",
    val categoria: String = "",
    val cantidad: Int = 0,
)

// POST api/Herramienta/crearHerranienta
data class HerramientaRequest(
    val nombre: String,
    val categoria: String,
    val cantidad: Int,
)

// GET api/Prestamos/listar, GET api/Prestamos/listar/{id} y POST api/Prestamos/crearPrestamo (respuesta)
// "herramienta" es el id de la herramienta; fechaPrestamo viene como "yyyy-MM-dd".
data class Prestamo(
    @SerializedName(
        value = "id",
        alternate = ["idPrestamo", "id_prestamo", "prestamoId", "Id", "ID", "_id"],
    )
    val id: Long? = null,
    val responsable: String = "",
    val fechaPrestamo: String = "",
    val cantidad: Int = 0,
    val herramienta: Long? = null,
)

// POST api/Prestamos/crearPrestamo (cuerpo)
data class PrestamoRequest(
    val responsable: String,
    val fechaPrestamo: String,
    val cantidad: Int,
    val herramienta: Long,
)