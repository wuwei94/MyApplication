# 统一代码与注释风格契约 (STYLE.md)

> 本文件是多 Agent 协同编码的可执行风格清单。
> 修改或新建代码时必须逐项对照。详尽领域文档见 `docs/`。

---

## 1. 示例页类头注释（Activity / Fragment 必须）

类头强制采用三段式 KDoc：

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

- **废弃 API 示例页**：标题加 `（已废弃）`，第二行固定写：`⚠️ 历史参考：<废弃原因>，生产代码应使用 <现代替代>。`
- **模块入口页 / 纯宿主容器页**：仅写 `名 — 定位` 与一句话职责，可省略外链与避坑点。

---

## 2. 命名与声明规范

| 场景 | 推荐正例 | 严禁反例 |
|---|---|---|
| ViewBinding 引用 | `binding` | `mBinding` |
| 变量 / 属性 | `client`, `log`, `isScanning` | `mClient`, `mLog`, `mIsScanning` |
| 状态封装 | `_uiState` (private) + `uiState: StateFlow` (public) | 外部直接暴露可变流 |
| 集合与列表 | `articles`, `actions` | `articleList`, `mList` |
| 操作项文案 | `"1. 动词短语"`, `"2. 动词短语"` | `"操作一"`, `"点击请求"` (无序/无动词) |
| 测试方法名 | `被测对象_场景_预期结果` | 反引号中文、无结构测试名 |

---

## 3. 控制台示例页结构（Showcase Activity）

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

    private fun executeBasicCall() {
        appendLog("→ 开始执行基础调用...")
        runCatching {
            // 直接调用库 API，严禁页面级包装调度器
        }.onSuccess {
            appendLog("✓ 调用成功")
        }.onFailure {
            appendLog("✗ 调用失败: ${it.message}")
        }
    }
}
```

---

## 4. 日志输出契约（BasicResponseActivity）

- 动作发起：`appendLog("→ <动作说明>...")`
- 离散成功：`appendLog("✓ <关键结果>")`
- 离散失败：`appendLog("✗ <错误详情>")`
- 高频更新：`updateLog("<key>", "<高频更新文案>")`（严禁高频循环追加 `appendLog`）
- 允许上下文方括号标签如 `[FormBody]`；**严禁**使用 `【成功】/【失败】/【异常】/【完成】` 作为结果装饰。

---

## 5. 注释三原则

1. **Why > What**：仅解释职责、约束与底层非显而易见的原因；严禁与代码同义反复（如 `// 创建实例` 下紧跟 `init()`）。
2. **保护既有技术决策**：严禁删除、缩减已有的技术选型对比、架构演进和设计约束注释。
3. **最小切片与增量追加**：新说明以增量形式追加在原注释末尾；严禁将未修改注释块划入替换范围；`git diff` 严禁出现非预期的 `- //` 或 `- *` 行。
