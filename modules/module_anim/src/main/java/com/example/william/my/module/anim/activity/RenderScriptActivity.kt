package com.example.william.my.module.anim.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlin.math.max
import kotlin.math.min

/**
 * RenderScript 模糊 — 已废弃（API 31 起废弃）
 *
 * ScriptIntrinsicBlur 是 Android 原生的高斯模糊实现，API 31 后被 RenderEffect 取代。
 * 保留此页面作为历史参考，展示废弃 API 的用法。
 *
 * 核心步骤：
 * 1. RenderScript.create() — 创建上下文
 * 2. ScriptIntrinsicBlur.create() — 创建模糊脚本
 * 3. Allocation.createFromBitmap() — 创建输入/输出缓冲区
 * 4. blurScript.forEach() — 执行模糊
 * 5. outputAlloc.copyTo() — 取出结果
 */
@Route(path = RouterPath.Anim.RenderScript)
class RenderScriptActivity : BasicImageActivity() {

    private var renderScript: RenderScript? = null
    private lateinit var originalBitmap: Bitmap
    private var isBlurred = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher, null)!!
        originalBitmap = drawable.toBitmap()
        showImage(originalBitmap)
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "RenderScript 高斯模糊（已废弃）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        if (isBlurred) {
            clearBlur()
        } else {
            applyBlur()
        }
    }

    private fun applyBlur() {
        val blurred = blurWithRenderScript(originalBitmap, 20f)
        showImage(blurred)
        isBlurred = true
    }

    private fun clearBlur() {
        showImage(originalBitmap)
        isBlurred = false
    }

    private fun blurWithRenderScript(bitmap: Bitmap, radius: Float): Bitmap {
        renderScript = RenderScript.create(this)

        val clampedRadius = max(0.1f, min(25.0f, radius))
        val output = createBitmap(bitmap.width, bitmap.height)

        val input = Allocation.createFromBitmap(renderScript, bitmap)
        val outputAlloc = Allocation.createFromBitmap(renderScript, output)

        val blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        blurScript.setInput(input)
        blurScript.setRadius(clampedRadius)
        blurScript.forEach(outputAlloc)

        outputAlloc.copyTo(output)

        input.destroy()
        outputAlloc.destroy()

        return output
    }

    override fun onDestroy() {
        super.onDestroy()
        renderScript?.destroy()
    }
}
