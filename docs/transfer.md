# 文件上传与下载

`lib_rx_download` 和 `lib_rx_upload` 使用与 `RxRequest` 一致的链式 Builder：单次传输通过 `buildSingle()` 生成 `Single`，批量下载通过 `buildFlowable()` 生成任务事件流。两个库都通过 Retrofit Service 执行请求，固定业务应注入由 Hilt 或 ServiceLocator 管理的共享 `Retrofit`；不注入时库内使用默认 Rx Retrofit。

两个库的根包分别为 `com.example.william.my.core.rx.download` 和 `com.example.william.my.core.rx.upload`。

`module_rxretrofit` 按 `request/download/upload` 拆分示例，文件传输页面分别为 `RxDownloadActivity` 和 `RxUploadActivity`。两个页面都继承 `BasicResponseActivity`，并像 `RxRequestActivity` 一样让每个列表项直接对应一个 API 示例方法，只使用 `CompositeDisposable` 管理取消。下载页分别在方法内展示单任务 `RxDownload.builder()` 和批量 `RxDownloadManager.builder()`，两者注入同一个页面级 Retrofit；上传页分别展示 `addFile()` 与 `addFiles()`。为突出传输 API，上传页使用 `File.writeText()` 准备少量文本示例文件，两个页面直接使用 `File` API 清理示例目录；生产业务的大文件 I/O 应调度到后台线程。进度通过 `updateLog()` 原位更新，完成和失败结果通过 `appendLog()` 追加。

## 目录结构

两个库的单任务能力采用相同分层，目录可以一一对应：

```text
lib_rx_download/.../rx/download/       lib_rx_upload/.../rx/upload/
├── RxDownload.kt                ├── RxUpload.kt              # 对外入口
├── RxDownloadManager.kt                                      # 业务级 Retrofit 与默认并发
├── builder/                     ├── builder/                 # 链式参数构建
├── callback/                    ├── callback/                # 统一业务回调
├── config/DownloadConfig.kt     ├── config/UploadConfig.kt   # 不可变配置快照
├── exception/                   ├── exception/               # HTTP 与统一异常转换
├── model/                       ├── model/                   # 单任务进度和结果
├── request/                     ├── request/                 # Retrofit Service 与请求执行
├── resume/DownloadResumeMetadata.kt                         # 下载续传资源身份
└── queue/                                                   # 下载特有的批量并发层
    ├── RxDownloadQueue.kt                                   # 并发执行和进度聚合
    ├── RxDownloadQueueBuilder.kt                            # 批量链式配置
    ├── DownloadQueueConfig.kt                               # 批量配置快照
    ├── DownloadDestinationRegistry.kt                       # 跨队列目标路径互斥
    ├── DownloadQueueTaskResources.kt                        # 任务资源与终止屏障
    └── model/                                               # 任务、事件、整体进度和结果
```

`queue/` 之外的下载文件都能在上传库找到同职责目录。`RxDownload` 和 `RxDownloadManager` 位于根包，作为两个公开下载入口；下载文件数量更多，是因为下载额外支持业务级 Manager、并发队列、单任务事件和整体聚合进度；这些扩展不进入单任务公共层。

## 下载

```kotlin
val disposable = RxDownload.builder()
    .api("https://example.com/files/app.apk")
    .destination(context.cacheDir, "app.apk")
    .addHeader("Authorization", "Bearer $token")
    .retrofit(businessRetrofit)
    .setProvider(this)
    .build()
    .subscribeWith(object : RxDownloadCallback<DownloadProgress, DownloadResult>() {
        override fun onProgress(progress: DownloadProgress) {
            updateProgress(
                currentBytes = progress.currentBytes,
                totalBytes = progress.totalBytes,
                percent = progress.percent,
            )
        }

        override fun onResponse(response: DownloadResult) {
            install(response.file)
        }

        override fun onFailure(error: ApiException) {
            showError(error.message)
        }
    })
```

