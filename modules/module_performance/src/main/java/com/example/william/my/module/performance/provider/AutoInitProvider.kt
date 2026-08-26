package com.example.william.my.module.performance.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 模拟由 ContentProvider 自动初始化的 SDK
 */
object AutoInitSdk {

    var isInitialized: Boolean = false
        private set

    var initTimestamp: Long = 0L
        private set

    var initThreadName: String = ""
        private set

    var appContext: Context? = null
        private set

    fun init(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext ?: context
        initTimestamp = System.currentTimeMillis()
        initThreadName = Thread.currentThread().name
        isInitialized = true
        Utils.logcat("AutoInitSdk", "AutoInitSdk 通过 AutoInitProvider 自动初始化成功！线程=$initThreadName, 时间戳=$initTimestamp")
    }
}

/**
 * AutoInitProvider — 演示使用 ContentProvider 实现「无侵入自动初始化」
 *
 * 核心原理：
 * 1. Android 进程拉起时，AMS 在 Application.onCreate() 执行之前，会串行拉起所有在 AndroidManifest 中声明的 ContentProvider 并回调其 onCreate()；
 * 2. 利用该时序，第三方 SDK（如早期 LeakCanary、Firebase）可在 ContentProvider.onCreate() 中通过 getContext() 自动完成初始化，免去在 Application.onCreate 中显式调用的侵入性；
 * 3. 弊端：每个 Provider 在系统层面均有反射拉起与 Binder 注册开销，多个 Provider 会显著拖慢冷启动。
 */
class AutoInitProvider : ContentProvider() {

    private val inMemoryItems = mutableListOf("Provider 初始配置项 A", "Provider 初始配置项 B")

    override fun onCreate(): Boolean {
        context?.let { ctx ->
            // 在 Application.onCreate 之前无侵入完成 SDK 初始化
            AutoInitSdk.init(ctx)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(arrayOf("_id", "item_name"))
        when (uriMatcher.match(uri)) {
            CODE_ITEMS -> {
                inMemoryItems.forEachIndexed { index, item ->
                    cursor.addRow(arrayOf<Any>(index.toLong(), item))
                }
            }
        }
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        when (uriMatcher.match(uri)) {
            CODE_ITEMS -> {
                val itemName = values?.getAsString("item_name") ?: "New Item ${System.currentTimeMillis()}"
                inMemoryItems.add(itemName)
                val newId = (inMemoryItems.size - 1).toLong()
                val resultUri = ContentUris.withAppendedId(CONTENT_URI, newId)
                context?.contentResolver?.notifyChange(resultUri, null)
                return resultUri
            }
        }
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            CODE_ITEMS -> "vnd.android.cursor.dir/vnd.com.example.autoinit.item"
            else -> null
        }
    }

    companion object {
        const val AUTHORITY = "com.example.william.my.module.performance.autoinit"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/items")

        private const val CODE_ITEMS = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "items", CODE_ITEMS)
        }
    }
}
