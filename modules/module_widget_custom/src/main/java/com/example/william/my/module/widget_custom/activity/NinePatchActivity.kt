package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.ninepatch.NinePatchHelper
import com.example.william.my.module.widget_custom.R

/**
 * 9-patch — 可拉伸图片
 *
 * 9-patch 是 Android 特有的图片格式，支持局部拉伸与内容安全边距。
 *
 * 核心特性：
 * 1. 局部拉伸：只拉伸指定区域，保持边角其他区域不变
 * 2. 内容区域：定义内容显示区域，自动适配不同尺寸
 * 3. 资源优化：减少图片资源数量，适配不同屏幕
 * 4. 性能优秀：系统原生支持
 */
@Route(path = RouterPath.WidgetCustom.NinePatch)
class NinePatchActivity : BasicLayoutActivity() {

    private lateinit var tipView: TextView
    private lateinit var bubbleView: TextView

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initPreviewCard()
    }

    private fun initPreviewCard() {
        val previewView = LayoutInflater.from(this)
            .inflate(R.layout.widget_layout_ninepatch_preview, mContainer, false)
        tipView = previewView.findViewById(R.id.widget_ninepatch_tip)
        bubbleView = previewView.findViewById(R.id.widget_ninepatch_bubble)

        setView(previewView)
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 加载 Asset .9 图片背景",
            "2. 加载 Network .9 图片背景",
            "3. 扩充文本长度（演示不失真自动拉伸）",
            "4. 恢复短文本内容",
            "5. 清空背景"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                NinePatchHelper.ninePatchChunk(this, bubbleView, Constants.Url_NinePatchAsset)
                tipView.text = "已加载 Asset .9 图片作为气泡背景"
            }

            1 -> {
                NinePatchHelper.ninePatchChunk(this, bubbleView, Constants.Url_NinePatchNetwork)
                tipView.text = "已加载 Network .9 图片作为气泡背景"
            }

            2 -> {
                bubbleView.text = "这是一段较长的动态文本内容，用于检验 9-Patch 图片在横向与纵向膨胀拉伸时，四个圆角保持清晰不失真的特性。"
                tipView.text = "文本已扩展，气泡平滑拉伸且边角完整"
            }

            3 -> {
                bubbleView.text = "Hello, 9-Patch!"
                tipView.text = "已恢复为短文本"
            }

            4 -> {
                bubbleView.background = null
                tipView.text = "已清空背景"
            }
        }
    }
}
