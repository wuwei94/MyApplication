package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.widget.titlebar.TitleBar

/**
 * 自定义 TitleBar 控件 — 动态配置与事件回调演示
 *
 * 展示项目自定义 TitleBar 控件的动态配置能力。TitleBar 封装了常用的顶部导航栏功能，
 * 通过链式调用 API 实现标题、返回键、右侧菜单和背景色的灵活配置。
 *
 * 核心能力：
 * 1. 居中标题与标题颜色定制
 * 2. 左侧返回键（支持文字 / 图标 / 隐藏 / 返回拦截）
 * 3. 右侧功能按钮（支持文字 / 图标与独立点击回调）
 * 4. 背景主题色动态切换
 * 5. 通过 TitleBar.build(rootView) 从布局中查找并初始化
 *
 * 适用场景：
 * - 需要统一顶部导航栏风格的页面
 * - 需要动态切换标题、菜单、背景色的交互场景
 * - 封装通用导航栏组件供多模块复用
 */
@Route(path = RouterPath.WidgetCustom.TitleBar)
class TitleBarActivity : BasicResponseActivity() {

    private var mTitleBar: TitleBar? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示自定义 TitleBar 动态配置与事件回调")
        initTitleBar()
    }

    private fun initTitleBar() {
        mTitleBar = TitleBar.build(mBinding.root)
        mTitleBar?.setTitle("TitleBar 演示")
        mTitleBar?.setBackPressed("返回")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "设置主标题与返回键文案",
            "添加右侧文字菜单（更多）",
            "修改 TitleBar 背景色",
            "隐藏返回按钮",
            "重置 TitleBar 默认状态"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                mTitleBar?.setTitle("自定义主标题")
                mTitleBar?.setBackPressed("后退")
                appendLog("【TitleBar】已更新标题为「自定义主标题」，返回键为「后退」")
            }

            1 -> {
                mTitleBar?.setBtnRight("更多") {
                    appendLog("【TitleBar】点击了右侧「更多」操作菜单")
                }
                appendLog("【TitleBar】已添加右侧文字菜单「更多」")
            }

            2 -> {
                mTitleBar?.setToolBarColor(R.color.shared_color_primary)
                appendLog("【TitleBar】已修改 TitleBar 背景色为 PrimaryColor")
            }

            3 -> {
                mTitleBar?.setBackPressed(false)
                appendLog("【TitleBar】已隐藏左侧返回按钮")
            }

            4 -> {
                mTitleBar?.setTitle("TitleBar 演示")
                mTitleBar?.setBackPressed("返回")
                mTitleBar?.setToolBarColor(android.R.color.transparent)
                appendLog("【TitleBar】已重置为默认状态")
            }
        }
    }
}