- `resume(true)` 默认启用。传输内容写入 `<目标>.rxdownload.part`，正式目标文件只在下载完整后替换。
- 临时文件同时保存 URL、强 `ETag` 或 `Last-Modified`；只有资源身份匹配且存在校验器时才发送 `Range` 和 `If-Range`。来源未知的已有文件不会作为续传前缀。
- 服务端返回 `206` 时追加临时文件；校验器变化或服务端忽略 Range 并返回 `200` 时从头覆盖临时文件。
- 服务端对完整临时文件返回 `416` 且 `Content-Range` 总长度一致时按成功处理，随后提交为正式文件。
- `RxDownloadCallback` 将 HTTP 和 I/O 异常统一转换为 `ApiException`；非 2xx 响应优先使用服务端错误体作为消息，使用普通 Rx Observer 时仍可获取包含 `statusCode` 和 `responseBody` 的原始 `DownloadHttpException`。
- 取消订阅会取消对应 Retrofit Call，临时文件与续传元数据保留；正式目标文件不受未完成请求影响。删除文件由调用方决定。
- `setProvider(owner)` 会在页面生命周期结束时自动取消下载。
- `onFinally {}` 在底层网络调用、响应体和文件流真正退出后触发；需要清理目标目录或串行启动下一任务时，应以该信号作为屏障。
- 批量队列从目标路径与并发许可的预处理阶段开始计入终止屏障；取消后，目标路径租约和并发许可会保持到对应预处理或物理下载真正退出，避免新队列与旧任务同时写入。
- `lib_rx_download` 不再保留旧 `DownloadTask` 状态机和 `start/stop/remove` 入口；单任务通过返回的 `Disposable` 取消，批量任务通过队列的 `Disposable` 整体取消。

## 批量下载与并发

每个业务通过独立 `RxDownloadManager` 共享自己的 `Retrofit` 和并发额度。Retrofit 内部仍由对应的 `OkHttpClient` 提供连接池、超时、证书、代理和拦截器。Manager 默认并发数为 3，可配置范围为 1 到 32；同一个 Manager 同时创建多个队列时，所有队列共同遵守该总额度。单次队列可以通过 `maxConcurrency()` 进一步限制自身并发，但不能突破 Manager 总额度。

```kotlin
val downloader = RxDownloadManager.builder()
    .retrofit(businessRetrofit)
    .maxConcurrency(5)
    .build()

val disposable = downloader.download()
    .addTasks(tasks)
    .setProvider(this)
    .build()
    .subscribeWith(
        object : RxDownloadCallback<DownloadQueueProgress, DownloadQueueResult>() {
            override fun onProgress(progress: DownloadQueueProgress) {
                updateOverall(
                    completedCount = progress.completedCount,
                    totalCount = progress.totalCount,
                    currentBytes = progress.currentBytes,
                    totalBytes = progress.totalBytes,
                    percent = progress.percent,
                )
            }

            override fun onResponse(response: DownloadQueueResult) {
                showResult(response.successes.size, response.failures.size)
            }
        }
    )
```

- `RxDownload.queue()` 使用默认共享 Rx Retrofit 和默认并发数 3，适合 Demo 或没有 DI 的简单场景。
- `RxDownloadManager` 通过共享公平信号量负责业务隔离与跨队列并发；Retrofit 负责 Service、URL、Header 和响应封装，底层 `OkHttpClient` 负责连接池与传输配置。当前下载使用同步 Retrofit `Call.execute()`，不能用 OkHttp Dispatcher 代替业务队列并发控制。
- `RxDownloadCallback<P, R>` 是不依赖 RxJava Observer 类型的统一业务回调：单任务使用 `DownloadProgress/DownloadResult`，队列使用 `DownloadQueueProgress/DownloadQueueResult`。两种场景都通过 `build().subscribeWith(callback)` 订阅。
- `buildSingle()` 和 `buildFlowable()` 保留为高级入口；需要队列中单任务开始、进度、成功或失败事件时，直接订阅 `Flowable<DownloadQueueEvent>`。单任务失败默认记录到最终队列结果并继续其他任务。
- 整体百分比按所有文件累计字节加权计算，不对单文件百分比取平均。任一任务总大小未知时 `totalBytes` 为 `-1`、`percent` 为 `null`，完成数与已下载字节仍然可用。
- 队列任务 ID 和目标文件必须唯一，避免两个并发请求同时写入同一文件。
- 同一个 Manager 的不同队列不能同时写入相同的规范化目标路径；后提交的冲突任务会作为任务失败返回，避免破坏临时文件和续传元数据。
- 批量下载进度间隔最小为 50 ms；事件使用背压错误策略，消费者长期无法处理事件时会终止队列，而不会无限占用内存。
- 队列的 `onFinally {}` 会等待所有已进入预处理的任务和物理下载退出；队列取消后不会立即把下游 `dispose()` 当作文件 I/O 已结束。

