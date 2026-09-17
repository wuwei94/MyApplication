package com.example.william.my.module.storage.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.storage.datastore.ExamplePreferenceDataStore
import com.example.william.my.module.storage.datastore.ExampleProtoDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DataStore — Jetpack 异步键值 / Proto 持久化方案
 *
 * 核心机制与避坑点：
 * 1. 两种形态：Preferences DataStore 无 schema 键值对；Proto DataStore 基于 Protobuf schema 提供编译期类型检查
 * 2. 异步读写：API 全面基于协程与 Flow，updateData / data 均为挂起，禁止在主线程同步读取
 * 3. 一致性：updateData 在事务内完成读改写，失败自动回抛 IOException，适合计数器等原子场景
 * 4. 流式监听：data 返回的 Flow 在每次写入后重发最新值；collect 需在生命周期作用域内，页面销毁自动取消
 *
 * 官方参考：
 * https://developer.android.com/topic/libraries/architecture/datastore
 */
@Route(path = RouterPath.Storage.DataStore)
class DataStoreActivity : BasicResponseActivity() {

    private val preDataStore by lazy { ExamplePreferenceDataStore(applicationContext) }
    private val protoDataStore by lazy { ExampleProtoDataStore(applicationContext) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "DataStore 示例：写入 / updateData 事务 / Flow 读取观察 / 清理重置",
        )
        observeDataStore()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 写入 Preferences 用户名",
        "2. Preferences 计数器自增 (updateData 事务)",
        "3. Proto 计数器自增 (类型安全)",
        "4. 读取当前 Flow 观察值",
        "5. 清空所有 DataStore 数据",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> setPrefUserName()
            1 -> incrementPrefCounter()
            2 -> incrementProtoCounter()
            3 -> readCurrentFlowValues()
            4 -> clearAllDataStore()
        }
    }

    /**
     * 响应式监听：写入后 Flow 自动重发最新值
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

    private fun setPrefUserName() {
        appendLog("→ 写入 Preferences 用户名...")
        lifecycleScope.launch(Dispatchers.IO) {
            val randomName = "User_${System.currentTimeMillis() % 1000}"
            preDataStore.setUserName(randomName)
            withContext(Dispatchers.Main) {
                appendLog("✓ 已写入 UserName=$randomName")
            }
        }
    }

    private fun incrementPrefCounter() {
        appendLog("→ Preferences Counter updateData 自增...")
        lifecycleScope.launch(Dispatchers.IO) {
            preDataStore.incrementCounter()
            withContext(Dispatchers.Main) {
                appendLog("✓ Preferences Counter 自增请求已提交")
            }
        }
    }

    private fun incrementProtoCounter() {
        appendLog("→ Proto Counter updateData 自增...")
        lifecycleScope.launch(Dispatchers.IO) {
            protoDataStore.incrementCounter()
            withContext(Dispatchers.Main) {
                appendLog("✓ Proto Counter 自增请求已提交")
            }
        }
    }

    private fun readCurrentFlowValues() {
        appendLog("→ 读取 Flow 当前值...")
        lifecycleScope.launch(Dispatchers.IO) {
            val prefCounter = preDataStore.getCounter().first()
            val userName = preDataStore.getUserName().first()
            val protoCounter = protoDataStore.getCounter().first()
            withContext(Dispatchers.Main) {
                appendLog(
                    "✓ [Flow first] PrefCounter=$prefCounter, UserName=$userName, ProtoCounter=$protoCounter",
                )
            }
        }
    }

    private fun clearAllDataStore() {
        appendLog("→ 清空 Preferences 与 Proto DataStore...")
        lifecycleScope.launch(Dispatchers.IO) {
            preDataStore.clear()
            protoDataStore.clear()
            withContext(Dispatchers.Main) {
                appendLog("✓ 已清空 Preferences 与 Proto DataStore 数据")
            }
        }
    }
}
