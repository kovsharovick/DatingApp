package com.example.androiddatingapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

data class StoredSession(
    val userId: Long,
    val token: String,
    val email: String,
)

class SessionStore(private val context: Context) {

    @Volatile
    var token: String? = null
        private set

    suspend fun save(session: StoredSession) {
        token = session.token
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_USER_ID] = session.userId
            prefs[KEY_TOKEN] = session.token
            prefs[KEY_EMAIL] = session.email
        }
    }

    suspend fun load(): StoredSession? {
        val snapshot = context.sessionDataStore.data.map { prefs ->
            val userId = prefs[KEY_USER_ID] ?: return@map null
            val tokenValue = prefs[KEY_TOKEN] ?: return@map null
            val email = prefs[KEY_EMAIL] ?: return@map null
            StoredSession(userId, tokenValue, email)
        }.first()
        token = snapshot?.token
        return snapshot
    }

    suspend fun clear() {
        token = null
        context.sessionDataStore.edit { it.clear() }
    }

    companion object {
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_EMAIL = stringPreferencesKey("email")
    }
}
