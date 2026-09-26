import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_highlight/flutter_highlight.dart';
import 'package:flutter_highlight/themes/atom-one-dark.dart';
import 'package:flutter_highlight/themes/github.dart';
import 'package:highlight/highlight.dart' as hl;

/// flutter_highlight — 多语言代码语法高亮
///
/// 核心机制与避坑点：
/// 1. 词法着色：基于 highlight.js 规则的纯 Dart 高亮，结果直接绑定 TextSpan。
/// 2. 主题切换：atom-one-dark（暗黑）与 github（明亮）两套配色对照。
/// 3. 耗时度量：`hl.highlight.parse` 可同步度量解析耗时，长代码建议下沉 Isolate。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_highlight
class MarkdownHighlightDemoPage extends BasicLayoutPage {
  const MarkdownHighlightDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<MarkdownHighlightDemoPage> createState() =>
      _MarkdownHighlightDemoPageState();
}

class _MarkdownHighlightDemoPageState
    extends BasicLayoutPageState<MarkdownHighlightDemoPage> {
  int _caseIndex = 0;
  int? _benchmarkMs;

  @override
  List<String> buildList() => const <String>[
        '1. 预览 Kotlin 与 Java 高亮',
        '2. 预览 Python 与 JS 高亮',
        '3. 预览 SQL 与 Bash 高亮',
        '4. 预览 C / C++ 高亮',
        '5. 切换暗黑代码主题',
        '6. 切换明亮代码主题',
        '7. 运行高亮耗时评测',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    setState(() {
      _caseIndex = position;
      if (position == 6) {
        _runBenchmark();
      }
    });
  }

  void _runBenchmark() {
    final Stopwatch stopwatch = Stopwatch()..start();
    hl.highlight.parse(_heavyDartBenchmarkCode, language: 'dart');
    stopwatch.stop();
    _benchmarkMs = stopwatch.elapsedMilliseconds;
  }

  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: _buildCaseContent(_caseIndex),
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
            theme: atomOneDarkTheme,
            background: const Color(0xFF282C34),
          ),
      5 => _buildTheme(
            title: '明亮代码主题 (Prism4jThemeDefault → github)',
            theme: githubTheme,
            background: const Color(0xFFF6F8FA),
          ),
      _ => _buildBenchmark(),
    };
  }

  List<Widget> _buildKotlinJava() {
    return <Widget>[
      const _CodeSectionTitle('Kotlin 协程与 Flow 数据流'),
      _codeCard('kotlin', _kotlinFlowCode, dark: true),
      const SizedBox(height: 12),
      const _CodeSectionTitle('Java 并发与反射示例'),
      _codeCard('java', _javaThreadPoolCode, dark: true),
    ];
  }

  List<Widget> _buildPythonJs() {
    return <Widget>[
      const _CodeSectionTitle('Python 异步与类型提示 (FastAPI Server)'),
      _codeCard('python', _pythonFastApiCode, dark: true),
      const SizedBox(height: 12),
      const _CodeSectionTitle('JavaScript / TypeScript 现代语法'),
      _codeCard('javascript', _jsStreamCode, dark: true),
    ];
  }

  List<Widget> _buildSqlBash() {
    return <Widget>[
      const _CodeSectionTitle('SQL 复杂查询与聚合'),
      _codeCard('sql', _sqlQueryCode, dark: true),
      const SizedBox(height: 12),
      const _CodeSectionTitle('Linux Shell / Bash 自动化脚本'),
      _codeCard('bash', _bashBuildCode, dark: true),
    ];
  }

  List<Widget> _buildCpp() {
    return <Widget>[
      const _CodeSectionTitle('C / C++ 底层系统与 NDK 编程'),
      _codeCard('cpp', _cppPoolCode, dark: true),
    ];
  }

  List<Widget> _buildTheme({
    required String title,
    required Map<String, TextStyle> theme,
    required Color background,
  }) {
    return <Widget>[
      Text(
        title,
        style: Theme.of(context)
            .textTheme
            .titleMedium
            ?.copyWith(fontWeight: FontWeight.w700),
      ),
      const SizedBox(height: 12),
      _codeCard('kotlin', _userProfileKotlin, theme: theme, background: background),
      const SizedBox(height: 12),
      _codeCard('json', _userProfileJson, theme: theme, background: background),
    ];
  }

  List<Widget> _buildBenchmark() {
    final int? ms = _benchmarkMs;
    return <Widget>[
      Text(
        ms == null
            ? '点击下方「运行高亮耗时评测」度量大段代码词法解析耗时'
            : 'hl.highlight.parse 耗时: $ms ms',
        style: Theme.of(context).textTheme.bodySmall,
      ),
      const SizedBox(height: 12),
      const _CodeSectionTitle('后台异步解析模式示例 (Kotlin → Dart)'),
      _codeCard('dart', _dartBenchmarkCode, dark: true),
      const SizedBox(height: 12),
      const _CodeSectionTitle('Python 性能监控数据结构'),
      _codeCard('python', _pythonMetricsCode, dark: true),
    ];
  }

  Widget _codeCard(
    String language,
    String code, {
    Map<String, TextStyle>? theme,
    Color? background,
    bool dark = false,
  }) {
    final Map<String, TextStyle> effectiveTheme =
        theme ?? (dark ? atomOneDarkTheme : githubTheme);
    final Color effectiveBackground = background ??
        (dark ? const Color(0xFF282C34) : const Color(0xFFF6F8FA));

    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: effectiveBackground,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
        border: Border.all(color: const Color(0x1FFFFFFF)),
      ),
      clipBehavior: Clip.antiAlias,
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.all(BasicDemoDimens.itemPadding),
        child: HighlightView(
          code,
          language: language,
          theme: effectiveTheme,
          padding: EdgeInsets.zero,
          textStyle: const TextStyle(
            fontFamily: 'monospace',
            fontSize: 12,
            height: 1.5,
          ),
        ),
      ),
    );
  }

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
        yield f"data: {chunk}\\\\n\\\\n"
    yield "data: [DONE]\\\\n\\\\n"

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
  const _CodeSectionTitle(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(
        text,
        style: Theme.of(context)
            .textTheme
            .titleSmall
            ?.copyWith(fontWeight: FontWeight.w700),
      ),
    );
  }
}
