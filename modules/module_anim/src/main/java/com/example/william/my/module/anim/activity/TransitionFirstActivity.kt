package com.example.william.my.module.anim.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.william.my.basic.basic_module.router.path.RouterPath
import com.example.william.my.lib.activity.BaseVBActivity
import com.example.william.my.module.anim.R
import com.example.william.my.module.anim.databinding.AnimActivityTransitionFirstBinding

/**
 * Activity 跳转过渡动画 — 选择页
 *
 * 演示 Activity 之间的窗口过渡效果（Window Transitions）。
 * 通过 Intent extra 传递过渡类型，在目标 Activity 中设置 window.enterTransition。
 *
 * 四种过渡方式：
 * - Explode — 元素从屏幕四周爆炸散开
 * - Slide — 内容从边缘滑入
 * - Fade — 内容淡入
 * - Shared Element — 共享元素过渡（View 到 View 的连续动画）
 */
@Route(path = RouterPath.Anim.Transition)
class TransitionFirstActivity : BaseVBActivity<AnimActivityTransitionFirstBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): AnimActivityTransitionFirstBinding {
        return AnimActivityTransitionFirstBinding.inflate(layoutInflater)
    }

    private lateinit var mIntent: Intent

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mIntent = Intent(this, TransitionSecondActivity::class.java)

        mBinding.transitionExplode.setOnClickListener(this)
        mBinding.transitionSlide.setOnClickListener(this)
        mBinding.transitionFade.setOnClickListener(this)
        mBinding.transitionShare.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.transition_explode,
            R.id.transition_slide,
            R.id.transition_fade -> {
                val type = when (v.id) {
                    R.id.transition_explode -> "explode"
                    R.id.transition_slide -> "slide"
                    else -> "fade"
                }
                mIntent.putExtra("transition", type)
                startActivity(
                    mIntent,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                )
            }

            R.id.transition_share -> {
                ARouter.getInstance()
                    .build(RouterPath.Anim.Transition2)
                    .withString("transition", "share")
                    .withOptionsCompat(
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            Pair(mBinding.transitionShare, "shareTransition")
                        )
                    )
                    .navigation(this)
            }
        }
    }
}
