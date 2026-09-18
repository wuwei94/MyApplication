package com.example.william.my.module.database.objectbox.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.database.objectbox.data.ObjectBox
import com.example.william.my.module.database.objectbox.data.ObjectBoxNote
import com.google.gson.Gson
import io.objectbox.Box
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.query.Query

/**
 * ObjectBox — 面向对象的嵌入式移动数据库
 *
 * 核心机制与避坑点：
 * 1. 对象直存：实体类经注解处理器生成 Box，put/get 直接操作对象，无需手写 SQL 与 ORM 映射
 * 2. 线程模型：Box 操作需在非主线程执行；BoxStore 为进程级单例，多线程共享同一 BoxStore 实例
 * 3. 批量与事务：Box.put(List) 一次提交多实体，失败时由底层事务保证一致性
 * 4. 响应式：ObjectBoxLiveData / Query.subscribe 可在数据变更时回调
 * 5. 生命周期：BoxStore 在 Application 初始化一次，页面仅通过 boxFor 获取 Box，不在 Activity 销毁时关闭 Store
 *
 * 官方参考：
 * https://github.com/objectbox/objectbox-java
 */
@Route(path = RouterPath.Database.ObjectBox)
class ObjectBoxActivity : BasicResponseActivity() {

    private lateinit var notesBox: Box<ObjectBoxNote>
    private var noteQuery: Query<ObjectBoxNote>? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "ObjectBox 示例（与 Room 平行轴：插入 / 批量 / 更新 / 查询 / 观察 / 清理）",
        )
        initBox()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 插入 Note 数据 (put)",
        "2. 批量插入 Note 数据 (Box.put List)",
        "3. 覆盖更新 Note (put 同 id，等价 upsert)",
        "4. 查询所有 Note",
        "5. 订阅数据变更 (ObjectBoxLiveData)",
        "6. 清空 Note 数据",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> addNote()
            1 -> addBatchNotes()
            2 -> updateLatestNote()
            3 -> showNote()
            4 -> observeNotes()
            5 -> clearNotes()
        }
    }

    private fun initBox() {
        ObjectBox.init(this)
        // 使用 ObjectBox 的 Kotlin 扩展函数（https://docs.objectbox.io/kotlin-support）
        notesBox = ObjectBox.boxStore.boxFor(ObjectBoxNote::class.java)
        noteQuery = notesBox.query().build()
    }

    private fun addNote() {
        appendLog("→ 插入 Note...")
        val note = ObjectBoxNote(text = "ObjectBox Note ${System.currentTimeMillis()}")
        notesBox.put(note)
        appendLog("✓ 插入 Note: id=${note.id}, text=${note.text}")
    }

    private fun addBatchNotes() {
        appendLog("→ 批量插入 3 条 Note...")
        val notes = listOf(
            ObjectBoxNote(text = "Batch A ${System.currentTimeMillis()}"),
            ObjectBoxNote(text = "Batch B ${System.currentTimeMillis()}"),
            ObjectBoxNote(text = "Batch C ${System.currentTimeMillis()}"),
        )
        notesBox.put(notes)
        appendLog("✓ 批量插入完成，id 列表=${notes.map { it.id }}")
    }

    /**
     * 覆盖更新：ObjectBox 无独立 UPDATE SQL，put 相同 id 即 upsert。
     */
    private fun updateLatestNote() {
        appendLog("→ 覆盖更新最近一条 Note (put upsert)...")
        val latest = notesBox.all.lastOrNull()
        if (latest == null) {
            appendLog("✗ 当前无数据，请先插入")
            return
        }
        val oldText = latest.text
        latest.text = "Updated ${System.currentTimeMillis()}"
        notesBox.put(latest)
        appendLog("✓ 已更新 id=${latest.id}: \"$oldText\" → \"${latest.text}\"")
    }

    private fun showNote() {
        appendLog("→ 查询所有 Note...")
        val notes = notesBox.all
        if (notes.isEmpty()) {
            appendLog("✓ 当前数据库无数据")
        } else {
            notes.forEach { note ->
                appendLog("✓ ${Gson().toJson(note)}")
            }
        }
    }

    /**
     * ObjectBoxLiveData 将 Query 结果桥接为生命周期感知的 LiveData。
     */
    private fun observeNotes() {
        val query = noteQuery
        if (query == null) {
            appendLog("✗ Box Query 未初始化")
            return
        }
        appendLog("→ 订阅 ObjectBoxLiveData 数据变更...")
        ObjectBoxLiveData(query).observe(this) { notes ->
            appendLog("✓ [LiveData] Note 变更，当前共 ${notes.size} 条")
        }
    }

    private fun clearNotes() {
        appendLog("→ 清空 Note...")
        notesBox.removeAll()
        appendLog("✓ 已清空所有 Note 数据")
    }
}
