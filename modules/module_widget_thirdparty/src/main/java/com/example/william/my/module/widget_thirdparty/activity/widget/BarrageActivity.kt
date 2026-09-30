package com.example.william.my.module.widget_thirdparty.activity.widget

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.core.widget.barrage.Direction
import com.example.william.my.core.widget.barrage.widget.DMTextureView
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityBarrageBinding

/**
 * Barrage — Surface/TextureView 弹幕引擎
 *
 * 弹幕引擎源自 xujiaji/Barrage（Apache-2.0），迁入 lib_widget.barrage。
 * 将任意 View 绘制为 Bitmap 后，在独立 DrawThread 上按轨道平移，透明叠在内容之上。
 *
 * 核心机制与避坑点：
 * 1. View → Bitmap 后台绘制：Controller.add() 在线程池 measure/layout/draw，
 *    弹幕条目不可持有 Activity 强引用，避免旋转/退出后泄漏
 * 2. 轨道防重叠：按 vSpace/hSpace 分轨道插入，同道弹幕自动错位，不会追尾重叠
 * 3. Surface 生命周期：DMTextureView 在 onSurfaceTextureDestroyed 会 destroy Controller，
 *    旋转屏幕后需重新添加；getController() 在 Surface 就绪前为 null，必须判空
 * 4. 必须 destroy：onDetachedFromWindow / destroy() 终止 DrawThread，否则线程与 Bitmap 泄漏
 *
 * https://github.com/xujiaji/Barrage
 */
@Route(path = RouterPath.WidgetThirdparty.Barrage)
class BarrageActivity : BaseVBActivity<WidgetThirdpartyActivityBarrageBinding>() {

    private var sendCount = 0
    private var isPaused = false
    private var directionIndex = 0

    private val directions = arrayOf(
        Direction.RIGHT_LEFT to "右→左",
        Direction.LEFT_RIGHT to "左→右",
        Direction.UP_DOWN to "上→下",
        Direction.DOWN_UP to "下→上",
    )

    private val sampleTexts = arrayOf(
        "这弹幕效果太丝滑了",
        "SurfaceView / TextureView 双实现",
        "轨道自动防重叠",
        "View 转 Bitmap 独立线程绘制",
        "支持四个方向运动",
        "Hello Barrage!",
        "直播弹幕就是这么玩的",
        "Controller 负责轨迹与排布",
    )

    private val colors = intArrayOf(
        0xFFFF6B6B.toInt(),
        0xFF4ECDC4.toInt(),
        0xFFFFE66D.toInt(),
        0xFF95E1D3.toInt(),
        0xFFF38181.toInt(),
        0xFFAA96DA.toInt(),
        0xFF6C5CE7.toInt(),
        0xFF00B894.toInt(),
    )

    override fun getViewBinding(): WidgetThirdpartyActivityBarrageBinding = WidgetThirdpartyActivityBarrageBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.barrageTextureView.setOnDMReadyListener(object : DMTextureView.OnDMReadyListener {
            override fun onReady() {
                runOnUiThread {
                    binding.barrageStatusText.text = "引擎就绪 · 方向：${directions[directionIndex].second}"
                    sendBarrage("欢迎体验弹幕引擎")
                }
            }
        })

        binding.barrageBtnSend.setOnClickListener { sendBarrage() }
        binding.barrageBtnBurst.setOnClickListener { sendBurst() }
        binding.barrageBtnPause.setOnClickListener { togglePause() }
        binding.barrageBtnClear.setOnClickListener { clearBarrage() }
        binding.barrageBtnDirection.setOnClickListener { switchDirection() }
    }

    private fun sendBarrage(text: String? = null) {
        val controller = binding.barrageTextureView.controller
        if (controller == null) {
            binding.barrageStatusText.text = "Surface 未就绪，请稍候"
            return
        }
        val content = text ?: sampleTexts[sendCount % sampleTexts.size]
        val color = colors[sendCount % colors.size]
        sendCount++
        controller.add(createBarrageView(content, color))
        binding.barrageStatusText.text = "已发送 $sendCount 条 · 方向：${directions[directionIndex].second}"
    }

    private fun sendBurst() {
        repeat(8) { i ->
            binding.barrageTextureView.postDelayed({ sendBarrage() }, i * 120L)
        }
    }

    private fun togglePause() {
        val controller = binding.barrageTextureView.controller ?: return
        isPaused = !isPaused
        if (isPaused) {
            controller.pause()
            binding.barrageBtnPause.text = "继续"
            binding.barrageStatusText.text = "已暂停 · 点击「继续」恢复运动"
        } else {
            controller.resume()
            binding.barrageBtnPause.text = "暂停"
            binding.barrageStatusText.text = "已恢复 · 方向：${directions[directionIndex].second}"
        }
    }

    private fun clearBarrage() {
        binding.barrageTextureView.controller?.clean()
        sendCount = 0
        binding.barrageStatusText.text = "已清空 · 方向：${directions[directionIndex].second}"
    }

    private fun switchDirection() {
        directionIndex = (directionIndex + 1) % directions.size
        val (direction, label) = directions[directionIndex]
        val controller = binding.barrageTextureView.controller ?: return
        // 方向变更：清空在途弹幕后重置轨迹起点，新弹幕按新方向运动
        controller.clean()
        controller.setDirection(direction)
        controller.initOffset()
        binding.barrageDirectionOverlay.text = "方向：$label"
        binding.barrageDirectionOverlay.visibility = View.VISIBLE
        binding.barrageDirectionOverlay.postDelayed({
            binding.barrageDirectionOverlay.visibility = View.GONE
        }, 800)
        binding.barrageStatusText.text = "方向切换为 $label"
        sendBarrage("方向：$label")
    }

    /**
     * 构建单条弹幕 View（圆角气泡 + 文案），由 Controller 在后台线程转 Bitmap。
     */
    private fun createBarrageView(text: String, color: Int): View {
        val context = binding.root.context
        val paddingH = dp(12)
        val paddingV = dp(6)
        val textView = TextView(context).apply {
            this.text = text
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(paddingH, paddingV, paddingH, paddingV)
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(color)
                alpha = 200
            }
        }
        return textView
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    override fun onDestroy() {
        binding.barrageTextureView.controller?.destroy()
        super.onDestroy()
    }
}
