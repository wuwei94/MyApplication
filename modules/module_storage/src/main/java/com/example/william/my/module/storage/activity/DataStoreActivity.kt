package com.example.william.my.module.storage.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.storage.datastore.ExamplePreferenceDataStore
import com.example.william.my.module.storage.datastore.ExampleProtoDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * DataStore — 数据存储框架
 *
 * DataStore 是 Android Jetpack 提供的数据存储框架，用于替代 SharedPreferences。
 *
 * 两种类型：
 * 1. Preferences DataStore：键值对存储，无需预先定义 schema
 * 2. Proto DataStore：类型安全存储，需要预先定义 Protocol Buffers schema
 *
 * 核心特性：
 * 1. 异步 API：基于 Kotlin 协程与 Flow，完全避免阻塞主线程
 * 2. 类型安全：Proto DataStore 提供编译时类型检查
 * 3. 事务支持：支持数据事务与原子读写，保证数据一致性
 * 4. 自动迁移：支持从 SharedPreferences 自动迁移
 * 5. 异常处理：支持 Flow catch 捕获 I/O 异常与默认值降级
 *
 * 基本用法：
 * ```kotlin
 * // Preferences DataStore（顶层单例声明）
 * val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
 *
 * // 读取数据
 * val counterFlow: Flow<Int> = dataStore.data.map { preferences ->
 *     preferences[intPreferencesKey("counter")] ?: 0
 * }
 *
 * // 写入数据
 * dataStore.edit { settings ->
 *     val currentCounter = settings[intPreferencesKey("counter")] ?: 0
 *     settings[intPreferencesKey("counter")] = currentCounter + 1
 * }
 * ```
 *
 * 适用场景：
 * - 替代 SharedPreferences
 * - 键值对数据存储
 * - 需要异步 API 与响应式 Flow 监听的场景
 *
 * https://developer.android.google.cn/topic/libraries/architecture/datastore
 */
@Route(path = RouterPath.Storage.DataStore)
class DataStoreActivity : BasicResponseActivity() {

    private val preDataStore by lazy { ExamplePreferenceDataStore(applicationContext) }
    private val protoDataStore by lazy { ExampleProtoDataStore(applicationContext) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项读写与监听 Preferences 和 Proto DataStore")
        observeDataStore()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "Preferences: 自增计数器",
        "Preferences: 写入用户名",
        "Proto: 自增计数器 (类型安全)",
        "清空所有 DataStore 数据",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> incrementPrefCounter()
            1 -> setPrefUserName()
            2 -> incrementProtoCounter()
            3 -> clearAllDataStore()
        }
    }

    /**
     * 响应式监听 DataStore 数据流变化
     */
    private fun observeDataStore() {
        lifecycleScope.launch(Dispatchers.Main) {
            preDataStore.getCounter().collect { count ->
                appendLog("[Preferences Flow] Counter: $count")
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            preDataStore.getUserName().collect { name ->
                appendLog("[Preferences Flow] UserName: $name")
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            protoDataStore.getCounter().collect { count ->
                appendLog("[Proto Flow] ProtoCounter: $count")
            }
        }
    }

    private fun incrementPrefCounter() {
        lifecycleScope.launch(Dispatchers.IO) {
            preDataStore.incrementCounter()
        }
    }

    private fun setPrefUserName() {
        lifecycleScope.launch(Dispatchers.IO) {
            val randomName = "User_${System.currentTimeMillis() % 1000}"
            preDataStore.setUserName(randomName)
        }
    }

    private fun incrementProtoCounter() {
        lifecycleScope.launch(Dispatchers.IO) {
            protoDataStore.incrementCounter()
        }
    }

    private fun clearAllDataStore() {
        lifecycleScope.launch(Dispatchers.IO) {
            preDataStore.clear()
            protoDataStore.clear()
            appendLog("已清空 Preferences 与 Proto DataStore 数据")
        }
    }
}
