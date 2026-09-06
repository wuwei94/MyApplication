package com.example.william.my.core.widget.gift

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.RelativeLayout
import com.blankj.utilcode.util.AdaptScreenUtils
import org.libpag.PAGScaleMode
import org.libpag.PAGView
import org.libpag.PAGView.PAGViewListener
import java.util.concurrent.LinkedBlockingQueue

/**
 * 礼物动画容器控件（PAG 大动画与小动画队列播放）
 */
class GiftLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr),
    PAGViewListener,
    Animator.AnimatorListener {
    private val TAG = this.javaClass.simpleName

    private var isPlaying = false

    private val mPagView: PAGView
    private val mSmallAnimView: SmallAnimView

    private val mGiftMsgBodyQueue = LinkedBlockingQueue<String>()

    init {
        this.mPagView = PAGView(context, attrs, defStyleAttr)
        mPagView.visibility = GONE
        mPagView.setRepeatCount(1)
        mPagView.setScaleMode(PAGScaleMode.Zoom)
        mPagView.addListener(this)
        val mPagViewParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(mPagView, mPagViewParams)

        this.mSmallAnimView = SmallAnimView(context, attrs, defStyleAttr)
        mSmallAnimView.visibility = GONE
        mSmallAnimView.addListener(this)
        val mallAnimViewParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        mallAnimViewParams.topMargin = AdaptScreenUtils.pt2Px(60f)
        addView(mSmallAnimView, mallAnimViewParams)
    }

    private fun notifyGiftMsg() {
        println("notifyGiftMsg 通知播放动画")

        if (!isPlaying && !mGiftMsgBodyQueue.isEmpty()) {
            val nextMessage = mGiftMsgBodyQueue.poll()
        }
    }

    override fun onAnimationStart(pagView: PAGView) {
        println("onAnimationStart")
        mPagView.visibility = VISIBLE
        isPlaying = true
    }

    override fun onAnimationEnd(pagView: PAGView) {
        println("onAnimationEnd")
        mPagView.visibility = GONE
        isPlaying = false
        notifyGiftMsg()
    }

    override fun onAnimationCancel(pagView: PAGView) {
    }

    override fun onAnimationRepeat(pagView: PAGView) {
    }

    override fun onAnimationUpdate(pagView: PAGView) {
    }

    override fun onAnimationStart(animation: Animator) {
        println("onAnimationStart")
        mSmallAnimView.visibility = VISIBLE
        isPlaying = true
    }

    override fun onAnimationEnd(animation: Animator) {
        println("onAnimationEnd")
        mSmallAnimView.visibility = GONE
        isPlaying = false
        notifyGiftMsg()
    }

    override fun onAnimationCancel(animation: Animator) {
    }

    override fun onAnimationRepeat(animation: Animator) {
    }

    private fun println(msg: String) {
        Log.e(TAG, msg)
    }
}
