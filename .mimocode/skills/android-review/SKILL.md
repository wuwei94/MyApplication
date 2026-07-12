---
name: android-review
description: 审查技术栈沉淀项目中的 Android 示例 Activity 质量。用于评审、审计或改进 demo/showcase 项目中的 Activity 示例。覆盖：交互设计、代码质量、教学注释、API 兼容性和资源管理。
---

# Android 示例 Activity 审查

审查技术栈沉淀项目中的 Android 示例 Activity，输出结构化审查报告和优先级排序的问题列表。

## 交互设计规范

### 基类与布局模式

| 基类 | 用途 | 上方展示 | 下方列表 |
|------|------|----------|----------|
| `BasicImageActivity` | 动画/图片类 | ImageView | 操作按钮 |
| `BasicResponseActivity` | 通信/调度类 | TextView 日志 | 操作按钮 |
| `BasicRecyclerActivity` | 纯列表类 | 无 | 列表项 |

### 日志显示规范

- **初始化**：使用 `showResponse()` 设置说明文字，`gravity = Gravity.CENTER`
- **追加日志**：使用 `appendLog()` 追加日志，`gravity = Gravity.TOP`
- **日志格式**：每条日志必须是一行，不要换行或多行输出
- **日志累积**：使用 `StringBuilder` 累积日志，不覆盖历史记录
- **不要直接操作**：禁止直接设置 `mBinding.basicsResponse.text`，必须通过基类方法

### 日志格式示例

```
✅ 正确：一行一条日志
【存储】已授权：存储、存储
【相机】已拒绝：相机（永久拒绝）
创建渠道成功：demo_channel（IMPORTANCE_HIGH）
发送高优先级通知（ID: 2）— 会弹出悬浮横幅

❌ 错误：一条日志占多行
【存储】权限请求结果：

  存储: ✅ 已授权
  存储: ❌ 已拒绝
```

### 操作触发规范

- **所有**使用 `BasicResponseActivity` 的 Activity（包括目标页）必须使用下方 RecyclerView 列表（`buildList` + `onRecyclerClick`）
- 禁止使用 `onResponseClick` 点击上方区域触发操作
- 上方作为结果展示区域
- 不要覆盖上一次的结果，日志要累积显示

## 审查维度

对每个 Activity 评估以下 3 个维度：

### 1. 交互性
- **操作触发方式**：操作项必须放在下方 RecyclerView 列表（`buildList` + `onRecyclerClick`），禁止使用 `onResponseClick` 点击上方区域
- 用户能否触发/切换演示行为？
- 操作 → 结果 的反馈循环是否清晰？
- 反面：打开就显示静态结果，无法操作
- 正面：点击/滑动可切换状态，有明确反馈
- 动画类额外检查：是否有状态提示，让用户知道当前播放的是哪种动画
- 用户可见的提示文字（Toast、状态 TextView）：可以使用英文 API 名称，但需附带中文说明，如「Alpha（透明度）」

### 2. 教学注释
- 注释是否解释了 WHY 而非只是 WHAT？
- 关键概念、公式、API 参数是否有文档？
- 反面：`// 模糊图片`
- 正面：`// 半径限制在 [0.1, 25.0]，API 要求`

### 3. 代码质量
- Kotlin 最佳实践（apply {} 作用域、避免不必要分配）
- 无 bug（Animator 复用崩溃、资源泄漏、API 版本崩溃）
- 类似 Activity 使用一致的基类

## 输出格式

```
## Activity: <名称>

**评级**: ✅ 良好 | ⚠️ 需改进 | ❌ 有严重问题

| 维度 | 状态 | 问题 |
|------|------|------|
| 交互性 | ✅/⚠️/❌ | 描述 |
| 教学注释 | ✅/⚠️/❌ | 描述 |
| 代码质量 | ✅/⚠️/❌ | 描述 |

**P0（严重）**: 必须修复 — 崩溃、数据丢失
**P1（重要）**: 应该修复 — 体验差、资源泄漏
**P2（建议）**: 可以修复 — 代码风格、小改进
```

## 常见反面模式

1. **点击上方区域触发操作**：使用 `onResponseClick` 点击上方 TextView 触发操作，应改为下方 RecyclerView 列表
2. **静态展示**：Activity 打开就显示结果，无法交互（如模糊直接显示，无开关）
3. **无状态提示（动画）**：动画 demo 没有文字显示当前播放的是哪种动画
4. **提示文字无中文说明**：Toast 或状态文字只有英文 API 名称，没有中文解释（应为「Alpha（透明度）」格式）
5. **Animator 复用崩溃**：`by lazy` 或 AnimatorSet 中共享 Animator 实例
6. **API 版本崩溃**：生命周期方法上加 `@RequiresApi` 但没有运行时判断
7. **资源泄漏**：Allocation / RenderScript 未在 onDestroy 中销毁
