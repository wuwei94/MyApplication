package com.example.william.my.module.async.activity

import android.os.AsyncTask
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.lang.ref.WeakReference

/**
 * AsyncTask — 经典异步任务机制（已废弃）
 *
 * ⚠️ 历史参考：AsyncTask 在 API 30 已废弃，生产代码应使用 Kotlin 协程（Coroutines）替代。
 * 本页保留展示其三泛型参数设计、生命周期回调时序与现代协程迁移方案。
 *
 * 核心机制与避坑点：
 * 1. 三泛型设计：`<Params, Progress, Result>` 分别对应入参、进度与最终结果类型
 * 2. 回调顺序：onPreExecute（主线程）→ doInBackground（工作线程）→ onProgressUpdate（主线程）→ onPostExecute（主线程）
 * 3. 线程切换：内部基于静态线程池与 Handler 实现主后台线程切换
 * 4. 协程迁移：使用 viewModelScope.launch + withContext(Dispatchers.IO) 代替 execute
 *
 * 协程现代替代方案见 [CoroutinesActivity]。
 *
 * https://developer.android.google.cn/reference/android/os/AsyncTask
 */
@Route(path = RouterPath.Async.AsyncTask)
class AsyncTaskActivity : BasicResponseActivity() {

    private var asyncTask: MyAsyncTask? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("AsyncTask（已废弃）— 异步任务演示")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "execute() — 执行任务",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> executeAsyncTask()
        }
    }

    private fun executeAsyncTask() {
        asyncTask?.cancel(true)
        asyncTask = MyAsyncTask(this)
        asyncTask?.execute()
    }

    override fun onDestroy() {
        super.onDestroy()
        asyncTask?.cancel(true)
    }

    @Suppress("deprecation")
    private open class MyAsyncTask(activity: AsyncTaskActivity?) : AsyncTask<Int?, Int?, Void?>() {

        private val weakReference: WeakReference<AsyncTaskActivity?> = WeakReference(activity)

        override fun onPreExecute() {
            weakReference.get()?.appendLog("onPreExecute — 任务开始前")
        }

        override fun doInBackground(vararg params: Int?): Void? {
            var i = 10
            while (i <= 100) {
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    return null
                }
                publishProgress(i)
                i += 10
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Int?) {
            weakReference.get()?.appendLog("onProgressUpdate — 进度：${values[0]}%")
        }

        override fun onPostExecute(aVoid: Void?) {
            weakReference.get()?.appendLog("onPostExecute — 任务完成后")
        }
    }
}