## 上传

```kotlin
val disposable = RxUpload.builder()
    .api("https://example.com/upload")
    .retrofit(businessRetrofit)
    .addHeader("Authorization", "Bearer $token")
    .addParam("type", "avatar")
    .addFile("file", avatarFile, "image/jpeg".toMediaType())
    .setProvider(this)
    .build()
    .subscribeWith(object : RxUploadCallback() {
        override fun onProgress(progress: UploadProgress) {
            updateProgress(progress.percent)
        }

        override fun onResponse(response: UploadResult) {
            handle(response.body)
        }

        override fun onFailure(error: ApiException) {
            showError(error.message)
        }
    })
```

- 上传固定使用 POST，调用方不配置 HTTP method；`addParam(s)` 对应 Multipart 表单字段，可重复调用 `addFile()` 上传多个文件，包括相同字段名的文件列表；同一字段名和媒体类型的文件也可以使用 `addFiles()` 批量追加。
- 上传请求会从默认或注入的 Retrofit 派生配置；若底层 `Call.Factory` 为 `OkHttpClient`，会自动关闭连接失败重试（自定义 `Call.Factory` 则保留原配置）。Redirect、Authenticator 和业务 Interceptor 仍可能重放请求，重要上传建议使用幂等键。
- 进度统计覆盖完整 Multipart 请求体，包含文件、表单字段和边界字节。
- 成功结果为原始 `UploadResult`，业务层自行按接口契约解析 `body`；`RxUploadCallback` 将失败统一转换为 `ApiException`。
- 取消订阅会取消对应 Retrofit Call。文件存在性在生成配置快照时校验。
- `setProvider(owner)` 会在页面生命周期结束时自动取消上传。
- `onFinally {}` 在上传请求体读取和底层网络调用真正退出后触发，上传源文件清理应等待该信号。

## 线程与快照

- 网络与文件 I/O 默认运行在 `Schedulers.io()`，结果和进度默认投递 Android 主线程。
- Worker、后台任务和 JVM 测试可分别使用 `subscribeOn()`、`observeOn()`、`progressOn()` 覆盖调度器。
- `addHeaders()` / `setHeaders()`、表单字段和文件列表会在 `build()` / `buildSingle()` 时复制；之后修改原始集合或 Builder 不影响已经生成的请求。
- 进度默认最多每 100 ms 分发一次，允许的最小间隔为 50 ms，并保证最终进度先于成功回调；可通过 `progressIntervalMillis()` 调整。
- 示例页面使用 `updateLog(key, message)` 覆盖当前进度，只将开始、完成、失败和取消追加到历史日志，避免长任务无限扩张 TextView 内容。
- 通过请求对象的 `subscribeWith(callback)` 订阅时，进度自动进入 `RxDownloadCallback` 或 `RxUploadCallback`；`onProgress {}` 仍作为直接订阅原始 Rx 类型时的高级入口保留。
- `subscribeWith(callback)` 返回 `Disposable`，页面应保存它用于主动取消；生命周期结束时仍可由 `setProvider(owner)` 自动取消。
