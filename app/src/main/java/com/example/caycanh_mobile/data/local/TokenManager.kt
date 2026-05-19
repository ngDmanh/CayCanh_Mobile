package com.example.caycanh_mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.caycanh_mobile.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Mở rộng Context để có property dataStore — gọi 1 lần ở top level
private val Context.dataStore by preferencesDataStore(name = Constants.DATASTORE_NAME)

/**
 * Quản lý token và thông tin user lưu cục bộ.
 *
 * Dùng DataStore thay SharedPreferences — async, type-safe, mã hóa.
 *
 * Singleton: chỉ tạo 1 instance trong toàn app.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Khai báo các key
    private val accessTokenKey = stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)
    private val userIdKey = stringPreferencesKey(Constants.KEY_USER_ID)
    private val userRoleKey = stringPreferencesKey(Constants.KEY_USER_ROLE)
    private val userNameKey = stringPreferencesKey(Constants.KEY_USER_NAME)

    // ── Save ────────────────────────────────────────────────────

    suspend fun saveAuth(token: String, userId: String, role: String, fullName: String) {
        context.dataStore.edit { prefs ->
            prefs[accessTokenKey] = token
            prefs[userIdKey] = userId
            prefs[userRoleKey] = role
            prefs[userNameKey] = fullName
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }

    // ── Read async (Flow) — dùng khi cần observe ───────────────

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { it[accessTokenKey] }
    val userRoleFlow: Flow<String?> = context.dataStore.data.map { it[userRoleKey] }
    val userNameFlow: Flow<String?> = context.dataStore.data.map { it[userNameKey] }

    // ── Read sync (1 lần) — dùng cho Interceptor ───────────────

    /**
     * Đọc token đồng bộ — chỉ dùng từ AuthInterceptor
     */
    suspend fun getAccessTokenOnce(): String? {
        return context.dataStore.data.first()[accessTokenKey]
    }

    suspend fun getUserRoleOnce(): String? {
        return context.dataStore.data.first()[userRoleKey]
    }

    suspend fun getUserNameOnce(): String? {
        return context.dataStore.data.first()[userNameKey]
    }

    suspend fun isLoggedIn(): Boolean = getAccessTokenOnce() != null
}