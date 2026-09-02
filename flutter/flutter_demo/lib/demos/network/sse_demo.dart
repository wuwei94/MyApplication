import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/constants/urls.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// SSE (Server-Sent Events) — 服务端推送流式传输（DeepSeek AI 大模型对话）
///
/// 演示标准 POST + SSE 流式响应协议，对标 Android module_sse。
/// 包含逐 Token 流式响应、打字机实时渲染与随时中断生成。
///
/// 核心特性：
/// 1. 流式读取：基于 `dio` 流式管道实时消费 SSE 数据
/// 2. 协议解析：标准解析 `data: {...}\n\n` 格式及 `[DONE]` 结束标志
/// 3. AI 对齐：兼容 `deepseek-chat` 格式，支持实时打字机输出
/// 4. 中断控制：支持随时主动 Cancel 中断当前生成流
///
/// 基本用法：
/// ```dart
/// final Response<ResponseBody> response = await dio.post<ResponseBody>(
///   'https://api.deepseek.com/chat/completions',
///   data: jsonEncode({
///     'model': 'deepseek-chat',
///     'stream': true,
///     'messages': [{'role': 'user', 'content': prompt}],
///   }),
///   options: Options(
///     responseType: ResponseType.stream,
///     headers: {'Authorization': 'Bearer $apiKey'},
///   ),
/// );
/// ```
///
/// 适用场景：
/// - AI 大模型对话流式打字输出（DeepSeek、ChatGPT、Claude 等）
/// - 服务端实时事件流单向通知
///
/// See also:
///
///  * [WebSocketDemoPage], 全双工双向实时通信方案
///  * [DioDemoPage], 标准 HTTP/RESTful 请求方案
///
/// https://api-docs.deepseek.com
class SseDemoPage extends StatelessWidget {
  const SseDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SseDemoView(title: title);
  }
}

class SseDemoView extends StatefulWidget {
  const SseDemoView({super.key, required this.title});

  final String title;

  @override
  State<SseDemoView> createState() => _SseDemoViewState();
}

enum _SseState {
  idle('就绪', Color(0xFF64748B), Icons.radio_button_unchecked_rounded),
  connecting('连接中...', Color(0xFFD97706), Icons.sync_rounded),
  streaming('生成中...', Color(0xFF0284C7), Icons.bolt_rounded),
  completed('已完成', Color(0xFF10B981), Icons.check_circle_outline_rounded),
  cancelled('已中断', Color(0xFFF59E0B), Icons.cancel_outlined),
  error('异常', Color(0xFFEF4444), Icons.error_outline_rounded);

  const _SseState(this.label, this.color, this.icon);

  final String label;
  final Color color;
  final IconData icon;
}

class _LogRecord {
  const _LogRecord({required this.message, required this.time});

  final String message;
  final DateTime time;

  String get formattedTime {
    final String hour = time.hour.toString().padLeft(2, '0');
    final String minute = time.minute.toString().padLeft(2, '0');
    final String second = time.second.toString().padLeft(2, '0');
    return '$hour:$minute:$second';
  }
}

class _SseDemoViewState extends State<SseDemoView> {
  static const String _defaultPrompt = '请用一句话介绍你自己和你的核心优势';
  static const String _serverUrl = Urls.deepSeek;
  static const String _keyDeepSeekApiKey = 'deepseek_api_key';

  final SharedPreferencesAsync _prefs = SharedPreferencesAsync();
  final TextEditingController _apiKeyController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  final Dio _dio = Dio();
  CancelToken? _cancelToken;
  StreamSubscription<dynamic>? _subscription;

  _SseState _sseState = _SseState.idle;
  bool _obscureApiKey = true;
  final StringBuffer _responseBuffer = StringBuffer();
  final List<_LogRecord> _logs = <_LogRecord>[];

  @override
  void initState() {
    super.initState();
    _loadSavedApiKey();
  }

  Future<void> _loadSavedApiKey() async {
    final String? savedKey = await _prefs.getString(_keyDeepSeekApiKey);
    if (savedKey != null && savedKey.isNotEmpty && mounted) {
      _apiKeyController.text = savedKey;
    }
  }

  @override
  void dispose() {
    _cancelStream();
    _apiKeyController.dispose();
    _scrollController.dispose();
    _dio.close(force: true);
    super.dispose();
  }

  void _addLog(String message) {
    if (!mounted) return;
    setState(() {
      _logs.insert(0, _LogRecord(message: message, time: DateTime.now()));
    });
  }

  void _clearLogs() {
    setState(() {
      _logs.clear();
    });
  }

