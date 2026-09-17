# 示例页与平行实现

> Showcase Activity 怎么写、多库横评怎么摆、类头注释怎么对齐。
> 工程结构与分类见 [structure.md](structure.md)；通用命名与库类注释见 [style.md](style.md)；模板见 [templates.md](templates.md)。

---

## 1. 页面架构形态

### 1.1 API 演示型（全工程默认）

首要目标：让读者快速看清库的入口、参数、返回值和回调。

- 依托 `BasicResponseActivity` / `BasicControlActivity`；锚点：`modules/module_http/.../okhttp/OkHttpActivity.kt`。
- `buildList()` 项 ↔ 命名明确的示例方法；`onRecyclerClick` 中 `when(position)` 直接转发。
- 页面直接调用库 API，禁止自造任务编排包装层。
- 离散 `appendLog()`；高频 `updateLog(key, message)`。

```kotlin
@Route(path = RouterPath.<模块名>.<页面名>)
class SampleActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 <技术名> 的核心 API 调用与回调处理")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 触发基础操作",
        "2. 触发异步流程",
        "3. 重置 / 清理资源"
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> executeBasicCall()
            1 -> executeAsyncCall()
            2 -> clearResources()
        }
    }
}
```

- **页面描述契约**：所有 `BasicResponseActivity` 必须在 `initView()` 中调用 `showDescription("...")` 清晰交代演示目标。

### 1.2 非控制台 Showcase 基类交互契约

- **`BasicControlActivity`**（纯按钮操作）：无控制台，结果反馈优先用 `Toast` 或状态指示，严禁自造复杂对话框或遮罩阻断流程。
- **`BasicLayoutActivity` / `BasicImageActivity`**：顶部区域纯粹作为组件渲染画布（Canvas / Preview），底部 `buildList` 专职驱动“参数调节 / 状态切换”，避免在页面内自造假业务容器。
- **`BasicRecyclerActivity`**：顶部展示实时数据源列表，底部保留基础操作项。

### 1.3 UI 原语型（`module_compose`）

Compose 官方 UI 与交互原语的「代码活字典」：

1. Stateful 容器与 Stateless 展示层分离；展示层支持独立 `@Preview`。
2. 单页精炼自包含（约 300 行），只讲透单个原语。
3. 页面内交互控件仅驱动 UI 参数，不引入假业务。
4. 原语为主、实战为辅（Canvas 看板 / Theme / Nav3 作能力上限）。

### 1.4 架构演进型（`module_arch`）

MVP / MVVM / MVI / Mavericks / SSOT 对比载体：

1. SSOT：Room Flow 为唯一事实源，派生不可变 `UiState`（`stateIn(WhileSubscribed)`）。
2. Activity/Fragment 仅渲染，`collectWithLifecycle` 监听。
3. 配套手写 Fake + Turbine 测试。

### 1.5 平行实现（多库横评）

同一能力多套库 / 多套 API 面并排（HTTP、BLE、SSE、Socket、MQTT、DI、事件总线、键值存储、图片加载等）构成**平行组**：

1. **矩阵完整**：入口按库/栈分组；缺页须有文档依据，否则补齐关键轴（Scan/Connect/Transfer、Listener/Flow/Rx、GET/POST 等）。
2. **名实相符**：标题/类头/showDescription/入口文案必须与实际调用的 API 一致；未引依赖不得假装使用该库；原理演算标「原理示意」，静态布局标「布局预览」。
3. **真实 API 面**：直接调库公开 API；`buildList` 覆盖关键方法族，与兄弟页方法轴对齐。
4. **操作项契约**：`buildList` 统一 `"N. 动词短语"`；布局页可不建 `buildList`，不得在入口伪装成可操作横评。
5. **注释随实现走**：类头遵守下文「平行注释」；实现变化同步类头与 `modules.md`。

#### 典型平行组最小能力轴清单

新建或重构平行组时，各库应尽可能对齐以下能力轴：

