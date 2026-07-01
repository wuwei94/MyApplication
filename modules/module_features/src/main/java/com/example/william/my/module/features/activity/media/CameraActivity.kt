package com.example.william.my.module.features.activity.media

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.path.RouterPath
import com.example.william.my.lib.activity.BaseVBActivity
import com.example.william.my.module.features.databinding.FeaturesActivityCameraBinding
import com.example.william.my.module.features.utils.CameraUtils

@Route(path = RouterPath.Features.Media.Camera)
class CameraActivity : BaseVBActivity<FeaturesActivityCameraBinding>(), View.OnClickListener {

    override fun getViewBinding(): FeaturesActivityCameraBinding {
        return FeaturesActivityCameraBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        CameraUtils.setupCamera(this, mBinding.previewView)

        mBinding.btnCapture.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.btnCapture -> {
                CameraUtils.captureImage(this) {
                    mBinding.imageView.setImageBitmap(it)
                }
            }

            mBinding.btnRecordStart -> {
                CameraUtils.startRecording(this) {

                }
            }

            mBinding.btnRecordStart -> {
                CameraUtils.stopRecording()
            }
        }
    }
}
