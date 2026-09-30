package com.example.tareashogar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tareashogar.ui.auth.AuthScreen
import com.example.tareashogar.ui.family.FamilySetupScreen
import com.example.tareashogar.ui.home.HomeScreen

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    val session by vm.session.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val state = session) {
            SessionState.Loading -> LoadingScreen()

            SessionState.LoggedOut -> AuthScreen(
                isBusy = vm.isBusy,
                error = vm.errorMessage,
                onLogin = vm::login,
                onRegister = vm::register,
                onClearError = vm::clearError
            )

            is SessionState.NoFamily -> FamilySetupScreen(
                user = state.user,
                isBusy = vm.isBusy,
                error = vm.errorMessage,
                onCreate = { name -> vm.createFamily(state.user, name) },
                onJoin = { code -> vm.joinFamily(state.user, code) },
                onLogout = vm::logout
            )

            is SessionState.Ready -> HomeScreen(
                user = state.user,
                familyId = state.familyId,
                onLogout = vm::logout
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
