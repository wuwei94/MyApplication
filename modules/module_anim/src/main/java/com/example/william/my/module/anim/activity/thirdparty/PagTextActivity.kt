package com.example.william.my.module.anim.activity.thirdparty

import android.os.Bundle
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.anim.R
import org.libpag.PAGFile
import org.libpag.PAGScaleMode
import org.libpag.PAGText
import org.libpag.PAGView

/**
 * PAG 替换文字 — PAGText 的读写回灌
 *
 * 核心机制与避坑点：
 * 1. 三步走：`getTextData(i)` 取副本 → 改 PAGText 字段 → `replaceText(i, data)` 回写。
 *    直接改 `getTextData()` 的返回值不会影响画面，这是最常见的踩坑点。
 * 2. 回灌后要刷新：`replaceText` 只改 PAGFile 数据，需重新 `setComposition()` 才会重建渲染内容。
 * 3. 取回的是素材原文：`getTextData(i)` 读的是文件里存的原始文案，**不含上一次 `replaceText` 的结果**。
 *    所以改样式（fillColor / fontSize 等）时不能直接拿它当底稿，否则会把刚改好的文案一起冲回去。
 *    页面用 [textOverrides] 留一份当前文案，每次改写先补回再叠加本次改动，文案与样式才能叠加生效。
 * 4. 槽位序号来自导出侧：`i` 是 PAGExporter 勾选「可编辑」的顺序，与图层树顺序无关；
 *    槽位数量取自 `numTexts()`，为 0 时 `replaceText` 静默失败，页面必须先判数量。
 * 5. 分槽独立：改槽 0 不动槽 1，反之亦然——文本槽是各自独立的 PAGText 对象，互不干扰。
 * 6. 字体回退：素材内 fontFamily 是设计侧字体（如 DFPLiJinHeiW8-GB），设备上没有该字体时
 *    PAG 会回退到默认字体，因此改写后的字形可能与设计稿不同——这是字体缺失，不是 API 用错。
 *
 * 素材 1334×750，槽 0 为主标题，槽 1 为副标题。
 *
 * 官方参考：
 * https://pag.io/docs/en/editable-text.html
 */
@Route(path = RouterPath.Anim.PagText)
class PagTextActivity : BasicLayoutActivity() {

    private lateinit var pagView: PAGView

    /** 原始文件，仅用于 copyOriginal() 取干净副本做还原 */
    private var baseFile: PAGFile? = null

    /** 当前展示的文件，所有替换都作用在它身上 */
    private var workingFile: PAGFile? = null

    private var fillColorIndex = 0

    /**
     * 各槽最近一次改写后的文案。
     *
     * `getTextData()` 取回的是素材原文，不含上一次 `replaceText` 的结果，
     * 改样式时拿它当底稿会把已改的文案冲掉，所以这里留一份当前文案。
     */
    private val textOverrides = mutableMapOf<Int, String>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val stage = layoutInflater.inflate(R.layout.anim_layout_pag_lab, container, false)
        pagView = stage.findViewById(R.id.anim_pag_view_player)
        setView(stage)
        loadComposition()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 改主标题（槽 0）· 副标题不动",
        "2. 改副标题（槽 1）· 主标题不动",
        "3. 改文字颜色",
        "4. 还原初始文案",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> applyText(SLOT_TITLE) { it.text = TITLE_TEXT }
            1 -> applyText(SLOT_SUBTITLE) { it.text = SUBTITLE_TEXT }
            2 -> rotateFillColor()
            3 -> restore()
        }
    }

    private fun loadComposition() {
        val file = PAGFile.Load(assets, Constants.Pag_Text)
        if (file == null) {
            toast("PAGFile.Load 失败：${Constants.Pag_Text}")
            return
        }
        baseFile = file
        workingFile = file
        pagView.setComposition(file)
        pagView.setScaleMode(PAGScaleMode.LetterBox)
        pagView.setRepeatCount(REPEAT_INFINITE)
        pagView.play()
    }

    /**
     * 轮换标题填充色，验证 PAGText 的样式字段同样可写。
     */
    private fun rotateFillColor() {
        fillColorIndex = (fillColorIndex + 1) % FILL_COLORS.size
        applyText(SLOT_TITLE) { it.fillColor = FILL_COLORS[fillColorIndex] }
    }

    /**
     * 取槽位副本、应用改动、回写并刷新画面。槽位不可用时提示后放弃本次改写。
     */
    private fun applyText(index: Int, block: (PAGText) -> Unit) {
        val file = workingFile ?: return
        if (file.numTexts() <= index) {
            toast("该素材只有 ${file.numTexts()} 个文本槽，槽 $index 不存在")
            return
        }
        val data = file.getTextData(index)
        if (data == null) {
            toast("getTextData($index) 返回 null")
            return
        }
        textOverrides[index]?.let { data.text = it }
        block(data)
        textOverrides[index] = data.text
        file.replaceText(index, data)
        pagView.setComposition(file)
        pagView.play()
    }

    /**
     * 用 `copyOriginal()` 拿一份未修改的副本，避免改动累积。
     */
    private fun restore() {
        val fresh = baseFile?.copyOriginal()
        if (fresh == null) {
            toast("copyOriginal() 返回 null")
            return
        }
        workingFile = fresh
        textOverrides.clear()
        pagView.setComposition(fresh)
        pagView.play()
        fillColorIndex = 0
    }

    override fun onDestroy() {
        pagView.pause()
        pagView.freeCache()
        super.onDestroy()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        private const val SLOT_TITLE = 0
        private const val SLOT_SUBTITLE = 1
        private const val REPEAT_INFINITE = -1

        private const val TITLE_TEXT = "周末去哪儿"

        private const val SUBTITLE_TEXT = "WEEKEND TRIP"

        private val FILL_COLORS = intArrayOf(
            0xFFFF4A26.toInt(),
            0xFF1EA7FD.toInt(),
            0xFFFFFFFF.toInt(),
        )
    }
}
