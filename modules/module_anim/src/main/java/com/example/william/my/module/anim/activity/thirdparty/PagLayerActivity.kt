package com.example.william.my.module.anim.activity.thirdparty

import android.graphics.Matrix
import android.os.Bundle
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.anim.R
import org.libpag.PAGFile
import org.libpag.PAGLayer
import org.libpag.PAGScaleMode
import org.libpag.PAGView

/**
 * PAG 图层属性 — PAGLayer 通用属性与动画矩阵叠加
 *
 * 核心机制与避坑点：
 * 1. 属性在基类上，与图层类型无关：`setMatrix` / `setAlpha` / `setVisible` / `setStartTime`
 *    定义于 PAGLayer，图片图层、文本图层、实色图层与预合成图层全都继承得到。因此本页不依赖
 *    素材槽位 —— 0 图片槽 0 文本槽的纯动效素材同样能演示。这也是与「替换图像」「替换文字」
 *    两页的分工：那两页改**内容**，本页改**图层自身**。
 * 2. 附加矩阵是叠加而非替换动画：`setMatrix` 的文档明确 "Altering it does not change the
 *    animation matrix, and it will be concatenated to current animation matrix"。缩放旋转不会
 *    破坏素材原有动画，只在其上再乘一层；也正因如此，附加矩阵的基准是**父合成坐标系**，
 *    缩放与旋转的锚点在合成左上角，不是各元素自身的中心。
 * 3. 每次 setMatrix 是覆盖不是累加：附加矩阵只有一个值，连续调用会冲掉上一次，想让多种变换
 *    同时生效必须一次构造出复合 Matrix。本页为让每一项的效果单独可辨，刻意每项只设一种变换。
 * 4. 改属性不必回灌 composition：图层属性直接作用于原生图层对象，下一帧 flush 即生效，
 *    无需重新 `setComposition()`。这与「替换图像 / 替换文字」改的是 PAGFile 自身数据、
 *    必须回灌才刷新不同，是本页与那两页最容易被写错的差别。
 * 5. 还原要回到素材初值：素材可能给图层设计过非零的 startTime 或初始不可见，还原时一律写
 *    0 / true 会改掉设计意图，所以装载时先按索引记录 startTime 与 visible 初值。
 *
 * 操作对象是全部顶层子图层：素材的图层命名与层级属设计侧结构，页面不假设其命名，统一遍历
 * `0 until numChildren()`，换任意素材都成立。
 *
 * 素材 1600×1600，0 文本槽 / 0 图片槽，时长 6000ms。
 * 带槽位素材的内容替换演示见同组「替换图像」「替换文字」两页，结构编辑见「渲染树组合」页。
 *
 * 官方参考：
 * https://pag.io/docs/en/api-instructions.html
 */
@Route(path = RouterPath.Anim.PagLayer)
class PagLayerActivity : BasicLayoutActivity() {

    private lateinit var pagView: PAGView

    private var pagFile: PAGFile? = null

    /** 各子图层装载时的 startTime 初值，还原时按索引回填 */
    private val initialStartTimes = mutableMapOf<Int, Long>()

    /** 各子图层装载时的 visible 初值，还原时按索引回填 */
    private val initialVisibilities = mutableMapOf<Int, Boolean>()

