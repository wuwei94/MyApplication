package com.example.william.my.module.anim.activity.thirdparty

import android.os.Bundle
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.anim.R
import org.libpag.PAGFile
import org.libpag.PAGScaleMode
import org.libpag.PAGView

/**
 * PAG 播放 — 加载素材并循环播放
 *
 * 核心机制与避坑点：
 * 1. 三步走：`PAGFile.Load()` 解码 → `setComposition()` 绑定 → `play()` 起播。
 *    Load 之后不回灌 composition，画面不会出现。
 * 2. 两套 scaleMode 各管一层：`PAGView.setScaleMode` 决定「合成如何适配 View」，
 *    `PAGImage.setScaleMode` 决定「替换图如何适配槽位矩形」（见图片替换页），两者互不影响。
 * 3. 循环语义：`setRepeatCount(-1)` 无限循环，`1` 则播完一轮停在末帧。
 * 4. 生命周期：销毁前 `pause()` + `freeCache()`，否则 TextureView 持有的 GPU 纹理不释放。
 * 5. 社区版边界：本库社区版不含音视频能力，`replaceVideo` 等 API 不存在，只能读到
 *    `getVideoRanges()` 这类只读信息；音视频同步与 MP4 导出属企业版，不要按可写能力设计。
 *
 * 素材 1600×1600 正方形画布，0 文本槽 / 0 图片槽（纯动效），时长 6000ms。
 * 带槽位素材的读写演示见同组「替换图像」「替换文字」两页。
 *
 * 官方参考：
 * https://pag.io/docs/en/api-instructions.html
 */
@Route(path = RouterPath.Anim.PagPlayback)
class PagPlaybackActivity : BasicLayoutActivity() {

    private lateinit var pagView: PAGView

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val stage = layoutInflater.inflate(R.layout.anim_layout_pag_lab, container, false)
        pagView = stage.findViewById(R.id.anim_pag_view_player)
        setView(stage)
        loadComposition()
    }

    /**
     * 装载素材、绑定到控件并起播。
     */
    private fun loadComposition() {
        val file = PAGFile.Load(assets, Constants.Pag_Playback)
        if (file == null) {
            toast("PAGFile.Load 失败：${Constants.Pag_Playback}")
            return
        }
        pagView.setComposition(file)
        pagView.setScaleMode(PAGScaleMode.LetterBox)
        pagView.setRepeatCount(REPEAT_INFINITE)
        pagView.play()
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
        /** -1 透传给内部 animator，表示无限循环 */
        private const val REPEAT_INFINITE = -1
    }
}
