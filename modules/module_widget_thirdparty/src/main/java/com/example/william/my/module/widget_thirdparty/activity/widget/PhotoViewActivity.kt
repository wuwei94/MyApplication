package com.example.william.my.module.widget_thirdparty.activity.widget

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityPhotoViewBinding

/**
 * PhotoView — 支持手势缩放的图片控件
 *
 * PhotoView 是一个支持手势缩放、平移的 ImageView 替代品。
 *
 * 核心特性：
 * 1. 手势缩放：支持双击缩放、双指缩放
 * 2. 平移拖拽：缩放后可拖拽查看细节
 * 3. 边界检测：缩放后自动检测边界，防止超出范围
 * 4. 兼容性好：可作为普通 ImageView 使用
 *
 * 基本用法：
 * ```kotlin
 * // XML 中使用
 * <com.github.chrisbanes.photoview.PhotoView
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 *
 * // 代码中设置图片
 * photoView.setImageResource(R.drawable.image)
 * ```
 *
 * 适用场景：
 * - 图片详情页、大图预览
 * - 地图、长图查看
 * - 需要缩放查看细节的场景
 *
 * https://github.com/chrisbanes/PhotoView
 */
@Route(path = RouterPath.WidgetThirdparty.PhotoView)
class PhotoViewActivity : BaseVBActivity<WidgetThirdpartyActivityPhotoViewBinding>() {

    override fun getViewBinding(): WidgetThirdpartyActivityPhotoViewBinding {
        return WidgetThirdpartyActivityPhotoViewBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        showPhotoView()
    }

    private fun showPhotoView() {
        mBinding.photoView.setImageResource(R.drawable.shared_ic_launcher)
    }
}
