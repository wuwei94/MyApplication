package com.example.william.my.module.gpuimage.filter

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.gpuimage.databinding.GpuimageActivityFilterBinding
import com.example.william.my.module.gpuimage.helper.GpuImageChipHelper
import com.example.william.my.module.gpuimage.helper.GpuImageFilterCatalog
import com.example.william.my.module.gpuimage.helper.GpuImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * GPUImage — 滤镜实时预览
 *
 * GPUImage 是移植自 iOS GPUImage 的 OpenGL ES 图像滤镜库，通过 GPUImageView
 * 在 GPU 上逐帧渲染滤镜。本页演示静态图片的滤镜实时预览与切换。
 *
 * 核心机制与避坑点：
 * 1. 载入图片：setImage(Bitmap / Uri) 加载 assets 示例或相册选图
 * 2. 滤镜切换：setFilter(GPUImageFilter) 应用滤镜并请求重绘，GPU 逐帧渲染
 * 3. 保存结果：capture() 后台线程取回当前帧 Bitmap，存入系统相册
 * 4. 16 种内置滤镜：复用 GpuImageFilterCatalog.FILTERS，含直通滤镜（原图）
 *
 * https://github.com/cats-oss/android-gpuimage
 */
@Route(path = RouterPath.GpuImage.Filter)
class GpuImageFilterActivity :
    BaseVBActivity<GpuimageActivityFilterBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): GpuimageActivityFilterBinding = GpuimageActivityFilterBinding.inflate(layoutInflater)

    private var currentBitmap: Bitmap? = null
    private var selectedIndex = 0

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { loadBitmapFromUri(it) }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.btnSampleDog.setOnClickListener(this)
        binding.btnSampleCar.setOnClickListener(this)
        binding.btnPickImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        GpuImageChipHelper.populate(
            container = binding.chipContainer,
            names = GpuImageFilterCatalog.FILTERS.map { it.name },
            initialIndex = selectedIndex,
        ) { index -> applyFilter(index) }

        // 初始展示内置示例 1
        loadAssetSample(GpuImageHelper.SAMPLE_DOG)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSampleDog -> loadAssetSample(GpuImageHelper.SAMPLE_DOG)
            binding.btnSampleCar -> loadAssetSample(GpuImageHelper.SAMPLE_CAR)
            binding.btnPickImage -> pickImageLauncher.launch("image/*")
            binding.btnSave -> saveCurrentFrame()
        }
    }

    /** 切换滤镜：工厂创建新滤镜实例并交给 GPUImageView 重绘 */
    private fun applyFilter(index: Int) {
        selectedIndex = index
        val spec = GpuImageFilterCatalog.FILTERS[index]
        binding.gpuImageView.setFilter(spec.factory())
        binding.tvCurrentFilter.text = spec.name
    }

    private fun loadAssetSample(fileName: String) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                GpuImageHelper.decodeAsset(this@GpuImageFilterActivity, fileName)
            }
            showBitmap(bitmap)
        }
    }

    private fun loadBitmapFromUri(uri: Uri) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                GpuImageHelper.decodeUri(this@GpuImageFilterActivity, uri)
            }
            showBitmap(bitmap)
        }
    }

    private fun showBitmap(bitmap: Bitmap?) {
        if (bitmap == null) {
            Toast.makeText(this, "图片解码失败", Toast.LENGTH_SHORT).show()
            return
        }
        currentBitmap?.takeIf { it !== bitmap }?.recycle()
        currentBitmap = bitmap
        binding.gpuImageView.setImage(bitmap)
        // 换图后重放当前选中滤镜
        applyFilter(selectedIndex)
    }

    /** 后台线程 capture() 取帧并保存到系统相册（capture 不允许在 UI 线程调用） */
    private fun saveCurrentFrame() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val frame = binding.gpuImageView.capture()
                    val uri = GpuImageHelper.saveToGallery(this@GpuImageFilterActivity, frame)
                    frame.recycle()
                    uri
                }
            }
            result.onSuccess { uri ->
                val message = if (uri != null) "已保存到系统相册（Pictures/GPUImage）" else "保存失败"
                Toast.makeText(this@GpuImageFilterActivity, message, Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@GpuImageFilterActivity, "保存失败: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.gpuImageView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.gpuImageView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentBitmap?.recycle()
        currentBitmap = null
    }
}
