import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';
import 'package:flutter_demo/demos/markdown/engine/markdown_stream_fixer.dart';
import 'package:flutter_demo/demos/markdown/engine/typewriter_engine.dart';
import 'package:flutter_demo/demos/markdown/widgets/markdown_case_selector.dart';
import 'package:flutter_demo/demos/markdown/widgets/markdown_code_highlighter.dart';
import 'package:flutter_markdown_plus/flutter_markdown_plus.dart';

/// 流式 Markdown 打字机与未闭合语法容错示例
///
/// 对标 Android module_markdown 的 `StreamTypewriterActivity`，业务逻辑一致：
/// 1. TypewriterEngine：动态自适应出字速率控制（积压加速 + 标点呼吸停顿
///    + 暂停/恢复/跳过）；
/// 2. MarkdownStreamFixer：实时检测未闭合的 ``` 代码块与行内富文本标签，
///    虚拟闭合防止界面跳动闪烁；
/// 3. 渲染管线：每次出字对全文做一次「修复 + 渲染」，保持单调递增的流畅体验。
///
/// 页面交互：顶部横滑标签切换案例（流式演示 1~4 与暂停/跳过/重置控制项），
/// 标签下方为引擎指标条（状态 / 缓冲积压 / 出字时钟），正文区域占满剩余空间。
/// 案例文案与功能逐项对齐 Android `StreamTypewriterActivity`。
class MarkdownTypewriterDemoPage extends StatefulWidget {
  final String title;

  const MarkdownTypewriterDemoPage({super.key, required this.title});

  @override
  State<MarkdownTypewriterDemoPage> createState() =>
      _MarkdownTypewriterDemoPageState();
}

