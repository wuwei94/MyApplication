import 'package:flutter/material.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';
import 'package:flutter_demo/demos/markdown/widgets/markdown_case_selector.dart';
import 'package:flutter_demo/demos/markdown/widgets/markdown_code_highlighter.dart';
import 'package:flutter_markdown_plus/flutter_markdown_plus.dart';

/// Markdown 基础与扩展渲染示例
///
/// 对标 Android module_markdown 的 `MarkwonBasicActivity`（Markwon 引擎），
/// Flutter 侧使用 flutter_markdown_plus 渲染引擎（flutter_markdown 停更后的
/// 社区延续维护版，API 兼容，默认 GitHub Flavored），业务逻辑保持一致。
///
/// 页面交互：顶部横滑标签切换案例（内容型页面将纵向空间留给阅读区，
/// 案例文案与功能逐项对齐 Android `MarkwonBasicActivity`）。
///
/// 引擎能力对照（Markwon → flutter_markdown_plus 逐项映射）：
/// 1. 基础语法：标题（H1~H6）、粗体、斜体、引用块、无序/有序多级列表、分割线
/// 2. GFM 扩展表格：多列对齐的标准 Markdown 表格
/// 3. 任务清单：GFM 复选框（`- [x] Task`）渲染为原生 Checkbox
/// 4. 删除线与富文本标签：与 Markwon HtmlPlugin 不同，flutter_markdown_plus
///    不支持内联 HTML，本示例显式说明该能力差异（案例 4）
/// 5. 图片加载：网络图片（https）异步加载并内联展示
/// 6. 主题与样式定制：MarkdownStyleSheet 定制引用条颜色/宽度、
///    代码块背景与文字颜色、列表圆点
/// 7. 交互拦截：onTapLink 链接点击回调
///
/// https://pub.dev/packages/flutter_markdown_plus
class MarkdownBasicDemoPage extends StatefulWidget {
  final String title;

  const MarkdownBasicDemoPage({super.key, required this.title});

  @override
  State<MarkdownBasicDemoPage> createState() => _MarkdownBasicDemoPageState();
}

class _MarkdownBasicDemoPageState extends State<MarkdownBasicDemoPage> {
  /// 案例操作列表（文案与 Android `MarkwonBasicActivity.buildList()` 一致）
  static const List<String> _caseTitles = <String>[
    '1. 基础排版（标题 / 引用 / 列表 / 分割线）',
    '2. GFM 扩展表格（多列对齐 & 复杂表格）',
    '3. 任务清单 TaskLists（复选框状态）',
    '4. HTML 标签与删除线（颜色 / 下划线 / 强调）',
    '5. 超链接与图片（交互拦截与异步渲染）',
    '6. 自定义主题样式（引用条绿色 / 暗色代码块 / 粗圆点）',
    '7. 综合长文档（技术文档综合实战测试）',
  ];

  int _caseIndex = 0;
  final ScrollController _scrollController = ScrollController();

  /// 自定义绿色主题案例（下标 5）使用定制样式表
  bool get _useCustomTheme => _caseIndex == 5;

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _selectCase(int index) {
    if (_caseIndex == index) return;
    setState(() {
      _caseIndex = index;
    });
    // 切换案例后回到顶部
    WidgetsBinding.instance.addPostFrameCallback((Duration _) {
      if (_scrollController.hasClients) {
        _scrollController.jumpTo(0);
      }
    });
  }

