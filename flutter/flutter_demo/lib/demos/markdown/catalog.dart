import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/markdown/markdown_basic_demo.dart';
import 'package:flutter_demo/demos/markdown/markdown_chat_demo.dart';
import 'package:flutter_demo/demos/markdown/markdown_highlight_demo.dart';
import 'package:flutter_demo/demos/markdown/markdown_typewriter_demo.dart';

/// Markdown 模块
///
/// 对标 Android `module_markdown` 的 4 个示例页面（业务逻辑一致）：
/// 1. Markdown 基础与扩展渲染（flutter_markdown_plus）
/// 2. 多语言代码语法高亮（flutter_highlight）
/// 3. 流式打字机与未闭合语法容错（TypewriterEngine + MarkdownStreamFixer）
/// 4. AI 流式对话完整实战
class MarkdownCatalog extends CatalogSection {
  const MarkdownCatalog._();

  @override
  String get path => 'markdown';

  @override
  String get title => 'Markdown';

  @override
  String get subtitle => '富文本渲染、代码高亮与 AI 流式交互';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = <CatalogEntry>[
    CatalogEntry.page(
      path: 'markdown-basic',
      title: 'Markdown 基础与扩展渲染',
      subtitle: 'flutter_markdown_plus：标题/引用/表格/任务清单/删除线/图片/主题',
      pageBuilder: (BuildContext context) =>
          const MarkdownBasicDemoPage(title: 'Markdown 基础与扩展渲染'),
    ),
    CatalogEntry.page(
      path: 'markdown-highlight',
      title: '多语言代码语法高亮',
      subtitle: 'flutter_highlight：Kotlin/Java/Python/JS/SQL/Bash/C++ 染色',
      pageBuilder: (BuildContext context) =>
          const MarkdownHighlightDemoPage(title: '多语言代码语法高亮'),
    ),
    CatalogEntry.page(
      path: 'markdown-typewriter',
      title: '流式打字机与语法容错',
      subtitle: 'TypewriterEngine 自适应调速 + MarkdownStreamFixer 未闭合补全',
      pageBuilder: (BuildContext context) =>
          const MarkdownTypewriterDemoPage(title: '流式打字机与语法容错'),
    ),
    CatalogEntry.page(
      path: 'markdown-chat',
      title: 'AI 流式对话完整实战',
      subtitle: '流式气泡列表、智能吸底、停止生成与全文复制',
      pageBuilder: (BuildContext context) =>
          const MarkdownChatDemoPage(title: 'AI 流式对话完整实战'),
    ),
  ];
}

/// 单例实例
const MarkdownCatalog markdownCatalog = MarkdownCatalog._();
