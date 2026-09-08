import 'package:flutter/material.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';
import 'package:flutter_demo/demos/markdown/widgets/markdown_case_selector.dart';
import 'package:flutter_highlight/flutter_highlight.dart';
import 'package:flutter_highlight/themes/atom-one-dark.dart';
import 'package:flutter_highlight/themes/github.dart';
import 'package:highlight/highlight.dart' as hl;

/// 多语言代码语法高亮示例
///
/// 对标 Android module_markdown 的 `MarkwonHighlightActivity`
/// （Markwon + Prism4j 离线词法着色），Flutter 侧使用 flutter_highlight
/// （基于 highlight.js 词法规则的纯 Dart 高亮组件），业务逻辑保持一致。
///
/// 页面交互：顶部横滑标签切换案例（内容型页面将纵向空间留给阅读区，
/// 案例文案与功能逐项对齐 Android `MarkwonHighlightActivity`）。
///
/// 核心特性与技术亮点：
/// 1. 多语言语法表覆盖：Kotlin、Java、Python、JavaScript、JSON、SQL、Bash、C/C++
/// 2. 丰富的主题色彩：atom-one-dark（对标 Darkula 暗黑）与 github（对标 Default 明亮）两种主题
/// 3. 原生 Widget 渲染：词法着色结果直接以 TextSpan 渲染，内存极轻、无 WebView 损耗
/// 4. 耗时评测：展示如何度量高亮解析耗时（Android 端对应后台协程方案）
///
/// https://pub.dev/packages/flutter_highlight
class MarkdownHighlightDemoPage extends StatefulWidget {
  final String title;

  const MarkdownHighlightDemoPage({super.key, required this.title});

  @override
  State<MarkdownHighlightDemoPage> createState() =>
      _MarkdownHighlightDemoPageState();
}

class _MarkdownHighlightDemoPageState extends State<MarkdownHighlightDemoPage> {
  /// 案例操作列表（文案与 Android `MarkwonHighlightActivity.buildList()` 一致）
  static const List<String> _caseTitles = <String>[
    '1. Kotlin & Java 高阶语法高亮（协程 / 泛型 / 注解）',
    '2. Python & JS / TS（装饰器 / 异步 / JSON）',
    '3. SQL & Linux Shell / Bash 脚本',
    '4. C / C++ 系统编程（宏 / 模板 / 指针）',
    '5. 暗黑主题模式 (Prism4jThemeDarkula)',
    '6. 明亮主题模式 (Prism4jThemeDefault)',
    '7. 异步后台协程高亮与耗时评测',
  ];

