package com.example.prestamosapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.prestamosapp.data.model.Herramienta
import com.example.prestamosapp.data.model.Prestamo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Mapa id -> nombre de las herramientas que sí traen id. */
fun nombresPorId(herramientas: List<Herramienta>): Map<Long, String> =
    herramientas.mapNotNull { h -> h.id?.let { it to h.nombre } }.toMap()

// ───────────────────────────── Herramientas ─────────────────────────────

@Composable
fun HerramientasScreen(
    herramientas: List<Herramienta>,
    loading: Boolean,
    onRefresh: () -> Unit,
) {
    if (herramientas.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Build,
            title = if (loading) "Cargando herramientas…" else "Sin herramientas",
            subtitle = if (loading) {
                "Si Render estaba dormido puede tardar cerca de 1 minuto."
            } else {
                "Toca “Nueva herramienta” para registrar la primera."
            },
            onRetry = if (loading) null else onRefresh,
        )
        return
    }

    val sinIds = herramientas.all { it.id == null }
    val unidades = herramientas.sumOf { it.cantidad }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Herramientas", herramientas.size.toString(), Modifier.weight(1f))
                StatCard("Unidades", unidades.toString(), Modifier.weight(1f))
            }
        }
        if (sinIds) {
            item { AvisoSinIds() }
        }
        items(herramientas) { h -> HerramientaCard(h) }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Explica por qué no se ve el ID: es el backend el que no lo está enviando. */
@Composable
private fun AvisoSinIds() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Info, contentDescription = null)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("El servidor no envía el ID", style = MaterialTheme.typography.titleSmall)
                Text(
                    "GET api/Herramienta/listar no incluye el campo id. " +
                            "Agrégalo en el backend y aparecerá aquí sin cambiar la app.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun HerramientaCard(h: Herramienta) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Avatar(MaterialTheme.colorScheme.primaryContainer) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(h.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(
                        h.categoria.ifBlank { "Sin categoría" },
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    h.id?.let {
                        Pill(
                            "ID $it",
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            StockBadge(h.cantidad)
        }
    }
}

@Composable
private fun StockBadge(cantidad: Int) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when {
        cantidad <= 0 -> scheme.errorContainer to scheme.onErrorContainer
        cantidad <= 3 -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        else -> scheme.primaryContainer to scheme.onPrimaryContainer
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = container, contentColor = content, shape = RoundedCornerShape(12.dp)) {
            Text(
                cantidad.toString(),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            if (cantidad <= 0) "agotado" else "disp.",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

// ───────────────────────────── Préstamos ─────────────────────────────

@Composable
fun PrestamosScreen(
    prestamos: List<Prestamo>,
    herramientas: List<Herramienta>,
    loading: Boolean,
    onRefresh: () -> Unit,
) {
    if (prestamos.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Info,
            title = if (loading) "Cargando préstamos…" else "Sin préstamos",
            subtitle = if (loading) "Un momento por favor." else "Toca “Nuevo préstamo” para registrar el primero.",
            onRetry = if (loading) null else onRefresh,
        )
        return
    }
    val nombres = nombresPorId(herramientas)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            StatCard("Préstamos registrados", prestamos.size.toString(), Modifier.fillMaxWidth())
        }
        items(prestamos) { p -> PrestamoCard(p, nombres) }
    }
}

@Composable
fun PrestamoCard(
    p: Prestamo,
    nombresPorId: Map<Long, String>,
    idMostrado: Long? = p.id,
) {
    val herramienta = p.herramienta?.let { nombresPorId[it] ?: "Herramienta #$it" } ?: "—"
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Avatar(MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    p.responsable.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    p.responsable.ifBlank { "Sin responsable" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "$herramienta × ${p.cantidad}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    formatearFecha(p.fechaPrestamo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            idMostrado?.let {
                Pill(
                    "ID $it",
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatearFecha(iso: String): String = runCatching {
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}.getOrDefault(iso.ifBlank { "Sin fecha" })

// ───────────────────────────── Piezas comunes ─────────────────────────────

@Composable
private fun Avatar(container: Color, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(46.dp).clip(CircleShape).background(container),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun Pill(text: String, container: Color, content: Color) {
    Surface(color = container, contentColor = content, shape = RoundedCornerShape(50)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Avatar(MaterialTheme.colorScheme.primaryContainer) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.size(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.size(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(Modifier.size(16.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}