class _MarkdownTypewriterDemoPageState
    extends State<MarkdownTypewriterDemoPage> {
  /// 案例操作项（文案与 Android `StreamTypewriterActivity.buildList()` 一致）
  static const List<String> _caseTitles = <String>[
    '1. 突发推流 (Burst Stream - 大段 Markdown 自适应加速)',
    '2. 平缓推流 (Smooth Stream - 逐字细腻呼吸感)',
    '3. 代码块补全验证 (Auto-close Fixer - 流式语法不崩溃)',
    '4. 标点节奏停顿演示 (Punctuation Rhythm)',
    '5. 暂停 / 恢复 (Pause & Resume)',
    '6. 一键跳过 / 立即完成 (Skip to Finish)',
    '7. 重置清空 (Reset)',
  ];

  final TypewriterEngine _engine = TypewriterEngine();
  final ScrollController _scrollController = ScrollController();

  String _displayText = '';

  // 引擎流控指标
  TypewriterState _engineState = TypewriterState.idle;
  int _backlog = 0;
  int _speedMs = 0;

  // 标签高亮：仅流式演示类(0~3)持久高亮，动作类(4~6)不改变高亮
  int _caseIndex = 0;

  // 模拟网络推流任务令牌：切换 / 重置时使旧任务安全退出
  int _feedToken = 0;

  @override
  void initState() {
    super.initState();
    _initEngine();
    // 首帧后默认开始突发推流演示
    WidgetsBinding.instance.addPostFrameCallback((Duration _) {
      if (mounted) _startBurstStreamDemo();
    });
  }

  @override
  void dispose() {
    _feedToken++;
    _engine.reset();
    _scrollController.dispose();
    super.dispose();
  }

  void _initEngine() {
    _engine.onTextUpdate = (String text, bool isFinished) {
      if (!mounted) return;
      setState(() {
        _displayText = MarkdownStreamFixer.fix(text, appendCursor: !isFinished);
      });
      // 每次出字后平滑滚动到底部
      WidgetsBinding.instance.addPostFrameCallback((Duration _) {
        if (_scrollController.hasClients) {
          _scrollController.animateTo(
            _scrollController.position.maxScrollExtent,
            duration: const Duration(milliseconds: 80),
            curve: Curves.easeOut,
          );
        }
      });
    };

    _engine.onMetrics = (int backlog, int speedMs, TypewriterState state) {
      if (!mounted) return;
      setState(() {
        _backlog = backlog;
        _speedMs = speedMs;
        _engineState = state;
      });
    };
  }

  /// 以「整段 chunk 推送 + 间隔」或「逐字推送」两种节奏模拟网络推流
  void _startStreamTask(
    List<String> chunks, {
    bool feedWholeChunk = false,
    int perCharDelayMs = 30,
    int chunkDelayMs = 0,
  }) {
    final int token = ++_feedToken;

    Future<void> task() async {
      for (final String chunk in chunks) {
        if (token != _feedToken) return;
        if (feedWholeChunk) {
          _engine.feed(chunk);
          if (chunkDelayMs > 0) {
            await Future<void>.delayed(Duration(milliseconds: chunkDelayMs));
          }
        } else {
          for (final String char in chunk.split('')) {
            if (token != _feedToken) return;
            _engine.feed(char);
            await Future<void>.delayed(Duration(milliseconds: perCharDelayMs));
          }
          if (chunkDelayMs > 0) {
            await Future<void>.delayed(Duration(milliseconds: chunkDelayMs));
          }
        }
      }
      if (token == _feedToken) {
        _engine.complete();
      }
    }

    unawaited(task());
  }

  // ---------- 案例操作 ----------

  void _selectCase(int index) {
    switch (index) {
      case 0:
        _startBurstStreamDemo();
      case 1:
        _startSmoothStreamDemo();
      case 2:
        _startCodeFixerDemo();
      case 3:
        _startPunctuationRhythmDemo();
      case 4:
        _togglePauseResume();
      case 5:
        _engine.skipToFinish();
      case 6:
        _resetAll();
    }
    setState(() {
      // 流式演示类操作高亮当前项，动作类操作不持久高亮
      if (index <= 3) _caseIndex = index;
    });
  }

  /// 1. 突发推流演示（内容与 Android 版一致）
  void _startBurstStreamDemo() {
    _resetAll();
    _engine.start();
    final List<String> chunks = <String>[
      '# 现代 AI 流式交互核心原理\n\n',
      '在真实的大模型 SSE 推流场景中，服务端推送往往是**突发性的**（Burst）。\n\n',
      '例如，大模型可能思考 200ms 后一次性吐出数十个字符：\n',
      '> 打字机引擎通过监测缓冲区积压量（Backlog），自动在 16ms ~ 36ms 之间'
          '动态切换出字速率，保证出字既有呼吸感，又不会落后网络推流！\n\n',
      '### 核心性能指标对比：\n\n',
      '| 模式 | 队列积压 | 出字节奏与策略 |\n',
      '| :--- | :--- | :--- |\n',
      '| **极速冲刺** | > 60 字符 | 18ms / 3字（冲刺追赶） |\n',
      '| **快速推进** | 20~60 字符 | 24ms / 2字（紧跟推流） |\n',
      '| **呼吸出字** | < 20 字符 | 36ms / 1字（细腻呼吸） |\n\n',
      '接下来是带有语法高亮的多行代码块演示：\n\n',
      '```kotlin\n',
      'class StreamEngine {\n',
      '    fun feed(chunk: String) {\n',
      '        buffer.append(chunk)\n',
      '    }\n',
      '}\n',
      '```\n\n',
      '整个流式过程自然丝滑，毫无界面闪烁！',
    ];
    _startStreamTask(
      chunks,
      feedWholeChunk: true,
      chunkDelayMs: 120, // 模拟网络突发间隔
    );
  }

  /// 2. 平缓细腻推流演示
  void _startSmoothStreamDemo() {
    _resetAll();
    _engine.start();
    const String text =
        '这是一个平缓细腻的打字机演示。网络推流以均匀的小节奏推送到客户端，'
        '打字机保持在 35ms 左右的稳定出字速度，光标在末尾优雅地闪烁。';
    _startStreamTask(<String>[text], perCharDelayMs: 30);
  }

  /// 3. 复杂语法与嵌套补全演示
  void _startCodeFixerDemo() {
    _resetAll();
    _engine.start();
    final List<String> chunks = <String>[
      '### 流式语法自动补全与容错测试\n\n',
      '在逐字输出时，观察以下未闭合语法是否从第 1 字符起就保持完整稳定：\n\n',
      '1. **多行代码块实时高亮**：\n\n',
      '```kotlin\n',
      'suspend fun fetchStream(): Flow<String> = flow {\n',
      '    emit("Zero Jitter Markdown")\n',
      '}\n',
      '```\n\n',
      '2. **行内语法与嵌套样式**：\n\n',
      '- 行内代码：`val response = api.call()` 即时闭合\n',
      '- 粗斜体嵌套：***这是加粗且斜体的流式文本*** 实时生效\n',
      '- 删除线：~~已废弃的旧版本逻辑~~ 稳定划线\n\n',
      '> MarkdownStreamFixer 单遍状态机保证了在闭合标签到达前，'
          'AST 语法树始终完整无闪烁！',
    ];
    _startStreamTask(chunks, perCharDelayMs: 22, chunkDelayMs: 80);
  }

  /// 4. 标点呼吸停顿演示（叙述文案与 Android 版一致）
  void _startPunctuationRhythmDemo() {
    _resetAll();
    _engine.start();
    final List<String> sentences = <String>[
      '### 标点呼吸停顿与节奏演示\n\n',
      '大模型的回答需要自然的节奏感。\n\n',
      '你看！遇到逗号时，会有约 150ms 的自然换气微停顿；\n',
      '遇到句号、感叹号与问号时？停顿会延长至约 280ms！\n\n',
      '段落换行也是一样。\n\n',
      '这种富有层次的呼吸节奏，让 AI 显得更加生动，就像是在实时思考一样！',
    ];
    _startStreamTask(sentences, perCharDelayMs: 25);
  }

  // ---------- 生命周期控制 ----------

  void _togglePauseResume() {
    if (_engineState == TypewriterState.paused) {
      _engine.resume();
      showToast('打字机已继续');
    } else if (_engineState == TypewriterState.typing) {
      _engine.pause();
      showToast('打字机已暂停');
    }
    setState(() {});
  }

  void _resetAll() {
    _feedToken++;
    _engine.reset();
    setState(() {
      _displayText = '';
      _backlog = 0;
      _speedMs = 0;
      _engineState = TypewriterState.idle;
    });
    if (_scrollController.hasClients) {
      _scrollController.jumpTo(0);
    }
  }

  // ---------- UI ----------

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
          _buildMetricsBar(),
          const Divider(height: 1),
          Expanded(child: _buildMarkdownArea()),
        ],
      ),
    );
  }

  /// 引擎流控指标条（状态 / 缓冲积压 / 出字时钟，配色对齐 Android）
  Widget _buildMetricsBar() {
    final (String label, Color color) = _statePresentation(_engineState);

    return Container(
      width: double.infinity,
      color: const Color(0xFFF8FAFC),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        children: <Widget>[
          _metricItem(label: '引擎状态', value: label, valueColor: color),
          const SizedBox(width: 24),
          _metricItem(
            label: '缓冲积压',
            value: '$_backlog 字',
            valueColor: const Color(0xFFFF9800),
          ),
          const SizedBox(width: 24),
          _metricItem(
            label: '出字时钟',
            value: '$_speedMs ms/字',
            valueColor: const Color(0xFF4CAF50),
          ),
        ],
      ),
    );
  }

  /// 指标条目（小标签 + 加粗数值）
  Widget _metricItem({
    required String label,
    required String value,
    required Color valueColor,
  }) {
    final ThemeData theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        Text(
          label,
          style: TextStyle(fontSize: 11, color: theme.colorScheme.outline),
        ),
        const SizedBox(height: 2),
        Text(
          value,
          style: TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.w700,
            color: valueColor,
          ),
        ),
      ],
    );
  }

  /// 状态 → 展示文案与颜色（与 Android 状态色映射一致）
  (String, Color) _statePresentation(TypewriterState state) {
    return switch (state) {
      TypewriterState.typing => ('TYPING', const Color(0xFF4CAF50)),
      TypewriterState.paused => ('PAUSED', const Color(0xFFFF9800)),
      TypewriterState.completed => ('COMPLETED', const Color(0xFF2196F3)),
      TypewriterState.idle => ('IDLE', const Color(0xFF9E9E9E)),
    };
  }

  /// Markdown 渲染区（代码块暗色底，自带滚动）
  Widget _buildMarkdownArea() {
    final ThemeData theme = Theme.of(context);
    final MarkdownStyleSheet styleSheet = MarkdownStyleSheet.fromTheme(theme)
        .copyWith(
          codeblockDecoration: const BoxDecoration(
            color: Color(0xFF282C34), // 多行代码块暗黑底色
            borderRadius: BorderRadius.all(Radius.circular(6)),
          ),
          codeblockPadding: const EdgeInsets.all(10),
          code: const TextStyle(
            color: Color(0xFFD81B60), // 行内代码高亮粉红色
            backgroundColor: Color(0x14000000), // 行内代码浅色背景
            fontFamily: 'monospace',
            fontSize: 12.5,
          ),
        );

    if (_displayText.isEmpty) {
      return Center(
        child: Text(
          '选择上方操作项开始流式打字机演示…',
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
      );
    }

    return Container(
      margin: const EdgeInsets.all(4),
      child: Markdown(
        controller: _scrollController,
        data: _displayText,
        selectable: true,
        syntaxHighlighter: MarkdownCodeHighlighter(),
        styleSheet: styleSheet,
      ),
    );
  }
}
