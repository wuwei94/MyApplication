package com.example.william.my.module.anim.activity.thirdparty

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.anim.R
import org.libpag.PAGFile
import org.libpag.PAGImage
import org.libpag.PAGScaleMode
import org.libpag.PAGView

/**
 * PAG 替换图像 — PAGImage 灌入图片槽
 *
 * 核心机制与避坑点：
 * 1. 三步走：构造 PAGImage → `replaceImage(i, image)` → 重新 `setComposition()` 刷新。
 *    只调 replaceImage 而不回灌 composition，画面不会变，这是最容易漏的一步。
 * 2. 槽位序号来自导出侧：`i` 是 PAGExporter 勾选「可编辑」的顺序，与图层树顺序无关；
 *    `numImages()` 为 0 时 replaceImage 静默失败，页面必须先判数量。
 * 3. 填充模式属于 PAGImage 不属于 PAGView：`PAGImage.setScaleMode` 决定替换图如何适配
 *    槽位矩形；`PAGView.setScaleMode` 管的是整个合成如何适配控件尺寸（见播放页）。
 *    本页统一用 LetterBox（等比留边），保证替换图不变形。
 * 4. Bitmap 生命周期：`PAGImage.FromBitmap` 持有位图引用，替换期间不能 recycle，
 *    否则渲染出来是空白。
 * 5. 恢复原图靠重新 Load：replaceImage 改的是 PAGFile 对象自身，只能换一份干净副本回来。
 *
 * 素材 800×800，含 1 个图片槽，自带内容是一张中性的图片占位图 —— 占位图与替换图的对比一眼可见。
 *
 * 官方参考：
 * https://pag.io/docs/en/image-fill.html
 */
@Route(path = RouterPath.Anim.PagImage)
class PagImageActivity : BasicLayoutActivity() {

    private lateinit var pagView: PAGView

    private var pagFile: PAGFile? = null

    /** 当前素材的图片槽数量，随素材刷新 */
    private var imageSlotCount = 0

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { insertFromUri(it) }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val stage = layoutInflater.inflate(R.layout.anim_layout_pag_lab, container, false)
        pagView = stage.findViewById(R.id.anim_pag_view_player)
        setView(stage)
        loadComposition()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 填入示例图覆盖全部图片槽",
        "2. 从相册选择图片",
        "3. 恢复素材原始图",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> importBatch(PRODUCT_IMAGES)
            1 -> pickImageLauncher.launch("image/*")
            2 -> loadComposition()
        }
    }

    /**
     * 装载素材并起播。
     *
     * 恢复原始图也走这里：重新 Load 得到干净的 PAGFile，抹掉此前所有 replaceImage 改动。
     */
    private fun loadComposition() {
        val file = PAGFile.Load(assets, Constants.Pag_Image)
        if (file == null) {
            toast("PAGFile.Load 失败：${Constants.Pag_Image}")
            return
        }
        pagFile = file
        imageSlotCount = file.numImages()
        pagView.setComposition(file)
        pagView.setScaleMode(PAGScaleMode.LetterBox)
        pagView.setRepeatCount(REPEAT_INFINITE)
        pagView.play()
    }

    /**
     * 按顺序把示例图灌进前 N 个图片槽，N 取示例图张数与素材实际槽位数的较小值。
     *
     * 示例图是一组通用素材，槽位数随素材而定：本页素材只有 1 个槽，故只灌第 1 张；
     * 换成多槽素材时同一段代码会自动铺满全部槽位。
     */
    private fun importBatch(images: Array<String>) {
        val file = pagFile ?: return
        if (imageSlotCount == 0) {
            toast("该素材 numImages()=0，replaceImage 会静默失败")
            return
        }
        val count = minOf(images.size, imageSlotCount)
        var applied = 0
        for (i in 0 until count) {
            if (replaceSlot(file, i, images[i])) applied++
        }
        if (applied == 0) {
            toast("PAGImage.FromAssets 全部失败")
            return
        }
        refresh()
    }

    /**
     * 从相册读位图，转成 PAGImage 灌入槽 0。
     *
     * 注意位图在这期间不能被 recycle，替换完成后仍由 PAGImage 持有。
     */
    private fun insertFromUri(uri: Uri) {
        val bitmap = runCatching {
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
        if (bitmap == null) {
            toast("选图解码失败")
            return
        }
        val image = PAGImage.FromBitmap(bitmap)
        if (image == null) {
            toast("PAGImage.FromBitmap 返回 null")
            return
        }
        val file = pagFile ?: return
        image.setScaleMode(SLOT_SCALE_MODE)
        file.replaceImage(0, image)
        refresh()
    }

    private fun replaceSlot(file: PAGFile, index: Int, assetPath: String): Boolean {
        val image = PAGImage.FromAssets(assets, assetPath) ?: return false
        image.setScaleMode(SLOT_SCALE_MODE)
        file.replaceImage(index, image)
        return true
    }

    /**
     * replaceImage 只改数据，重新 setComposition 才会重建纹理并刷新画面。
     */
    private fun refresh() {
        val file = pagFile ?: return
        pagView.setComposition(file)
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
        private const val REPEAT_INFINITE = -1

        /** 替换图统一等比留边，避免不同宽高比的图被拉变形 */
        private const val SLOT_SCALE_MODE = PAGScaleMode.LetterBox

        /** 示例图池，按素材实际槽位数取前 N 张 */
        private val PRODUCT_IMAGES = arrayOf(
            Constants.Pag_Image_Product_Bottle,
            Constants.Pag_Image_Product_Bag,
            Constants.Pag_Image_Product_Cat,
            Constants.Pag_Image_Product_Logo,
            Constants.Pag_Image_Poster_Sale,
        )
    }
}
