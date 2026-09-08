import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';
import 'package:flutter_demo/demos/markdown/engine/markdown_stream_fixer.dart';
import 'package:flutter_demo/demos/markdown/engine/typewriter_engine.dart';
import 'package:flutter_demo/demos/markdown/models/chat_message.dart';
import 'package:flutter_demo/demos/markdown/widgets/markdown_code_highlighter.dart';
import 'package:flutter_markdown_plus/flutter_markdown_plus.dart';
import 'package:uuid/uuid.dart';

/// AI 流式对话完整实战界面（对标 Android module_markdown 的 `AiChatActivity`）
///
/// 页面视觉、文案与交互细节对齐 Android：
/// - 顶部标题栏右侧「清空对话」红色文字按钮；
/// - 助手气泡：紫色圆形 AI 头像 + Markdown 卡片（思考中 → 生成中 → 完成），
///   底部状态标签与「复制全文」入口；
/// - 用户气泡：浅蓝卡片（#E3F2FD / #90CAF9 描边）；
/// - 底部快捷提示词卡片 + 胶囊输入框 + 发送 / 停止生成按钮（发送蓝 / 停止红）；
/// - 智能吸底：用户上滑暂停跟随并展示「⬇ 回到最新内容」悬浮胶囊；
/// - 语义回复与 Android `generateResponseChunks` 保持一致：
///   问候语 / SSE 推流 / 120fps 渲染 / SQL 调优 / Hilt vs Koin / 通用兜底。
class MarkdownChatDemoPage extends StatefulWidget {
  final String title;

  const MarkdownChatDemoPage({super.key, required this.title});

  @override
  State<MarkdownChatDemoPage> createState() => _MarkdownChatDemoPageState();
}

class _MarkdownChatDemoPageState extends State<MarkdownChatDemoPage> {
  static const Uuid _uuid = Uuid();

  /// 快捷提示词卡片展示文案（与 Android markdown_activity_chat.xml 一致）
  static const List<String> _promptLabels = <String>[
    '🚀 Kotlin 协程 Flow SSE 推流',
    '⚡ 120fps 流式 Markdown 渲染优化',
    '📊 SQL 复杂多表连接与性能调优',
    '🏛️ Hilt vs Koin 深度架构对比',
  ];

  /// 快捷提示词点击后实际发送的提问（与 Android 点击监听一致）
  static const List<String> _promptMessages = <String>[
    '🚀 请用 Kotlin 写一段基于 SSE 的流式推流解析器',
    '⚡ 如何在 Android 客户端实现 120fps 流式 Markdown 丝滑渲染？',
    '📊 请给出一个 SQL 复杂多表统计与索引调优案例',
    '🏛️ 详细对比 Android 依赖注入 Hilt 与 Koin 的异同与选型建议',
  ];

  final TextEditingController _inputController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  final TypewriterEngine _engine = TypewriterEngine();
  final List<ChatMessage> _messages = <ChatMessage>[];

  String _currentAiMessageId = '';
  bool _isGenerating = false;
  bool _stopRequested = false;
  bool _autoScrollEnabled = true;
  bool _showJumpToBottom = false;
  int _mockStreamToken = 0;

  @override
  void initState() {
    super.initState();
    _initEngine();
    _initScrollListener();
    // 默认载入一条欢迎消息
    _showWelcomeMessage();
  }

