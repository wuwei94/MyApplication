package com.example.william.my.core.rx.upload.config

import androidx.lifecycle.Lifecycle
import com.example.william.my.core.rx.upload.model.UploadProgress
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Scheduler
import okhttp3.MediaType
import retrofit2.Retrofit
import java.io.File

/** 单次上传的只读配置快照。 */
internal data class UploadConfig(
    val url: String,
    val headers: Map<String, String>,
    val formFields: List<Pair<String, String>>,
    val files: List<UploadFilePart>,
    val retrofit: Retrofit,
    val lifecycle: LifecycleProvider<Lifecycle.Event>?,
    val subscribeScheduler: Scheduler,
    val observeScheduler: Scheduler?,
    val progressScheduler: Scheduler?,
    val progressIntervalMillis: Long,
    val onProgress: ((UploadProgress) -> Unit)?,
    val onFinally: (() -> Unit)?,
)

internal data class UploadFilePart(
    val name: String,
    val fileName: String,
    val file: File,
    val mediaType: MediaType?,
)
