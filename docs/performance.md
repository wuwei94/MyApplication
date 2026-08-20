# Android 性能优化指南与实战手册

> 本文档系统性梳理 Android 移动端性能优化的核心维度、底层原理、治理手段与工具链，并与工程中的交互式性能演示示例（`modules/module_sample/performance`）相互映射。

---

## 性能优化全景图

```
                       ┌───────────────────────────────┐
                       │     Android 性能优化体系       │
                       └──────────────┬────────────────┘
                                      │
       ┌──────────────┬───────────────┼───────────────┬──────────────┐
       ▼              ▼               ▼               ▼              ▼
┌────────────┐ ┌────────────┐ ┌───────────────┐ ┌────────────┐ ┌────────────┐
│  内存优化  │ │ 渲染与流畅 │ │ 线程与并发调度 │ │  启动优化  │ │ 网络与存储 │
│ (Memory)   │ │  (Render)  │ │ (Concurrency) │ │ (Startup)  │ │ (I/O & Net)│
└────────────┘ └────────────┘ └───────────────┘ └────────────┘ └────────────┘
```

---

## 一、内存优化与内存抖动治理

内存优化是保障 App 稳定运行（避免 OOM 崩溃与 GC 卡顿）的关键。

### 1. 内存抖动治理（Memory Churn & Object Pool）
- **问题本质**：在高频场景（如 `onDraw`、`onTouchEvent`、`RecyclerView` 滑动、高频数据解析）中短时间内大量创建临时小对象，迅速耗尽年轻代 Eden 区，触发高频垃圾回收（`GC_CONCURRENT` / `GC_FOR_ALLOC`）。GC 会导致主线程短暂挂起（Stop-The-World），造成掉帧和卡顿。
- **治理方案**：引入对象池机制（`Pools.SimplePool` / `Pools.SynchronizedPool`），实现类似 `Message.obtain()` 的复用与零分配（Zero Allocation）。
- **避坑准则**：
  1. 归还对象前必须调用 `reset()` 清理字段，防止脏数据与隐式内存泄漏；
  2. 避免 Double-Release（重复归还同一个对象）；
  3. 仅对高频、短生命周期对象使用对象池，低频对象无须池化。

### 2. 轻量级数据结构选型（SparseArray vs HashMap）
- **问题本质**：`HashMap<Integer, Object>` 每个键值对均包装为一个 `Map.Entry`（或 `Node`）对象，且基本类型 `int` 会强制装箱为 `Integer`，带来额外的内存头开销、引用指针与对象碎片。
- **治理方案**：对于 Key 为整型（`int` / `long`）且数据量适中（通常 $< 1000$ 条）的场景，采用 `SparseArray`、`SparseIntArray`、`LongSparseArray` 或 `ArrayMap`。
  - `SparseArray` 使用双数组结构（`int[] mKeys` + `Object[] mValues`），节省约 50%~80% 内存；
  - 拥有 `DELETED` 延迟压缩机制，在 `remove()` 时不立即触发数组拷贝，只在下次扩容/插入时集中执行 `gc()` 压缩。

### 3. Bitmap 内存与采样率优化（Image & Bitmap Management）
- **内存公式**：$\text{内存占用} = \text{宽} \times \text{高} \times \text{单像素字节数}$（例如 $4000 \times 3000$ 的 ARGB_8888 原图占用 $4000 \times 3000 \times 4 \approx 48\text{ MB}$）。
- **治理方案**：
  1. **尺寸预检**：设置 `BitmapFactory.Options.inJustDecodeBounds = true`，0 内存开销解析图片宽高与类型；
  2. **动态采样率**：根据目标 View 尺寸计算 `inSampleSize`（为 2 的幂次方），例如 `inSampleSize = 4` 时内存直接降为原图的 $1/16$；
  3. **色彩格式优化**：针对不含 Alpha 透明通道的大图/背景图，使用 `RGB_565`（2 字节/像素），内存直接减少 50%；Android 8.0+ 推荐 `HARDWARE` 格式直接存入 GPU 显存；
  4. **内存复用（inBitmap）**：重用既有 Bitmap 的物理内存缓冲区，消除解码时的内存分配与碎片；
  5. **跨进程安全**：禁止直接通过 Intent / Bundle 传递大 Bitmap（Binder 事务缓冲区上限仅 1MB，易触发 `TransactionTooLargeException`）。

