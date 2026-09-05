package com.example.william.my.module.feature.activity

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.feature.databinding.FeatureActivityMicAnimationBinding
import com.example.william.my.module.feature.layoutmanager.MicLayoutManager

/**
 * 麦位动画 — 自定义 LayoutManager 切换动画演示
 *
 * 使用 FLIP (First, Last, Invert, Play) 技术实现自定义 LayoutManager 切换动画。
 * 演示 RecyclerView 布局切换时的流畅动画效果。
 *
 * FLIP 技术原理：
 * 1. First：记录每个子 View 当前在屏幕上的实际视觉位置
 * 2. Last：切换 LayoutManager 布局模式并触发重新布局
 * 3. Invert：获取子 View 新位置，通过 translation 反向偏移回旧位置
 * 4. Play：启动属性动画将 translation 平滑过渡回 0f
 *
 * 适用场景：
 * - 自定义 LayoutManager 切换动画
 * - 列表布局模式切换（如网格 ↔ 列表）
 * - 需要流畅过渡的布局变化
 */
@Route(path = RouterPath.Feature.MicAnimation)
class MicAnimationActivity : BaseVBActivity<FeatureActivityMicAnimationBinding>() {

    private lateinit var micLayoutManager: MicLayoutManager

    private val colors = intArrayOf(
        "#E57373".toColorInt(),
        "#81C784".toColorInt(),
        "#64B5F6".toColorInt(),
        "#FFD54F".toColorInt(),
        "#BA68C8".toColorInt(),
        "#4DB6AC".toColorInt(),
        "#FF8A65".toColorInt(),
        "#A1887F".toColorInt(),
    )

    override fun getViewBinding(): FeatureActivityMicAnimationBinding = FeatureActivityMicAnimationBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        micLayoutManager = MicLayoutManager()
        mBinding.recyclerView.layoutManager = micLayoutManager
        mBinding.recyclerView.adapter = MicAdapter()

        mBinding.btnModeA.setOnClickListener {
            switchLayout(MicLayoutManager.LayoutMode.MODE_A)
        }

        mBinding.btnModeB.setOnClickListener {
            switchLayout(MicLayoutManager.LayoutMode.MODE_B)
        }
    }

    /**
     * 使用 FLIP (First, Last, Invert, Play) 技术实现自定义 LayoutManager 切换动画：
     * 1. First（初始态）：记录每个子 View 当前在屏幕上的实际视觉位置（包含未完成动画的 translation 偏移量）。
     * 2. Last（最终态）：切换 LayoutManager 布局模式并请求重新测量布局（requestLayout）。
     * 3. Invert（反转）：在布局完成后的 rv.post 回调中，获取子 View 新的物理位置，通过 translation 反向偏移回旧位置。
     * 4. Play（播放）：启动属性动画将 translation 平滑过渡回 0f，呈现流畅的位置移动动画。
     */
    private fun switchLayout(mode: MicLayoutManager.LayoutMode) {
        val rv = mBinding.recyclerView

        // 1. First: 记录旧视觉坐标（累加当前 translation 防止动画打断跳帧），并取消上一轮未完成的动画
        val oldPositions = mutableMapOf<Int, Pair<Float, Float>>()
        for (i in 0 until rv.childCount) {
            val child = rv.getChildAt(i)
            val pos = rv.getChildAdapterPosition(child)
            val currentVisualX = child.left + child.translationX
            val currentVisualY = child.top + child.translationY
            oldPositions[pos] = Pair(currentVisualX, currentVisualY)
            child.animate().cancel()
        }

        // 2. Last: 切换布局模式并触发 requestLayout()
        if (!micLayoutManager.switchMode(mode)) return

        // 3. Invert & Play: 在新布局完成后执行反向偏移与平滑动画
        rv.post {
            for (i in 0 until rv.childCount) {
                val child = rv.getChildAt(i)
                val pos = rv.getChildAdapterPosition(child)
                val old = oldPositions[pos] ?: continue
                val dx = old.first - child.left
                val dy = old.second - child.top
                child.translationX = dx
                child.translationY = dy
                child.animate()
                    .translationX(0f)
                    .translationY(0f)
                    .setDuration(350)
                    .setStartDelay((i * 40).toLong())
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
    }

    private inner class MicAdapter : RecyclerView.Adapter<MicAdapter.VH>() {

        inner class VH(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val tv = TextView(parent.context).apply {
                textSize = 16f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            }
            return VH(tv)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.textView.text = "麦位${position + 1}"
            holder.textView.setBackgroundColor(colors[position])
        }

        override fun getItemCount(): Int = 8
    }
}
