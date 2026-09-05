package com.example.william.my.module.storage.mmkv

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.tencent.mmkv.MMKV

/**
 * MMKV — 高性能键值存储框架
 *
 * MMKV 是腾讯开源的高性能键值存储库，基于 mmap 内存映射实现。
 *
 * 核心特性：
 * 1. 高性能：基于 mmap 内存映射，读写性能远超 SharedPreferences
 * 2. 多进程支持：支持多进程并发访问与数据同步
 * 3. 跨平台：支持 Android、iOS、Windows、macOS 等平台
 * 4. 数据安全：支持数据加密，保障数据安全
 *
 * 基本用法：
 * ```kotlin
 * // 初始化
 * MMKV.initialize(context)
 *
 * // 写入数据
 * val kv = MMKV.defaultMMKV()
 * kv.encode("key", "value")
 *
 * // 读取数据
 * val value = kv.decodeString("key", "default")
 * ```
 *
 * 适用场景：
 * - 替代 SharedPreferences，提升性能
 * - 多进程数据共享
 * - 需要高性能键值存储的场景
 *
 * https://github.com/Tencent/MMKV
 */
@Route(path = RouterPath.Storage.MMKV)
class MMKVActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 MMKV 高性能键值存储（写入 / 读取 / 删除 / 清空）")
        initMMKV()
    }

    private fun initMMKV() {
        val rootDir: String = MMKV.initialize(this)
        appendLog("【MMKV 初始化】存储根目录: $rootDir")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "写入基础类型数据（String / Int / Boolean / Float）",
        "读取基础类型数据",
        "写入二进制数据（ByteArray）",
        "读取二进制数据",
        "删除指定键（user_name）",
        "清空全部存储（clearAll）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        val kv = MMKV.defaultMMKV()
        when (position) {
            0 -> {
                kv.encode("user_name", "Antigravity")
                kv.encode("user_age", 28)
                kv.encode("is_vip", true)
                kv.encode("score", 99.5f)
                appendLog("【MMKV 写入】已成功写入 user_name, user_age, is_vip, score")
            }

            1 -> {
                val name = kv.decodeString("user_name", "默认用户")
                val age = kv.decodeInt("user_age", 0)
                val isVip = kv.decodeBool("is_vip", false)
                val score = kv.decodeFloat("score", 0f)
                appendLog("【MMKV 读取】name=$name, age=$age, is_vip=$isVip, score=$score")
            }

            2 -> {
                val bytes = "MMKV_BINARY_PAYLOAD".toByteArray()
                kv.encode("bytes_key", bytes)
                appendLog("【MMKV 写入】写入 ByteArray，字节长度: ${bytes.size}")
            }

            3 -> {
                val bytes = kv.decodeBytes("bytes_key")
                val content = bytes?.let { String(it) } ?: "null"
                appendLog("【MMKV 读取】bytes_key 解析文本: $content")
            }

            4 -> {
                kv.removeValueForKey("user_name")
                val check = kv.decodeString("user_name", "已不存在")
                appendLog("【MMKV 删除】已删除 user_name，当前读取值: $check")
            }

            5 -> {
                kv.clearAll()
                appendLog("【MMKV 清空】已调用 clearAll() 清空全部 Key-Value 数据")
            }
        }
    }
}