### 4. 内存多级缓存（LRU & TTL 策略）
- **核心机制**：利用 `LruCache` 基于 `LinkedHashMap(accessOrder = true)` 的双向链表机制，维护最近最少使用顺序，当内存超过 `maxSize` 时自动触发 `trimToSize` 淘汰旧节点。
- **最佳实践**：封装携带时间戳的 `CacheEntry` 支持 TTL（Time-To-Live）时效控制，在获取时主动判定是否过期并触发异步回源更新。
- **示例源码**：[`LruCacheActivity.kt`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/LruCacheActivity.kt)（路由：`/Sample/LruCache`）

---

## 二、UI 渲染与流畅度优化

保障 UI 持续稳定在 60fps / 120fps，消除丢帧（Jank）与交互迟滞。

### 1. 列表差量计算与局部刷新（DiffUtil）
- **对比机制**：
  - `notifyDataSetChanged()`：全量失效，强制全部可见 Item 重新绑定 ViewHolder，无法触发局部更新动画，开销随数据量线性增长；
  - `DiffUtil`：基于 Myers 差分算法计算新旧数据集的最小差异集（$O(N + D^2)$），定向派发 `notifyItemRangeInserted` / `notifyItemRangeRemoved` / `notifyItemRangeChanged`；
  - 推荐结合 `ListAdapter` / `AsyncListDiffer` 将耗时的差量计算调度至后台子线程执行。
- **示例源码**：[`DiffUtilActivity.kt`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/DiffUtilActivity.kt)（路由：`/Sample/DiffUtil`）

### 2. 列表视图复用与模块化拆分（RecycledViewPool & ConcatAdapter）
- **跨列表共享视图池（RecycledViewPool）**：在 ViewPager2 多 Tab 或垂直列表嵌套横向列表时，多个 RecyclerView 共用同一个 `RecycledViewPool`，减少重复创建 ViewHolder 的性能与内存开销；
  - **示例源码**：[`RecycledViewPoolActivity.kt`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/RecycledViewPoolActivity.kt)（路由：`/Sample/RecycledViewPool`）
- **模块化列表组合（ConcatAdapter）**：替代单个包含数十种 ViewType 的臃肿 Adapter，将 Header、Banner、Feed 拆分为独立子 Adapter，开启 `setIsolateViewTypes(true)` 隔离类型并实现单模块独立增量局部刷新；
  - **示例源码**：[`ConcatAdapterActivity.kt`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/ConcatAdapterActivity.kt)（路由：`/Sample/ConcatAdapter`）

### 3. 布局层级与过度绘制（Overdraw）治理
- **扁平化层级**：优先使用 `ConstraintLayout` 替代多层嵌套的 `LinearLayout` / `RelativeLayout`，降低 Measure/Layout 的递归时间复杂度；
- **异步布局解析**：使用 `AsyncLayoutInflater` 在后台子线程异步反序列化复杂 XML 并反射实例化 View，避免主线程首帧或复杂弹窗阻塞；
- **组件复用与精简**：使用 `<include>` 复用组件，搭配 `<merge>` 标签消除多余根节点；
- **懒加载机制**：使用 `ViewStub` 承载低频/按需展示的视图（如网络错误重试页、空状态占位），避免冷启动与首次渲染时的无效解析；
- **过度绘制检查**：开启开发者选项中的“调试 GPU 过度绘制（Show GPU Overdraw）”，移除无意义的 `android:background`。
- **示例源码**：[`AsyncLayoutInflaterActivity.kt`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/AsyncLayoutInflaterActivity.kt)（路由：`/Sample/AsyncLayoutInflater`）

---

## 三、线程与协程调度优化

合理分配计算与 I/O 资源，避免主线程阻塞与线程池过度竞争。

### 1. 协程 Dispatcher 隔离与适用边界
| 调度器 | 线程池特征 | 适合任务类型 | 避坑准则 |
|-------|----------|------------|---------|
| `Dispatchers.Main` | 单线程（UI 主线程） | UI 渲染、轻量数据流绑定 | 严禁执行耗时计算或同步 I/O |
| `Dispatchers.IO` | 弹性线程池（上限 64 或 CPU 核数） | 网络请求、磁盘文件、数据库读写 | 避免执行高并发纯 CPU 运算，防止线程暴增 |
| `Dispatchers.Default` | 固定核心数（等于 CPU 核心数） | 复杂计算、数据排序、JSON 解析 | 避免在其中执行阻塞式 I/O |

### 2. withContext 切换开销与并发加速
- 频繁跨线程 `withContext` 会引入线程上下文切换与 Continuation 对象封装开销（平均单次约数微秒级）；
- 对多个独立 I/O 请求使用 `async` / `launch` 并行发起，大幅缩减端到端总耗时。

---

## 四、启动优化（App Startup）

