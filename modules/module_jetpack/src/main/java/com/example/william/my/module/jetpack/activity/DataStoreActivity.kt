package com.example.william.my.module.jetpack.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.jetpack.datastore.ExamplePreferenceDataStore
import com.example.william.my.module.jetpack.datastore.ExampleProtoDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
 * 1. 异步 API：基于 Kotlin 协程，避免阻塞主线程
 * 2. 类型安全：Proto DataStore 提供编译时类型检查
 * 3. 事务支持：支持数据事务，保证数据一致性
 * 4. 自动迁移：支持从 SharedPreferences 自动迁移
 *
 * 基本用法：
 * ```kotlin
 * // Preferences DataStore
 * val dataStore = context.createDataStore(name = "settings")
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
 * - 需要异步 API 的场景
 *
 * https://developer.android.google.cn/topic/libraries/architecture/datastore
 */
@Route(path = RouterPath.Jetpack.DataStore)
class DataStoreActivity : BasicResponseActivity() {

    private val preDataStore = ExamplePreferenceDataStore(this)
    private val protoDataStore = ExampleProtoDataStore(this)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项操作 DataStore")
        initCounter()
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("自增计数器 (Increment)", "清空数据 (Clear)")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> incrementCounter()
            1 -> clearCounter()
        }
    }

    /**
     * 从 DataStore 读取内容
     */
    private fun initCounter() {
        lifecycleScope.launch(Dispatchers.Main) {
            preDataStore.getCounter()
                .collect {
                    appendLog("Preferences Count: $it")
                }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            protoDataStore.getCounter()
                .collect {
                    appendLog("Proto Count: $it")
                }
        }
    }

    /**
     * 将内容写入 DataStore
     */
    private fun incrementCounter() {
        lifecycleScope.launch(Dispatchers.Main) {
            preDataStore.incrementCounter()
            protoDataStore.incrementCounter()
        }
    }

    private fun clearCounter() {
        lifecycleScope.launch(Dispatchers.Main) {
            preDataStore.clear()
            protoDataStore.clear()
            appendLog("已清空 DataStore 数据")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runBlocking {
            preDataStore.clear()
            protoDataStore.clear()
        }
    }
}