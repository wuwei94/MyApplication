package com.example.william.my.module.features.activity

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.path.RouterPath
import com.example.william.my.lib.activity.BaseVBActivity
import com.example.william.my.module.features.databinding.FeaturesActivityMicAnimationBinding
import com.example.william.my.module.features.layoutmanager.MicLayoutManager

@Route(path = RouterPath.Features.Business.MicAnimation)
class MicAnimationActivity : BaseVBActivity<FeaturesActivityMicAnimationBinding>() {

    private lateinit var micLayoutManager: MicLayoutManager

    private val colors = intArrayOf(
        "#E57373".toColorInt(),
        "#81C784".toColorInt(),
        "#64B5F6".toColorInt(),
        "#FFD54F".toColorInt(),
        "#BA68C8".toColorInt(),
        "#4DB6AC".toColorInt(),
        "#FF8A65".toColorInt(),
        "#A1887F".toColorInt()
    )

    override fun getViewBinding(): FeaturesActivityMicAnimationBinding {
        return FeaturesActivityMicAnimationBinding.inflate(layoutInflater)
    }

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

    private fun switchLayout(mode: MicLayoutManager.LayoutMode) {
        val rv = mBinding.recyclerView

        val oldPositions = mutableMapOf<Int, Pair<Float, Float>>()
        for (i in 0 until rv.childCount) {
            val child = rv.getChildAt(i)
            val pos = rv.getChildAdapterPosition(child)
            oldPositions[pos] = Pair(child.left.toFloat(), child.top.toFloat())
        }

        if (!micLayoutManager.switchMode(mode)) return

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
