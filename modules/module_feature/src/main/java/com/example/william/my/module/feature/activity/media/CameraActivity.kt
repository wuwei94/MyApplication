package com.example.william.my.module.feature.activity.media

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.feature.databinding.FeatureActivityCameraBinding
import com.example.william.my.module.feature.utils.CameraUtils

@Route(path = RouterPath.Feature.Camera)
class CameraActivity : BaseVBActivity<FeatureActivityCameraBinding>(), View.OnClickListener {

    override fun getViewBinding(): FeatureActivityCameraBinding {
        return FeatureActivityCameraBinding.inflate(layoutInflater)
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
