package com.example.tareashogar.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val users = db.collection("users")

    /** Emite el uid del usuario logueado, o null si no hay sesión. */
    fun authState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun register(name: String, email: String, password: String) {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("No se pudo crear el usuario")
        users.document(user.uid).set(
            mapOf(
                "name" to name.trim(),
                "email" to email.trim(),
                "createdAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
    }

    fun observeUser(uid: String): Flow<AppUser?> =
        users.document(uid).asFlow().map { if (it.exists()) it.toAppUser() else null }

    /** Usuario armado con los datos de Auth mientras el documento de Firestore todavía no existe. */
    fun fallbackUser(uid: String): AppUser {
        val user = auth.currentUser
        return AppUser(
            uid = uid,
            name = user?.displayName?.takeIf { it.isNotBlank() }
                ?: user?.email?.substringBefore("@")
                ?: "Usuario",
            email = user?.email.orEmpty()
        )
    }

    fun logout() = auth.signOut()
}
