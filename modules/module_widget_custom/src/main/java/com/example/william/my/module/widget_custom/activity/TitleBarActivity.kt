package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.widget.titlebar.TitleBar

/**
 * TitleBar — 通用顶部导航栏封装
 *
 * 展示项目自定义 TitleBar 控件的动态配置能力。TitleBar 封装了常用的顶部导航栏功能，
 * 通过链式调用 API 实现标题、返回键、右侧菜单和背景色的灵活配置。
 *
 * 核心机制与避坑点：
 * 1. 居中标题与标题颜色定制
 * 2. 左侧返回键：支持文字 / 图标 / 隐藏 / 返回拦截
 * 3. 右侧功能按钮：支持文字 / 图标与独立点击回调
 * 4. 背景主题色动态切换
 * 5. 布局查找初始化：TitleBar.build(rootView) 从布局中查找并绑定
 */
@Route(path = RouterPath.WidgetCustom.TitleBar)
class TitleBarActivity : BasicResponseActivity() {

    private var titleBar: TitleBar? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示自定义 TitleBar 动态配置与事件回调")
        initTitleBar()
    }

    private fun initTitleBar() {
        titleBar = TitleBar.build(binding.root)
        titleBar?.setTitle("TitleBar 演示")
        titleBar?.setBackPressed("返回")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "设置主标题与返回键文案",
        "添加右侧文字菜单（更多）",
        "修改 TitleBar 背景色",
        "隐藏返回按钮",
        "重置 TitleBar 默认状态",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                titleBar?.setTitle("自定义主标题")
                titleBar?.setBackPressed("后退")
                appendLog("【TitleBar】已更新标题为「自定义主标题」，返回键为「后退」")
            }

            1 -> {
                titleBar?.setBtnRight("更多") {
                    appendLog("【TitleBar】点击了右侧「更多」操作菜单")
                }
                appendLog("【TitleBar】已添加右侧文字菜单「更多」")
            }

            2 -> {
                titleBar?.setToolBarColor(R.color.shared_color_primary)
                appendLog("【TitleBar】已修改 TitleBar 背景色为 PrimaryColor")
            }

            3 -> {
                titleBar?.setBackPressed(false)
                appendLog("【TitleBar】已隐藏左侧返回按钮")
            }

            4 -> {
                titleBar?.setTitle("TitleBar 演示")
                titleBar?.setBackPressed("返回")
                titleBar?.setToolBarColor(android.R.color.transparent)
                appendLog("【TitleBar】已重置为默认状态")
            }
        }
    }
}
