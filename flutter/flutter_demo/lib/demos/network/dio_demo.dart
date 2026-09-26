import 'package:dio/dio.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/constants/urls.dart';
import 'package:flutter_demo/core/utils/logger/logger.dart';
import 'package:lib_network_dio/lib_network_dio.dart';

/// dio — 请求发送与 CancelToken 取消
///
/// 核心机制与避坑点：
/// 1. 取消语义：`CancelToken.cancel` 触发 `DioExceptionType.cancel`，需与业务异常分支区分。
/// 2. 生命周期：`DioClient.close` 不关闭外部注入的 `Dio`，dispose 时两者都要关闭。
///
/// 官方参考：
/// https://pub.dev/packages/dio
class DioDemoPage extends BasicResponsePage {
  const DioDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<DioDemoPage> createState() => _DioDemoPageState();
}

class _DioDemoPageState extends BasicResponsePageState<DioDemoPage> {
  late final Dio _dio;
  late final DioClient _dioClient;
  CancelToken? _cancelToken;

  @override
  void initState() {
    super.initState();
    _dio = Dio()
      ..interceptors.add(
        LogInterceptor(
          requestHeader: false,
          requestBody: false,
          responseHeader: false,
          responseBody: false,
          logPrint: logDebug,
        ),
      );
    _dioClient = DioClient(dio: _dio);
    showDescription('dio 示例：POST 表单请求与 CancelToken 取消');
  }

  @override
  void dispose() {
    _cancelToken?.cancel('Widget disposed');
    _dioClient.close();
    _dio.close(force: true);
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 发送 POST 表单请求',
        '2. 取消进行中的请求',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _handlePost();
      case 1:
        _handleCancel();
    }
  }

  Map<String, String> _buildLoginData() {
    return <String, String>{
      Urls.keyUsername: Urls.valueUsername,
      Urls.keyPassword: Urls.valuePassword,
    };
  }

  Future<void> _handlePost() async {
    final CancelToken cancelToken = CancelToken();
    _cancelToken = cancelToken;
    appendLog('→ [POST Form] ${Urls.login}');

    try {
      final NetworkResponse<Map<String, dynamic>> response =
          await _dioClient.post<Map<String, dynamic>>(
        Urls.login,
        body: _buildLoginData(),
        bodyType: RequestBodyType.form,
        cancelToken: cancelToken,
        decoder: (dynamic data) => data as Map<String, dynamic>,
      );
      appendLog(
        '✓ [POST Form] code=${response.code} success=${response.isSuccess}',
      );
      appendFormatLog(
        '[POST Form] data: ',
        response.data?.toString() ?? 'null',
      );
    } on DioException catch (error) {
      if (error.type == DioExceptionType.cancel) {
        appendLog('✗ [POST Form] 请求被取消');
        return;
      }
      appendLog('✗ [POST Form] ${error.message ?? error}');
    } on NetworkException catch (error) {
      appendLog('✗ [POST Form] code=${error.code} message=${error.message}');
    } catch (error) {
      appendLog('✗ [POST Form] unexpected: $error');
    } finally {
      if (identical(_cancelToken, cancelToken)) {
        _cancelToken = null;
      }
    }
  }

  void _handleCancel() {
    final CancelToken? cancelToken = _cancelToken;
    if (cancelToken == null || cancelToken.isCancelled) {
      appendLog('→ [Cancel] 当前没有进行中的请求');
      return;
    }
    cancelToken.cancel('User cancelled');
    appendLog('→ [Cancel] 已触发 CancelToken.cancel');
  }
}
