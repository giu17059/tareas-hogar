package com.example.tareashogar.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun tasks(familyId: String) =
        db.collection("families").document(familyId).collection("tasks")

    fun observeTasks(familyId: String): Flow<List<HouseTask>> =
        tasks(familyId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .asFlow()
            .map { snapshot -> snapshot.documents.map { it.toHouseTask() } }

    suspend fun addTask(
        familyId: String,
        title: String,
        description: String,
        category: TaskCategory,
        assignee: AppUser?,
        createdBy: String
    ) {
        tasks(familyId).add(
            mapOf(
                "title" to title.trim(),
                "description" to description.trim(),
                "category" to category.name,
                "assignedTo" to assignee?.uid,
                "assignedName" to assignee?.name,
                "done" to false,
                "createdBy" to createdBy,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun setDone(familyId: String, taskId: String, done: Boolean, uid: String) {
        tasks(familyId).document(taskId).update(
            mapOf(
                "done" to done,
                "completedBy" to if (done) uid else null,
                "completedAt" to if (done) System.currentTimeMillis() else null
            )
        ).await()
    }

    suspend fun deleteTask(familyId: String, taskId: String) {
        tasks(familyId).document(taskId).delete().await()
    }
}
