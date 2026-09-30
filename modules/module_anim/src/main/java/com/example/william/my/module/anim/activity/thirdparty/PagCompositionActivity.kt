package com.example.william.my.module.anim.activity.thirdparty

import android.graphics.Matrix
import android.os.Bundle
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.anim.R
import org.libpag.PAGComposition
import org.libpag.PAGFile
import org.libpag.PAGScaleMode
import org.libpag.PAGView

/**
 * PAG 渲染树组合 — PAGComposition 把多个 PAG 文件拼成一张渲染树
 *
 * 核心机制与避坑点：
 * 1. PAGFile 本身就是 PAGComposition：继承链是 `PAGFile → PAGComposition → PAGLayer`，
 *    所以一个 pag 文件既可单独作为 PAGView 的根内容，也可被当作普通图层塞进别的合成里，
 *    多个素材由此拼成一棵统一渲染树，这是 PAG 相对播放器类动效库的独有能力。
 * 2. 组合是「移入」不是「复制」：`addLayer` 会把该图层从原父节点摘下来，文档原文
 *    "the layer is removed from the other PAGComposition object"，同一个 PAGFile 实例不能同时
 *    挂在两个父节点下。
 * 3. 子图层保留自己的时间轴：PAGFile 作为子图层时仍带着自身的时长与动画矩阵，
 *    `setStartTime` 决定它在父合成时间轴上的出场点，`setMatrix` 决定它占据哪块画布。
 *    父合成的总时长由子图层合成而来，素材时长不一致时以最长者为准，因此不会强行对齐时长。
 * 4. 改结构必须回灌 composition：增删图层与调整层级改变的是渲染树本身，只改对象不重新
 *    `setComposition()` 画面不会更新 —— 这一点与「图层属性」页相反，那页改属性是下一帧即生效的。
 * 5. 索引即绘制顺序，层号越大越靠前（后者盖前者）：`setLayerIndex` / `swapLayer` 调的是叠放
 *    次序；`removeLayer` 之后图层对象仍然有效，可以再次 `addLayer` 装回来。
 *
 * 素材组合方式：`gift_moonlight.pag`（1600×1600）缩到 0.5 倍占左半屏，
 * `replacement.pag`（800×800）原尺寸占右半屏，拼成 1600×800 的画布。
 * 两个素材各自独立动画，组合后共用一条父级时间轴。
 *
 * 单一素材的图层属性演示见「图层属性」页，内容替换见「替换图像」「替换文字」两页。
 *
 * 官方参考：
 * https://pag.io/docs/en/api-instructions.html
 */
@Route(path = RouterPath.Anim.PagComposition)
class PagCompositionActivity : BasicLayoutActivity() {

    private lateinit var pagView: PAGView

    /** 左半屏素材，来自 1600×1600 的纯动效文件 */
    private var leftFile: PAGFile? = null

    /** 右半屏素材，来自 800×800 的含图片槽文件 */
    private var rightFile: PAGFile? = null

    /** 当前挂在 PAGView 上的合成，增删图层都作用在它身上 */
    private var composition: PAGComposition? = null

