package com.domeos.agent.util

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rxpreferences2.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 偏好设置工具类
 * 使用 DataStore 存储简单配置
 */
@Singleton
class PreferencesUtil @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {

    private val dataStore: RxDataStore<Preferences> by lazy {
        RxPreferenceDataStoreBuilder(context, "domeos_prefs").build()
    }

    // Keys
    private val KEY_AUTO_START = preferencesKey<Boolean>("auto_start_quick_access")
    private val KEY_FLOATING_POSITION_X = preferencesKey<Int>("floating_position_x")
    private val KEY_FLOATING_POSITION_Y = preferencesKey<Int>("floating_position_y")
    private val KEY_HOTKEYS = preferencesKey<String>("hotkeys_json")
    private val KEY_BIOMETRIC_ENABLED = preferencesKey<Boolean>("biometric_enabled")
    private val KEY_LAST_SYNC = preferencesKey<Long>("last_sync_time")
    private val KEY_APP_THEME = preferencesKey<Int>("app_theme")
    private val KEY_FONT_SCALE = preferencesKey<Float>("font_scale")

    /** 获取布尔值 */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val prefs = context.getSharedPreferences("domeos_legacy", Context.MODE_PRIVATE)
        return prefs.getBoolean(key, defaultValue)
    }

    /** 设置布尔值 */
    fun setBoolean(key: String, value: Boolean): Completable {
        val prefs = context.getSharedPreferences("domeos_legacy", Context.MODE_PRIVATE)
        return Completable.fromAction { prefs.edit().putBoolean(key, value).apply() }
    }

    /** 获取整数 */
    fun getInt(key: String, defaultValue: Int): Int {
        val prefs = context.getSharedPreferences("domeos_legacy", Context.MODE_PRIVATE)
        return prefs.getInt(key, defaultValue)
    }

    /** 设置整数 */
    fun setInt(key: String, value: Int): Completable {
        val prefs = context.getSharedPreferences("domeos_legacy", Context.MODE_PRIVATE)
        return Completable.fromAction { prefs.edit().putInt(key, value).apply() }
    }

    /** 获取字符串 */
    fun getString(key: String, defaultValue: String): String {
        val prefs = context.getSharedPreferences("domeos_legacy", Context.MODE_PRIVATE)
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    /** 设置字符串 */
    fun setString(key: String, value: String): Completable {
        val prefs = context.getSharedPreferences("domeos_legacy", Context.MODE_PRIVATE)
        return Completable.fromAction { prefs.edit().putString(key, value).apply() }
    }

    /** 获取长整数 */
    fun getLong(key: String, defaultValue: Long): Long {
        val prefs = context.getSharedPreferences("domeos_legacy", Context.MODE_PRIVATE)
        return prefs.getLong(key, defaultValue)
    }

    /** 设置长整数 */
    fun setLong(key: String, value: Long): Completable {
        val prefs = context.getSharedPreferences("domeos_legacy", Context.MODE_PRIVATE)
        return Completable.fromAction { prefs.edit().putLong(key, value).apply() }
    }

    /** 获取浮点数 */
    fun getFloat(key: String, defaultValue: Float): Float {
        val prefs = context.getSharedPreferences("domeos_legacy", Context.MODE_PRIVATE)
        return prefs.getFloat(key, defaultValue)
    }

    /** 设置浮点数 */
    fun setFloat(key: String, value: Float): Completable {
        val prefs = context.getSharedPreferences("domeos_legacy", Context.MODE_PRIVATE)
        return Completable.fromAction { prefs.edit().putFloat(key, value).apply() }
    }

    /** DataStore 获取布尔值 */
    fun getBooleanDataStore(key: Preferences.Key<Boolean>): Single<Boolean> {
        return dataStore.data.firstOrError().map { it[key] ?: false }
    }

    /** DataStore 设置布尔值 */
    fun setBooleanDataStore(key: Preferences.Key<Boolean>, value: Boolean): Completable {
        return dataStore.updateDataAsync { it.toMutablePreferences().apply { put(key, value) } }
    }
}