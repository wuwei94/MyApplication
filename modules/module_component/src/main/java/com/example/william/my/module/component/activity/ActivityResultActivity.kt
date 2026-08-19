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
 * ActivityResultContracts — 新版结果回调 API
 *
 * ActivityResultContracts 是 AndroidX 提供的新版结果回调 API，替代已废弃的 startActivityForResult。
 *
 * 核心特性：
 * 1. 类型安全：使用泛型确保输入输出类型安全
 * 2. 生命周期感知：自动处理生命周期，避免内存泄漏
 * 3. 代码简洁：无需重写 onActivityResult，代码更简洁
 * 4. 自定义 Contract：支持自定义 ActivityResultContract 封装业务逻辑
 *
 * 内置 Contract：
 * 1. StartActivityForResult：启动 Activity 并接收结果
 * 2. RequestPermission：请求单个权限
 * 3. RequestMultiplePermissions：请求多个权限
 * 4. TakePicturePreview：拍照并获取缩略图
 * 5. TakePicture：拍照并获取原图
 * 6. TakeVideo：录像并获取视频
 *
 * 基本用法：
 * ```kotlin
 * // 注册回调
 * val launcher = registerForActivityResult(StartActivityForResult()) { result ->
 *     if (result.resultCode == RESULT_OK) {
 *         // 处理结果
 *     }
 * }
 *
 * // 启动 Activity
 * launcher.launch(intent)
 *
 * // 自定义 Contract
 * class MyContract : ActivityResultContract<String, String?>() {
 *     override fun createIntent(context: Context, input: String): Intent {
 *         return Intent(context, TargetActivity::class.java).apply {
 *             putExtra("input", input)
 *         }
 *     }
 *     override fun parseResult(resultCode: Int, intent: Intent?): String? {
 *         return if (resultCode == RESULT_OK) intent?.getStringExtra("result") else null
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - Activity 间数据传递
 * - 权限请求
 * - 拍照、录像、选择图片
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
