package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_custom.R
import com.example.william.my.module.widget_custom.databinding.DemoActivityMarqueeViewBinding

/**
 * MarqueeView — 跑马灯控件
 *
 * 跑马灯控件，支持垂直滚动的公告、消息展示。
 *
 * 核心机制与避坑点：
 * 1. 垂直滚动：支持垂直方向的滚动动画
 * 2. 自动轮播：支持自动轮播，可设置间隔时间
 * 3. 自定义布局：支持自定义每项的布局样式
 * 4. 点击事件：支持每项的点击事件
 */
@Route(path = RouterPath.WidgetCustom.MarqueeView)
class MarqueeViewActivity : BaseVBActivity<DemoActivityMarqueeViewBinding>() {

    override fun getViewBinding(): DemoActivityMarqueeViewBinding = DemoActivityMarqueeViewBinding.inflate(layoutInflater)

    private val data = arrayListOf("第一条数据", "第二条数据", "第三条数据", "第四条数据")
    private val marqueeViews = mutableListOf<View>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initMarqueeView()
    }

    private fun initMarqueeView() {
        var i = 0
        while (i < data.size) {
            val viewGroup = layoutInflater.inflate(
                R.layout.demo_item_marquee_view,
                window.decorView as ViewGroup,
                false,
            ) as LinearLayout
            val textView1 = viewGroup.findViewById<TextView>(R.id.item_marquee_primary)
            val textView2 = viewGroup.findViewById<TextView>(R.id.item_marquee_accent)
            // 进行对控件赋值
            textView1.text = data[i]
            if (data.size > i + 1) {
                // 因为淘宝那儿是两条数据，但是当数据是奇数时就不需要赋值第二个，所以加了一个判断，还应该把第二个布局给隐藏掉
                textView2.text = data[i + 1]
            } else {
                textView2.visibility = View.GONE
            }
            viewGroup.gravity = Gravity.CENTER
            // 添加到循环滚动数组里面去
            marqueeViews.add(viewGroup)
            i += 2
        }
        binding.marqueeView.setViews(marqueeViews)
    }
}
