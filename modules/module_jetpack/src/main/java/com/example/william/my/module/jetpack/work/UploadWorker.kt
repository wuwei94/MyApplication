package com.example.william.my.module.jetpack.work

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.william.my.basic.basic_shared.utils.Utils

class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val TAG = this.javaClass.simpleName

    override fun doWork(): Result {
        // 安全读取输入参数（避免强制解包抛 NPE）
        val input = inputData.getString("key") ?: "默认输入数据"
        uploadImages(input)

        // 模拟后台耗时与进度更新（Data 对象大小上限 10KB）
        setProgressAsync(Data.Builder().putInt("progress", 50).build())
        Thread.sleep(1000)
        setProgressAsync(Data.Builder().putInt("progress", 100).build())

        // 创建任务输出数据
        val outputData = Data.Builder()
            .putString("result", "上传成功: $input")
            .build()

        return Result.success(outputData)
    }

    private fun uploadImages(s: String) {
        Utils.logcat(TAG, "uploadImages: $s")
    }
}
