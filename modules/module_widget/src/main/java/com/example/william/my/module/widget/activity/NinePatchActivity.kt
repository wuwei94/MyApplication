package com.example.william.my.module.widget.activity

import android.os.Bundle
import android.view.Gravity
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.ninepatch.NinePatchHelper

/**
 * 9-patch 图片演示
 *
 * 拉伸区域(左上)：设置拉伸区域
 * 内容区域(右下)：识别内容区域
 */
@Route(path = RouterPath.Widget.NinePatch)
class NinePatchActivity : BasicResponseActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "Asset .9 图片",
            "Network .9 图片",
            "清空",
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方按钮加载 9-patch 图片")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                NinePatchHelper.ninePatchChunk(this, mBinding.basicsResponse, Constants.Url_NinePatchAsset)
                appendLog("已加载 Asset .9 图片")
            }

            1 -> {
                NinePatchHelper.ninePatchChunk(this, mBinding.basicsResponse, Constants.Url_NinePatchNetwork)
                appendLog("已加载 Network .9 图片")
            }

            2 -> {
                mBinding.basicsResponse.background = null
                appendLog("已清空背景")
            }
        }
    }
}
