import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/constants/secrets.dart';
import 'package:flutter_demo/core/constants/urls.dart';

/// SSE — 服务端推送流式传输（DeepSeek）
///
/// 核心机制与避坑点：
/// 1. 流式读取：`ResponseType.stream` 搭配 `utf8.decoder` + `LineSplitter` 逐行消费。
/// 2. 协议解析：`data: {...}` 为载荷行，`data: [DONE]` 为结束标志；Cancel 走
///    `DioExceptionType.cancel` 静默处理。
/// 3. 密钥注入：API Key 来自 `Secrets.deepSeekApiKey` 编译期注入，禁止硬编码。
///
/// 官方参考：
/// https://api-docs.deepseek.com
class SseDemoPage extends BasicResponsePage {
  const SseDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<SseDemoPage> createState() => _SseDemoPageState();
}

class _SseDemoPageState extends BasicResponsePageState<SseDemoPage> {
  static const String _defaultPrompt = '请用一句话介绍你自己和你的核心优势';
  static const String _serverUrl = Urls.deepSeek;
  static const String _streamKey = 'sse_stream';

  final Dio _dio = Dio();
  final StringBuffer _responseBuffer = StringBuffer();
  CancelToken? _cancelToken;
  // 订阅在流式回调中创建，dispose 时显式 cancel。
  // ignore: cancel_subscriptions
  StreamSubscription<String>? _subscription;
  bool _streaming = false;

  @override
  void initState() {
    super.initState();
    showDescription('SSE 示例：DeepSeek 流式对话（POST Stream → 逐 Token → [DONE]）');
  }

  @override
  void dispose() {
    final StreamSubscription<String>? subscription = _subscription;
    _subscription = null;
    if (subscription != null) {
      unawaited(subscription.cancel());
    }
    _cancelToken?.cancel('Widget disposed');
    _cancelToken = null;
    _dio.close(force: true);
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 发起 DeepSeek 流式对话',
        '2. 中断当前流式生成',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _sendPrompt();
      case 1:
        _handleCancel();
    }
  }

  Future<void> _sendPrompt() async {
    final String apiKey = Secrets.deepSeekApiKey;
    if (apiKey.isEmpty) {
      appendLog('✗ [SSE] 未配置 DeepSeek API Key');
      appendLog(
        '→ [SSE] 请在工程根目录 local.properties 配置 deepseek.api.key=sk-xxxx，'
        '执行 dart tools/sync_dart_defines.dart 后以 '
        'fvm flutter run --dart-define-from-file=dart_defines.json 重新编译。',
      );
      return;
    }

    if (_streaming) {
      appendLog('✗ [SSE] 已有流式任务进行中，请先中断');
      return;
    }

    _cancelStream(notify: false);
    _responseBuffer.clear();

    appendLog('→ [SSE] 目标 $_serverUrl');
    appendLog('→ [SSE] 提问 $_defaultPrompt');
    appendLog('→ [SSE] 正在建立流式连接...');

    final String jsonBody = jsonEncode(<String, dynamic>{
      'model': 'deepseek-chat',
      'stream': true,
      'messages': <Map<String, String>>[
        <String, String>{'role': 'user', 'content': _defaultPrompt},
      ],
    });

    final CancelToken cancelToken = CancelToken();
    _cancelToken = cancelToken;

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
        cancelToken: cancelToken,
      );

      final ResponseBody? body = response.data;
      if (body == null) {
        appendLog('✗ [SSE] 响应体为空');
        _cancelToken = null;
        return;
      }

      if (!mounted) return;
      _streaming = true;
      appendLog('✓ [SSE] HTTP ${response.statusCode}，开始接收流式 Token...');

      _subscription = body.stream
          .cast<List<int>>()
          .transform(utf8.decoder)
          .transform(const LineSplitter())
          .listen(
            _handleLine,
            onError: (Object error) {
              if (error is DioException &&
                  error.type == DioExceptionType.cancel) {
                return;
              }
              _handleError('$error');
            },
            onDone: _handleDone,
            cancelOnError: true,
          );
    } on DioException catch (error) {
      if (error.type == DioExceptionType.cancel) {
        return;
      }
      _handleError(error.message ?? '$error');
    } catch (error) {
      _handleError('$error');
    }
  }

  void _handleLine(String line) {
    final String trimmed = line.trim();
    if (trimmed.isEmpty || !trimmed.startsWith('data:')) {
      return;
    }

    final String data = trimmed.substring(5).trim();
    if (data == '[DONE]') {
      _handleDone();
      return;
    }

    final String delta = _parseDeltaContent(data);
    if (delta.isEmpty || !mounted) {
      return;
    }
    _responseBuffer.write(delta);
    updateLog(_streamKey, '流式输出: $_responseBuffer');
  }

  void _handleDone() {
    if (!mounted || !_streaming) {
      return;
    }
    _streaming = false;
    _cancelToken = null;
    final StreamSubscription<String>? subscription = _subscription;
    _subscription = null;
    if (subscription != null) {
      unawaited(subscription.cancel());
    }
    removeUpdatingLog(_streamKey);
    appendLog('✓ [SSE] 收到 [DONE]，模型生成完毕');
    if (_responseBuffer.isNotEmpty) {
      appendLog('✓ [SSE] 完整响应: $_responseBuffer');
    }
  }

  void _handleError(String error) {
    if (!mounted) return;
    _streaming = false;
    removeUpdatingLog(_streamKey);
    appendLog('✗ [SSE] $error');
  }

  void _handleCancel() {
    if (!_streaming && _cancelToken == null) {
      appendLog('→ [Cancel] 当前没有进行中的流式任务');
      return;
    }
    _cancelStream(notify: true);
  }

  void _cancelStream({required bool notify}) {
    final StreamSubscription<String>? subscription = _subscription;
    _subscription = null;
    if (subscription != null) {
      unawaited(subscription.cancel());
    }
    final CancelToken? cancelToken = _cancelToken;
    _cancelToken = null;
    cancelToken?.cancel('User cancelled');

    final bool wasStreaming = _streaming;
    _streaming = false;
    if (!mounted) return;
    removeUpdatingLog(_streamKey);
    if (notify && wasStreaming) {
      appendLog('→ [Cancel] 已主动中断当前流式输出');
    }
  }

  String _parseDeltaContent(String data) {
    try {
      final Map<String, dynamic> json =
          jsonDecode(data) as Map<String, dynamic>;
      final List<dynamic>? choices = json['choices'] as List<dynamic>?;
      if (choices == null || choices.isEmpty) {
        return '';
      }
      final Map<String, dynamic>? first =
          choices.first as Map<String, dynamic>?;
      final Map<String, dynamic>? delta =
          first?['delta'] as Map<String, dynamic>?;
      if (delta == null) {
        return '';
      }
      final String reasoning = delta['reasoning_content'] as String? ?? '';
      if (reasoning.isNotEmpty) {
        return '[思考] $reasoning';
      }
      return delta['content'] as String? ?? '';
    } catch (_) {
      return data;
    }
  }
}
