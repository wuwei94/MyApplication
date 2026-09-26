import 'dart:async';

import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/markdown/engine/markdown_stream_fixer.dart';
import 'package:flutter_demo/demos/markdown/engine/typewriter_engine.dart';

/// AI Chat — 流式对话推流与打字机输出
///
/// 核心机制与避坑点：
/// 1. 流式推流：`TypewriterEngine.feed` 分段灌入，`complete` 标记结束。
/// 2. 语法容错：出字过程经 `MarkdownStreamFixer.fix` 虚拟闭合，避免半截语法闪烁。
/// 3. 中断语义：停止生成时递增 token 使旧推流任务安全退出，并 `skipToFinish`。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_markdown_plus
class MarkdownChatDemoPage extends BasicResponsePage {
  const MarkdownChatDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<MarkdownChatDemoPage> createState() =>
      _MarkdownChatDemoPageState();
}

class _MarkdownChatDemoPageState
    extends BasicResponsePageState<MarkdownChatDemoPage> {
  static const String _streamKey = 'ai_stream';

  final TypewriterEngine _engine = TypewriterEngine();

  bool _isGenerating = false;
  bool _stopRequested = false;
  int _mockStreamToken = 0;

  @override
  void initState() {
    super.initState();
    showDescription('AI 流式对话示例：TypewriterEngine 推流 + MarkdownStreamFixer 语法容错');
    _initEngine();
  }

  @override
  void dispose() {
    _mockStreamToken++;
    _engine.reset();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 发送问候语',
        '2. 发送 SSE 推流提问',
        '3. 发送 120fps 渲染提问',
        '4. 发送 SQL 调优提问',
        '5. 发送 Hilt vs Koin 提问',
        '6. 停止生成',
        '7. 清空对话日志',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _sendMessage('你好');
      case 1:
        _sendMessage('🚀 请用 Kotlin 写一段基于 SSE 的流式推流解析器');
      case 2:
        _sendMessage('⚡ 如何在 Android 客户端实现 120fps 流式 Markdown 丝滑渲染？');
      case 3:
        _sendMessage('📊 请给出一个 SQL 复杂多表统计与索引调优案例');
      case 4:
        _sendMessage('🏛️ 详细对比 Android 依赖注入 Hilt 与 Koin 的异同与选型建议');
      case 5:
        _stopGeneration();
      case 6:
        _clearChat();
    }
  }

  void _initEngine() {
    _engine.onTextUpdate = (String text, bool isFinished) {
      if (!mounted) return;
      final String fixed =
          MarkdownStreamFixer.fix(text, appendCursor: !isFinished);
      updateLog(_streamKey, '流式输出: $fixed');
      if (isFinished) {
        removeUpdatingLog(_streamKey);
        if (_stopRequested) {
          appendLog('→ [Cancel] 已停止生成');
        } else {
          appendLog('✓ [Chat] 生成完成');
          appendLog('✓ [Chat] 完整回复:\n$fixed');
        }
        _isGenerating = false;
        _stopRequested = false;
      }
    };
  }

  void _sendMessage(String prompt) {
    if (_isGenerating) {
      appendLog('✗ [Chat] 已有生成任务进行中，请先停止');
      return;
    }

    final String trimmed = prompt.trim();
    if (trimmed.isEmpty) return;

    _isGenerating = true;
    _stopRequested = false;
    appendLog('→ [Chat] 提问 $trimmed');

    _engine.start();
    _startMockAiStream(trimmed);
  }

  void _startMockAiStream(String prompt) {
    final List<String> responseChunks = _generateResponseChunks(prompt);
    final int token = ++_mockStreamToken;

    Future<void> task() async {
      await Future<void>.delayed(const Duration(milliseconds: 260));
      for (final String chunk in responseChunks) {
        if (token != _mockStreamToken) return;
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
    if (!_isGenerating) {
      appendLog('→ [Cancel] 当前没有进行中的生成任务');
      return;
    }
    _mockStreamToken++;
    _stopRequested = true;
    _engine.skipToFinish();
  }

  void _clearChat() {
    if (_isGenerating) {
      _stopGeneration();
    }
    clearLog();
    showDescription('AI 流式对话示例：TypewriterEngine 推流 + MarkdownStreamFixer 语法容错');
  }

  List<String> _generateResponseChunks(String prompt) {
    final String trimmed = prompt.trim();
    final String lower = trimmed.toLowerCase();

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
        '> 请随时通过下方操作列表发起提问！',
      ];
    }

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
      '> 💡 **提示**：您可以继续通过操作列表探索 SSE 流式推流、'
          'Markdown 语法高亮与打字机流控！',
    ];
  }
}
