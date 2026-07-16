package com.example.william.my.module.ui.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.ui.databinding.UiActivityViewFlipperBinding

/**
 * ViewFlipper 可以在多个子 View 之间切换，自带切换动画
 * - flipInterval: 切换间隔（毫秒），通过 setFlipInterval() 设置
 * - autoStart: 是否自动开始翻转
 * - inAnimation / outAnimation: 切入/切出动画，通过 XML 或代码设置
 * - startFlipping() / stopFlipping(): 控制自动翻转
 * - showNext() / showPrevious(): 手动切换到下一个/上一个 View
 */
@Route(path = RouterPath.UI.ViewFlipper)
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