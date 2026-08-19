package com.example.william.my.module.kotlin.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.kotlin.viewmodel.CoroutinesVMFactory
import com.example.william.my.module.kotlin.viewmodel.CoroutinesViewModel

/**
 * Android 上的 Kotlin 协程
 *
 * 协程是 Kotlin 的轻量级线程管理方案，简化异步编程。
 *
 * 核心优势：
 * - 轻量级：一个线程可运行成千上万个协程
 * - 结构化并发：自动管理协程生命周期
 * - 代码简洁：用同步代码风格写异步逻辑
 *
 * 在 Android 中的使用：
 * - viewModelScope.launch {} — 在 ViewModel 中启动协程
 * - withContext(Dispatchers.IO) — 切换到 IO 线程执行耗时操作
 * - Flow — 响应式数据流，替代 LiveData 处理异步数据
 *
 * 本示例使用 ViewModel + 协程发起网络请求，展示基本用法。
 *
 * https://developer.android.google.cn/kotlin/coroutines
 */
@Route(path = RouterPath.Kotlin.Coroutines)
class CoroutinesActivity : BasicResponseActivity() {

    private val mViewModel: CoroutinesViewModel by viewModels {
        CoroutinesVMFactory
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项发起协程登录请求")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("协程登录请求")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        if (position == 0) {
            appendLog("发起登录请求...")
            mViewModel.login(Constants.Value_Username, Constants.Value_Password)
        }
    }

    override fun observeViewModel() {
        super.observeViewModel()

        mViewModel.login.observe(this) {
            appendLog("登录结果: $it")
        }
    }
}