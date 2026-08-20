package com.example.william.my.module.sample.activity

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.sample.R

/**
 * Typeface — 字体加载与切换
 *
 * Typeface 是 Android 的字体管理类，用于加载和切换字体样式。
 *
 * 核心特性：
 * 1. 系统内置字体：DEFAULT、DEFAULT_BOLD、MONOSPACE、SERIF、SANS_SERIF
 * 2. 自定义字体：支持从 assets、resources、文件加载字体
 * 3. 字体样式：支持粗体、斜体、粗斜体等样式组合
 * 4. 性能优化：字体文件会被缓存，避免重复加载
 */
@Route(path = RouterPath.Sample.Typeface)
class TypefaceActivity : BasicLayoutActivity() {

    private lateinit var nameView: TextView
    private lateinit var enView: TextView
    private lateinit var zhView: TextView
    private lateinit var numView: TextView

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initPreviewCard()
    }

    private fun initPreviewCard() {
        val previewView = LayoutInflater.from(this)
            .inflate(R.layout.sample_layout_typeface_preview, mContainer, false)
        nameView = previewView.findViewById(R.id.sample_typeface_name)
        enView = previewView.findViewById(R.id.sample_typeface_en)
        zhView = previewView.findViewById(R.id.sample_typeface_zh)
        numView = previewView.findViewById(R.id.sample_typeface_num)

        setView(previewView)
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "系统默认字体（Typeface.DEFAULT）",
            "系统粗体样式（Typeface.DEFAULT_BOLD）",
            "等宽字体（Typeface.MONOSPACE）",
            "衬线字体（Typeface.SERIF）",
            "无衬线字体（Typeface.SANS_SERIF）",
            "Asset 自定义字体（fonts/juice.ttf）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> applyTypeface("Typeface.DEFAULT", Typeface.DEFAULT)
            1 -> applyTypeface("Typeface.DEFAULT_BOLD", Typeface.DEFAULT_BOLD)
            2 -> applyTypeface("Typeface.MONOSPACE", Typeface.MONOSPACE)
            3 -> applyTypeface("Typeface.SERIF", Typeface.SERIF)
            4 -> applyTypeface("Typeface.SANS_SERIF", Typeface.SANS_SERIF)
            5 -> {
                try {
                    val customTypeface = Typeface.createFromAsset(assets, "fonts/juice.ttf")
                    applyTypeface("Asset 字体 (fonts/juice.ttf)", customTypeface)
                } catch (e: Exception) {
                    nameView.text = "字体加载失败: ${e.message}"
                }
            }
        }
    }

    private fun applyTypeface(name: String, typeface: Typeface) {
        nameView.text = "当前字体：$name"
        enView.typeface = typeface
        zhView.typeface = typeface
        numView.typeface = typeface
    }
}
