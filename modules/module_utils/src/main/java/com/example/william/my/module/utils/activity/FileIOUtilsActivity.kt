package com.example.william.my.module.utils.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.FileIOUtils
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.io.ByteArrayInputStream
import java.io.File

/**
 * 文件读写工具类演示
 *
 * 演示 BlankJ FileIOUtils 文件读写能力
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