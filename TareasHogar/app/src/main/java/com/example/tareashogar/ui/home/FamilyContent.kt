package com.example.tareashogar.ui.home

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tareashogar.analytics.AnalyticsTracker
import com.example.tareashogar.data.AppUser
import com.example.tareashogar.data.Family

@Composable
fun FamilyContent(vm: HomeViewModel, user: AppUser, modifier: Modifier = Modifier) {
    val family by vm.family.collectAsState()
    val members by vm.members.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val context = LocalContext.current
    var confirmLeave by remember { mutableStateOf(false) }

    val completedByMember = remember(tasks) {
        tasks.filter { it.done }.groupingBy { it.completedBy }.eachCount()
    }
    val ranking = remember(members, completedByMember) {
        members.sortedByDescending { completedByMember[it.uid] ?: 0 }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Código de invitación", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = family?.inviteCode ?: "------",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 6.sp
                    )
                    Text(
                        "Compartilo con tu familia para que se unan desde la app.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { family?.let { shareInvite(context, it) } },
                        enabled = family != null
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir código")
                    }
                }
            }
        }

        item {
            Text(
                text = "Ranking de tareas completadas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        itemsIndexed(ranking, key = { _, member -> member.uid }) { index, member ->
            val count = completedByMember[member.uid] ?: 0
            val medal = when (index) {
                0 -> "🥇"
                1 -> "🥈"
                2 -> "🥉"
                else -> ""
            }
            ListItem(
                leadingContent = { Avatar(member.name) },
                headlineContent = {
                    Text(member.name + if (member.uid == user.uid) " (vos)" else "")
                },
                supportingContent = { Text(member.email) },
                trailingContent = {
                    Text(
                        text = "$medal $count ✔".trim(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }

        item {
            OutlinedButton(
                onClick = { confirmLeave = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Salir de la familia")
            }
        }
    }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Salir de la familia") },
            text = { Text("Vas a dejar de ver las tareas de \"${family?.name.orEmpty()}\". Podés volver a unirte con el código.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLeave = false
                    vm.leaveFamily()
                }) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmLeave = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun Avatar(name: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

private fun shareInvite(context: Context, family: Family) {
    val text = "¡Sumate a \"${family.name}\" en TareasHogar para organizar las tareas de la casa! " +
        "Código de invitación: ${family.inviteCode}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir código"))
    AnalyticsTracker.log("invite_shared")
}
