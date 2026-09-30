package com.example.tareashogar.data

import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FeedbackRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun send(
        user: AppUser,
        familyId: String?,
        rating: Int,
        wouldUseWeekly: String,
        comment: String,
        tasksInFamily: Int
    ) {
        db.collection("feedback").add(
            mapOf(
                "uid" to user.uid,
                "userName" to user.name,
                "familyId" to familyId,
                "rating" to rating,
                "wouldUseWeekly" to wouldUseWeekly,
                "comment" to comment.trim(),
                "tasksInFamily" to tasksInFamily,
                "device" to "${Build.MANUFACTURER} ${Build.MODEL}",
                "androidVersion" to Build.VERSION.SDK_INT,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }
}
