package com.example.william.my.module.widget_thirdparty.activity.widget

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityPhotoViewBinding

/**
 * PhotoView — 支持手势缩放的图片控件
 *
 * PhotoView 是一个支持手势缩放、平移的 ImageView 替代品。
 *
 * 核心机制与避坑点：
 * 1. 手势缩放：支持双击缩放、双指缩放
 * 2. 平移拖拽：缩放后可拖拽查看细节
 * 3. 边界检测：缩放后自动检测边界，防止超出范围
 * 4. 兼容性好：可作为普通 ImageView 使用
 *
 * https://github.com/chrisbanes/PhotoView
 */
@Route(path = RouterPath.WidgetThirdparty.PhotoView)
class PhotoViewActivity : BaseVBActivity<WidgetThirdpartyActivityPhotoViewBinding>() {

    override fun getViewBinding(): WidgetThirdpartyActivityPhotoViewBinding = WidgetThirdpartyActivityPhotoViewBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        showPhotoView()
    }

    private fun showPhotoView() {
        binding.photoView.setImageResource(R.drawable.shared_ic_launcher)
    }
}