  void _sendDeepSeekPrompt() async {
    final String apiKey = _apiKeyController.text.trim();
    if (apiKey.isEmpty) {
      _addLog('----------------------------------------');
      _addLog('【提示】未填写 DeepSeek API Key！');
      _addLog('👉 请在上方 API Key 输入框中填写你的 sk-xxxx 密钥');
      return;
    }

    const String prompt = _defaultPrompt;

    // 异步保存在本地设备（不入 Git）
    await _prefs.setString(_keyDeepSeekApiKey, apiKey);

    _cancelStream();
    setState(() {
      _sseState = _SseState.connecting;
      _responseBuffer.clear();
    });

    _addLog('----------------------------------------');
    _addLog('【目标】$_serverUrl');
    _addLog('【提问】$prompt');
    _addLog('【连接】正在建立 DeepSeek SSE 流式连接...');

    final String jsonBody = jsonEncode(<String, dynamic>{
      'model': 'deepseek-chat',
      'stream': true,
      'messages': <Map<String, String>>[
        <String, String>{'role': 'user', 'content': prompt},
      ],
    });

    _cancelToken = CancelToken();

    try {
      final Response<ResponseBody> response = await _dio.post<ResponseBody>(
        _serverUrl,
        data: jsonBody,
        options: Options(
          responseType: ResponseType.stream,
          headers: <String, String>{
            'Authorization': 'Bearer $apiKey',
            'Content-Type': 'application/json',
            'Accept': 'text/event-stream',
          },
        ),
        cancelToken: _cancelToken,
      );

      if (!mounted) return;
      setState(() {
        _sseState = _SseState.streaming;
      });
      _addLog(
        '【连接成功】HTTP ${response.statusCode}，开始接收流式 Token...',
      );

      _subscription = response.data!.stream
          .cast<List<int>>()
          .transform(utf8.decoder)
          .transform(const LineSplitter())
          .listen(
            (String line) => _handleLine(line),
            onError: (dynamic error) {
              if (error is DioException &&
                  error.type == DioExceptionType.cancel) {
                return;
              }
              _handleError(error.toString());
            },
            onDone: () => _handleDone(),
            cancelOnError: true,
          );
    } on DioException catch (e) {
      if (e.type == DioExceptionType.cancel) return;
      _handleError(e.message ?? e.toString());
    } catch (e) {
      _handleError(e.toString());
    }
  }

  void _handleLine(String line) {
    final String trimmed = line.trim();
    if (trimmed.isEmpty) return;

    if (!trimmed.startsWith('data:')) return;

    final String data = trimmed.substring(5).trim();
    if (data == '[DONE]') {
      _handleDone();
      return;
    }

    final String delta = _parseDeltaContent(data);
    if (delta.isNotEmpty) {
      if (!mounted) return;
      setState(() {
        _responseBuffer.write(delta);
      });
    }
  }

  void _handleDone() {
    if (!mounted) return;
    setState(() {
      _sseState = _SseState.completed;
    });
    _addLog('【完成】收到 [DONE] 标志，DeepSeek 模型生成完毕！');
    _cancelToken = null;
    _subscription?.cancel();
    _subscription = null;
  }

  void _handleError(String error) {
    if (!mounted) return;
    setState(() {
      _sseState = _SseState.error;
    });
    _addLog('【错误】$error');
  }

  void _cancelStream() {
    _subscription?.cancel();
    _subscription = null;
    _cancelToken?.cancel('User cancelled');
    _cancelToken = null;

    if (_sseState == _SseState.streaming || _sseState == _SseState.connecting) {
      setState(() {
        _sseState = _SseState.cancelled;
      });
      _addLog('【中断】已主动取消当前大模型流式输出');
    }
  }