  @override
  void dispose() {
    _mockStreamToken++;
    _engine.reset();
    _inputController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  // ---------- 引擎与滚动 ----------

  void _initEngine() {
    _engine.onTextUpdate = (String text, bool isFinished) {
      if (!mounted || _currentAiMessageId.isEmpty) return;
      final ChatMessage? message = _findMessage(_currentAiMessageId);
      if (message == null) return;

      setState(() {
        message.content = text;
        message.status = _stopRequested
            ? ChatStatus.failed
            : (isFinished ? ChatStatus.completed : ChatStatus.streaming);
      });

      if (isFinished) {
        _onGenerationFinished();
        if (_autoScrollEnabled) {
          _scrollToBottom(smooth: true);
        }
      } else if (_autoScrollEnabled) {
        _scrollToBottom(smooth: false);
      }
    };
  }

  void _initScrollListener() {
    _scrollController.addListener(() {
      if (!_scrollController.hasClients) return;
      final ScrollPosition position = _scrollController.position;
      final bool atBottom = position.pixels >= position.maxScrollExtent - 8;
      if (atBottom) {
        _autoScrollEnabled = true;
        if (_showJumpToBottom) {
          setState(() {
            _showJumpToBottom = false;
          });
        }
      }
    });
  }

  /// 用户手指拖拽上滑时暂停吸底，并在生成中展示「回到最新内容」悬浮胶囊
  bool _handleScrollNotification(ScrollNotification notification) {
    if (notification is ScrollUpdateNotification &&
        notification.dragDetails != null &&
        notification.metrics.axis == Axis.vertical) {
      final bool canScrollDown =
          notification.metrics.pixels <
          notification.metrics.maxScrollExtent - 8;
      if (canScrollDown) {
        _autoScrollEnabled = false;
        if (_isGenerating && !_showJumpToBottom) {
          setState(() {
            _showJumpToBottom = true;
          });
        }
      }
    }
    return false;
  }

  void _scrollToBottom({required bool smooth}) {
    WidgetsBinding.instance.addPostFrameCallback((Duration _) {
      if (!_scrollController.hasClients) return;
      final double target = _scrollController.position.maxScrollExtent;
      if (target <= 0) return;
      if (smooth) {
        _scrollController.animateTo(
          target,
          duration: const Duration(milliseconds: 200),
          curve: Curves.easeOut,
        );
      } else {
        _scrollController.jumpTo(target);
      }
    });
  }

  ChatMessage? _findMessage(String id) {
    for (final ChatMessage message in _messages) {
      if (message.id == id) return message;
    }
    return null;
  }

  // ---------- 会话操作 ----------

  void _showWelcomeMessage() {
    _messages.add(
      ChatMessage(
        id: _uuid.v4(),
        role: ChatRole.assistant,
        status: ChatStatus.completed,
        content: '''
👋 **您好！我是您的 AI 流式智能助手。**

本界面完整演示了 Flutter 端**极致丝滑**的大模型聊天交互：
- 🚀 **flutter_markdown_plus**：代码块多语言离线语法高亮与 GFM 表格排版；
- ⚡ **局部增量刷新**：流式出字 0 掉帧、0 闪烁；
- 🎯 **自适应打字机**：大模型突发推流自适应提速 + 标点呼吸停顿；
- 🛡️ **未闭合语法容错**：代码块在流式生成中语法树不崩塌。

您可以点击下方的**快捷提示词**或直接在底部输入框提问！
''',
      ),
    );
  }

  void _clearChat() {
    if (_isGenerating) {
      _stopGeneration();
    }
    setState(() {
      _messages.clear();
      _currentAiMessageId = '';
    });
    _showWelcomeMessage();
    showToast('对话已清空');
  }

  void _sendMessage(String prompt) {
    if (_isGenerating) return;
    final String trimmed = prompt.trim();
    if (trimmed.isEmpty) return;

    // 1. 创建用户消息与 AI 占位消息（原子性批量追加）
    final ChatMessage userMsg = ChatMessage(
      id: _uuid.v4(),
      role: ChatRole.user,
      content: trimmed,
      status: ChatStatus.completed,
    );
    final ChatMessage aiMsg = ChatMessage(
      id: _uuid.v4(),
      role: ChatRole.assistant,
      content: '',
      status: ChatStatus.sending,
    );

    setState(() {
      _messages.add(userMsg);
      _messages.add(aiMsg);
      _currentAiMessageId = aiMsg.id;
      _isGenerating = true;
      _stopRequested = false;
      _autoScrollEnabled = true;
      _showJumpToBottom = false;
    });

    // 2. 启动打字机并模拟推流
    _engine.start();
    _startMockAiStream(trimmed);
    _scrollToBottom(smooth: true);
  }

  void _startMockAiStream(String prompt) {
    final List<String> responseChunks = _generateResponseChunks(prompt);
    final int token = ++_mockStreamToken;

    Future<void> task() async {
      // 模拟大模型首字 TTFT 思考耗时
      await Future<void>.delayed(const Duration(milliseconds: 260));
      for (final String chunk in responseChunks) {
        if (token != _mockStreamToken) return;
        // 每次推 2 个字符，间隔 40ms（约 50 字符/秒）
        int i = 0;
        while (i < chunk.length) {
          if (token != _mockStreamToken) return;
          final int step = (chunk.length - i) < 2 ? (chunk.length - i) : 2;
          _engine.feed(chunk.substring(i, i + step));
          i += step;
          await Future<void>.delayed(const Duration(milliseconds: 40));
        }
      }
      if (token == _mockStreamToken) {
        _engine.complete();
      }
    }

    unawaited(task());
  }

  void _stopGeneration() {
    _mockStreamToken++;
    _stopRequested = true;
    _engine.skipToFinish();
    _onGenerationFinished();
    showToast('已停止生成');
  }

  void _onGenerationFinished() {
    setState(() {
      _isGenerating = false;
      _showJumpToBottom = false;
    });
  }

  void _jumpToLatest() {
    setState(() {
      _autoScrollEnabled = true;
      _showJumpToBottom = false;
    });
    _scrollToBottom(smooth: true);
  }

  void _copyMessage(ChatMessage message) {
    Clipboard.setData(ClipboardData(text: message.content));
    showToast('已复制到剪贴板');
  }

  // ---------- 语义回复生成（与 Android AiChatActivity.generateResponseChunks 一致） ----------

  List<String> _generateResponseChunks(String prompt) {
    final String trimmed = prompt.trim();
    final String lower = trimmed.toLowerCase();

    // 1. 问候语语义识别
    const List<String> greetings = <String>[
      '你好',
      '您好',
      'hi',
      'hello',
      '在吗',
      '早',
      '早上好',
      '下午好',
      '晚上好',
      'hey',
      '嗨',
    ];
    if (greetings.contains(lower) ||
        lower.startsWith('你好') ||
        lower.startsWith('您好') ||
        lower.startsWith('hello') ||
        lower.startsWith('hi ')) {
      return <String>[
        '👋 **您好！我是您的 AI 流式对话与技术探索助手。**\n\n',
        '很高兴为您服务！本界面支持现代大模型标准流式协议与富文本排版。\n\n',
        '您可以向我提问各种 Android 与技术问题，例如：\n',
        '- 🚀 **Kotlin & 协程**：Flow SSE 流式解析、Channel 通道与生命周期感知；\n',
        '- ⚡ **渲染与架构**：120fps 丝滑 Markdown 渲染、RecyclerView Payload 局部增量刷新；\n',
        '- 🏛️ **依赖注入**：Google Hilt vs Koin 方案深度对比；\n',
        '- 📊 **数据库调优**：SQL 复杂多表统计与索引优化；\n\n',
        '> 请随时在下方输入框提问或点击预设提示词！',
      ];
    }

    // 2. 预设技术主题匹配：SSE 流式推流
    if (lower.contains('sse') ||
        prompt.contains('推流') ||
        lower.contains('flow')) {
      return <String>[
        '### 基于 Kotlin Flow 与 OkHttp 的 SSE 流式解析器\n\n',
        '在 Android 客户端，推荐使用 OkHttp 的 `EventSource` 或直接基于 '
            '`ResponseBody.byteStream()` 封装为 Kotlin **冷流（Flow）**：\n\n',
        '```kotlin\n',
        'class SseStreamClient(private val okHttpClient: OkHttpClient) {\n\n',
        '    fun streamChat(prompt: String): Flow<String> = channelFlow {\n',
        '        val request = Request.Builder()\n',
        '            .url("https://api.deepseek.com/v1/chat/completions")\n',
        '            .post(createJsonBody(prompt))\n',
        '            .addHeader("Accept", "text/event-stream")\n',
        '            .build()\n\n',
        '        val response = okHttpClient.newCall(request).execute()\n',
        '        val reader = response.body?.charStream()?.buffered()'
            ' ?: return@channelFlow\n\n',
        '        reader.useLines { lines ->\n',
        '            for (line in lines) {\n',
        '                if (line.startsWith("data: ")) {\n',
        '                    val data = line.removePrefix("data: ").trim()\n',
        '                    if (data == "[DONE]") break\n',
        '                    trySend(parseChunkText(data))\n',
        '                }\n',
        '            }\n',
        '        }\n',
        '    }.flowOn(Dispatchers.IO)\n',
        '}\n',
        '```\n\n',
        '**架构优势**：\n',
        '- 🚀 **背压支持**：Flow 自动支持协程背压与生命周期协作取消；\n',
        '- 🛡️ **内存友好**：逐行流式读取，无 OOM 风险！',
      ];
    }

    // 3. 预设技术主题匹配：120fps 高帧率渲染
    if (lower.contains('120fps') ||
        prompt.contains('渲染') ||
        prompt.contains('掉帧') ||
        prompt.contains('流畅')) {
      return <String>[
        '### Android 端 120fps 流式 Markdown 核心优化方案\n\n',
        '要实现极致丝滑的流式富文本体验，必须解决以下四大核心痛点：\n\n',
        '| 瓶颈环节 | 传统做法 | 本方案优化实践 |\n',
        '| :--- | :--- | :--- |\n',
        '| **列表刷新** | `notifyDataSetChanged` | **RecyclerView Payload 局部增量刷新** |\n',
        '| **语法突变** | 随流直出 | **`MarkdownStreamFixer` 虚拟补齐** |\n',
        '| **时钟流控** | 固定 Timer | **`TypewriterEngine` 自适应积压调速** |\n',
        '| **滚动冲突** | 强制跟随 | **手势识别 + 悬浮回底按钮** |\n\n',
        '```kotlin\n',
        '// 在 Adapter 中拦截 Payload 刷新，耗时 < 1ms：\n',
        'override fun onBindViewHolder(holder: ViewHolder, pos: Int, payloads: List<Any>) {\n',
        '    if (payloads.contains(PAYLOAD_STREAM_CONTENT)) {\n',
        '        holder.updateTextOnly(mMarkwon, item)\n',
        '    } else {\n',
        '        holder.bindFull(item)\n',
        '    }\n',
        '}\n',
        '```',
      ];
    }

    // 4. 预设技术主题匹配：SQL 调优
    if (lower.contains('sql') ||
        prompt.contains('数据库') ||
        prompt.contains('索引') ||
        prompt.contains('查询')) {
      return <String>[
        '### SQL 复杂多表查询与索引调优实战\n\n',
        '```sql\n',
        '-- 统计各模型在过去 7 天的 P99 响应延迟与 Token 消耗\n',
        'SELECT \n',
        '    m.model_name,\n',
        '    COUNT(r.request_id) AS total_calls,\n',
        '    ROUND(AVG(r.latency_ms), 2) AS avg_latency,\n',
        '    MAX(r.latency_ms) AS max_latency,\n',
        '    SUM(r.tokens_used) AS total_tokens\n',
        'FROM \n',
        '    models m\n',
        'INNER JOIN \n',
        '    request_logs r ON m.id = r.model_id\n',
        'WHERE \n',
        '    r.created_at >= NOW() - INTERVAL 7 DAY\n',
        'GROUP BY \n',
        '    m.model_name\n',
        'HAVING \n',
        '    total_calls > 500\n',
        'ORDER BY \n',
        '    total_tokens DESC;\n',
        '```\n\n',
        '**索引调优建议**：\n',
        '在 `request_logs` 表建立联合索引：'
            '`CREATE INDEX idx_model_created ON request_logs(model_id, created_at);`。',
      ];
    }

    // 5. 预设技术主题匹配：Hilt vs Koin
    if (lower.contains('hilt') ||
        lower.contains('koin') ||
        prompt.contains('依赖注入') ||
        lower.contains('di')) {
      return <String>[
        '### Android 依赖注入方案对比：Hilt vs Koin\n\n',
        '| 维度 | Google Hilt | Koin |\n',
        '| :--- | :--- | :--- |\n',
        '| **实现原理** | 基于 Dagger APT/KSP 编译期生成代码 | '
            '纯 Kotlin 运行时反射 / DSL Service Locator |\n',
        '| **编译开销** | 增加注解处理器耗时 | **零编译期开销** |\n',
        '| **运行性能** | **极高**（直接方法调用） | 包含运行时查找轻微损耗 |\n',
        '| **错误排查** | **编译期校验**，不合规直接报错 | '
            '运行时解析，可能抛出 `InstanceCreationException` |\n\n',
        '```kotlin\n',
        '// Hilt 构造注入\n',
        '@HiltViewModel\n',
        'class ChatViewModel @Inject constructor(\n',
        '    private val repo: ChatRepository\n',
        ') : ViewModel()\n',
        '```\n\n',
        '**选型建议**：大型商业 App 首选 **Hilt** 获得编译期安全保障；'
            '中小型或快速原型项目推荐 **Koin**。',
      ];
    }

    // 6. 通用自定义提问动态回复（正文与代码样例与 Android 一致）
    return <String>[
      '### 关于「$trimmed」的分析与解答\n\n',
      '感谢您的提问！针对您提出的 **$trimmed**，为您整理了以下核心分析与实践思路：\n\n',
      '1. **核心要点分析**：\n',
      '   - 在架构设计中，`$trimmed` 应当明确边界职责，避免与 UI 生命周期产生耦合；\n',
      '   - 推荐使用声明式状态流（如 `StateFlow`）与单向数据流（UDF）模式进行数据流转；\n\n',
      '2. **推荐实现范式示例**：\n\n',
      '```kotlin\n',
      '// 针对「$trimmed」的通用设计模式\n',
      'class FeatureHandler {\n',
      '    fun executeTask(param: String): Result<String> {\n',
      '        return runCatching {\n',
      '            // 处理「$trimmed」业务链路\n',
      '            "Task [$trimmed] completed successfully."\n',
      '        }\n',
      '    }\n',
      '}\n',
      '```\n\n',
      '> 💡 **提示**：您可以点击下方快捷提示词进一步探索 SSE 流式推流、'
          'Markdown 语法高亮与打字机流控！',
    ];
  }

  // ---------- UI ----------

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          TextButton(
            onPressed: _messages.isEmpty ? null : _clearChat,
            style: TextButton.styleFrom(
              foregroundColor: const Color(0xFFF44336),
              textStyle: const TextStyle(fontSize: 13),
            ),
            child: const Text('清空对话'),
          ),
        ],
      ),
      body: Column(
        children: <Widget>[
          Expanded(
            child: NotificationListener<ScrollNotification>(
              onNotification: _handleScrollNotification,
              child: Stack(
                children: <Widget>[
                  ListView.builder(
                    controller: _scrollController,
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    itemCount: _messages.length,
                    itemBuilder: (BuildContext context, int index) {
                      return _buildMessage(_messages[index]);
                    },
                  ),
                  // 悬浮回到最新内容（对齐 Android cardScrollBottom 吸底胶囊）
                  Positioned(
                    left: 0,
                    right: 0,
                    bottom: 16,
                    child: Center(
                      child: AnimatedOpacity(
                        opacity: _showJumpToBottom ? 1 : 0,
                        duration: const Duration(milliseconds: 150),
                        child: _showJumpToBottom ? _buildJumpToBottom() : null,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          _buildQuickPromptsBar(),
          const Divider(height: 1),
          _buildInputBar(),
        ],
      ),
    );
  }

  /// 悬浮「回到最新内容」胶囊按钮（#2196F3 圆角胶囊）
  Widget _buildJumpToBottom() {
    return Material(
      color: const Color(0xFF2196F3),
      borderRadius: BorderRadius.circular(18),
      elevation: 4,
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: _jumpToLatest,
        child: const Padding(
          padding: EdgeInsets.symmetric(horizontal: 14, vertical: 8),
          child: Text(
            '⬇ 回到最新内容',
            style: TextStyle(
              color: Color(0xFFFFFFFF),
              fontSize: 12,
              fontWeight: FontWeight.w700,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildQuickPromptsBar() {
    return SizedBox(
      height: 40,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        scrollDirection: Axis.horizontal,
        itemCount: _promptLabels.length,
        separatorBuilder: (BuildContext context, int index) =>
            const SizedBox(width: 8),
        itemBuilder: (BuildContext context, int index) {
          return Material(
            color: const Color(0xFFF1F3F4),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(14),
              side: const BorderSide(color: Color(0xFFE0E0E0)),
            ),
            child: InkWell(
              borderRadius: BorderRadius.circular(14),
              onTap: () => _sendMessage(_promptMessages[index]),
              child: Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 6,
                ),
                child: Text(
                  _promptLabels[index],
                  style: const TextStyle(
                    fontSize: 12,
                    color: Color(0xFF424242),
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildInputBar() {
    return SafeArea(
      top: false,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(12, 6, 12, 8),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: <Widget>[
            Expanded(
              child: TextField(
                controller: _inputController,
                minLines: 1,
                maxLines: 4,
                textInputAction: TextInputAction.send,
                onSubmitted: (String value) => _handleSendPressed(),
                style: const TextStyle(fontSize: 14),
                decoration: InputDecoration(
                  hintText: '向 AI 助手提问...',
                  hintStyle: TextStyle(
                    fontSize: 14,
                    color: Theme.of(context).colorScheme.outline,
                  ),
                  isDense: true,
                  filled: true,
                  fillColor: const Color(0xFFF1F3F4),
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 14,
                    vertical: 10,
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(20),
                    borderSide: const BorderSide(color: Color(0xFFE0E0E0)),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(20),
                    borderSide: const BorderSide(
                      color: Color(0xFF2196F3),
                      width: 1.2,
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(width: 8),
            // 发送 / 停止生成按钮（发送蓝 #2196F3，停止红 #E53935）
            SizedBox(
              height: 40,
              child: FilledButton(
                onPressed: _handleSendPressed,
                style: FilledButton.styleFrom(
                  backgroundColor: _isGenerating
                      ? const Color(0xFFE53935)
                      : const Color(0xFF2196F3),
                  padding: const EdgeInsets.symmetric(horizontal: 18),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(20),
                  ),
                  textStyle: const TextStyle(fontSize: 13),
                ),
                child: Text(_isGenerating ? '停止生成' : '发送'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _handleSendPressed() {
    if (_isGenerating) {
      _stopGeneration();
    } else {
      final String input = _inputController.text.trim();
      if (input.isNotEmpty) {
        _sendMessage(input);
        _inputController.clear();
      }
    }
  }

  Widget _buildMessage(ChatMessage message) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      child: message.role == ChatRole.user
          ? _buildUserBubble(message)
          : _buildAssistantRow(message),
    );
  }

  /// 用户气泡：右对齐浅蓝卡片（#E3F2FD / #90CAF9 描边 / #0D47A1 文字）
  Widget _buildUserBubble(ChatMessage message) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.end,
      children: <Widget>[
        Flexible(
          child: Container(
            constraints: BoxConstraints(
              maxWidth: MediaQuery.sizeOf(context).width - 96,
            ),
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
            decoration: BoxDecoration(
              color: const Color(0xFFE3F2FD),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: const Color(0xFF90CAF9)),
            ),
            child: Text(
              message.content,
              style: const TextStyle(
                fontSize: 15,
                height: 1.2,
                color: Color(0xFF0D47A1),
              ),
            ),
          ),
        ),
      ],
    );
  }

  /// 助手消息行：紫色 AI 头像 + Markdown 气泡
  Widget _buildAssistantRow(ChatMessage message) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        // AI 头像（#6200EE 圆形）
        Container(
          width: 32,
          height: 32,
          margin: const EdgeInsets.only(top: 4),
          alignment: Alignment.center,
          decoration: const BoxDecoration(
            color: Color(0xFF6200EE),
            shape: BoxShape.circle,
          ),
          child: const Text(
            'AI',
            style: TextStyle(
              color: Color(0xFFFFFFFF),
              fontSize: 12,
              fontWeight: FontWeight.w700,
            ),
          ),
        ),
        const SizedBox(width: 8),
        Expanded(child: _buildAssistantBubble(message)),
        const SizedBox(width: 16),
      ],
    );
  }

  /// 助手气泡卡片：Markdown 富文本 + 思考指示器 + 状态/复制页脚
  Widget _buildAssistantBubble(ChatMessage message) {
    final bool sending =
        message.status == ChatStatus.sending && message.content.isEmpty;
    final MarkdownStyleSheet styleSheet =
        MarkdownStyleSheet.fromTheme(Theme.of(context)).copyWith(
          p: const TextStyle(fontSize: 14, height: 1.5),
          codeblockDecoration: const BoxDecoration(
            color: Color(0xFF282C34),
            borderRadius: BorderRadius.all(Radius.circular(6)),
          ),
          codeblockPadding: const EdgeInsets.all(8),
          code: const TextStyle(
            color: Color(0xFFD81B60),
            backgroundColor: Color(0x14000000),
            fontFamily: 'monospace',
            fontSize: 12.5,
          ),
        );

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(14, 12, 14, 8),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0x20000000)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x0F000000),
            blurRadius: 2,
            offset: Offset(0, 1),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          if (sending)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 6),
              child: SizedBox(
                width: 16,
                height: 16,
                child: CircularProgressIndicator(strokeWidth: 2),
              ),
            )
          else
            MarkdownBody(
              data: MarkdownStreamFixer.fix(
                message.content,
                appendCursor: message.status == ChatStatus.streaming,
              ),
              selectable: true,
              syntaxHighlighter: MarkdownCodeHighlighter(),
              styleSheet: styleSheet,
            ),
          const SizedBox(height: 6),
          _buildAssistantFooter(message),
        ],
      ),
    );
  }

  /// 助手页脚：状态标签 + 一键复制全文
  Widget _buildAssistantFooter(ChatMessage message) {
    final ThemeData theme = Theme.of(context);
    final (String text, Color color) = switch (message.status) {
      ChatStatus.sending => (
        '正在思考中...',
        theme.colorScheme.onSurfaceVariant,
      ),
      ChatStatus.streaming => (
        '正在生成回答...',
        theme.colorScheme.onSurfaceVariant,
      ),
      ChatStatus.completed => ('生成完成', theme.colorScheme.onSurfaceVariant),
      ChatStatus.failed => ('生成已中断', theme.colorScheme.error),
    };

    final bool canCopy = message.status == ChatStatus.completed ||
        message.status == ChatStatus.failed;

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: <Widget>[
        Text(
          text,
          style: TextStyle(fontSize: 11, color: color),
        ),
        if (canCopy)
          InkWell(
            onTap: () => _copyMessage(message),
            child: const Padding(
              padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              child: Text(
                '复制全文',
                style: TextStyle(
                  fontSize: 11,
                  color: Color(0xFF2196F3),
                ),
              ),
            ),
          ),
      ],
    );
  }
}
