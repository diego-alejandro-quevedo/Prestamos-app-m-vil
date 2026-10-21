package com.example.prestamosapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.prestamosapp.data.model.Herramienta
import com.example.prestamosapp.data.model.HerramientaRequest
import com.example.prestamosapp.data.model.PrestamoRequest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun HerramientaFormDialog(
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (HerramientaRequest) -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }

    val cantidadInt = cantidad.toIntOrNull()
    // Mismas reglas que el backend: nombre/categoría obligatorios (máx. 100), cantidad >= 0
    val valido = nombre.isNotBlank() && nombre.length <= 100 &&
        categoria.isNotBlank() && categoria.length <= 100 &&
        cantidadInt != null && cantidadInt >= 0

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Nueva herramienta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it.filter(Char::isDigit) },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido && !saving,
                onClick = { onSave(HerramientaRequest(nombre.trim(), categoria.trim(), cantidadInt!!)) },
            ) { Text(if (saving) "Guardando…" else "Guardar") }
        },
        dismissButton = {
            TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestamoFormDialog(
    herramientas: List<Herramienta>,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (PrestamoRequest) -> Unit,
) {
    var responsable by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDate.now().toString()) } // yyyy-MM-dd
    var cantidad by remember { mutableStateOf("1") }
    var herramientaId by remember { mutableStateOf<Long?>(null) }
    var idManual by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var menuAbierto by remember { mutableStateOf(false) }

    // Si la API ya expone el id de cada herramienta se usa un selector; si no, id manual.
    val conIds = herramientas.filter { it.id != null }
    val usarSelector = conIds.isNotEmpty() && conIds.size == herramientas.size

    val cantidadInt = cantidad.toIntOrNull()
    val herramientaFinal: Long? = if (usarSelector) herramientaId else idManual.toLongOrNull()
    val seleccionada = conIds.firstOrNull { it.id == herramientaFinal }
    val excedeStock = seleccionada != null && cantidadInt != null && cantidadInt > seleccionada.cantidad

    val valido = responsable.isNotBlank() && cantidadInt != null && cantidadInt >= 1 && herramientaFinal != null

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Nuevo préstamo") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = responsable,
                    onValueChange = { responsable = it },
                    label = { Text("Responsable") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Fecha del préstamo: $fecha") }

                if (usarSelector) {
                    ExposedDropdownMenuBox(
                        expanded = menuAbierto,
                        onExpandedChange = { menuAbierto = it },
                    ) {
                        OutlinedTextField(
                            value = seleccionada?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Herramienta") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false },
                        ) {
                            conIds.forEach { h ->
                                DropdownMenuItem(
                                    text = { Text("${h.nombre} (disp. ${h.cantidad})") },
                                    onClick = {
                                        herramientaId = h.id
                                        menuAbierto = false
                                    },
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = idManual,
                        onValueChange = { idManual = it.filter(Char::isDigit) },
                        label = { Text("ID de la herramienta") },
                        supportingText = {
                            Text("La API aún no devuelve el id de las herramientas; escríbelo a mano.")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it.filter(Char::isDigit) },
                    label = { Text("Cantidad") },
                    isError = excedeStock,
                    supportingText = if (excedeStock) {
                        { Text("Supera el stock disponible (${seleccionada?.cantidad})") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido && !saving,
                onClick = {
                    onSave(PrestamoRequest(responsable.trim(), fecha, cantidadInt!!, herramientaFinal!!))
                },
            ) { Text(if (saving) "Guardando…" else "Guardar") }
        },
        dismissButton = {
            TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancelar") }
        },
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = runCatching {
                LocalDate.parse(fecha).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }.getOrNull(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        fecha = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
