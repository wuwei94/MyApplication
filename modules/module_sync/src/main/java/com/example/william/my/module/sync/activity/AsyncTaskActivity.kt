package com.example.william.my.module.sync.activity

import android.os.AsyncTask
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.lang.ref.WeakReference

/**
 * AsyncTask（已废弃）— 异步任务演示
 *
 * 泛型参数：<Params, Progress, Result>
 *   - Params: doInBackground 入参类型
 *   - Progress: onProgressUpdate 入参类型（publishProgress 发送）
 *   - Result: doInBackground 返回值类型，onPostExecute 入参
 *
 * 回调顺序：onPreExecute → doInBackground → onProgressUpdate → onPostExecute
 *
 * 注意：生产代码应使用 Kotlin Coroutines 替代
 */
@Route(path = RouterPath.Sync.AsyncTask)
class AsyncTaskActivity : BasicResponseActivity() {

    private var mAsyncTask: MyAsyncTask? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        showResponse("AsyncTask（已废弃）— 异步任务演示")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "execute() — 执行任务"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> executeAsyncTask()
        }
    }

    private fun executeAsyncTask() {
        mAsyncTask?.cancel(true)
        mAsyncTask = MyAsyncTask(this)
        mAsyncTask?.execute()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAsyncTask?.cancel(true)
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
