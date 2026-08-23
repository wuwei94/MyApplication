package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget_custom.R
import com.example.william.my.module.widget_custom.databinding.DemoActivityMarqueeViewBinding

/**
 * MarqueeView — 跑马灯控件
 *
 * 跑马灯控件，支持垂直滚动的公告、消息展示。
 *
 * 核心特性：
 * 1. 垂直滚动：支持垂直方向的滚动动画
 * 2. 自动轮播：支持自动轮播，可设置间隔时间
 * 3. 自定义布局：支持自定义每项的布局样式
 * 4. 点击事件：支持每项的点击事件
 *
 * 基本用法：
 * ```kotlin
 * // XML 中使用
 * <com.example.widget.MarqueeView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:marquee_interval="3000" />
 *
 * // 代码中设置数据
 * marqueeView.setViews(viewList)
 * marqueeView.startFlipping()
 * ```
 *
 * 适用场景：
 * - 公告通知、消息滚动
 * - 商品推荐、活动宣传
 * - 任何需要垂直滚动的场景
 */
@Route(path = RouterPath.WidgetCustom.MarqueeView)
class MarqueeViewActivity : BaseVBActivity<DemoActivityMarqueeViewBinding>() {

    override fun getViewBinding(): DemoActivityMarqueeViewBinding {
        return DemoActivityMarqueeViewBinding.inflate(layoutInflater)
    }

    private val mData = arrayListOf("第一条数据", "第二条数据", "第三条数据", "第四条数据")
    private val marqueeViews = mutableListOf<View>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initMarqueeView()
    }

    private fun initMarqueeView() {
        var i = 0
        while (i < mData.size) {
            //设置滚动的单个布局
            val viewGroup = layoutInflater.inflate(
                R.layout.demo_item_marquee_view,
                window.decorView as ViewGroup,
                false
            ) as LinearLayout
            //初始化布局的控件
            val textView1 = viewGroup.findViewById<TextView>(R.id.item_marquee_primary)
            val textView2 = viewGroup.findViewById<TextView>(R.id.item_marquee_accent)
            //进行对控件赋值
            textView1.text = mData[i]
            if (mData.size > i + 1) {
                //因为淘宝那儿是两条数据，但是当数据是奇数时就不需要赋值第二个，所以加了一个判断，还应该把第二个布局给隐藏掉
                textView2.text = mData[i + 1]
            } else {
                textView2.visibility = View.GONE
            }
            viewGroup.gravity = Gravity.CENTER
            //添加到循环滚动数组里面去
            marqueeViews.add(viewGroup)
            i += 2
        }
        mBinding.marqueeView.setViews(marqueeViews)
    }
}