  void _onTapLink(String text, String? href, String title) {
    showToast('点击了链接: ${href ?? text}');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Column(
        children: <Widget>[
          // 顶部案例选择器（横向可滚动）
          MarkdownCaseSelector(
            titles: _caseTitles,
            index: _caseIndex,
            onSelected: _selectCase,
          ),
          const Divider(height: 1),
          // 内容区域（Markdown 自带滚动）
          Expanded(
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              child: Markdown(
                controller: _scrollController,
                data: _markdownByCase(_caseIndex),
                selectable: true,
                onTapLink: _onTapLink,
                syntaxHighlighter: MarkdownCodeHighlighter(
                  // 暗色代码块上使用浅青文字，浅色页面使用深色文字
                  textColor: _useCustomTheme
                      ? const Color(0xFF80CBC4)
                      : const Color(0xFF0F172A),
                ),
                styleSheet: _useCustomTheme
                    ? _buildCustomTheme(context)
                    : MarkdownStyleSheet.fromTheme(Theme.of(context)),
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// 自定义主题：绿色引用条 / 暗色代码块 / 行内代码配色 / 粗圆点
  MarkdownStyleSheet _buildCustomTheme(BuildContext context) {
    return MarkdownStyleSheet.fromTheme(Theme.of(context)).copyWith(
      blockquoteDecoration: const BoxDecoration(
        color: Color(0x1A4CAF50),
        border: Border(left: BorderSide(color: Color(0xFF4CAF50), width: 4)),
      ),
      blockquotePadding: const EdgeInsets.symmetric(
        horizontal: 12,
        vertical: 4,
      ),
      codeblockDecoration: const BoxDecoration(
        color: Color(0xFF263238),
        borderRadius: BorderRadius.all(Radius.circular(6)),
      ),
      codeblockPadding: const EdgeInsets.all(12),
      code: const TextStyle(
        color: Color(0xFF00796B),
        backgroundColor: Color(0xFFE0F2F1),
        fontFamily: 'monospace',
        fontSize: 13,
      ),
      listBullet: const TextStyle(
        fontSize: 20,
        height: 1,
        color: Color(0xFF212121),
        fontWeight: FontWeight.w700,
      ),
    );
  }

  String _markdownByCase(int index) => switch (index) {
    0 => _basicMarkdown,
    1 => _tableMarkdown,
    2 => _taskListMarkdown,
    3 => _htmlStrikethroughMarkdown,
    4 => _linkImageMarkdown,
    5 => _customThemeMarkdown,
    _ => _comprehensiveMarkdown,
  };

  /// 1. 基础排版示例（正文与 Android 对齐，仅引擎相关表述替换为 Flutter 侧实现）
  String get _basicMarkdown => '''
# 一级标题 (H1)
## 二级标题 (H2)
### 三级标题 (H3)
#### 四级标题 (H4)

这是普通的正文段落，支持 **加粗文本 (Bold)**、*斜体文本 (Italic)*、***粗斜体 (Bold Italic)*** 以及 `inline code` 行内代码。

---

> **引用块 (Blockquote)**
> 这是一个嵌套的引用块段落。
>> 这是二级嵌套引用内容，常用于 AI 回复的引用来源标注。

### 无序列表 (Unordered List)
- 列表项 A：Dart async / await 与 Stream 数据流
- 列表项 B：Flutter 现代声明式组件架构
  - 子列表项 B-1：Widget / Element / RenderObject 三层树
  - 子列表项 B-2：状态管理与 InheritedWidget
- 列表项 C：flutter_markdown_plus 高性能富文本渲染

### 有序列表 (Ordered List)
1. 第一步：配置 Markdown 渲染核心组件
2. 第二步：接入 GFM Table / TaskList 扩展语法
3. 第三步：实现打字机流控与代码高亮
''';

  /// 2. GFM 表格示例
  String get _tableMarkdown => '''
## GFM 扩展表格 (TablePlugin)

flutter_markdown_plus 默认支持 GitHub Flavored Markdown 规范的表格语法，并支持左对齐、居中和右对齐：

| 方案 | 渲染机制 | 内存开销 | 适用场景 | 性能评级 |
| :--- | :---: | :---: | :--- | ---: |
| **flutter_markdown_plus** | 原生 Widget 树 | 极低 | AI 聊天、文章阅读、轻量富文本 | ⭐⭐⭐⭐⭐ |
| **WebView** | WebKit 内核 | 较高 | 复杂排版网页、数学公式重度渲染 | ⭐⭐⭐ |
| **markdown_widget** | Widget 扩展 | 中等 | 需要代码高亮等高级扩展 | ⭐⭐⭐⭐ |
| **RichTextView** | 自定义 ViewGroup | 较高 | 特殊定制复合控件需求 | ⭐⭐⭐ |

### 混合格式单元格

| 功能特性 | 支持状态 | 备注说明 |
| :--- | :---: | :--- |
| 基础排版 | ✅ | 标题、粗斜体、引用、分割线 |
| 代码高亮 | ✅ | 配合 flutter_highlight 组件 |
| 任务列表 | ✅ | GFM 复选框原生渲染 |
| 内联图片 | ✅ | 网络 / 本地 / asset 均可 |
''';

  /// 3. 任务清单示例
  String get _taskListMarkdown => '''
## 任务清单 (TaskListPlugin)

flutter_markdown_plus 任务列表支持 GFM 复选框语法，呈现直观的待办项状态：

### Flutter Markdown 示例核心演进路线：
- [x] **阶段 1：基础与扩展渲染环境搭建**
  - [x] 引入 `flutter_markdown_plus` 依赖
  - [x] 搭建 `MarkdownBasicDemoPage` 基础交互体验
  - [x] 验证表格、任务列表与删除线渲染
- [ ] **阶段 2：代码语法高亮**
  - [ ] 引入 `flutter_highlight` 词法解析组件
  - [ ] 支持 Kotlin / Java / Python / JS / SQL 染色
  - [ ] 异步后台协程高亮优化
- [ ] **阶段 3：流式打字机与未闭合语法容错**
  - [ ] 动态自适应 Buffer 出字速率调控
  - [ ] 未闭合 Markdown 标签与代码块自动补齐
  - [ ] 光标呼吸闪烁动效
- [ ] **阶段 4：AI 聊天完整实战页面**
  - [ ] 局部增量刷新
  - [ ] 智能贴底平滑滚动与手势打断
''';

  /// 4. HTML 标签与删除线示例（能力差异已在文中说明）
  String get _htmlStrikethroughMarkdown => '''
## 删除线 (StrikethroughPlugin) 与富文本标签说明

### 1. 删除线语法
- 原价：~~¥ 999.00~~，现限时特惠：**¥ 19.90**
- ~~旧版全量刷新导致的卡顿掉帧~~ ➔ **采用局部增量刷新**

### 2. 关于 HTML 标签的能力差异说明
> **与 Android Markwon HtmlPlugin 不同**：flutter_markdown_plus 按 Flutter 原生
> 组件渲染 Markdown，**不支持内联 HTML**（如下划线 `<u>`、`<font color>`、`<sub>`/`<sup>`）。
> Android 案例中的「下划线 / 字体颜色 / 上下标」效果需用原生 Widget 或行内样式替代。

以下为 Android 侧 HTML 写法在 Flutter 中的等效实现思路：

```dart
// 行内着色 / 上下标：使用 Text.rich 拆分 Span 渲染
Text.rich(
  TextSpan(
    children: [
      TextSpan(text: 'H'),
      TextSpan(text: '2', style: TextStyle(fontSize: 10)),
      TextSpan(text: 'O（水分子式）'),
    ],
  ),
)
```

> 行内 `code` 代码与 GFM 自动链接、Emoji 语法不受影响，可直接使用。
''';

  /// 5. 超链接与图片示例
  String get _linkImageMarkdown => '''
## 超链接与图片 (网络异步渲染)

### 1. 超链接与交互拦截
通过 `onTapLink` 回调自定义链接点击行为，点击下方链接将触发 Toast 拦截反馈：

- 访问 [flutter_markdown_plus 官方仓库](https://pub.dev/packages/flutter_markdown_plus)
- 查看 [Flutter 官方文档](https://docs.flutter.dev)
- 探索 [Dart 语言官方指南](https://dart.dev/guides)

### 2. 图片内嵌展示
网络图片（https）由组件内部异步下载并内联渲染：

![Android Logo](https://developer.android.com/static/images/brand/Android_Robot.png)

> 图片加载走原生网络栈，支持自动缓存与占位、本地文件与 asset 加载。
''';

  /// 6. 自定义主题样式示例
  String get _customThemeMarkdown => '''
## 自定义主题样式 (MarkdownStyleSheet)

当前正在使用 **自定义绿色主题与暗色代码块配置**：

> **定制化引用条 (Custom Blockquote)**
> 引用条颜色已配置为 Material Green (#4CAF50)，且宽度加粗至 4dp。

### 列表圆点加粗
- 列表项 1：圆点宽度放大为 20dp
- 列表项 2：段落间距与字体间距已重新优化

### 代码块与行内代码样式
行内代码使用了浅青底色与深青文字：`val message = "Hello Markwon"`。

多行代码块配置了暗色背景 (#263238) 与淡青色文字 (#80CBC4)：

```dart
MarkdownStyleSheet buildCustomTheme(BuildContext context) {
  return MarkdownStyleSheet.fromTheme(Theme.of(context)).copyWith(
    blockquoteDecoration: const BoxDecoration(
      color: Color(0x1A4CAF50),
      border: Border(
        left: BorderSide(color: Color(0xFF4CAF50), width: 4),
      ),
    ),
    codeblockDecoration: const BoxDecoration(
      color: Color(0xFF263238),
    ),
    listBullet: TextStyle(fontSize: 20, fontWeight: FontWeight.w700),
  );
}

// 代码块文字通过 SyntaxHighlighter 单独着色（行内代码仍由 code 样式控制）
syntaxHighlighter: MarkdownCodeHighlighter(textColor: Color(0xFF80CBC4)),
```
''';

  /// 7. 综合长文档示例
  String get _comprehensiveMarkdown => '''
# 现代 Flutter AI 流式客户端设计与架构实践

> 随着生成式 AI（Generative AI）技术的普及，在客户端实现流畅、优雅的流式交互体验成为核心竞争力。

---

## 一、 为什么传统的 Markdown 渲染会卡顿？

在传统的文本展示中，页面通常是一次性加载并渲染的。而在 **AI SSE 流式对话场景** 中，网络数据是以每秒数十次的频率持续追加推送的：

1. **频繁 AST 解析**：每次收到 3~5 个字符就重新解析整篇几千字的 Markdown 树，计算复杂度呈 O(N²) 递增。
2. **UI 频繁重排 (Re-layout)**：RichText 持续计算文本测量与行高，造成界面掉帧。
3. **语法闪烁**：当 ```kotlin 或 **加粗** 处于未闭合状态时，解析器会将其渲染为普通文本，闭合瞬间突变为格式化样式。

---

## 二、 关键技术选型对比

| 技术方案 | 解析性能 | 扩展性 | 流式支持友好度 | 推荐指数 |
| :--- | :--- | :--- | :--- | :---: |
| **flutter_markdown_plus (原生)** | 极高（基于 Widget 树） | 极高（Builder 体系完善） | 优秀（支持逐帧重建） | ⭐⭐⭐⭐⭐ |
| **MarkdownWidget 扩展库** | 较高（声明式 Widget） | 中等 | 中等（需拆分 Block 避免重建） | ⭐⭐⭐⭐ |
| **WebView / WebKit** | 中等（DOM 操作） | 较高（CSS 高度自由） | 较差（跨进程通信开销大） | ⭐⭐⭐ |

---

## 三、 演进路线任务清单

- [x] **基础设施搭建**：flutter_markdown_plus 基础与 GFM 扩展支持
- [ ] **语法高亮集成**：flutter_highlight 词法着色
- [ ] **打字机动态流控**：自适应 Buffer 队列与标点节奏
- [ ] **聊天列表实战**：列表增量刷新与吸底跟随

---

## 四、 核心代码片段示例

```dart
// flutter_markdown_plus 基础用法
const MarkdownBody(
  data: markdownSource,
  // GitHub Flavored 默认支持表格 / 任务清单 / 删除线
);
```

点击链接探索更多：[深入学习 flutter_markdown_plus 官方文档](https://pub.dev/packages/flutter_markdown_plus)
''';
}
