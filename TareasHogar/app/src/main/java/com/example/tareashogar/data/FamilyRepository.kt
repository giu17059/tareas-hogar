package com.example.tareashogar.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FamilyRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val families = db.collection("families")
    private val users = db.collection("users")

    suspend fun createFamily(user: AppUser, familyName: String): String {
        val familyRef = families.document()
        db.batch().apply {
            set(
                familyRef,
                mapOf(
                    "name" to familyName.trim(),
                    "inviteCode" to generateInviteCode(),
                    "members" to listOf(user.uid),
                    "createdBy" to user.uid,
                    "createdAt" to System.currentTimeMillis()
                )
            )
            set(
                users.document(user.uid),
                mapOf("name" to user.name, "email" to user.email, "familyId" to familyRef.id),
                SetOptions.merge()
            )
        }.commit().await()
        return familyRef.id
    }

    suspend fun joinFamily(user: AppUser, code: String): String {
        val snapshot = families
            .whereEqualTo("inviteCode", code.trim().uppercase())
            .limit(1)
            .get()
            .await()
        val familyDoc = snapshot.documents.firstOrNull()
            ?: throw IllegalArgumentException("No existe ninguna familia con ese código")

        db.batch().apply {
            update(familyDoc.reference, "members", FieldValue.arrayUnion(user.uid))
            set(
                users.document(user.uid),
                mapOf("name" to user.name, "email" to user.email, "familyId" to familyDoc.id),
                SetOptions.merge()
            )
        }.commit().await()
        return familyDoc.id
    }

    suspend fun leaveFamily(uid: String, familyId: String) {
        db.batch().apply {
            update(families.document(familyId), "members", FieldValue.arrayRemove(uid))
            update(users.document(uid), "familyId", FieldValue.delete())
        }.commit().await()
    }

    fun observeFamily(familyId: String): Flow<Family?> =
        families.document(familyId).asFlow().map { if (it.exists()) it.toFamily() else null }

    fun observeMembers(familyId: String): Flow<List<AppUser>> =
        users.whereEqualTo("familyId", familyId).asFlow()
            .map { snapshot -> snapshot.documents.map { it.toAppUser() } }

    private fun generateInviteCode(): String {
        // Sin 0/O ni 1/I para evitar confusiones al dictar el código.
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { alphabet.random() }.joinToString("")
    }
}
