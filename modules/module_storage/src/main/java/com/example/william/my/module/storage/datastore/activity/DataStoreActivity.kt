package com.example.william.my.module.storage.datastore.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.coroutine.collectWithLifecycle
import com.example.william.my.module.storage.datastore.data.ExamplePreferenceDataStore
import com.example.william.my.module.storage.datastore.data.ExampleProtoDataStore
import com.example.william.my.module.storage.proto.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DataStore — Jetpack 异步键值 / Proto 偏好持久化方案
 *
 * 核心机制与避坑点：
 * 1. 两种形态与分工：Preferences 自由键值 / Proto schema 偏好对象；多实体查询与关系数据归 Room（Offline-First 中 Room 作实体 SSOT）
 * 2. 异步读写：API 全面基于协程与 Flow，updateData / data 均为挂起，禁止在主线程同步读取
 * 3. 一致性：updateData 在事务内完成读改写，失败自动回抛 IOException，适合计数器与多字段偏好原子场景
 * 4. 流式监听：data 返回的 Flow 在每次写入后重发最新值；UI 收集走 collectWithLifecycle，页面销毁自动取消
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
            "DataStore 示例：Preferences 自由键值 + Proto 类型安全偏好对象（非结构化偏好；多实体查询见 Room）",
        )
        observeDataStore()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 写入 Preferences 用户名",
        "2. 读取当前 Flow 观察值",
        "3. Preferences 计数器自增 (updateData 事务)",
        "4. Proto 偏好对象原子写入 (多字段 updateData，库特有)",
        "5. Proto 主题模式切换 (枚举类型安全，库特有)",
        "6. Proto 功能开关增删 (repeated 字段，库特有)",
        "7. 清空所有 DataStore 数据",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> setPrefUserName()
            1 -> readCurrentFlowValues()
            2 -> incrementPrefCounter()
            3 -> writeProtoUserPreferences()
            4 -> cycleProtoThemeMode()
            5 -> toggleProtoFeatureFlag()
            6 -> clearAllDataStore()
        }
    }

    /**
     * 响应式监听：写入后 Flow 自动重发最新值
     */
    private fun observeDataStore() {
        preDataStore.getCounter().collectWithLifecycle(this) { count ->
            appendLog("[Preferences Flow] Counter: $count")
        }
        preDataStore.getUserName().collectWithLifecycle(this) { name ->
            appendLog("[Preferences Flow] UserName: $name")
        }
        protoDataStore.getSettings().collectWithLifecycle(this) { settings ->
            appendLog(
                "[Proto Flow] theme=${settings.themeMode}, lang=${settings.preferredLanguage}, " +
                    "notify=${settings.notificationEnabled}, flags=${settings.enabledFeatureFlagsList}, " +
                    "counter=${settings.exampleCounter}",
            )
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

    private fun writeProtoUserPreferences() {
        appendLog("→ Proto 多字段偏好原子写入 (theme/lang/notify)...")
        lifecycleScope.launch(Dispatchers.IO) {
            protoDataStore.updateUserPreferences(
                themeMode = ThemeMode.THEME_MODE_DARK,
                preferredLanguage = "zh-CN",
                notificationEnabled = true,
            )
            withContext(Dispatchers.Main) {
                appendLog("✓ Proto 偏好已原子写入 theme=DARK, lang=zh-CN, notify=true")
            }
        }
    }

    private fun cycleProtoThemeMode() {
        appendLog("→ Proto 主题模式切换...")
        lifecycleScope.launch(Dispatchers.IO) {
            val current = protoDataStore.getSettings().first().themeMode
            val next = when (current) {
                ThemeMode.THEME_MODE_SYSTEM,
                ThemeMode.THEME_MODE_UNSPECIFIED,
                -> ThemeMode.THEME_MODE_LIGHT
                ThemeMode.THEME_MODE_LIGHT -> ThemeMode.THEME_MODE_DARK
                ThemeMode.THEME_MODE_DARK -> ThemeMode.THEME_MODE_SYSTEM
                else -> ThemeMode.THEME_MODE_SYSTEM
            }
            protoDataStore.setThemeMode(next)
            withContext(Dispatchers.Main) {
                appendLog("✓ Proto ThemeMode: $current → $next")
            }
        }
    }

    private fun toggleProtoFeatureFlag() {
        appendLog("→ Proto 功能开关增删 (repeated)...")
        lifecycleScope.launch(Dispatchers.IO) {
            val flag = "offline_sync_v2"
            val before = protoDataStore.getSettings().first().enabledFeatureFlagsList.toList()
            protoDataStore.toggleFeatureFlag(flag)
            val after = protoDataStore.getSettings().first().enabledFeatureFlagsList.toList()
            withContext(Dispatchers.Main) {
                val action = if (after.contains(flag)) "追加" else "移除"
                appendLog("✓ FeatureFlag $flag $action：$before → $after")
            }
        }
    }

    private fun readCurrentFlowValues() {
        appendLog("→ 读取 Flow 当前值...")
        lifecycleScope.launch(Dispatchers.IO) {
            val prefCounter = preDataStore.getCounter().first()
            val userName = preDataStore.getUserName().first()
            val settings = protoDataStore.getSettings().first()
            withContext(Dispatchers.Main) {
                appendLog(
                    "✓ [Flow first] PrefCounter=$prefCounter, UserName=$userName, " +
                        "Proto(theme=${settings.themeMode}, lang=${settings.preferredLanguage}, " +
                        "notify=${settings.notificationEnabled}, flags=${settings.enabledFeatureFlagsList}, " +
                        "counter=${settings.exampleCounter})",
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
