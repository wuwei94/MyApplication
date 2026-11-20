package com.example.william.my.module.anim.activity.thirdparty

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.module.anim.R
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.inter.IAnimListener
import com.tencent.qgame.animplayer.inter.IFetchResource
import com.tencent.qgame.animplayer.inter.OnResourceClickListener
import com.tencent.qgame.animplayer.mix.Resource
import com.tencent.qgame.animplayer.util.ScaleType
import java.io.File

/**
 * VAP — Alpha 视频动画播放与融合动画
 *
 * 核心机制与避坑点：
 * 1. Alpha 视频：MP4 画面分区存储 RGB 与 Alpha（vapc BOX 声明区域与融合配置），
 *    MediaCodec 硬解后由 OpenGL 逐帧合成透明画面，单 mp4 即完整素材
 * 2. 播放互斥与异步停止：isRunning 期间 startPlay 被忽略；stopPlay 仅置停止标记，
 *    解码线程退出后才回调 onVideoComplete（失败也会回调），切换素材须接力起播；
 *    IAnimListener 全部回调在 VAP 工作线程，触碰 UI 必须切主线程
 * 3. 循环语义：setLoop(n) 是剩余次数倒计时（0 单次、n>0 共播 n 次），次数会被解码
 *    消耗并回写，每次起播前须重设；不支持 PAG setRepeatCount(-1) 式无限循环
 * 4. 融合供给：IFetchResource 按素材 srcTag 回传图文，无论成败都必须回调 result，
 *    否则播放无限等待；返回的 Bitmap 在 releaseResource 中回收
 * 5. 素材经缓存文件播放：融合素材 vapc 配置较大，assets 流式读取可能截断配置，
 *    先拷贝到 cacheDir 再交由 FileContainer 可靠读取（见 assetToFile）
 *
 * 官方参考：
 * https://github.com/Tencent/vap/tree/vap_open_source
 */
class VapActivity : BasicLayoutActivity() {

    private lateinit var animView: AnimView

    /** 当前素材路径；缩放模式与融合头像切换后按它重播 */
    private var currentAsset = Constants.Vap_Demo

    /** stopPlay 异步生效期间暂存的目标素材，onVideoComplete 接力起播；仅主线程读写 */
    private var pendingAsset: String? = null

    @Volatile
    private var useAvatarA = true