  String _parseDeltaContent(String data) {
    if (data.trim() == '[DONE]') return '';
    try {
      final Map<String, dynamic> json =
          jsonDecode(data) as Map<String, dynamic>;
      final List<dynamic>? choices = json['choices'] as List<dynamic>?;
      if (choices != null && choices.isNotEmpty) {
        final Map<String, dynamic>? first =
            choices.first as Map<String, dynamic>?;
        final Map<String, dynamic>? delta =
            first?['delta'] as Map<String, dynamic>?;
        if (delta != null) {
          final String content = delta['content'] as String? ?? '';
          final String reasoning = delta['reasoning_content'] as String? ?? '';
          if (reasoning.isNotEmpty) {
            return '[思考] $reasoning';
          }
          if (content.isNotEmpty) {
            return content;
          }
        }
      }
      return '';
    } catch (_) {
      return data;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            icon: const Icon(Icons.delete_outline_rounded),
            tooltip: '清空日志',
            onPressed: _logs.isEmpty ? null : _clearLogs,
          ),
        ],
      ),
      body: ListView(
        controller: _scrollController,
        padding: const EdgeInsets.all(16),
        children: <Widget>[
          _buildInfoCard(),
          const SizedBox(height: 16),
          _buildActionCard(),
          const SizedBox(height: 16),
          _buildResponseCard(),
          const SizedBox(height: 16),
          _buildLogSectionHeader(),
          const SizedBox(height: 12),
          if (_logs.isEmpty)
            _buildEmptyLogsCard()
          else
            for (final _LogRecord log in _logs)
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: _buildLogItem(log),
              ),
        ],
      ),
    );
  }

  Widget _buildInfoCard() {
    final ThemeData theme = Theme.of(context);
    final bool isStreaming =
        _sseState == _SseState.streaming || _sseState == _SseState.connecting;

    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        gradient: const LinearGradient(
          colors: <Color>[Color(0xFF0F172A), Color(0xFF1E293B)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x1E0F172A),
            blurRadius: 16,
            offset: Offset(0, 8),
          ),
        ],
      ),
      padding: const EdgeInsets.all(18),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: <Widget>[
              Text(
                'DeepSeek AI 流式对话',
                style: theme.textTheme.titleMedium?.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.w700,
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 10,
                  vertical: 4,
                ),
                decoration: BoxDecoration(
                  color: _sseState.color.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                    color: _sseState.color.withValues(alpha: 0.5),
                  ),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: <Widget>[
                    if (isStreaming)
                      const SizedBox(
                        width: 12,
                        height: 12,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Color(0xFF38BDF8),
                        ),
                      )
                    else
                      Icon(_sseState.icon, size: 14, color: _sseState.color),
                    const SizedBox(width: 6),
                    Text(
                      _sseState.label,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: _sseState.color,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          const Text(
            '地址：$_serverUrl\n模型：deepseek-chat\n特性：POST Prompt -> 逐 Token 流式响应 -> 收到 [DONE] 完成',
            style: TextStyle(
              color: Color(0xFF94A3B8),
              fontSize: 12,
              height: 1.5,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildActionCard() {
    final ThemeData theme = Theme.of(context);
    final bool isStreaming =
        _sseState == _SseState.streaming || _sseState == _SseState.connecting;

    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            'API Key 配置与操作',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _apiKeyController,
            obscureText: _obscureApiKey,
            decoration: InputDecoration(
              labelText: 'DeepSeek API Key (sk-...)',
              hintText: '填写你的 DeepSeek API Key',
              isDense: true,
              filled: true,
              fillColor: Colors.white,
              prefixIcon: const Icon(Icons.vpn_key_outlined, size: 20),
              suffixIcon: IconButton(
                icon: Icon(
                  _obscureApiKey
                      ? Icons.visibility_off_outlined
                      : Icons.visibility_outlined,
                  size: 20,
                ),
                onPressed: () {
                  setState(() {
                    _obscureApiKey = !_obscureApiKey;
                  });
                },
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: const BorderSide(color: Color(0xFFCBD5E1)),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: const BorderSide(color: Color(0xFFCBD5E1)),
              ),
            ),
          ),
          const SizedBox(height: 14),
          Row(
            children: <Widget>[
              Expanded(
                child: FilledButton.icon(
                  onPressed: isStreaming ? null : _sendDeepSeekPrompt,
                  style: FilledButton.styleFrom(
                    backgroundColor: const Color(0xFF0284C7),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                  icon: const Icon(Icons.play_arrow_rounded),
                  label: const Text('发起 DeepSeek 对话（POST Stream）'),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Row(
            children: <Widget>[
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: isStreaming ? _cancelStream : null,
                  style: OutlinedButton.styleFrom(
                    foregroundColor: const Color(0xFFEF4444),
                    side: BorderSide(
                      color: isStreaming
                          ? const Color(0xFFEF4444)
                          : const Color(0xFFCBD5E1),
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                  icon: const Icon(Icons.stop_rounded),
                  label: const Text('中断当前生成（Cancel Stream）'),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildResponseCard() {
    final ThemeData theme = Theme.of(context);
    final String content = _responseBuffer.toString();
    final bool isStreaming = _sseState == _SseState.streaming;

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x06000000),
            blurRadius: 12,
            offset: Offset(0, 4),
          ),
        ],
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Row(
            children: <Widget>[
              const Icon(
                Icons.chat_bubble_outline_rounded,
                size: 18,
                color: Color(0xFF0284C7),
              ),
              const SizedBox(width: 8),
              Text(
                'AI 流式响应结果',
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          if (content.isNotEmpty)
            SelectableText(
              content + (isStreaming ? ' ▍' : ''),
              style: theme.textTheme.bodyMedium?.copyWith(
                fontSize: 14,
                height: 1.6,
                color: const Color(0xFF1E293B),
              ),
            )
          else
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 20),
              child: Center(
                child: Text(
                  '点击上方按钮发起对话，AI 将在此处实时流式打字输出...',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: const Color(0xFF94A3B8),
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildLogSectionHeader() {
    final ThemeData theme = Theme.of(context);
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: <Widget>[
        Text(
          '通信日志 (${_logs.length})',
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.w700,
          ),
        ),
      ],
    );
  }

  Widget _buildEmptyLogsCard() {
    final ThemeData theme = Theme.of(context);
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(vertical: 24, horizontal: 16),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Center(
        child: Text(
          '暂无通信日志',
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
      ),
    );
  }

  Widget _buildLogItem(_LogRecord log) {
    final ThemeData theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      padding: const EdgeInsets.all(10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            log.formattedTime,
            style: theme.textTheme.bodySmall?.copyWith(
              color: const Color(0xFF94A3B8),
              fontSize: 11,
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: SelectableText(
              log.message,
              style: const TextStyle(
                fontFamily: 'monospace',
                fontSize: 12,
                color: Color(0xFF1E293B),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

