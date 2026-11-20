package com.example.william.my.module.anim.activity.thirdparty

import android.os.Bundle
import android.widget.Toast
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Rive
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.module.anim.R

/**
 * Rive — 矢量动画与状态机播放（官方 View 轨道 API）
 *
 * 核心机制与避坑点：
 * 1. `.riv` 二进制素材：动画、状态机与输入定义全在文件内，一个素材同时承载
 *    线性播放与交互逻辑；`setRiveResource` 主线程读 raw 字节流并解码为 C++ File，
 *    后续播放用（状态机名, 输入名）寻址，无需持有实例对象。
 * 2. 原生运行时手动初始化：库 AAR 清单把 androidx.startup 自动初始化项显式移除，任何 native
 *    调用前必须完成一次 `Rive.init` —— 含 `RiveAnimationView` 的构造（其默认 FileAssetLoader
 *    直接触 native），只把 init 排在 `.riv` 解码前仍会崩在 inflate；进程级标志防重复。
 * 3. 状态机输入语义：`setNumberState` 写入的 Level=0/1/2 对应素材设计侧的
 *    入门 / 中级 / 高级三段动画，只有状态机在播放中改输入才可见效果，
 *    先播状态机再切 Level。
 * 4. 生命周期与原生引用：视图绑定视图树最近的 LifecycleOwner，onDestroy 时
 *    自动释放 native 引用计数，宿主必须是带生命周期的组件；渲染走 TextureView，
 *    透明区域透出外层容器深色底。
 * 5. API 代际：官方新主推 Compose API（beta），View 轨道 `RiveAnimationView`
 *    KDoc 已标 legacy 但持续维护；本页取 View 轨道以与 VAP / PAG 兄弟页同构。
 *
 * 素材 skills.riv 取自官方示例 app：默认画板含循环动画与 "Designer's Test"
 * 状态机（数字输入 Level），线性动画的布局预览对照见兄弟页 LottieActivity。
 *
 * 官方参考：
 * https://github.com/rive-app/rive-android
 */
class RiveActivity : BasicLayoutActivity() {

    private lateinit var riveView: RiveAnimationView

    /** Level 数字输入的循环游标，0/1/2 对应素材的入门/中级/高级三段动画 */
    private var levelIndex = 0

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // Rive.init 必须先于视图创建：RiveAnimationView 构造时即实例化 native FileAssetLoader，
        // native 库未加载会直接 UnsatisfiedLinkError（见 inflate 崩溃现场）
        ensureRuntime()
        val stage = layoutInflater.inflate(R.layout.anim_layout_rive_player, container, false)
        riveView = stage.findViewById(R.id.anim_rive_view)
        setView(stage)
        riveView.setRiveResource(R.raw.anim_rive_skills)
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 播放状态机（${SM_NAME}）",
        "2. 切换数字输入 Level（0 → 1 → 2）",
        "3. 暂停",
        "4. 继续",
        "5. 复位画板",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> playStateMachine()
            1 -> cycleLevel()
            2 -> riveView.pause()
            3 -> riveView.play()
            4 -> riveView.reset()
        }
    }

    private fun playStateMachine() {
        riveView.play(SM_NAME, isStateMachine = true)
        toast("已切换到状态机：$SM_NAME")
    }

    /**
     * 循环写入 Level：状态机按数值在入门/中级/高级三段动画间迁移。
     */
    private fun cycleLevel() {
        levelIndex = (levelIndex + 1) % LEVEL_COUNT
        riveView.setNumberState(SM_NAME, INPUT_LEVEL, levelIndex.toFloat())
        toast("Level = $levelIndex（${LEVEL_NAMES[levelIndex]}）")
    }

    /**
     * Rive 解码 `.riv` 依赖 native 库；库清单移除了 startup 自动初始化，
     * 首次使用前手动加载一次，进程内防重复。
     */
    private fun ensureRuntime() {
        if (runtimeInitialized) return
        Rive.init(applicationContext)
        runtimeInitialized = true
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        @Volatile
        private var runtimeInitialized = false

        /** skills.riv 设计侧命名，必须与素材内状态机 / 输入名一致 */
        private const val SM_NAME = "Designer's Test"
        private const val INPUT_LEVEL = "Level"

        private const val LEVEL_COUNT = 3
        private val LEVEL_NAMES = arrayOf("入门", "中级", "高级")
    }
}
