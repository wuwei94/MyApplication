package com.example.william.my.module.storage.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.storage.objectbox.ObjectBox
import com.example.william.my.module.storage.objectbox.ObjectBoxNote
import com.google.gson.Gson
import io.objectbox.Box

/**
 * ObjectBox — 高性能移动数据库
 *
 * ObjectBox 是一个高性能的移动数据库，专为 Android 和 IoT 设备优化。
 *
 * 核心特性：
 * 1. 高性能：比 SQLite 快 10 倍，比 Room 快 2 倍
 * 2. 轻量级：APK 体积小，内存占用低
 * 3. 对象存储：直接存储对象，无需 ORM 映射
 * 4. 跨平台：支持 Android、iOS、Linux、Windows
 *
 * 基本用法：
 * ```kotlin
 * // 初始化
 * val boxStore = MyObjectBox.builder().androidContext(context).build()
 *
 * // 获取 Box
 * val notesBox = boxStore.boxFor(Note::class.java)
 *
 * // 插入数据
 * val note = Note(text = "Hello ObjectBox")
 * notesBox.put(note)
 *
 * // 查询数据
 * val notes = notesBox.all
 * ```
 *
 * 适用场景：
 * - 本地数据存储、缓存
 * - 离线应用、数据同步
 * - 需要高性能数据库的场景
 *
 * https://github.com/objectbox/objectbox-java
 */
@Route(path = RouterPath.Storage.ObjectBox)
class ObjectBoxActivity : BasicResponseActivity() {

    private lateinit var notesBox: Box<ObjectBoxNote>

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项操作 ObjectBox 数据库")
        initBox()
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("插入 Note 数据", "查询所有 Note", "清空 Note 数据")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                addNote()
                showNote()
            }

            1 -> {
                showNote()
            }

            2 -> {
                clearNotes()
                showNote()
            }
        }
    }

    private fun initBox() {
        ObjectBox.init(this)
        // Using ObjectBox Kotlin extension functions (https://docs.objectbox.io/kotlin-support)
        notesBox = ObjectBox.boxStore.boxFor(ObjectBoxNote::class.java)
    }

    private fun addNote() {
        val note = ObjectBoxNote(text = "ObjectBox Note ${System.currentTimeMillis()}")
        notesBox.put(note)
        appendLog("插入 Note: id=${note.id}, text=${note.text}")
    }

    private fun clearNotes() {
        notesBox.removeAll()
        appendLog("已清空所有 Note 数据")
    }

    private fun showNote() {
        val notes = notesBox.all
        if (notes.isEmpty()) {
            appendLog("当前数据库无数据")
        } else {
            notes.forEach { note ->
                appendLog(Gson().toJson(note))
            }
        }
    }
}
