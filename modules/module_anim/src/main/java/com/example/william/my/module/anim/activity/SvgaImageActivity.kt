package com.example.william.my.module.anim.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity

/**
 * SVGA — 动态图片插入（SVGADynamicEntity）
 *
 * 核心机制与避坑点：
 * 1. 图槽约定：.svga 内 images 以语义 imageKey（本页为 "heart"）占位，运行时
 *    [SVGADynamicEntity.setDynamicImage] 按 key 替换该图层像素；同文件中的
 *    "heartbeat" 为音轨图层，不要当作图片槽替换
 * 2. 对齐裁切：替换图必须先按图槽宽高比（本资源 194:178）居中裁剪缩放到槽位像素，
 *    再写入 dynamicEntity；直接塞原始 Bitmap 会被 layout 矩形拉伸导致错位
 * 3. 绑定时机：解析完成后 `setVideoItem(videoItem, dynamicEntity)`，改图后需重设并重播；
 *    imageKey 不在 MovieEntity.images 中时替换静默失败
 *
 * 资源来源：
 * https://github.com/svga/SVGA-Samples/blob/master/heartbeat.svga
 *
 * 官方参考：
 * https://github.com/svga/SVGAPlayer-Android
 */
@Route(path = RouterPath.Anim.SvgaImage)
class SvgaImageActivity : BasicLayoutActivity() {

    private lateinit var svgaImageView: SVGAImageView

    private var videoItem: SVGAVideoEntity? = null
    private val dynamicEntity = SVGADynamicEntity()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { insertFromUri(it) }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initPreview()
        loadSvga()
    }

    private fun initPreview() {
        val previewView = layoutInflater.inflate(
            com.example.william.my.module.anim.R.layout.anim_layout_svga_preview,
            container,
            false,
        )
        svgaImageView = previewView.findViewById(
            com.example.william.my.module.anim.R.id.anim_svga_player,
        )
        setView(previewView)
    }

    /**
     * 解析带图槽资源并绑定 [SVGADynamicEntity]。
     *
     * 图槽 key 为 "heart"，与 heartbeat.svga 中 MovieEntity.images 一致。
     */
    private fun loadSvga() {
        SVGAParser.shareParser().init(this)
        SVGAParser.shareParser().decodeFromAssets(
            Constants.Url_SVGA_Heartbeat,
            object : SVGAParser.ParseCompletion {
                override fun onError() {
                    Toast.makeText(this@SvgaImageActivity, "SVGA 解析失败", Toast.LENGTH_SHORT).show()
                }

                override fun onComplete(videoItem: SVGAVideoEntity) {
                    this@SvgaImageActivity.videoItem = videoItem
                    bindAndPlay()
                }
            },
        )
    }

    private fun bindAndPlay() {
        val item = videoItem ?: return
        svgaImageView.setVideoItem(item, dynamicEntity)
        svgaImageView.startAnimation()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 插入示例头像 A（橙色圆脸）",
        "2. 插入示例头像 B（蓝色方脸）",
        "3. 从相册选择图片插入",
        "4. 清除动态图（恢复占位图）",
        "5. 重播心跳动效",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> insertFromAssets(Constants.SVGA_Avatar_A)
            1 -> insertFromAssets(Constants.SVGA_Avatar_B)
            2 -> pickImageLauncher.launch("image/*")
            3 -> clearDynamicImage()
            4 -> bindAndPlay()
        }
    }

    /**
     * 从 assets 解码位图并写入图槽。
     */
    private fun insertFromAssets(fileName: String) {
        val bitmap = runCatching {
            assets.open(fileName).use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
        if (bitmap == null) {
            Toast.makeText(this, "示例图解码失败", Toast.LENGTH_SHORT).show()
            return
        }
        applyAvatar(bitmap, fileName)
    }

    /**
     * 从相册 Uri 解码位图并写入图槽。
     */
    private fun insertFromUri(uri: Uri) {
        val bitmap = runCatching {
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
        if (bitmap == null) {
            Toast.makeText(this, "选图解码失败", Toast.LENGTH_SHORT).show()
            return
        }
        applyAvatar(bitmap, "相册图片")
    }

    private fun applyAvatar(bitmap: Bitmap, source: String) {
        dynamicEntity.setDynamicImage(cropToSlot(bitmap), IMAGE_KEY)
        bindAndPlay()
        Toast.makeText(this, "已插入 $source", Toast.LENGTH_SHORT).show()
    }

    private fun clearDynamicImage() {
        dynamicEntity.clearDynamicObjects()
        bindAndPlay()
        Toast.makeText(this, "已恢复占位图", Toast.LENGTH_SHORT).show()
    }

    /**
     * 按图槽宽高比居中裁剪缩放到槽位像素，避免替换图被 layout 矩形拉伸导致槽位错位。
     */
    private fun cropToSlot(source: Bitmap): Bitmap {
        if (source.width == SLOT_WIDTH && source.height == SLOT_HEIGHT) return source

        val scale = maxOf(
            SLOT_WIDTH.toFloat() / source.width,
            SLOT_HEIGHT.toFloat() / source.height,
        )
        val dx = (SLOT_WIDTH - source.width * scale) / 2f
        val dy = (SLOT_HEIGHT - source.height * scale) / 2f
        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(dx, dy)
        }
        val out = Bitmap.createBitmap(SLOT_WIDTH, SLOT_HEIGHT, Bitmap.Config.ARGB_8888)
        Canvas(out).drawBitmap(source, matrix, null)
        return out
    }

    companion object {
        /** heartbeat.svga 中可替换图片图层的 imageKey（"heartbeat" 是音轨，勿替换） */
        private const val IMAGE_KEY = "heart"

        /** 图槽原始像素尺寸，与 heartbeat.svga 中 heart 的 layout 一致 */
        private const val SLOT_WIDTH = 194
        private const val SLOT_HEIGHT = 178
    }
}
