package com.example.william.my.module.widget.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget.databinding.UiActivityViewFlipperBinding

/**
 * ViewFlipper — View 切换控件
 *
 * ViewFlipper 可以在多个子 View 之间切换，自带切换动画。
 *
 * 核心特性：
 * 1. 自动切换：支持自动切换子 View
 * 2. 切换动画：支持切入/切出动画
 * 3. 手动控制：支持手动切换到下一个/上一个 View
 * 4. 灵活配置：支持配置切换间隔、动画等
 *
 * 核心属性：
 * 1. flipInterval：切换间隔（毫秒），通过 setFlipInterval() 设置
 * 2. autoStart：是否自动开始翻转
 * 3. inAnimation / outAnimation：切入/切出动画，通过 XML 或代码设置
 * 4. startFlipping() / stopFlipping()：控制自动翻转
 * 5. showNext() / showPrevious()：手动切换到下一个/上一个 View
 *
 * 基本用法：
 * ```kotlin
 * // XML 中使用
 * <ViewFlipper
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     android:flipInterval="3000"
 *     android:autoStart="true">
 *     <ImageView ... />
 *     <ImageView ... />
 * </ViewFlipper>
 *
 * // 代码中控制
 * viewFlipper.startFlipping()
 * viewFlipper.stopFlipping()
 * viewFlipper.showNext()
 * ```
 *
 * 适用场景：
 * - 图片轮播
 * - 广告轮播
 * - 自动切换的场景
 */
@Route(path = RouterPath.Widget.ViewFlipper)
class ViewFlipperActivity : BaseVBActivity<UiActivityViewFlipperBinding>() {

    override fun getViewBinding(): UiActivityViewFlipperBinding {
        return UiActivityViewFlipperBinding.inflate(layoutInflater)
    }

    /**
     * 在 onStart 中启动翻转，onStop 中停止
     * 这样在 Activity 不可见时不会浪费资源
     */
    override fun onStart() {
        super.onStart()
        mBinding.viewFlipper.startFlipping()
    }

    override fun onStop() {
        super.onStop()
        mBinding.viewFlipper.stopFlipping()
    }
}