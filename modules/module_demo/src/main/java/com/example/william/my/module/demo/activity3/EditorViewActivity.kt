package com.example.william.my.module.demo.activity3

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.path.RouterPath
import com.example.william.my.lib.activity.BaseVBActivity
import com.example.william.my.module.demo.R
import com.example.william.my.module.demo.databinding.DemoActivityEditorViewBinding
import com.example.william.my.module.demo.widget.EditorView

@Route(path = RouterPath.Demo.EditorView)
class EditorViewActivity : BaseVBActivity<DemoActivityEditorViewBinding>() {

    override fun getViewBinding(): DemoActivityEditorViewBinding {
        return DemoActivityEditorViewBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        val editorView = findViewById<EditorView>(R.id.editorView)

        // 1. 加载一张图片 (实际开发中从相册获取)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.demo_infinite_image)
        editorView.setImage(bitmap)

        // 2. 贴纸
        findViewById<Button>(R.id.btnAddSticker).setOnClickListener {
            val stickerBm = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher)
            editorView.addSticker(stickerBm)
        }

        // 3. 文字
        findViewById<Button>(R.id.btnAddText).setOnClickListener {
            editorView.addText("Hello World")
        }

        // 4. 旋转
        findViewById<Button>(R.id.btnRotate).setOnClickListener {
            editorView.rotateImage90()
        }

        // 5. 虚化开关
        findViewById<Button>(R.id.btnBlur).setOnClickListener {
            editorView.isBlurActive = !editorView.isBlurActive
            editorView.applyEffects()
        }

        // 6. 亮度调节 (-100 到 100)
        findViewById<SeekBar>(R.id.seekBrightness).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // progress is 0-200, map to -100 to 100
                val value = (progress - 100).toFloat()
                editorView.brightness = value * 2
                editorView.applyEffects()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}