package com.example.william.my.basic.basic_shared.activity

import android.graphics.Bitmap
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutRecyclerImageBinding

/**
 * 图片/动画类示例 Activity 基类。
 *
 * 布局结构：
 * - 上方展示：ImageView 图片/动画展示区（[mBinding.basicsImage]）
 * - 下方列表：RecyclerView 操作列表（通过 [buildList] 与 [onRecyclerClick] 触发操作）
 *
 * 约定与规范：
 * 1. 禁止点击上方 ImageView 触发操作，所有演示行为必须由下方列表项触发。
 * 2. 继承类实现 [buildList] 提供操作项，并在 [onRecyclerClick] 中执行相应动作。
 * 3. 支持使用 [showImage] 在主线程更新展示的图片或资源。
 */
abstract class BasicImageActivity : BasicControlActivity() {

    protected lateinit var mBinding: SharedLayoutRecyclerImageBinding

    override fun initViewBinding() {
        mBinding = SharedLayoutRecyclerImageBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mRecycler = mBinding.basicsRecycler
    }

    /**
     * 在主线程更新展示的 Bitmap 图片。
     */
    protected fun showImage(bitmap: Bitmap?) {
        runOnUiThread {
            mBinding.basicsImage.setImageBitmap(bitmap)
        }
    }

    /**
     * 在主线程更新展示的 Drawable 资源图片。
     */
    protected fun showImage(resId: Int) {
        runOnUiThread {
            mBinding.basicsImage.setImageResource(resId)
        }
    }
}