  int _caseIndex = 0;
  final ScrollController _scrollController = ScrollController();

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
    WidgetsBinding.instance.addPostFrameCallback((Duration _) {
      if (_scrollController.hasClients) {
        _scrollController.jumpTo(0);
      }
      // Android 在点击第 7 项时同步触发后台异步高亮评测
      if (index == 6 && mounted) {
        _runBenchmark();
      }
    });
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
          // 内容区域
          Expanded(
            child: ListView(
              controller: _scrollController,
              padding: const EdgeInsets.all(12),
              children: _buildCaseContent(_caseIndex),
            ),
          ),
        ],
      ),
    );
  }

  List<Widget> _buildCaseContent(int index) {
    return switch (index) {
      0 => _buildKotlinJava(),
      1 => _buildPythonJs(),
      2 => _buildSqlBash(),
      3 => _buildCpp(),
      4 => _buildTheme(
        title: '暗黑代码主题 (Prism4jThemeDarkula → atom-one-dark)',
        desc: '当前正在使用 **暗黑主题** 配色，适合暗黑模式或代码气泡背景：',
        theme: atomOneDarkTheme,
        background: const Color(0xFF282C34),
        samples: const <(String, String)>[
          ('kotlin', _userProfileKotlin),
          ('json', _userProfileJson),
        ],
      ),
      5 => _buildTheme(
        title: '明亮代码主题 (Prism4jThemeDefault → github)',
        desc: '当前正在使用 **明亮主题** 配色，适合浅色卡片背景：',
        theme: githubTheme,
        background: const Color(0xFFF6F8FA),
        samples: const <(String, String)>[
          ('kotlin', _userProfileKotlin),
          ('json', _userProfileJson),
        ],
      ),
      _ => _buildBenchmark(),
    };
  }

  // ---------- 1. Kotlin & Java ----------

  List<Widget> _buildKotlinJava() {
    return <Widget>[
      const _CodeSectionTitle('Kotlin 协程与 Flow 数据流'),
      _codeCard('kotlin', _kotlinFlowCode, dark: true),
      const SizedBox(height: 12),
      const _CodeSectionTitle('Java 并发与反射示例'),
      _codeCard('java', _javaThreadPoolCode, dark: true),
    ];
  }

  // ---------- 2. Python & JS ----------

  List<Widget> _buildPythonJs() {
    return <Widget>[
      const _CodeSectionTitle('Python 异步与类型提示 (FastAPI Server)'),
      _codeCard('python', _pythonFastApiCode, dark: true),
      const SizedBox(height: 12),
      const _CodeSectionTitle('JavaScript / TypeScript 现代语法'),
      _codeCard('javascript', _jsStreamCode, dark: true),
    ];
  }

  // ---------- 3. SQL & Bash ----------

  List<Widget> _buildSqlBash() {
    return <Widget>[
      const _CodeSectionTitle('SQL 复杂查询与聚合'),
      _codeCard('sql', _sqlQueryCode, dark: true),
      const SizedBox(height: 12),
      const _CodeSectionTitle('Linux Shell / Bash 自动化脚本'),
      _codeCard('bash', _bashBuildCode, dark: true),
    ];
  }

  // ---------- 4. C / C++ ----------

  List<Widget> _buildCpp() {
    return <Widget>[
      const _CodeSectionTitle('C / C++ 底层系统与 NDK 编程'),
      _codeCard('cpp', _cppPoolCode, dark: true),
    ];
  }

  // ---------- 5 / 6. 主题对比 ----------

  List<Widget> _buildTheme({
    required String title,
    required String desc,
    required Map<String, TextStyle> theme,
    required Color background,
    required List<(String, String)> samples,
  }) {
    return <Widget>[
      Text(
        title,
        style: Theme.of(
          context,
        ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
      ),
      const SizedBox(height: 6),
      Text(desc, style: Theme.of(context).textTheme.bodySmall),
      const SizedBox(height: 12),
      for (final (String language, String code) in samples) ...<Widget>[
        _codeCard(language, code, theme: theme, background: background),
        const SizedBox(height: 12),
      ],
    ];
  }

  // ---------- 7. 高亮耗时评测 ----------

  List<Widget> _buildBenchmark() {
    return <Widget>[
      const Text(
        '异步协程后台高亮解析评测',
        style: TextStyle(fontWeight: FontWeight.w700, fontSize: 15),
      ),
      const SizedBox(height: 6),
      Text(
        '在流式输出长代码块时，如果每帧在主线程重复构建 AST 并做词法染色极易掉帧；'
        '推荐将高亮解析下沉到后台 Isolate / compute，再回主线程一次性绑定 TextSpan。'
        '以下展示大段代码的解析耗时度量。',
        style: Theme.of(context).textTheme.bodySmall,
      ),
      const SizedBox(height: 10),
      ElevatedButton.icon(
        onPressed: _runBenchmark,
        icon: const Icon(Icons.timer_outlined, size: 18),
        label: const Text('运行高亮解析耗时评测'),
      ),
      const SizedBox(height: 12),
      const _CodeSectionTitle('后台异步解析模式示例 (Kotlin → Dart)'),
      _codeCard('dart', _dartBenchmarkCode, dark: true),
      const SizedBox(height: 12),
      const _CodeSectionTitle('Python 性能监控数据结构'),
      _codeCard('python', _pythonMetricsCode, dark: true),
    ];
  }

  void _runBenchmark() {
    final Stopwatch stopwatch = Stopwatch()..start();
    hl.highlight.parse(_heavyDartBenchmarkCode, language: 'dart');
    stopwatch.stop();
    showToast('后台解析耗时: ${stopwatch.elapsedMilliseconds}ms（界面零卡顿）');
  }

  // ---------- 代码卡片 ----------

  Widget _codeCard(
    String language,
    String code, {
    Map<String, TextStyle>? theme,
    Color? background,
    bool dark = false,
  }) {
    final Map<String, TextStyle> effectiveTheme =
        theme ?? (dark ? atomOneDarkTheme : githubTheme);
    final Color effectiveBackground =
        background ??
        (dark ? const Color(0xFF282C34) : const Color(0xFFF6F8FA));

    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: effectiveBackground,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: const Color(0x1FFFFFFF)),
      ),
      clipBehavior: Clip.antiAlias,
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.all(8),
        child: HighlightView(
          code,
          language: language,
          theme: effectiveTheme,
          padding: const EdgeInsets.all(0),
          textStyle: const TextStyle(
            fontFamily: 'monospace',
            fontSize: 12,
            height: 1.5,
          ),
        ),
      ),
    );
  }

  // ================= 示例代码内容 =================

  static const String _kotlinFlowCode = '''
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val repository: SseRepository
) : ViewModel() {

    private val _messageFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messageFlow: StateFlow<List<ChatMessage>> = _messageFlow.asStateFlow()

    fun sendMessage(prompt: String) = viewModelScope.launch(Dispatchers.IO) {
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), content = prompt, isUser = true)
        _messageFlow.update { it + userMsg }

        repository.streamChat(prompt)
            .flowOn(Dispatchers.IO)
            .catch { e -> Log.e("Chat", "Stream failed", e) }
            .collect { chunk ->
                // 动态追加 AI 回复片段
                updateAssistantMessage(chunk)
            }
    }
}
''';

  static const String _javaThreadPoolCode = '''
public class ThreadPoolManager {
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 8;
    private final ExecutorService mExecutor;

    public ThreadPoolManager() {
        this.mExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(128),
            new ThreadFactoryBuilder().setNameFormat("worker-%d").build()
        );
    }

    public <T> CompletableFuture<T> submitTask(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, mExecutor);
    }
}
''';

  static const String _pythonFastApiCode = '''
from typing import AsyncGenerator
from fastapi import FastAPI, HTTPException
from fastapi.responses import StreamingResponse
import asyncio

app = FastAPI(title="AI Stream Server")

async def generate_chat_stream(prompt: str) -> AsyncGenerator[str, None]:
    chunks = ["你好！", "这是", "由 Fast", "API 推送", "的流式 Markdown", "内容。"]
    for chunk in chunks:
        await asyncio.sleep(0.08)  # 模拟大模型推理延迟
        yield f"data: {chunk}\\n\\n"
    yield "data: [DONE]\\n\\n"

@app.post("/v1/chat/completions")
async def chat_endpoint(prompt: str):
    if not prompt:
        raise HTTPException(status_code=400, detail="Prompt cannot be empty")
    return StreamingResponse(generate_chat_stream(prompt), media_type="text/event-stream")
''';

  static const String _jsStreamCode = '''
// ES2022 异步流式解析器
export class StreamDecoder {
    #buffer = '';

    constructor(onChunkReceived) {
        this.onChunk = onChunkReceived;
    }

    async processStream(readableStream) {
        const reader = readableStream.getReader();
        const decoder = new TextDecoder('utf-8');

        try {
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                const text = decoder.decode(value, { stream: true });
                this.onChunk?.(text);
            }
        } finally {
            reader.releaseLock();
        }
    }
}
''';

  static const String _sqlQueryCode = '''
-- 查询过去 30 天内各模型请求吞吐量与平均延迟
SELECT
    model_name,
    COUNT(request_id) AS total_requests,
    ROUND(AVG(latency_ms), 2) AS avg_latency,
    SUM(prompt_tokens + completion_tokens) AS total_tokens
FROM
    ai_request_logs
WHERE
    created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
    AND status_code = 200
GROUP BY
    model_name
HAVING
    total_requests > 100
ORDER BY
    total_tokens DESC
LIMIT 10;
''';

  static const String _bashBuildCode = '''
#!/usr/bin/env bash
set -euo pipefail

# 自动化 Flutter 构建与产物收集
PROJECT_DIR="\$(cd "\$(dirname "\${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="\${PROJECT_DIR}/build/app/outputs/flutter-apk"

echo "[INFO] Starting clean build for flutter_demo..."
flutter build apk --release

if [ -d "\${OUTPUT_DIR}" ]; then
    echo "[SUCCESS] Build artifact found at: \${OUTPUT_DIR}"
    ls -lh "\${OUTPUT_DIR}"/*.apk
else
    echo "[ERROR] Output directory not found!" >&2
    exit 1
fi
''';

  static const String _cppPoolCode = '''
#include <iostream>
#include <memory>
#include <vector>
#include <thread>
#include <mutex>

// 基于 RAII 与智能指针的线程安全缓存池
template <typename T>
class SafeResourcePool {
public:
    void push(std::unique_ptr<T> resource) {
        std::lock_guard<std::mutex> lock(mMtx);
        mPool.push_back(std::move(resource));
    }

    std::unique_ptr<T> pop() {
        std::lock_guard<std::mutex> lock(mMtx);
        if (mPool.empty()) {
            return nullptr;
        }
        auto item = std::move(mPool.back());
        mPool.pop_back();
        return item;
    }

private:
    std::vector<std::unique_ptr<T>> mPool;
    std::mutex mMtx;
};

int main() {
    auto pool = std::make_unique<SafeResourcePool<int>>();
    pool->push(std::make_unique<int>(1024));
    std::cout << "Resource initialized successfully." << std::endl;
    return 0;
}
''';

  static const String _userProfileKotlin = '''
// Kotlin 数据类与默认参数
data class UserProfile(
    val userId: Long,
    val username: String,
    val email: String,
    val isVip: Boolean = false
) {
    fun getDisplayName(): String = if (isVip) "👑 \$username" else username
}
''';

  static const String _userProfileJson = '''
{
  "status": "success",
  "code": 200,
  "data": {
    "id": 10086,
    "name": "DeepSeek-V3",
    "tokens": 4096
  }
}
''';

  static const String _dartBenchmarkCode = '''
// Flutter 侧推荐：compute(Isolate) 后台词法分析，主线程零卡顿
final Stopwatch stopwatch = Stopwatch()..start();
final dynamic result = await compute(highlightTask, code);
stopwatch.stop();
debugPrint('后台高亮解析耗时: \${stopwatch.elapsedMilliseconds}ms');
''';

  static const String _heavyDartBenchmarkCode = '''
import 'dart:async';
import 'dart:convert';

/// 模拟大段流式代码：包含泛型、async/await、Factory 构造与正则
class SseStreamDecoder<T> {
  SseStreamDecoder._(this._buffer);

  factory SseStreamDecoder.create() => SseStreamDecoder._(StringBuffer());

  final StringBuffer _buffer;

  Stream<String> decode(Stream<List<int>> source) {
    return source
        .transform(utf8.decoder)
        .transform(const LineSplitter())
        .where((String line) => line.startsWith('data: '))
        .map((String line) => line.substring(6).trim());
  }

  Future<String> accumulate(Stream<String> stream) async {
    await for (final String chunk in stream) {
      _buffer.write(chunk);
      if (chunk == '[DONE]') break;
    }
    return _buffer.toString();
  }
}
''';

  static const String _pythonMetricsCode = '''
# 性能监控数据结构
class PerformanceMetrics:
    def __init__(self, parse_time_ms: float, render_time_ms: float):
        self.parse_time_ms = parse_time_ms
        self.render_time_ms = render_time_ms
        self.total_time_ms = parse_time_ms + render_time_ms
''';
}

class _CodeSectionTitle extends StatelessWidget {
  final String text;

  const _CodeSectionTitle(this.text);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(
        text,
        style: Theme.of(
          context,
        ).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
      ),
    );
  }
}
