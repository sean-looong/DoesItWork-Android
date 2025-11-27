import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// 给Context添加一个全局的dataStore - 使用 preferencesDataStore 委托
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preference")

class DataStoreManager() {

    companion object {
        @Volatile
        private var INSTANCE: DataStoreManager? = null

        private lateinit var appContext: Context

        // 需要在 Application 中初始化
        fun init(context: Context) {
            appContext = context.applicationContext
        }

        fun getInstance(): DataStoreManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataStoreManager().also {
                    INSTANCE = it
                }
            }
        }
    }

    // 创建Preferences Key的辅助方法
    private fun stringKey(key: String) = stringPreferencesKey(key)
    private fun intKey(key: String) = intPreferencesKey(key)
    private fun booleanKey(key: String) = booleanPreferencesKey(key)
    private fun longKey(key: String) = longPreferencesKey(key)
    private fun floatKey(key: String) = floatPreferencesKey(key)
    private fun doubleKey(key: String) = doublePreferencesKey(key)
    private fun stringSetKey(key: String) = stringSetPreferencesKey(key)

    // String
    suspend fun putString(key: String, value: String) {
        appContext.dataStore.edit { preferences ->
            preferences[stringKey(key)] = value
        }
    }

    // String - 同步版本
    fun putStringSync(key: String, value: String) {
        runBlocking {
            putString(key, value)
        }
    }

    fun getString(key: String, defaultValue: String = ""): Flow<String> {
        return appContext.dataStore.data.map { preferences ->
            preferences[stringKey(key)] ?: defaultValue
        }
    }

    // String - 同步版本
    fun getStringSync(key: String, defaultValue: String = ""): String {
        return runBlocking {
            getString(key, defaultValue).first()
        }
    }

    // Int
    suspend fun putInt(key: String, value: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[intKey(key)] = value
        }
    }

    // Int - 同步版本
    fun putIntSync(key: String, value: Int) {
        runBlocking {
            putInt(key, value)
        }
    }

    fun getInt(key: String, defaultValue: Int = 0): Flow<Int> {
        return appContext.dataStore.data.map { preferences ->
            preferences[intKey(key)] ?: defaultValue
        }
    }

    // Int - 同步版本
    fun getIntSync(key: String, defaultValue: Int = 0): Int {
        return runBlocking {
            getInt(key, defaultValue).first()
        }
    }

    // Boolean
    suspend fun putBoolean(key: String, value: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[booleanKey(key)] = value
        }
    }

    // Boolean - 同步版本
    fun putBooleanSync(key: String, value: Boolean) {
        runBlocking {
            putBoolean(key, value)
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean> {
        return appContext.dataStore.data.map { preferences ->
            preferences[booleanKey(key)] ?: defaultValue
        }
    }

    // Boolean - 同步版本
    fun getBooleanSync(key: String, defaultValue: Boolean = false): Boolean {
        return runBlocking {
            getBoolean(key, defaultValue).first()
        }
    }

    // Long
    suspend fun putLong(key: String, value: Long) {
        appContext.dataStore.edit { preferences ->
            preferences[longKey(key)] = value
        }
    }

    // Long - 同步版本
    fun putLongSync(key: String, value: Long) {
        runBlocking {
            putLong(key, value)
        }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Flow<Long> {
        return appContext.dataStore.data.map { preferences ->
            preferences[longKey(key)] ?: defaultValue
        }
    }

    // Long - 同步版本
    fun getLongSync(key: String, defaultValue: Long = 0L): Long {
        return runBlocking {
            getLong(key, defaultValue).first()
        }
    }

    // Float
    suspend fun putFloat(key: String, value: Float) {
        appContext.dataStore.edit { preferences ->
            preferences[floatKey(key)] = value
        }
    }

    // Float - 同步版本
    fun putFloatSync(key: String, value: Float) {
        runBlocking {
            putFloat(key, value)
        }
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Flow<Float> {
        return appContext.dataStore.data.map { preferences ->
            preferences[floatKey(key)] ?: defaultValue
        }
    }

    // Float - 同步版本
    fun getFloatSync(key: String, defaultValue: Float = 0f): Float {
        return runBlocking {
            getFloat(key, defaultValue).first()
        }
    }

    // Double
    suspend fun putDouble(key: String, value: Double) {
        appContext.dataStore.edit { preferences ->
            preferences[doubleKey(key)] = value
        }
    }

    // Double - 同步版本
    fun putDoubleSync(key: String, value: Double) {
        runBlocking {
            putDouble(key, value)
        }
    }

    fun getDouble(key: String, defaultValue: Double = 0.0): Flow<Double> {
        return appContext.dataStore.data.map { preferences ->
            preferences[doubleKey(key)] ?: defaultValue
        }
    }

    // Double - 同步版本
    fun getDoubleSync(key: String, defaultValue: Double = 0.0): Double {
        return runBlocking {
            getDouble(key, defaultValue).first()
        }
    }

    // String Set
    suspend fun putStringSet(key: String, value: Set<String>) {
        appContext.dataStore.edit { preferences ->
            preferences[stringSetKey(key)] = value
        }
    }

    // String Set - 同步版本
    fun putStringSetSync(key: String, value: Set<String>) {
        runBlocking {
            putStringSet(key, value)
        }
    }

    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Flow<Set<String>> {
        return appContext.dataStore.data.map { preferences ->
            preferences[stringSetKey(key)] ?: defaultValue
        }
    }

    // String Set - 同步版本
    fun getStringSetSync(key: String, defaultValue: Set<String> = emptySet()): Set<String> {
        return runBlocking {
            getStringSet(key, defaultValue).first()
        }
    }

    // Remove key
    suspend fun remove(key: String) {
        appContext.dataStore.edit { preferences ->
            preferences.remove(stringKey(key))
            preferences.remove(intKey(key))
            preferences.remove(booleanKey(key))
            preferences.remove(longKey(key))
            preferences.remove(floatKey(key))
            preferences.remove(doubleKey(key))
            preferences.remove(stringSetKey(key))
        }
    }

    // Remove key - 同步版本
    fun removeSync(key: String) {
        runBlocking {
            remove(key)
        }
    }

    // Clear all data
    suspend fun clear() {
        appContext.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Clear all data - 同步版本
    fun clearSync() {
        runBlocking {
            clear()
        }
    }

    // 批量获取多个key的值 - 同步版本
    fun getMultipleSync(keys: List<String>): Map<String, Any?> {
        return runBlocking {
            appContext.dataStore.data.first().let { preferences ->
                keys.associateWith { key ->
                    when {
                        preferences.contains(stringKey(key)) -> preferences[stringKey(key)]
                        preferences.contains(intKey(key)) -> preferences[intKey(key)]
                        preferences.contains(booleanKey(key)) -> preferences[booleanKey(key)]
                        preferences.contains(longKey(key)) -> preferences[longKey(key)]
                        preferences.contains(floatKey(key)) -> preferences[floatKey(key)]
                        preferences.contains(doubleKey(key)) -> preferences[doubleKey(key)]
                        preferences.contains(stringSetKey(key)) -> preferences[stringSetKey(key)]
                        else -> null
                    }
                }
            }
        }
    }

    // 批量设置多个key的值 - 同步版本
    fun putMultipleSync(values: Map<String, Any>) {
        runBlocking {
            appContext.dataStore.edit { preferences ->
                values.forEach { (key, value) ->
                    when (value) {
                        is String -> preferences[stringKey(key)] = value
                        is Int -> preferences[intKey(key)] = value
                        is Boolean -> preferences[booleanKey(key)] = value
                        is Long -> preferences[longKey(key)] = value
                        is Float -> preferences[floatKey(key)] = value
                        is Double -> preferences[doubleKey(key)] = value
                        is Set<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            preferences[stringSetKey(key)] = value as Set<String>
                        }
                        else -> throw IllegalArgumentException("Unsupported type: ${value::class.java}")
                    }
                }
            }
        }
    }

    // 检查key是否存在 - 同步版本
    fun containsSync(key: String): Boolean {
        return runBlocking {
            appContext.dataStore.data.first().let { preferences ->
                preferences.contains(stringKey(key)) ||
                        preferences.contains(intKey(key)) ||
                        preferences.contains(booleanKey(key)) ||
                        preferences.contains(longKey(key)) ||
                        preferences.contains(floatKey(key)) ||
                        preferences.contains(doubleKey(key)) ||
                        preferences.contains(stringSetKey(key))
            }
        }
    }

    // 获取所有key - 同步版本（修正版）
    fun getAllKeysSync(): Set<String> {
        return runBlocking {
            appContext.dataStore.data.first().asMap().keys.map { preferenceKey ->
                // 直接使用 preferenceKey 的 name 属性
                preferenceKey.name
            }.toSet()
        }
    }

    // 获取所有数据 - 同步版本
    fun getAllDataSync(): Map<String, Any?> {
        return runBlocking {
            appContext.dataStore.data.first().asMap().map { (preferenceKey, value) ->
                preferenceKey.name to value
            }.toMap()
        }
    }
}