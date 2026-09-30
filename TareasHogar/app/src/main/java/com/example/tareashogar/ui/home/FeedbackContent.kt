package com.example.tareashogar.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val StarColor = Color(0xFFFFC107)

@Composable
fun FeedbackContent(vm: HomeViewModel, modifier: Modifier = Modifier) {
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var wouldUse by rememberSaveable { mutableStateOf<String?>(null) }
    var comment by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (sent) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("¡Gracias por tu opinión! 💚", style = MaterialTheme.typography.titleLarge)
                    Text("La vamos a usar para mejorar la próxima versión de la app.")
                }
            }
            OutlinedButton(onClick = { sent = false }) { Text("Enviar otra opinión") }
            return@Column
        }

        Text(
            "Estamos probando esta primera versión. Tu opinión nos ayuda a decidir qué mejorar.",
            style = MaterialTheme.typography.bodyLarge
        )

        Text("¿Qué tan útil te resulta la app?", style = MaterialTheme.typography.titleMedium)
        Row {
            (1..5).forEach { star ->
                IconButton(onClick = { rating = star }) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "$star estrellas",
                        tint = if (star <= rating) StarColor else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Text("¿La usarías todas las semanas?", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Sí", "Tal vez", "No").forEach { option ->
                FilterChip(
                    selected = wouldUse == option,
                    onClick = { wouldUse = option },
                    label = { Text(option) }
                )
            }
        }

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("¿Qué te gustó? ¿Qué le falta?") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                vm.sendFeedback(rating, wouldUse.orEmpty(), comment) {
                    sent = true
                    rating = 0
                    wouldUse = null
                    comment = ""
                }
            },
            enabled = rating > 0 && !vm.isSending,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (vm.isSending) "Enviando..." else "Enviar opinión")
        }
    }
}