    private var loopCount = LOOP_ONCE
    private var scaleTypeIndex = 0
    private var mixPlayCount = 0

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val stage = layoutInflater.inflate(R.layout.anim_layout_vap_player, container, false)
        animView = stage.findViewById(R.id.anim_vap_view)
        setView(stage)
        animView.setScaleType(SCALE_TYPES[scaleTypeIndex])
        animView.setAnimListener(animListener)
        animView.setFetchResource(fetchResource)
        animView.setOnResourceClickListener(object : OnResourceClickListener {
            override fun onClick(resource: Resource) {
                toast("点击融合资源 tag=${resource.tag}")
            }
        })
        playAsset(Constants.Vap_Demo)
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 播放 Alpha 视频（demo.mp4）",
        "2. 播放融合动画（vapx.mp4）",
        "3. 切换融合头像（示例 A ↔ B）",
        "4. 切换循环次数（单次 ↔ 3 次）",
        "5. 切换缩放模式（FIT_CENTER / CENTER_CROP / FIT_XY）",
        "6. 停止播放",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> playAsset(Constants.Vap_Demo)
            1 -> playAsset(Constants.Vap_Mix)
            2 -> switchAvatar()
            3 -> toggleLoop()
            4 -> toggleScaleType()
            5 -> stopPlay()
        }
    }

    /**
     * 起播指定素材。
     *
     * 播放中 startPlay 会被忽略，而 stopPlay 异步生效，故播放中切换素材
     * 先挂起目标路径，待 onVideoComplete 接力起播。
     */
    private fun playAsset(path: String) {
        currentAsset = path
        if (animView.isRunning()) {
            pendingAsset = path
            animView.stopPlay()
        } else {
            startAsset(path)
        }
    }

    /**
     * 起播前重设循环次数：次数会被解码器消耗并回写，解码器复用时不重设则静默退回单次。
     */
    private fun startAsset(path: String) {
        animView.setLoop(loopCount)
        animView.startPlay(assetToFile(path))
    }

    /**
     * 素材先落到缓存文件再播放：融合素材的 vapc 配置约 17KB，AssetInputStream 单次 read
     * 不保证读满，截断的 JSON 会令配置解析失败；FileContainer 走 RandomAccessFile 可靠读满，
     * 与官方 demo 一致。按长度校验缓存完整性，避免复用上次中断留下的半截文件。
     */
    private fun assetToFile(path: String): File {
        val target = File(cacheDir, path)
        val expected = assets.openFd(path).use { it.length }
        if (target.exists() && target.length() == expected) return target
        target.parentFile?.mkdirs()
        assets.open(path).use { input -> target.outputStream().use { input.copyTo(it) } }
        return target
    }

    /**
     * 切换融合头像；融合动画是当前素材时立即重播应用新头像。
     */
    private fun switchAvatar() {
        useAvatarA = !useAvatarA
        val name = if (useAvatarA) "A（橙色圆脸）" else "B（蓝色方脸）"
        if (currentAsset == Constants.Vap_Mix) {
            playAsset(Constants.Vap_Mix)
            toast("已切换头像 $name 并重播融合动画")
        } else {
            toast("已选择头像 $name，播放融合动画时生效")
        }
    }

    /**
     * 切换循环次数。setLoop 实时透传给解码器，对当前播放立即生效。
     */
    private fun toggleLoop() {
        loopCount = if (loopCount == LOOP_ONCE) LOOP_THREE else LOOP_ONCE
        animView.setLoop(loopCount)
        toast(if (loopCount == LOOP_ONCE) "循环次数：单次" else "循环次数：$loopCount 次")
    }

    /**
     * 循环切换缩放模式。缩放参数在准备 TextureView 时才参与布局，需重播当前素材生效。
     */
    private fun toggleScaleType() {
        scaleTypeIndex = (scaleTypeIndex + 1) % SCALE_TYPES.size
        val scaleType = SCALE_TYPES[scaleTypeIndex]
        animView.setScaleType(scaleType)
        playAsset(currentAsset)
        toast("缩放模式：$scaleType")
    }

    private fun stopPlay() {
        pendingAsset = null
        animView.stopPlay()
        toast("已停止播放")
    }

    /**
     * 播放回调（全部在 VAP 工作线程）。
     *
     * onVideoComplete 在停止 / 失败 / 自然结束时都会触发，这里只承担接力起播；
     * 错误上报走 onFailed（一次播放可能多次回调）。
     */
    private val animListener = object : IAnimListener {
        override fun onVideoStart() {}

        override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {}

        override fun onVideoComplete() {
            // 接力决策收敛在主线程：stopPlay 先于本任务执行时 pendingAsset 已清空，重播即取消
            runOnUiThread {
                val next = pendingAsset ?: return@runOnUiThread
                pendingAsset = null
                if (!isDestroyed) {
                    startAsset(next)
                }
            }
        }

        override fun onVideoDestroy() {}

        override fun onFailed(errorType: Int, errorMsg: String?) {
            runOnUiThread { toast("播放失败 errorType=$errorType：$errorMsg") }
        }
    }

    /**
     * 融合资源供给：按 vapx.mp4 声明的 srcTag 回传头像与文字。
     *
     * 无论解码成败都必须回调 result，否则播放线程无限等待；
     * 头像复用 module_anim 示例头像 A/B（与 SVGA 插入图片页同源）。
     */
    private val fetchResource = object : IFetchResource {
        override fun fetchImage(resource: Resource, result: (Bitmap?) -> Unit) {
            if (resource.tag != TAG_IMAGE) {
                result(null)
                return
            }
            val path = if (useAvatarA) Constants.SVGA_Avatar_A else Constants.SVGA_Avatar_B
            val bitmap = runCatching {
                assets.open(path).use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
            result(bitmap)
        }

        override fun fetchText(resource: Resource, result: (String?) -> Unit) {
            if (resource.tag != TAG_TEXT) {
                result(null)
                return
            }
            mixPlayCount++
            result("恭喜 No.$mixPlayCount 用户升神")
        }

        override fun releaseResource(resources: List<Resource>) {
            resources.forEach { it.bitmap?.recycle() }
        }
    }

    override fun onDestroy() {
        animView.stopPlay()
        super.onDestroy()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        /** vapx.mp4 融合配置（vapc BOX）中声明的图片槽 / 文字槽 srcTag */
        private const val TAG_IMAGE = "[sImg1]"
        private const val TAG_TEXT = "[sTxt1]"

        /** setLoop 为剩余次数倒计时：0 单次，3 共播 3 次；负数不是无限循环 */
        private const val LOOP_ONCE = 0
        private const val LOOP_THREE = 3

        private val SCALE_TYPES = arrayOf(ScaleType.FIT_CENTER, ScaleType.CENTER_CROP, ScaleType.FIT_XY)
    }
}
