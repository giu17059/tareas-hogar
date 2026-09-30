package com.example.tareashogar.data

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "Firestore"

// Los errores (ej. PERMISSION_DENIED al cerrar sesión) se registran pero no cortan el Flow.
internal fun Query.asFlow(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.w(TAG, "Error escuchando la consulta", error)
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registration.remove() }
}

internal fun DocumentReference.asFlow(): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.w(TAG, "Error escuchando el documento $path", error)
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registration.remove() }
}

internal fun DocumentSnapshot.toAppUser() = AppUser(
    uid = id,
    name = getString("name").orEmpty(),
    email = getString("email").orEmpty(),
    familyId = getString("familyId")
)

internal fun DocumentSnapshot.toFamily() = Family(
    id = id,
    name = getString("name").orEmpty(),
    inviteCode = getString("inviteCode").orEmpty(),
    members = (get("members") as? List<*>)?.filterIsInstance<String>().orEmpty(),
    createdBy = getString("createdBy").orEmpty()
)

internal fun DocumentSnapshot.toHouseTask() = HouseTask(
    id = id,
    title = getString("title").orEmpty(),
    description = getString("description").orEmpty(),
    category = TaskCategory.from(getString("category")),
    assignedTo = getString("assignedTo"),
    assignedName = getString("assignedName"),
    done = getBoolean("done") ?: false,
    createdBy = getString("createdBy").orEmpty(),
    createdAt = getLong("createdAt") ?: 0L,
    completedBy = getString("completedBy"),
    completedAt = getLong("completedAt")
)
