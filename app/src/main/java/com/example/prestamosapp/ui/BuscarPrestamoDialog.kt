package com.example.prestamosapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.prestamosapp.data.model.Herramienta

/** Busca un préstamo por ID usando GET api/Prestamos/listar/{id}. */
@Composable
fun BuscarPrestamoDialog(
    estado: BusquedaState,
    herramientas: List<Herramienta>,
    onBuscar: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var idTexto by rememberSaveable { mutableStateOf("") }
    val id = idTexto.toLongOrNull()
    val buscando = estado is BusquedaState.Loading

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Search, contentDescription = null) },
        title = { Text("Buscar préstamo por ID") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = idTexto,
                    onValueChange = { idTexto = it.filter(Char::isDigit) },
                    label = { Text("ID del préstamo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(onSearch = { id?.let(onBuscar) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                when (estado) {
                    BusquedaState.Idle -> Unit
                    BusquedaState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    is BusquedaState.Found -> PrestamoCard(
                        p = estado.prestamo,
                        nombresPorId = nombresPorId(herramientas),
                        idMostrado = estado.id,
                    )
                    is BusquedaState.NotFound -> Text(
                        estado.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = id != null && !buscando,
                onClick = { id?.let(onBuscar) },
            ) { Text(if (buscando) "Buscando…" else "Buscar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
    )
}
