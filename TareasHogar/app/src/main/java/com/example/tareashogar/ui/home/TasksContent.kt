package com.example.tareashogar.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.tareashogar.data.AppUser
import com.example.tareashogar.data.HouseTask

@Composable
fun TasksContent(vm: HomeViewModel, user: AppUser, modifier: Modifier = Modifier) {
    val tasks by vm.tasks.collectAsState()
    val members by vm.members.collectAsState()
    val names = remember(members) { members.associate { it.uid to it.name } }
    val visible = vm.visibleTasks(tasks)
    var taskToDelete by remember { mutableStateOf<HouseTask?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        ProgressHeader(tasks)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskFilter.entries.forEach { option ->
                FilterChip(
                    selected = vm.filter == option,
                    onClick = { vm.filter = option },
                    label = { Text(option.label) }
                )
            }
        }

        if (visible.isEmpty()) {
            EmptyTasks(filter = vm.filter, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visible, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        currentUid = user.uid,
                        names = names,
                        onToggle = { vm.toggleDone(task) },
                        onDelete = { taskToDelete = task }
                    )
                }
            }
        }
    }

    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Eliminar tarea") },
            text = { Text("¿Seguro que querés eliminar \"${task.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteTask(task)
                    taskToDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProgressHeader(tasks: List<HouseTask>) {
    val total = tasks.size
    val done = tasks.count { it.done }
    val progress = if (total == 0) 0f else done.toFloat() / total

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (total == 0) "Todavía no hay tareas cargadas"
                else "$done de $total tareas completadas",
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun TaskCard(
    task: HouseTask,
    currentUid: String,
    names: Map<String, String>,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isMine = task.assignedTo == currentUid
    val subtitle = when {
        task.done -> "✔ Hecha por ${names[task.completedBy] ?: "alguien de la familia"}"
        task.assignedTo == null -> "Sin asignar · cualquiera puede hacerla"
        isMine -> "Asignada a vos"
        else -> "Asignada a ${names[task.assignedTo] ?: task.assignedName ?: "otro integrante"}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isMine && !task.done) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${task.category.emoji} ${task.title}",
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null
                )
                if (task.description.isNotBlank()) {
                    Text(task.description, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar tarea")
            }
        }
    }
}

@Composable
private fun EmptyTasks(filter: TaskFilter, modifier: Modifier = Modifier) {
    val text = when (filter) {
        TaskFilter.PENDIENTES -> "¡No hay tareas pendientes! 🎉\nTocá \"Nueva tarea\" para agregar una."
        TaskFilter.MIAS -> "No tenés tareas asignadas."
        TaskFilter.TODAS -> "Todavía no hay tareas.\nTocá \"Nueva tarea\" para crear la primera."
        TaskFilter.HECHAS -> "Todavía no se completó ninguna tarea."
    }
    Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
