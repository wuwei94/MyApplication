package com.example.william.my.module.component.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * ActivityResultContracts 新版结果回调 API
 *
 * 演示 StartActivityForResult 和自定义 ActivityResultContract 的用法。
 * 替代已废弃的 startActivityForResult + onActivityResult。
 */
@Route(path = RouterPath.Component.ActivityResult)
class ActivityResultActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("ActivityResultContracts\n\n点击下方按钮启动目标页，接收回传数据")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "StartActivityForResult — 启动目标页并接收回传",
            "ActivityResultContract — 封装 Intent 构建和结果解析"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> startForResult()
            1 -> startWithCustomContract()
        }
    }

    private fun startForResult() {
        val intent = Intent(this, ActivityResultActivity2::class.java).apply {
            putExtra("input", "来自 StartActivityForResult")
        }
        startActivityForResult.launch(intent)
    }

    private val startActivityForResult =
        registerForActivityResult(StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                appendLog("StartActivityForResult 收到回传")
            } else {
                appendLog("StartActivityForResult 用户取消")
            }
        }

    private fun startWithCustomContract() {
        customContract.launch("来自 ActivityResultContract")
    }

    private val customContract =
        registerForActivityResult(CustomResultContract()) { result ->
            if (result != null) {
                appendLog("ActivityResultContract 收到回传")
            } else {
                appendLog("ActivityResultContract 用户取消")
            }
        }

    /**
     * 自定义 ActivityResultContract 示例。
     * 输入 String，输出 String?，封装了 Intent 构建和结果解析。
     */
    class CustomResultContract : ActivityResultContract<String, String?>() {

        override fun createIntent(context: Context, input: String): Intent {
            return Intent(context, ActivityResultActivity2::class.java).apply {
                putExtra("input", input)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String? {
            val data = intent?.getStringExtra("result")
            return if (resultCode == RESULT_OK && !TextUtils.isEmpty(data)) data else null
        }
    }
}
