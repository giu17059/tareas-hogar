package com.example.tareashogar.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Eventos que se envían a Firebase Analytics (etapa "Measure" del ciclo Build-Measure-Learn):
 * sign_up, login, family_created, family_joined, family_left, task_created,
 * task_completed, task_deleted, invite_shared, feedback_sent.
 */
object AnalyticsTracker {
    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context.applicationContext)
    }

    fun setUser(uid: String?) {
        analytics?.setUserId(uid)
    }

    fun log(event: String, vararg params: Pair<String, Any?>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                null -> Unit
                is String -> bundle.putString(key, value)
                is Int -> bundle.putLong(key, value.toLong())
                is Long -> bundle.putLong(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        analytics?.logEvent(event, bundle)
    }
}
