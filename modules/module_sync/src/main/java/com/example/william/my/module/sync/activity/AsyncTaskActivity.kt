package com.example.william.my.module.sync.activity

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.lang.ref.WeakReference

/**
 * AsyncTask（已废弃）— 异步任务演示
 */
@Route(path = RouterPath.Sync.AsyncTask)
class AsyncTaskActivity : BasicResponseActivity() {

    private var mAsyncTask: MyAsyncTask? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initAsyncTask()
    }

    private fun initAsyncTask() {
        mAsyncTask = MyAsyncTask(this@AsyncTaskActivity)
    }

    public override fun onResponseClick(view: View) {
        super.onResponseClick(view)

        executeAsyncTask()
    }

    private fun executeAsyncTask() {
        mAsyncTask?.execute()
    }

    override fun onDestroy() {
        super.onDestroy()

        cancelAsyncTask()
    }

    private fun cancelAsyncTask() {
        mAsyncTask?.let { task ->
            if (!task.isCancelled && task.status == AsyncTask.Status.RUNNING) {
                task.cancel(true)
            }
        }
    }

    /**
     * AsyncTask（已废弃于 API 30）— 异步任务演示
     * 泛型参数：<Params, Progress, Result>
     *   - Params: doInBackground 入参类型
     *   - Progress: onProgressUpdate 入参类型（publishProgress 发送）
     *   - Result: doInBackground 返回值类型，onPostExecute 入参
     *
     * 注意：生产代码应使用 Kotlin Coroutines 替代
     */
    @Suppress("deprecation")
    private open class MyAsyncTask(activity: AsyncTaskActivity?) : AsyncTask<Int?, Int?, Void?>() {

        private val weakReference: WeakReference<AsyncTaskActivity?> = WeakReference(activity)

        /**
         * 在execute被调用后立即执行
         * 一般用来执行后台操作前对UI做一些标记
         */
        override fun onPreExecute() {
            weakReference.get()?.showResponse("onPreExecute — 任务开始前")
        }

        /**
         * 必须重写
         * AsyncTask的关键，用于执行耗时操作
         * 执行过程中可以调用publishProgress来更新进度信息
         */
        override fun doInBackground(vararg params: Int?): Void? {
            var i = 10
            while (i <= 100) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                publishProgress(i)
                i += 10
            }
            return null
        }

        /**
         * 调用publishProgress时，此方法被执行
         * 将进度信息更新到UI组件
         */
        override fun onProgressUpdate(vararg values: Int?) {
            weakReference.get()?.showResponse("进度：${values[0]}%")
        }

        /**
         * 当后台操作结束时，此方法会被调用，
         * 将计算结果传递到此方法中，直接将结果显示到UI组件。
         */
        override fun onPostExecute(aVoid: Void?) {
            weakReference.get()?.showResponse("onPostExecute — 任务完成后")
        }
    }
}
