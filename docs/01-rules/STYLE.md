# 代码风格与注释

> 通用命名、库类/成员注释。示例页类头与日志见 [showcase.md](showcase.md)；工程化落点见 [engineering.md](engineering.md)。

## 命名与声明

| 场景 | 推荐正例 | 严禁反例 |
|---|---|---|
| ViewBinding 引用 | `binding` | `mBinding` |
| 变量 / 属性 | `client`, `log`, `isScanning` | `mClient`, `mLog`, `mIsScanning` |
| 架构组件 / 生命周期持有者 | `viewModel`, `adapter`, `coroutineScope`, `job` | `mViewModel`, `mAdapter`, `mScope` |
| 状态封装 | `_uiState` (private) + `uiState: StateFlow` (public) | 外部直接暴露可变流 |
| 集合与列表 | `articles`, `actions` | `articleList`, `mList` |
| 资源文件与 View ID | `<模块前缀>_activity_<功能>.xml`、`btn_submit` / `<模块前缀>_btn_submit` | 无前缀资源、无意义缩写如 `tv1`、`btn2` |
| 类内 / 局部常量 | `DEFAULT_TIMEOUT_SECONDS` (UPPER_SNAKE_CASE) | 小驼峰魔数变量 |
| 全局路由与业务 Key | `RouterPath.Http.OkHttp`、`Constants.Url_Article_List` (工程大驼峰) | 局部零散定义、硬编码魔法字符串 |
| 测试方法名 | `被测对象_场景_预期结果` | 反引号中文、无结构测试名 |

自有成员禁止 `m` 前缀。演示反射 / 系统源码字段（如 `ListenerInfo.mOnClickListener`）或调用第三方库既有 Java 字段名时，字符串与注释中可保留原名，不得据此给自有属性加 `m` 前缀。

「工程大驼峰」**仅限** `RouterPath` / `Constants` 等全局 Key 对象；类内与局部常量一律 `UPPER_SNAKE_CASE`，禁止把 `Value_Username` 风格扩散到业务类。资源前缀表见 [structure.md](structure.md)。

## 注释原则

1. **中文优先**：注释默认中文，标识符、类型名与必要专有术语除外。
2. **Why > What**：仅解释职责、约束与底层非显而易见的原因；严禁与代码同义反复（如 `// 创建实例` 下紧跟 `init()`）。
3. **保护既有技术决策**：严禁删除、缩减已有的技术选型对比、架构演进和设计约束注释。
4. **最小切片与增量追加**：新说明以增量形式追加；严禁将未修改注释块划入替换范围；`git diff` 严禁出现非预期的 `- //` 或 `- *` 行。

## 库类 / 基类注释

- 多行 KDoc；首段「职责 + 类型」简短描述。
- 特殊调用约定、生命周期、安全/性能限制时补充说明或最短用法示例。
- 不要求所有类都有详细说明。

```kotlin
/**
 * 格式化日志拦截器
 *
 * 可解析的请求/响应体（JSON、XML 等）会格式化输出，
 * 不可解析或不适合安全读取的内容只输出 URL 和 Header。
 */
```

需要补充调用约定 / 生命周期 / 安全限制时，在职责段下增写 2–4 行约束说明或最短用法，不写卖点词。

锚点：`libs/lib_okhttp/.../interceptor/InterceptorLogging.kt`

## 成员 / 方法

- 关键成员（状态、回调、线程/默认值约定）写单段 KDoc。
- 示例方法写清「做什么 + 关键流程/约束」。
- 显而易见的空 override / 简单字段不写注释；行内 `//` 只解释「为什么」。
