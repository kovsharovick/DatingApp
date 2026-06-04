package com.example.androiddatingapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

    suspend fun getReadNotificationIds(userId: Long): Set<String> {
        val key = readNotificationsKey(userId)
        return context.sessionDataStore.data.map { prefs ->
            prefs[key] ?: emptySet()
        }.first()
    }

    suspend fun addReadNotificationIds(userId: Long, ids: Collection<String>) {
        if (ids.isEmpty()) return
        val key = readNotificationsKey(userId)
        context.sessionDataStore.edit { prefs ->
            val current = prefs[key] ?: emptySet()
            prefs[key] = current + ids
        }
    }

    suspend fun getSwipedUserIds(userId: Long): Set<Long> {
        val key = swipedUsersKey(userId)
        return context.sessionDataStore.data.map { prefs ->
            prefs[key]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        }.first()
    }

    suspend fun addSwipedUserId(userId: Long, targetUserId: Long) {
        val key = swipedUsersKey(userId)
        context.sessionDataStore.edit { prefs ->
            val current = prefs[key] ?: emptySet()
            prefs[key] = current + targetUserId.toString()
        }
    }

    suspend fun removeSwipedUserId(userId: Long, targetUserId: Long) {
        val key = swipedUsersKey(userId)
        context.sessionDataStore.edit { prefs ->
            val current = prefs[key] ?: return@edit
            val updated = current - targetUserId.toString()
            if (updated.isEmpty()) prefs.remove(key) else prefs[key] = updated
        }
    }

    suspend fun clearSwipedUserIds(userId: Long) {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(swipedUsersKey(userId))
        }
    }

    companion object {
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_EMAIL = stringPreferencesKey("email")

        private fun readNotificationsKey(userId: Long) =
            stringSetPreferencesKey("read_notifications_$userId")

        private fun swipedUsersKey(userId: Long) =
            stringSetPreferencesKey("swiped_users_$userId")
    }
}
