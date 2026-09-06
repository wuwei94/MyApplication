package com.example.william.my.module.database.objectbox

import android.content.Context
import android.util.Log
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.module.database.BuildConfig
import io.objectbox.BoxStore
import io.objectbox.android.Admin
import io.objectbox.exception.DbException
import io.objectbox.exception.FileCorruptException

/**
 * ObjectBox 数据库管理
 *
 * 负责初始化 BoxStore，处理数据库文件损坏等异常，并在 debug 构建启用 Admin。
 */
object ObjectBox {

    private val TAG = this.javaClass.simpleName

    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        if (::boxStore.isInitialized && !boxStore.isClosed) {
            return
        }
        // 在 Android 上构建 Store 时必须传入 Context。
        boxStore = try {
            MyObjectBox.builder()
                .androidContext(context.applicationContext)
                .build()
        } catch (e: DbException) {
            if (e.javaClass == DbException::class.java || e is FileCorruptException) {
                // 演示如何处理设备文件系统损坏导致的问题
                Log.w(TAG, "File corrupt, trying previous data snapshot...", e)
                return
            } else {
                // 由于开发者错误导致 BoxStore 构建失败。
                throw e
            }
        }

        if (BuildConfig.DEBUG) {
            Utils.logcat(
                TAG,
                String.format(
                    "Using ObjectBox %s (%s)",
                    BoxStore.getVersion(),
                    BoxStore.getVersionNative(),
                ),
            )
            // 在 debug 构建中启用 ObjectBox Admin（https://docs.objectbox.io/data-browser）。
            Admin(boxStore).start(context.applicationContext)
        }
    }
}
