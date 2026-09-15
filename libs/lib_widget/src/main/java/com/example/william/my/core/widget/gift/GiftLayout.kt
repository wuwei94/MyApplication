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

    private val pagView: PAGView
    private val smallAnimView: SmallAnimView

    private val giftMsgBodyQueue = LinkedBlockingQueue<String>()

    init {
        this.pagView = PAGView(context, attrs, defStyleAttr)
        pagView.visibility = GONE
        pagView.setRepeatCount(1)
        pagView.setScaleMode(PAGScaleMode.Zoom)
        pagView.addListener(this)
        val pagViewParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(pagView, pagViewParams)

        this.smallAnimView = SmallAnimView(context, attrs, defStyleAttr)
        smallAnimView.visibility = GONE
        smallAnimView.addListener(this)
        val mallAnimViewParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        mallAnimViewParams.topMargin = AdaptScreenUtils.pt2Px(60f)
        addView(smallAnimView, mallAnimViewParams)
    }

    private fun notifyGiftMsg() {
        println("notifyGiftMsg 通知播放动画")

        if (!isPlaying && !giftMsgBodyQueue.isEmpty()) {
            val nextMessage = giftMsgBodyQueue.poll()
        }
    }

    override fun onAnimationStart(pagView: PAGView) {
        println("onAnimationStart")
        this.pagView.visibility = VISIBLE
        isPlaying = true
    }

    override fun onAnimationEnd(pagView: PAGView) {
        println("onAnimationEnd")
        this.pagView.visibility = GONE
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
        smallAnimView.visibility = VISIBLE
        isPlaying = true
    }

    override fun onAnimationEnd(animation: Animator) {
        println("onAnimationEnd")
        smallAnimView.visibility = GONE
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
