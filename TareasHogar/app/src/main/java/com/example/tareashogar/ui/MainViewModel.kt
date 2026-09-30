package com.example.tareashogar.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tareashogar.analytics.AnalyticsTracker
import com.example.tareashogar.data.AppUser
import com.example.tareashogar.data.AuthRepository
import com.example.tareashogar.data.FamilyRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class NoFamily(val user: AppUser) : SessionState
    data class Ready(val user: AppUser, val familyId: String) : SessionState
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val familyRepo = FamilyRepository()

    val session: StateFlow<SessionState> = authRepo.authState()
        .flatMapLatest { uid ->
            AnalyticsTracker.setUser(uid)
            if (uid == null) {
                flowOf(SessionState.LoggedOut)
            } else {
                authRepo.observeUser(uid).map { user ->
                    val current = user ?: authRepo.fallbackUser(uid)
                    val familyId = current.familyId
                    if (familyId.isNullOrBlank()) SessionState.NoFamily(current)
                    else SessionState.Ready(current, familyId)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    var isBusy by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Completá email y contraseña"
            return
        }
        runAction {
            authRepo.login(email, password)
            AnalyticsTracker.log(FirebaseAnalytics.Event.LOGIN)
        }
    }

    fun register(name: String, email: String, password: String) {
        errorMessage = when {
            name.isBlank() -> "Ingresá tu nombre"
            email.isBlank() -> "Ingresá tu email"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
        if (errorMessage != null) return
        runAction {
            authRepo.register(name, email, password)
            AnalyticsTracker.log(FirebaseAnalytics.Event.SIGN_UP)
        }
    }

    fun createFamily(user: AppUser, familyName: String) {
        if (familyName.isBlank()) {
            errorMessage = "Ingresá un nombre para la familia"
            return
        }
        runAction {
            familyRepo.createFamily(user, familyName)
            AnalyticsTracker.log("family_created")
        }
    }

    fun joinFamily(user: AppUser, code: String) {
        if (code.trim().length != 6) {
            errorMessage = "El código tiene 6 caracteres"
            return
        }
        runAction {
            familyRepo.joinFamily(user, code)
            AnalyticsTracker.log("family_joined")
        }
    }

    fun logout() {
        errorMessage = null
        authRepo.logout()
    }

    private fun runAction(block: suspend () -> Unit) {
        if (isBusy) return
        viewModelScope.launch {
            isBusy = true
            errorMessage = null
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                isBusy = false
            }
        }
    }
}

private fun Exception.toUserMessage(): String = when (this) {
    // WeakPassword hereda de InvalidCredentials, por eso va primero.
    is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres"
    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese email"
    is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese email"
    is FirebaseAuthInvalidCredentialsException -> "Email o contraseña incorrectos"
    is FirebaseNetworkException -> "Sin conexión a internet"
    is IllegalArgumentException -> message ?: "Datos inválidos"
    else -> message ?: "Ocurrió un error inesperado"
}
