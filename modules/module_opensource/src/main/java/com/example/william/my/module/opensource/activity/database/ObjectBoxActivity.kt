package com.example.william.my.module.opensource.activity.database

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.opensource.objectbox.ObjectBox
import com.example.william.my.module.opensource.objectbox.ObjectBoxNote
import com.google.gson.Gson
import io.objectbox.Box

/**
 * https://objectbox.io/
 * https://github.com/objectbox/objectbox-java
 */
@Route(path = RouterPath.OpenSource.Database.ObjectBox)
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