| 平行技术领域 | 覆盖模块 / 典型库 | 必须对齐的核心操作轴（buildList 方法族） |
|---|---|---|
| **网络通信** | OkHttp / Retrofit / RxRequest / Ktor | GET 请求、POST 表单 (FormBody)、POST JSON / Raw、Multipart 多部分上传、客户端 DSL 配置 (超时/日志) |
| **低功耗蓝牙 (BLE)** | Native / FastBle / Nordic / RxBle | 扫描与过滤规则、设备连接与状态监听、特征读写与 Notify 订阅、大包队列/传输、连接断开与资源释放 |
| **长连接与流式通信** | Socket / WebSocket / SSE / MQTT | 建立长连接、数据上行发送、数据流下行监听 (Flow/Rx/Listener)、异常断网重连/心跳、连接关闭与注销 |
| **事件总线** | EventBus / LiveEventBus / FlowEventBus / RxEventBus | 普通事件发布与监听、粘性事件 (Sticky)、生命周期绑定 (自动注销)、手动反注册 |
| **键值与对象存储** | DataStore / MMKV / Room / ObjectBox | 写入基础键值/实体、读取与观察响应式流、批量事务操作、数据清理与重置 |

---

## 2. 类头注释

### 2.1 统一结构

```kotlin
/**
 * <技术名> — <一句话职责定位>
 *
 * 核心机制与避坑点：
 * 1. <机制/时序/线程约束>：<关键说明，聚焦非显而易见的设计要点>
 * 2. <生命周期/资源释放/内存防范>：<明确释放时机与避坑要点>
 *
 * 官方参考：
 * <官方权威裸 URL 或仓库相对路径>
 */
```

### 2.2 「官方参考」填写边界

| 页面类型 | 要求 |
|---|---|
| 库能力横评 / 协议栈页 | **必填**权威裸 URL |
| 纯 UI / 动画 / 控件演示页 | **可选**；禁止硬塞无信息量泛链 |
| 模块入口页 | **可省** |

避坑点禁止卖点罗列（「高性能」「简单易用」「代码简洁」「比 X 快 N 倍」）；只写线程、生命周期、资源释放、版本约束。

### 2.3 页型变体

| 页型 | 类头要求 |
|---|---|
| 完整示例页 | §2.1 三段式 + 按 §2.2 链接 |
| 宿主容器页 | 名—定位 / 概述 / 承载子页；可省外链 |
| 模块入口页 | `*模块入口 — 导航到…` + 一句职责；禁止卖点词 |
| 布局预览页 | 名—定位含「布局预览」；写明无交互操作项 |
| 废弃 API 示例页 | 标题加 `（已废弃）`；第二行 `⚠️ 历史参考：<废弃原因>，生产代码应使用 <现代替代>。`；可用 `@see` 指向现代页 |

Fragment 示例页与 Activity 完整示例页共用同一套类头模板。

### 2.4 平行注释（兄弟页可横向对照）

| 约束 | 要求 |
|---|---|
| 结构同构 | 全组统一 §2.1；禁止自造「特性对比：」「核心组件：」「库选型定位：」「基本用法代码块」 |
| 信息密度对等 | 避坑点 2–4 条且深度对齐；禁止兄弟页一边写生命周期一边只写「简单易用」 |
| 版本与能力一致 | 类头机制必须与实际调用的库 API / 协议版本一致 |
| 对照关系可读 | 差异写成避坑点编号条，不另开对比矩阵小节 |
| 入口文案同步 | RouterItem 与页名职责一致；布局预览必须标明 |

非平行组独立页不受「结构同构」约束，仍须遵守 §2.1–§2.3。

---

## 3. 日志输出契约

- 动作发起：`appendLog("→ <动作说明>...")`
- 离散成功：`appendLog("✓ <关键结果>")`
- 离散失败：`appendLog("✗ <错误详情>")`
- 高频更新：`updateLog("<key>", "<高频更新文案>")`（严禁高频循环 `appendLog`）
- 允许半角上下文标签如 `[FormBody]`；**严禁** `【成功】/【失败】/【异常】/【完成】` 作结果装饰。

---

## 4. 锚点

| 场景 | 文件 |
|---|---|
| 类头 + 交互 + 日志 | `modules/module_http/.../okhttp/OkHttpActivity.kt` |
| 控制台基类 | `basic/basic_shared/.../BasicResponseActivity.kt` |
| 平行组入口 | `modules/module_bluetooth/BluetoothMainActivity.kt`、`HttpMainActivity.kt` |
