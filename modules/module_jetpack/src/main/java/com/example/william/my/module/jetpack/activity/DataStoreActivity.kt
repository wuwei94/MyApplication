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
 * DataStore
 * https://developer.android.google.cn/topic/libraries/architecture/datastore
 * <p>
 * Preferences DataStore：不需要预先定义，但是不支持类型安全
 * Proto DataStore：需要预先使用protocol buffers定义数据，但是类型安全
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