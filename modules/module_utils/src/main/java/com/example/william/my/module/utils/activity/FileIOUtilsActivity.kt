package com.example.william.my.module.utils.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.FileIOUtils
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.io.ByteArrayInputStream
import java.io.File

/**
 * FileIOUtils — 文件读写工具
 *
 * BlankJ FileIOUtils 提供便捷的文件读写功能。
 *
 * 核心特性：
 * 1. 文本读写：支持字符串读写
 * 2. 流读写：支持 InputStream、OutputStream 读写
 * 3. 文件操作：支持文件创建、删除、复制等
 * 4. 缓冲优化：使用缓冲区提升读写性能
 *
 * 基本用法：
 * ```kotlin
 * // 写入文本
 * FileIOUtils.writeFileFromString(file, "Hello World")
 *
 * // 读取文本
 * val content = FileIOUtils.readFile2String(file)
 *
 * // 从输入流写入
 * FileIOUtils.writeFileFromIS(file, inputStream)
 * ```
 *
 * 适用场景：
 * - 文件读写操作
 * - 配置文件管理
 * - 日志文件写入
 *
 * https://github.com/Blankj/AndroidUtilCode
 */
@Route(path = RouterPath.Utils.FileIOUtils)
class FileIOUtilsActivity : BasicResponseActivity() {

    private val targetFile: File by lazy {
        File(getExternalFilesDir("FileIOUtils"), "demo_file_io.txt")
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 BlankJ FileIOUtils 文件读写操作")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "writeFileFromString (写入文本)",
            "readFile2String (读取文本)",
            "writeFileFromIS (从输入流写入)",
            "deleteFile (清理文件)"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                val content = "Hello BlankJ FileIOUtils! 写入时间: ${System.currentTimeMillis()}"
                val success = FileIOUtils.writeFileFromString(targetFile, content)
                appendLog("写入文本结果: $success, 路径: ${targetFile.absolutePath}")
            }

            1 -> {
                val content = FileIOUtils.readFile2String(targetFile)
                if (content != null) {
                    appendLog("读取文件内容: $content")
                } else {
                    appendLog("读取文件失败或文件不存在: ${targetFile.absolutePath}")
                }
            }

            2 -> {
                val isContent = "来自 InputStream 的数据: ${System.currentTimeMillis()}"
                val inputStream = ByteArrayInputStream(isContent.toByteArray())
                val success = FileIOUtils.writeFileFromIS(targetFile, inputStream)
                appendLog("从输入流写入结果: $success")
            }

            3 -> {
                val deleted = if (targetFile.exists()) targetFile.delete() else false
                appendLog("删除文件结果: $deleted")
            }
        }
    }
}