package com.example.william.my.module.storage.mmkv.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.tencent.mmkv.MMKV

/**
 * MMKV — 基于 mmap 的高性能键值存储
 *
 * 核心机制与避坑点：
 * 1. mmap 映射：读写经内存映射文件完成，进程异常退出后数据仍可从页缓存恢复；写入即时生效无需手动 commit
 * 2. 初始化：Application 启动时调用 MMKV.initialize(context) 一次，返回的 rootDir 为存储根目录
 * 3. 多进程：多进程访问须使用 MMKV.mmkvWithID(id, MMKV.MULTI_PROCESS_MODE)，默认实例仅限单进程
 * 4. 观察面：MMKV 无原生 Flow/LiveData 观察 API，响应式场景需结合上层监听或对比 DataStore
 *
 * 官方参考：
 * https://github.com/Tencent/MMKV
 */
@Route(path = RouterPath.Storage.MMKV)
class MMKVActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "MMKV 示例：写入 / 读取 / 批量写入 / 二进制 / 删除 / 清空（同步 decode，无原生观察流）",
        )
        initMMKV()
    }

    private fun initMMKV() {
        val rootDir: String = MMKV.initialize(this)
        appendLog("[MMKV 初始化] 存储根目录: $rootDir")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 写入基础类型数据（String / Int / Boolean / Float）",
        "2. 读取基础类型数据",
        "3. 批量写入多键数据",
        "4. 删除指定键（user_name）",
        "5. 清空全部存储（clearAll）",
        "6. 写入二进制数据（ByteArray，库特有）",
        "7. 读取二进制数据（库特有）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        val kv = MMKV.defaultMMKV()
        when (position) {
            0 -> writeBasicTypes(kv)
            1 -> readBasicTypes(kv)
            2 -> writeBatchKeys(kv)
            3 -> removeUserName(kv)
            4 -> clearAll(kv)
            5 -> writeBinary(kv)
            6 -> readBinary(kv)
        }
    }

    private fun writeBasicTypes(kv: MMKV) {
        appendLog("→ 写入基础类型键值...")
        kv.encode("user_name", "Antigravity")
        kv.encode("user_age", 28)
        kv.encode("is_vip", true)
        kv.encode("score", 99.5f)
        appendLog("✓ 已写入 user_name, user_age, is_vip, score")
    }

    private fun readBasicTypes(kv: MMKV) {
        appendLog("→ 读取基础类型键值...")
        val name = kv.decodeString("user_name", "默认用户")
        val age = kv.decodeInt("user_age", 0)
        val isVip = kv.decodeBool("is_vip", false)
        val score = kv.decodeFloat("score", 0f)
        appendLog("✓ name=$name, age=$age, is_vip=$isVip, score=$score")
    }

    private fun writeBatchKeys(kv: MMKV) {
        appendLog("→ 批量写入多键数据...")
        kv.encode("batch_a", "alpha")
        kv.encode("batch_b", "beta")
        kv.encode("batch_c", 100)
        appendLog("✓ 已批量写入 batch_a, batch_b, batch_c")
    }

    private fun writeBinary(kv: MMKV) {
        appendLog("→ 写入 ByteArray...")
        val bytes = "MMKV_BINARY_PAYLOAD".toByteArray()
        kv.encode("bytes_key", bytes)
        appendLog("✓ 写入 ByteArray，字节长度: ${bytes.size}")
    }

    private fun readBinary(kv: MMKV) {
        appendLog("→ 读取 ByteArray...")
        val bytes = kv.decodeBytes("bytes_key")
        val content = bytes?.let { String(it) } ?: "null"
        appendLog("✓ bytes_key 解析文本: $content")
    }

    private fun removeUserName(kv: MMKV) {
        appendLog("→ 删除键 user_name...")
        kv.removeValueForKey("user_name")
        val check = kv.decodeString("user_name", "已不存在")
        appendLog("✓ 已删除 user_name，当前读取值: $check")
    }

    private fun clearAll(kv: MMKV) {
        appendLog("→ clearAll 清空存储...")
        kv.clearAll()
        appendLog("✓ 已调用 clearAll() 清空全部 Key-Value 数据")
    }
}
