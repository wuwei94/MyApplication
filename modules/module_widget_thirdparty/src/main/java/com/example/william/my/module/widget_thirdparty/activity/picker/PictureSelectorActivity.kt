package com.example.william.my.module.widget_thirdparty.activity.picker

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.adapter.PictureSelectorAdapter
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityPictureSelectorBinding
import com.example.william.my.module.widget_thirdparty.engine.GlideEngine
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener

/**
 * PictureSelector — 图片选择器
 *
 * PictureSelector 是一个功能强大的图片选择库，支持图片、视频、音频的选择和预览。
 *
 * 核心特性：
 * 1. 多媒体选择：支持图片、视频、音频的选择
 * 2. 丰富的配置：支持裁剪、压缩、过滤等配置
 * 3. 预览功能：支持图片、视频的预览和删除
 * 4. 多种来源：支持相册、拍照、录像等多种来源
 *
 * 基本用法：
 * ```kotlin
 * // 打开相册选择图片
 * PictureSelector.create(this)
 *     .openGallery(SelectMimeType.ofImage())
 *     .setImageEngine(GlideEngine.createGlideEngine())
 *     .forResult(object : OnResultCallbackListener<LocalMedia?> {
 *         override fun onResult(result: ArrayList<LocalMedia?>) {
 *             // 处理选择结果
 *         }
 *         override fun onCancel() {}
 *     })
 * ```
 *
 * 适用场景：
 * - 用户头像、商品图片选择
 * - 图片上传、分享功能
 * - 多媒体内容管理
 *
 * https://github.com/LuckSiege/PictureSelector
 */
@Route(path = RouterPath.WidgetThirdparty.PictureSelector)
class PictureSelectorActivity : BaseVBActivity<WidgetThirdpartyActivityPictureSelectorBinding>() {

    private lateinit var mAdapter: PictureSelectorAdapter

    override fun getViewBinding(): WidgetThirdpartyActivityPictureSelectorBinding {
        return WidgetThirdpartyActivityPictureSelectorBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        val manager = GridLayoutManager(this, 4)
        mBinding.recyclerView.layoutManager = manager

        mAdapter = PictureSelectorAdapter()
        mBinding.recyclerView.adapter = mAdapter

        mAdapter.setOnItemClickListener(object : PictureSelectorAdapter.OnItemClickListener {
            override fun openPicture() {
                PictureSelector.create(this@PictureSelectorActivity)
                    .openGallery(SelectMimeType.ofImage())
                    .setImageEngine(GlideEngine.createGlideEngine())
                    .forResult(object : OnResultCallbackListener<LocalMedia?> {
                        override fun onResult(result: ArrayList<LocalMedia?>) {

                            val oldSize: Int = mAdapter.data.size
                            val count = if (result.size == mAdapter.maxSelect) {
                                oldSize + 1
                            } else {
                                oldSize
                            }
                            mAdapter.notifyItemRangeRemoved(0, count)
                            mAdapter.data.clear()

                            mAdapter.data.addAll(result)
                            mAdapter.notifyItemRangeInserted(0, result.size)
                        }

                        override fun onCancel() {

                        }
                    })
            }

            override fun onItemClick(v: View, position: Int) {
                PictureSelector.create(this@PictureSelectorActivity)
                    .openPreview()
                    .setImageEngine(GlideEngine.createGlideEngine())
                    .setExternalPreviewEventListener(object : OnExternalPreviewEventListener {
                        override fun onPreviewDelete(position: Int) {
                            mAdapter.remove(position)
                            mAdapter.notifyItemRemoved(position)
                        }

                        override fun onLongPressDownload(
                            context: Context?,
                            media: LocalMedia?
                        ): Boolean {
                            return false
                        }
                    })
                    .startActivityPreview(position, true, mAdapter.data)
            }
        })
    }
}
