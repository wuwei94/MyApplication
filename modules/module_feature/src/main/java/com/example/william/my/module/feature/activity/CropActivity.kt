package com.example.william.my.module.feature.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.router.service.ImageUtilsService
import com.example.william.my.basic.basic_shared.utils.Utils
import java.io.File

/**
 * 图片裁剪示例（系统 Intent 调用）
 *
 * 核心注意点：
 * 1. Android 7.0+ (API 24+) 禁止向第三方应用直接暴露 `file://` URI，否则将抛出 [android.os.FileUriExposedException]。
 *    必须使用 [FileProvider.getUriForFile] 生成 `content://` URI 并附加读写权限标志。
 * 2. `com.android.camera.action.CROP` 为 Android 非公开标准 Intent（部分厂商定制 ROM 可能未内置裁剪 App），
 *    实际商业应用建议优先使用 UCrop 等成熟内嵌裁剪库。
 * 3. 结果回调已全面迁移至 AndroidX [registerForActivityResult]。
 */
@Route(path = RouterPath.Feature.Crop)
class CropActivity : BasicImageActivity() {

    private var photoCaptureUri: Uri? = null
    private var cropDestinationUri: Uri? = null

    // 1. 图库选择 Launcher
    private val albumLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val sourceUri = result.data?.data
            if (sourceUri != null) {
                startCrop(sourceUri)
            } else {
                Utils.toast("未能获取图库选取的图片")
            }
        }
    }

    // 2. 缩略图拍照 Launcher
    private val cameraThumbLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val extras = result.data?.extras
            val bitmap = if (extras != null) {
                androidx.core.os.BundleCompat.getParcelable(extras, "data", Bitmap::class.java)
            } else null
            val sourceUri = saveBitmap2Uri(bitmap)
            if (sourceUri != null) {
                startCrop(sourceUri)
            } else {
                Utils.toast("保存拍照缩略图失败")
            }
        }
    }

    // 3. 高清原图拍照 Launcher
    private val cameraFullLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            photoCaptureUri?.let { uri ->
                startCrop(uri)
            } ?: Utils.toast("未找到拍摄的高清原图 URI")
        }
    }

    // 4. 图片裁剪 Launcher
    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadCroppedImage()
        }
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "从图库选择并裁剪",
            "拍摄照片并裁剪（缩略图）",
            "拍摄高清照片并裁剪（原图）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> pickFromAlbum()
            1 -> captureThumbnail()
            2 -> captureFullPhoto()
        }
    }

    private fun pickFromAlbum() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        try {
            albumLauncher.launch(intent)
        } catch (e: Exception) {
            Utils.toast("打开图库失败: ${e.message}")
        }
    }

    private fun captureThumbnail() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            cameraThumbLauncher.launch(intent)
        } catch (e: Exception) {
            Utils.toast("启动相机失败: ${e.message}")
        }
    }

    private fun captureFullPhoto() {
        val file = File(externalCacheDir, "PHOTO_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "$packageName.fileProvider", file)
        photoCaptureUri = uri

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        try {
            cameraFullLauncher.launch(intent)
        } catch (e: Exception) {
            Utils.toast("启动相机失败: ${e.message}")
        }
    }

    /**
     * 发起系统裁剪 Intent
     */
    private fun startCrop(sourceUri: Uri) {
        val destinationFile = File(externalCacheDir, "CROP_${System.currentTimeMillis()}.jpg")
        val destinationUri = FileProvider.getUriForFile(
            this,
            "$packageName.fileProvider",
            destinationFile
        )
        this.cropDestinationUri = destinationUri

        val intent = Intent("com.android.camera.action.CROP").apply {
            setDataAndType(sourceUri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra("crop", "true")
            putExtra("aspectX", 1)
            putExtra("aspectY", 1)
            putExtra("outputX", 1024)
            putExtra("outputY", 1024)
            putExtra("scale", true)
            putExtra("return-data", false)
            putExtra(MediaStore.EXTRA_OUTPUT, destinationUri)
            putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            putExtra("noFaceDetection", true)
        }

        // 兼容性授权：为响应裁剪动作的应用授予 URI 临时访问权限
        val resolveInfos = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolveInfo in resolveInfos) {
            val targetPkg = resolveInfo.activityInfo.packageName
            grantUriPermission(targetPkg, sourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            grantUriPermission(
                targetPkg,
                destinationUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }

        try {
            cropLauncher.launch(intent)
        } catch (e: Exception) {
            Utils.toast("系统不支持裁剪或未找到裁剪应用: ${e.message}")
        }
    }

    /**
     * 加载裁剪输出的 Bitmap 并展示
     */
    private fun loadCroppedImage() {
        val uri = cropDestinationUri ?: return
        try {
            val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
            if (bitmap != null) {
                mBinding.basicsImage.setImageBitmap(bitmap)
                Utils.toast("图片裁剪完成")
            } else {
                Utils.toast("无法解码裁剪图片")
            }
        } catch (e: Exception) {
            Utils.toast("读取裁剪结果失败: ${e.message}")
        }
    }

    private fun saveBitmap2Uri(bitmap: Bitmap?): Uri? {
        if (bitmap == null) return null
        val file = File(externalCacheDir, "THUMB_${System.currentTimeMillis()}.jpg")
        return try {
            val service = ARouter.getInstance().build(RouterPath.Service.ImageUtilsService)
                .navigation() as? ImageUtilsService
            val successful = service?.save(bitmap, file, Bitmap.CompressFormat.JPEG) ?: false
            if (successful) {
                FileProvider.getUriForFile(this, "$packageName.fileProvider", file)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
