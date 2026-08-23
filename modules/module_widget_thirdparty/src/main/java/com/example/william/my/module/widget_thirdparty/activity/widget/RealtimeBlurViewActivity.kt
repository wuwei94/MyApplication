package com.example.william.my.module.widget_thirdparty.activity.widget

import android.os.Bundle
import android.util.TypedValue
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.R
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityBlurViewBinding

/**
 * RealtimeBlurView — 实时高斯模糊控件
 *
 * RealtimeBlurView 是一个轻量级的高斯模糊库，类似 iOS 的 UIVisualEffectView。
 *
 * 核心特性：
 * 1. 实时模糊：动态模糊背景内容，支持实时更新
 * 2. 高性能：使用 RenderScript 加速，性能优秀
 * 3. 简单易用：一行代码实现毛玻璃效果
 * 4. 兼容性好：支持 Android 4.0+
 *
 * 基本用法：
 * ```kotlin
 * // XML 中使用
 * <com.github.mmin18.realtimeblurview.RealtimeBlurView
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:realtimeBlurRadius="10dp"
 *     app:realtimeDownsampleFactor="4" />
 *
 * // 代码中设置模糊半径
 * blurView.setBlurRadius(10f)
 * ```
 *
 * 适用场景：
 * - 毛玻璃效果、背景虚化
 * - 弹窗背景模糊
 * - 导航栏、状态栏模糊
 *
 * https://github.com/mmin18/RealtimeBlurView
 */
@Route(path = RouterPath.WidgetThirdparty.RealtimeBlurView)
class RealtimeBlurViewActivity : BaseVBActivity<WidgetThirdpartyActivityBlurViewBinding>() {

    override fun getViewBinding(): WidgetThirdpartyActivityBlurViewBinding {
        return WidgetThirdpartyActivityBlurViewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_thirdparty_activity_blur_view)

        mBinding.realtimeBlurView.setBlurRadius(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10f,
                getResources().displayMetrics
            )
        )
    }
}
