package com.willard.avisoscampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.willard.avisoscampus.model.Aviso
import com.willard.avisoscampus.model.Origen

@Composable
fun HistorialAvisos(avisos: List<Aviso>) {
    Tarjeta("Historial") {
        if (avisos.isEmpty()) {
            Text(
                text = "Sin avisos todavía",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            avisos.take(20).forEach { aviso ->
                FilaAviso(aviso)
            }
        }
    }
}

@Composable
private fun FilaAviso(aviso: Aviso) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EtiquetaOrigen(aviso.origen)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = aviso.titulo, fontWeight = FontWeight.Bold)
                Text(
                    text = aviso.texto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = aviso.hora, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun EtiquetaOrigen(origen: Origen) {
    val color = when (origen) {
        Origen.BOTON -> Color(0xFF1565C0)
        Origen.RECORDATORIO -> Color(0xFFB26A00)
        Origen.MODO_AVION -> Color(0xFF7B1FA2)
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = origen.texto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}