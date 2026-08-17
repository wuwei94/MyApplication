package com.example.william.my.module.utils.activity

import android.os.Bundle
import android.os.SystemClock
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ThreadUtils
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 线程池与线程切换工具类演示
 *
 * - CPU 密集型任务：线程池中线程个数应尽量少，推荐配置为 (CPU 核心数 + 1)
 * - IO 密集型任务：线程池可配置较多线程以提高利用率，推荐配置为 (2 * CPU 核心数 + 1)
 */
@Route(path = RouterPath.Utils.ThreadUtils)
class ThreadUtilsActivity : BasicResponseActivity() {

    private var mRunningTask: ThreadUtils.Task<String>? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 BlankJ ThreadUtils 线程切换与线程池任务执行")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "isMainThread (判断当前线程)",
            "runOnUiThread (切换至主线程)",
            "executeByIo (异步 IO 任务)",
            "executeByCpu (CPU 密集型任务)",
            "cancel (取消正在执行的任务)"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                appendLog("isMainThread: ${ThreadUtils.isMainThread()} (当前线程: ${Thread.currentThread().name})")
            }

            1 -> {
                ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Unit>() {
                    override fun doInBackground() {
                        ThreadUtils.runOnUiThread {
                            appendLog("runOnUiThread 执行 (当前线程: ${Thread.currentThread().name})")
                        }
                    }

                    override fun onSuccess(result: Unit?) {}
                })
            }

            2 -> {
                ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<String>() {
                    override fun doInBackground(): String {
                        SystemClock.sleep(300)
                        return "IO 异步任务完成 (执行线程: ${Thread.currentThread().name})"
                    }

                    override fun onSuccess(result: String?) {
                        appendLog("$result -> 回调线程: ${Thread.currentThread().name}")
                    }
                })
            }

            3 -> {
                ThreadUtils.executeByCpu(object : ThreadUtils.SimpleTask<Int>() {
                    override fun doInBackground(): Int {
                        return (1..1000).sum()
                    }

                    override fun onSuccess(result: Int?) {
                        appendLog("CPU 任务求和 (1..1000) 结果: $result (回调线程: ${Thread.currentThread().name})")
                    }
                })
            }

            4 -> {
                val task = object : ThreadUtils.Task<String>() {
                    override fun doInBackground(): String {
                        for (i in 1..5) {
                            if (isCanceled) return "任务已取消"
                            SystemClock.sleep(200)
                        }
                        return "任务正常执行完毕"
                    }

                    override fun onSuccess(result: String?) {
                        appendLog(result.orEmpty())
                    }

                    override fun onCancel() {
                        appendLog("Task onCancel 触发 (任务已被成功取消)")
                    }

                    override fun onFail(t: Throwable?) {
                        appendLog("Task 发生异常: ${t?.message}")
                    }
                }
                mRunningTask = task
                ThreadUtils.executeByIo(task)
                appendLog("启动长时间异步任务，准备取消...")
                ThreadUtils.getMainHandler().postDelayed({
                    ThreadUtils.cancel(task)
                    appendLog("调用 ThreadUtils.cancel(task)")
                }, 300)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRunningTask?.let { ThreadUtils.cancel(it) }
    }
}