缩短冷启动时间，降低从点击桌面图标到首帧可交互的时间（TTID / TTFD）。

1. **初始化收敛**：使用 Jetpack `androidx.startup`（App Startup）将多个 SDK 的初始化聚合至单一 ContentProvider，减少 ContentProvider 启动开销；
2. **异步初始化与 DAG 任务调度**：
   - 必须在主线程同步完成的 SDK 放在主线程首批执行；
   - 可以异步并发的 SDK 放入后台线程池初始化；
   - 依赖主界面的次要 SDK 放入 `IdleHandler.queueIdle()` 在主线程空闲时延迟执行。
   - **示例源码**：[`IdleHandlerActivity.kt`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/IdleHandlerActivity.kt)（路由：`/Sample/IdleHandler`）
3. **视觉障眼法**：配置 WindowBackground 占位主题（Splash Theme），消除启动白屏/黑屏等待感。

---

## 五、网络与存储性能优化

### 1. 网络请求优化
- **连接复用**：配置 OkHttp `ConnectionPool` 实现 TCP/TLS 连接复用，减少握手耗时；
- **协议升级**：支持 HTTP/2 与 HTTP/3 (QUIC)，享受多路复用与头部压缩；
- **数据压缩与精简**：启用 GZIP / Brotli 压缩，移除冗余字段，采用 Protocol Buffers 替代庞大的 JSON。

### 2. 本地存储优化
- **淘汰 SharedPreferences**：避免 SP 的同步 `commit()` 阻塞主线程与 `apply()` 带来的隐式 ANR 风险；
- **采用现代化存储**：
  - 轻量键值对：采用 **MMKV**（基于 `mmap` 内存映射，读写速度提升数十倍）；
  - 结构化数据：采用 **Jetpack DataStore** 或 **Room**（支持 Flow 异步观察、类型安全与事务隔离）。

---

## 六、性能检测与分析工具链

| 工具 | 适用场景 | 关键关注指标 |
|-----|---------|-------------|
| **Android Studio Profiler** | 开发期综合性能分析 | Heap Dump 内存泄漏、CPU 耗时火焰图、网络 Payload 监控 |
| **Perfetto / Systrace** | 深度系统级追踪 | Choreographer 掉帧原因、主线程阻塞时长、锁竞争分析 |
| **Layout Inspector** | 布局结构与重绘检查 | 布局层级深度、Compose 重组次数（Recomposition count） |
| **LeakCanary** | 内存泄漏检测 | 自动检测 Activity / Fragment / View 泄漏并输出引用链 |
| **StrictMode** | 严苛模式违规告警 | 主线程读写磁盘、网络请求、SQLite 违规操作拦截 |

---

## 七、工程内交互式性能演示速查表

在项目中可通过 `SampleMainActivity`（路由 `/Sample/Main`）直达以下性能实战演示：

| 演示功能 | 对应 Activity | 路由路径 | 核心演示与写法内容 |
|---------|--------------|---------|-------------------|
| **列表局部差量刷新** | [`DiffUtilActivity`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/DiffUtilActivity.kt) | `/Sample/DiffUtil` | `DiffUtil.Callback` 差量计算、定向更新与 Payload 细粒度局部刷新写法 |
| **多级内存缓存设计** | [`LruCacheActivity`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/LruCacheActivity.kt) | `/Sample/LruCache` | Cache-Aside 回源读取模式、容量超出自动淘汰与 `entryRemoved` 监听 |
| **主线程空闲调度** | [`IdleHandlerActivity`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/IdleHandlerActivity.kt) | `/Sample/IdleHandler` | `IdleHandler` 单次/持续空闲监听、生命周期注销与次要任务延迟初始化 |
| **异步布局解析预加载** | [`AsyncLayoutInflaterActivity`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/AsyncLayoutInflaterActivity.kt) | `/Sample/AsyncLayoutInflater` | `AsyncLayoutInflater` 后台异步解析 XML、主线程回调挂载与视图预加载池模式 |
| **跨列表共享视图池** | [`RecycledViewPoolActivity`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/RecycledViewPoolActivity.kt) | `/Sample/RecycledViewPool` | `RecycledViewPool` 跨 Tab/嵌套列表共用 ViewHolder 缓存池与容量扩容 |
| **多模块列表拼装与隔离** | [`ConcatAdapterActivity`](file:///E:/StudioProjects/MyApplication/modules/module_sample/src/main/java/com/example/william/my/module/sample/performance/ConcatAdapterActivity.kt) | `/Sample/ConcatAdapter` | `ConcatAdapter` 多 Adapter 拼装、`isolateViewTypes` 类型隔离与单模块独立局部刷新 |
