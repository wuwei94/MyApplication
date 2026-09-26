import 'package:async/async.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/constants/urls.dart';
import 'package:lib_network_http/lib_network_http.dart';

/// http — 业务响应与请求取消
///
/// 核心机制与避坑点：
/// 1. 取消语义：`CancelableOperation` 在 dispose / 取消时中断未完成请求，避免结果回写已卸载 UI。
/// 2. 响应契约：`code/message/data` 业务响应与 `NetworkException` 异常面保持与兄弟网络示例对齐。
///
/// 官方参考：
/// https://pub.dev/packages/http
class HttpDemoPage extends BasicResponsePage {
  const HttpDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<HttpDemoPage> createState() => _HttpDemoPageState();
}

class _HttpDemoPageState extends BasicResponsePageState<HttpDemoPage> {
  final HttpClient _httpClient = HttpClient(enableLogging: true);
  CancelableOperation<NetworkResponse<Map<String, dynamic>>>? _currentOperation;

  @override
  void initState() {
    super.initState();
    showDescription('http 示例：GET / POST 表单与请求取消');
  }

  @override
  void dispose() {
    _currentOperation?.cancel();
    _httpClient.close();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 发送 GET 请求',
        '2. 发送 POST 表单请求 (Form)',
        '3. 取消进行中的请求',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _handleGet();
      case 1:
        _handlePost();
      case 2:
        _handleCancel();
    }
  }

  Map<String, String> _buildLoginData() {
    return <String, String>{
      Urls.keyUsername: Urls.valueUsername,
      Urls.keyPassword: Urls.valuePassword,
    };
  }

  Future<void> _handleGet() async {
    final CancelableOperation<NetworkResponse<Map<String, dynamic>>> operation =
        _httpClient.get<Map<String, dynamic>>(
      Urls.posts,
      decoder: (dynamic data) => data as Map<String, dynamic>,
    );
    _currentOperation = operation;
    await _handleRequest(operation: operation, actionPrefix: '[GET]');
  }

  Future<void> _handlePost() async {
    final CancelableOperation<NetworkResponse<Map<String, dynamic>>> operation =
        _httpClient.post<Map<String, dynamic>>(
      Urls.login,
      body: _buildLoginData(),
      bodyType: RequestBodyType.form,
      decoder: (dynamic data) => data as Map<String, dynamic>,
    );
    _currentOperation = operation;
    await _handleRequest(operation: operation, actionPrefix: '[POST Form]');
  }

  void _handleCancel() {
    _currentOperation?.cancel();
    appendLog('→ [Cancel] 请求已取消');
  }

  Future<void> _handleRequest({
    required CancelableOperation<NetworkResponse<Map<String, dynamic>>>
        operation,
    required String actionPrefix,
  }) async {
    appendLog('→ $actionPrefix 发起请求...');

    try {
      final NetworkResponse<Map<String, dynamic>>? response =
          await operation.valueOrCancellation();
      if (response == null) {
        appendLog('✗ $actionPrefix 请求被取消');
        return;
      }
      appendLog('✓ $actionPrefix code=${response.code} success=${response.isSuccess}');
      appendFormatLog('$actionPrefix data: ', response.data?.toString() ?? 'null');
    } on NetworkException catch (error) {
      appendLog('✗ $actionPrefix code=${error.code} message=${error.message}');
    } catch (error) {
      appendLog('✗ $actionPrefix unexpected: $error');
    }
  }
}