    /** 全部子图层当前是否为隐藏态，供可见性切换项取反 */
    private var layersHidden = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val stage = layoutInflater.inflate(R.layout.anim_layout_pag_lab, container, false)
        pagView = stage.findViewById(R.id.anim_pag_view_player)
        setView(stage)
        loadComposition()
    }

    /**
     * 操作项文案直接引用属性常量，避免改动数值后列表与实现脱节。
     */
    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 缩放全部子图层 ${SCALE_FACTOR}×",
        "2. 旋转全部子图层 ${ROTATE_DEGREES.toInt()}°",
        "3. 平移全部子图层 (${TRANSLATE_X.toInt()}, ${TRANSLATE_Y.toInt()})",
        "4. 全部子图层叠加半透明 ${ALPHA_PARTIAL}",
        "5. 隐藏全部子图层",
        "6. 全部子图层延迟 ${DELAY_US / MICROS_PER_MILLI} ms 出场",
        "7. 还原全部图层属性",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> applyMatrix { setScale(SCALE_FACTOR, SCALE_FACTOR) }
            1 -> applyMatrix { setRotate(ROTATE_DEGREES) }
            2 -> applyMatrix { setTranslate(TRANSLATE_X, TRANSLATE_Y) }
            3 -> forEachLayer { _, layer -> layer.setAlpha(ALPHA_PARTIAL) }
            4 -> toggleVisible()
            5 -> delayStartTime()
            6 -> restore()
        }
    }

    /**
     * 装载素材、记录子图层初值并起播。
     *
     * 先把初值记下来再展示：还原项必须回到素材设计时的状态，而不是一律写成 0 / true。
     */
    private fun loadComposition() {
        val file = PAGFile.Load(assets, Constants.Pag_Playback)
        if (file == null) {
            toast("PAGFile.Load 失败：${Constants.Pag_Playback}")
            return
        }
        pagFile = file
        recordInitialState(file)
        pagView.setComposition(file)
        pagView.setScaleMode(PAGScaleMode.LetterBox)
        pagView.setRepeatCount(REPEAT_INFINITE)
        pagView.play()
    }

    private fun recordInitialState(file: PAGFile) {
        initialStartTimes.clear()
        initialVisibilities.clear()
        for (index in 0 until file.numChildren()) {
            val layer = file.getLayerAt(index) ?: continue
            initialStartTimes[index] = layer.startTime()
            initialVisibilities[index] = layer.visible()
        }
        layersHidden = false
    }

    /**
     * 用一次构造出的矩阵覆盖全部子图层的附加矩阵。
     *
     * 附加矩阵是覆盖语义，复合变换必须一次生成，不能靠连续调用叠加。
     */
    private fun applyMatrix(configure: Matrix.() -> Unit) {
        val matrix = Matrix().apply(configure)
        forEachLayer { _, layer -> layer.setMatrix(matrix) }
    }

    /**
     * 遍历全部顶层子图层执行 [block]，随后补一帧播放。
     *
     * 属性作用在原生图层对象上，下一帧即生效，不必重新 `setComposition()`；末尾的 play()
     * 只是确保动画在播（重复调用 play() 不改变当前进度）。
     */
    private fun forEachLayer(block: (index: Int, layer: PAGLayer) -> Unit) {
        val file = pagFile ?: return
        val count = file.numChildren()
        if (count == 0) {
            toast("该素材 numChildren()=0，没有可改属性的子图层")
            return
        }
        for (index in 0 until count) {
            file.getLayerAt(index)?.let { block(index, it) }
        }
        pagView.play()
    }

    /**
     * 切换全部子图层可见性。
     *
     * `setVisible` 只影响显示，不动动画时间轴，恢复显示后动画仍按主时间轴继续。
     */
    private fun toggleVisible() {
        layersHidden = !layersHidden
        forEachLayer { _, layer -> layer.setVisible(!layersHidden) }
        toast(if (layersHidden) "已隐藏全部子图层" else "已恢复显示全部子图层")
    }

    /**
     * 在各自初值基础上整体推迟出场。
     *
     * `setStartTime` 定义图层在父合成时间轴上的可见起点，属合成级时间轴，与动画内部关键帧无关；
     * 推迟后尾部会被主时间轴截断，所以这是有损演示，还原项可回到初值。
     */
    private fun delayStartTime() {
        forEachLayer { index, layer ->
            layer.setStartTime((initialStartTimes[index] ?: 0L) + DELAY_US)
        }
        toast("全部子图层推迟 ${DELAY_US / MICROS_PER_MILLI} ms 出场")
    }

    /**
     * 按装载时记录的初值还原矩阵、透明度、可见性与起始时间。
     */
    private fun restore() {
        forEachLayer { index, layer ->
            layer.resetMatrix()
            layer.setAlpha(ALPHA_FULL)
            layer.setVisible(initialVisibilities[index] ?: true)
            layer.setStartTime(initialStartTimes[index] ?: 0L)
        }
        layersHidden = false
    }

    override fun onDestroy() {
        pagView.pause()
        pagView.freeCache()
        super.onDestroy()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        private const val REPEAT_INFINITE = -1

        private const val SCALE_FACTOR = 0.6f

        private const val ROTATE_DEGREES = 25f

        private const val TRANSLATE_X = 240f

        private const val TRANSLATE_Y = 160f

        private const val ALPHA_PARTIAL = 0.3f

        private const val ALPHA_FULL = 1f

        /** 出场偏移，PAG 时间单位统一为微秒 */
        private const val DELAY_US = 1_000_000L

        private const val MICROS_PER_MILLI = 1000
    }
}
