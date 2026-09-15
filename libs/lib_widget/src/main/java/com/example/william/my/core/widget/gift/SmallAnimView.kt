package com.example.william.my.core.widget.gift

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.blankj.utilcode.util.ScreenUtils
import com.example.william.my.core.widget.databinding.AnimItemBinding

/**
 * 礼物小动画视图（位移动画）
 */
class SmallAnimView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var listener: Animator.AnimatorListener? = null
    private val binding =
        AnimItemBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun setImageUrl(imageUrl: String?) {
    }

    fun addListener(listener: Animator.AnimatorListener?) {
        this.listener = listener
    }

    fun play() {
        val animatorSet = AnimatorSet()
        val translationIn = ObjectAnimator.ofFloat(
            this,
            "translationX",
            ScreenUtils.getScreenWidth().toFloat(),
            0f,
        )
        translationIn.setDuration(1200)
        val translationOut = ObjectAnimator.ofFloat(
            this,
            "translationX",
            0f,
            -ScreenUtils.getScreenWidth().toFloat(),
        )
        translationOut.setDuration(1200)
        val translationWait = ObjectAnimator.ofFloat(
            this,
            "translationX",
            0f,
            0f,
        )
        translationWait.setDuration(6000)
        animatorSet.playSequentially(translationIn, translationWait, translationOut)
        animatorSet.addListener(listener)
        animatorSet.start()
    }
}
