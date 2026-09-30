package com.example.tareashogar.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tareashogar.data.AppUser

enum class HomeTab(val label: String, val title: String, val icon: ImageVector) {
    TAREAS("Tareas", "Tareas del hogar", Icons.Filled.Home),
    FAMILIA("Familia", "Mi familia", Icons.Filled.Person),
    OPINION("Opinión", "Tu opinión", Icons.Filled.Star)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(user: AppUser, familyId: String, onLogout: () -> Unit) {
    val vm: HomeViewModel = viewModel(key = "home-$familyId")
    LaunchedEffect(familyId, user) { vm.start(familyId, user) }

    val family by vm.family.collectAsState()
    val members by vm.members.collectAsState()
    var tab by rememberSaveable { mutableStateOf(HomeTab.TAREAS) }
    var showAddTask by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val message = vm.message
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            vm.consumeMessage()
        }
    }

    BackHandler(enabled = tab != HomeTab.TAREAS) { tab = HomeTab.TAREAS }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tab.title)
                        family?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == HomeTab.TAREAS) {
                ExtendedFloatingActionButton(
                    onClick = { showAddTask = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva tarea") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (tab) {
            HomeTab.TAREAS -> TasksContent(vm = vm, user = user, modifier = contentModifier)
            HomeTab.FAMILIA -> FamilyContent(vm = vm, user = user, modifier = contentModifier)
            HomeTab.OPINION -> FeedbackContent(vm = vm, modifier = contentModifier)
        }
    }

    if (showAddTask) {
        AddTaskDialog(
            members = members,
            onDismiss = { showAddTask = false },
            onConfirm = { title, description, category, assignee ->
                vm.addTask(title, description, category, assignee)
                showAddTask = false
            }
        )
    }
}
