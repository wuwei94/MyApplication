import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/markdown/engine/markdown_stream_fixer.dart';
import 'package:flutter_demo/demos/markdown/engine/typewriter_engine.dart';
import 'package:flutter_demo/demos/markdown/widgets/markdown_code_highlighter.dart';
import 'package:flutter_markdown_plus/flutter_markdown_plus.dart';

/// TypewriterEngine — 流式打字机与语法容错
///
/// 核心机制与避坑点：
/// 1. 自适应流控：积压加速 + 标点呼吸停顿，保证出字既有节奏又不落后推流。
/// 2. 语法容错：`MarkdownStreamFixer` 虚拟闭合未完成代码块 / 富文本标签，消除闪烁。
/// 3. 生命周期：`feed` / `complete` / `pause` / `resume` / `skipToFinish` / `reset`。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_markdown_plus
class MarkdownTypewriterDemoPage extends BasicLayoutPage {
  const MarkdownTypewriterDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<MarkdownTypewriterDemoPage> createState() =>
      _MarkdownTypewriterDemoPageState();
}

class _MarkdownTypewriterDemoPageState
    extends BasicLayoutPageState<MarkdownTypewriterDemoPage> {
  final TypewriterEngine _engine = TypewriterEngine();
  final ScrollController _scrollController = ScrollController();

  String _displayText = '';
  TypewriterState _engineState = TypewriterState.idle;
  int _backlog = 0;
  int _speedMs = 0;
  int _feedToken = 0;

  @override
  void initState() {
    super.initState();
    _initEngine();
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

  @override
  List<String> buildList() => const <String>[
        '1. 启动突发推流演示',
        '2. 启动平缓推流演示',
        '3. 启动代码块补全验证',
        '4. 启动标点节奏演示',
        '5. 暂停或恢复打字',
        '6. 跳过并立即完成',
        '7. 重置清空',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
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
  }

  void _initEngine() {
    _engine.onTextUpdate = (String text, bool isFinished) {
      if (!mounted) return;
      setState(() {
        _displayText = MarkdownStreamFixer.fix(text, appendCursor: !isFinished);
      });
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
    _startStreamTask(chunks, feedWholeChunk: true, chunkDelayMs: 120);
  }

  void _startSmoothStreamDemo() {
    _resetAll();
    _engine.start();
    const String text =
        '这是一个平缓细腻的打字机演示。网络推流以均匀的小节奏推送到客户端，'
        '打字机保持在 35ms 左右的稳定出字速度，光标在末尾优雅地闪烁。';
    _startStreamTask(<String>[text], perCharDelayMs: 30);
  }

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

  void _togglePauseResume() {
    if (_engineState == TypewriterState.paused) {
      _engine.resume();
    } else if (_engineState == TypewriterState.typing) {
      _engine.pause();
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

  @override
  Widget buildPreview() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        _buildMetricsBar(),
        const Divider(height: 1),
        Expanded(child: _buildMarkdownArea()),
      ],
    );
  }

  Widget _buildMetricsBar() {
    final (String label, Color color) = _statePresentation(_engineState);
    return Container(
      width: double.infinity,
      color: const Color(0xFFF8FAFC),
      padding: const EdgeInsets.symmetric(
        horizontal: BasicDemoDimens.cardPadding,
        vertical: BasicDemoDimens.itemPadding,
      ),
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
          style: TextStyle(
            fontSize: BasicDemoDimens.fontBody,
            color: theme.colorScheme.outline,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          value,
          style: TextStyle(
            fontSize: BasicDemoDimens.fontTitle,
            fontWeight: FontWeight.w700,
            color: valueColor,
          ),
        ),
      ],
    );
  }

  (String, Color) _statePresentation(TypewriterState state) {
    return switch (state) {
      TypewriterState.typing => ('TYPING', const Color(0xFF4CAF50)),
      TypewriterState.paused => ('PAUSED', const Color(0xFFFF9800)),
      TypewriterState.completed => ('COMPLETED', const Color(0xFF2196F3)),
      TypewriterState.idle => ('IDLE', const Color(0xFF9E9E9E)),
    };
  }

  Widget _buildMarkdownArea() {
    final ThemeData theme = Theme.of(context);
    final MarkdownStyleSheet styleSheet = MarkdownStyleSheet.fromTheme(theme)
        .copyWith(
          codeblockDecoration: const BoxDecoration(
            color: Color(0xFF282C34),
            borderRadius: BorderRadius.all(Radius.circular(6)),
          ),
          codeblockPadding: const EdgeInsets.all(10),
          code: const TextStyle(
            color: Color(0xFFD81B60),
            backgroundColor: Color(0x14000000),
            fontFamily: 'monospace',
            fontSize: 12.5,
          ),
        );

    if (_displayText.isEmpty) {
      return Center(
        child: Text(
          '选择下方操作项开始流式打字机演示…',
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
      );
    }

    return Container(
      margin: const EdgeInsets.all(BasicDemoDimens.compact),
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