    /** 右半屏图层是否已处于错峰出场状态，供错峰项取反 */
    private var staggered = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val stage = layoutInflater.inflate(R.layout.anim_layout_pag_lab, container, false)
        pagView = stage.findViewById(R.id.anim_pag_view_player)
        setView(stage)
        loadLayers()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 组合两个 PAG 文件",
        "2. 交换两个图层顺序",
        "3. 把第二个图层移到最底",
        "4. 错开两个图层出场时间",
        "5. 移除第二个图层",
        "6. 清空组合",
        "7. 重建组合",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> compose()
            1 -> swapLayers()
            2 -> moveSecondToBottom()
            3 -> staggerStartTime()
            4 -> removeSecondLayer()
            5 -> clearLayers()
            6 -> compose()
        }
    }

    /**
     * 装载两个素材、各自摆好位置并立即组合成一张渲染树。
     *
     * 摆位交给 `setMatrix`：子图层保留自己的时长与动画矩阵，附加矩阵只负责决定它占哪块画布。
     */
    private fun loadLayers() {
        val left = PAGFile.Load(assets, Constants.Pag_Playback)
        val right = PAGFile.Load(assets, Constants.Pag_Image)
        if (left == null || right == null) {
            toast("PAGFile.Load 失败：${Constants.Pag_Playback} / ${Constants.Pag_Image}")
            return
        }
        leftFile = left
        rightFile = right
        left.setMatrix(placement(LEFT_SCALE, 0f, 0f))
        right.setMatrix(placement(RIGHT_SCALE, RIGHT_ORIGIN_X, 0f))
        compose()
    }

    /**
     * 构造「先缩放、再平移」的附加矩阵，把子图层摆到画布指定区域。
     *
     * 顺序不能反：`postTranslate` 是在缩放结果之上再平移，反了位移量会被缩放一起放大。
     */
    private fun placement(scale: Float, dx: Float, dy: Float): Matrix = Matrix().apply {
        setScale(scale, scale)
        postTranslate(dx, dy)
    }

    /**
     * 新建一张合成装入两个文件，并挂到 PAGView 上。
     *
     * 新建而非复用旧合成，是因为清空组合后旧合成的子节点已被摘掉，重建要的是回到初始状态。
     */
    private fun compose() {
        val left = leftFile ?: return
        val right = rightFile ?: return
        val canvas = PAGComposition.Make(CANVAS_WIDTH, CANVAS_HEIGHT)
        if (canvas == null) {
            toast("PAGComposition.Make 返回 null")
            return
        }
        canvas.addLayer(left)
        canvas.addLayer(right)
        composition = canvas
        staggered = false
        applyComposition()
        toast("已组合，numChildren()=${canvas.numChildren()}")
    }

    /**
     * 把当前合成重新挂给 PAGView 并起播。
     *
     * 结构发生任何变化都要走这一步：渲染树是在 `setComposition()` 时重建的，
     * PAGView 会沿用当前进度，末尾 play() 保证仍在播放。
     */
    private fun applyComposition() {
        val canvas = composition ?: return
        pagView.setComposition(canvas)
        pagView.setScaleMode(PAGScaleMode.LetterBox)
        pagView.setRepeatCount(REPEAT_INFINITE)
        pagView.play()
    }

    /**
     * 交换前两个图层的叠放次序。
     *
     * 用 `swapLayerAt` 一次成型，比两次 `setLayerIndex` 少一个中间态（不会出现两层短暂同层）。
     */
    private fun swapLayers() {
        val canvas = requireTwoLayers() ?: return
        canvas.swapLayerAt(0, 1)
        applyComposition()
        toast("已交换叠放次序，numChildren()=${canvas.numChildren()}")
    }

    /**
     * 把第二个图层移到索引 0，即压到最底层被另一个盖住。
     */
    private fun moveSecondToBottom() {
        val canvas = requireTwoLayers() ?: return
        val second = canvas.getLayerAt(1) ?: return
        canvas.setLayerIndex(second, 0)
        applyComposition()
        toast("第二个图层已下移到 index=${canvas.getLayerIndex(second)}")
    }

    /**
     * 让右半屏图层推迟 1s 出场，验证子图层各自保留时间轴。
     *
     * 读回值来自 `startTime()`，用来确认改动真的落到了图层对象上；再次点击取消错峰。
     */
    private fun staggerStartTime() {
        val second = rightFile ?: return
        staggered = !staggered
        second.setStartTime(if (staggered) STAGGER_US else 0L)
        applyComposition()
        toast("右半屏图层 startTime=${second.startTime() / MICROS_PER_MILLI} ms")
    }

    /**
     * 移除第二个图层。
     *
     * `removeLayerAt` 返回被摘下的图层对象，它并未销毁，重建组合时会被再次装入。
     */
    private fun removeSecondLayer() {
        val canvas = requireTwoLayers() ?: return
        canvas.removeLayerAt(1)
        applyComposition()
        toast("已移除第二个图层，剩余 numChildren()=${canvas.numChildren()}")
    }

    /**
     * 清空当前合成的全部子图层，画布回到空白态。
     */
    private fun clearLayers() {
        val canvas = composition ?: return
        canvas.removeAllLayers()
        applyComposition()
        toast("已清空组合，numChildren()=${canvas.numChildren()}")
    }

    /**
     * 取当前合成并确认至少有两个子图层，不满足时提示后放弃本次操作。
     */
    private fun requireTwoLayers(): PAGComposition? {
        val canvas = composition
        if (canvas == null) {
            toast("组合尚未建立")
            return null
        }
        if (canvas.numChildren() < REQUIRED_LAYERS) {
            toast("当前 numChildren()=${canvas.numChildren()}，该操作需要 $REQUIRED_LAYERS 个图层")
            return null
        }
        return canvas
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

        /** 组合画布尺寸：左半屏 1600×1600 缩到 0.5 倍，右半屏 800×800 原尺寸 */
        private const val CANVAS_WIDTH = 1600

        private const val CANVAS_HEIGHT = 800

        private const val LEFT_SCALE = 0.5f

        private const val RIGHT_SCALE = 1f

        private const val RIGHT_ORIGIN_X = 800f

        /** 依赖两个子图层的操作所要求的最小图层数 */
        private const val REQUIRED_LAYERS = 2

        /** 错峰出场的偏移量，PAG 时间单位统一为微秒 */
        private const val STAGGER_US = 1_000_000L

        private const val MICROS_PER_MILLI = 1000
    }
}
