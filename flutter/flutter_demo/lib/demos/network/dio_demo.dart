import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/constants/urls.dart';
import 'package:flutter_demo/core/utils/logger/logger.dart';
import 'package:lib_network_dio/lib_network_dio.dart';

/// dio
/// https://pub.dev/packages/dio
class DioDemoPage extends StatelessWidget {
  const DioDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return DioDemoView(title: title);
  }
}

class DioDemoView extends StatefulWidget {
  const DioDemoView({super.key, required this.title});

  final String title;

  @override
  State<DioDemoView> createState() => _DioDemoViewState();
}

class _DioDemoViewState extends State<DioDemoView> {
  late final Dio _dio;
  late final DioClient _dioClient;
  CancelToken? _cancelToken;

  String _info = 'Tap the button to send a POST request.';
  bool _isLoading = false;

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
  }

  @override
  void dispose() {
    _cancelToken?.cancel('Widget disposed');
    _dioClient.close();
    _dio.close(force: true);
    super.dispose();
  }

  Map<String, String> _buildLoginData() {
    return <String, String>{
      Urls.keyUsername: Urls.valueUsername,
      Urls.keyPassword: Urls.valuePassword,
    };
  }

  Future<void> _handlePost() async {
    _cancelToken = CancelToken();
    await _handleRequest(
      request: () => _dioClient.post<Map<String, dynamic>>(
        Urls.login,
        body: _buildLoginData(),
        bodyType: RequestBodyType.form,
        cancelToken: _cancelToken,
        decoder: (dynamic data) => data as Map<String, dynamic>,
      ),
      successPrefix: 'POST success',
    );
  }

  Future<void> _handleRequest<T>({
    required Future<NetworkResponse<T>> Function() request,
    required String successPrefix,
  }) async {
    _setStateIfMounted(() {
      _isLoading = true;
      _info = 'Loading...';
    });

    try {
      final NetworkResponse<T> response = await request();
      _setStateIfMounted(() {
        _info = _formatResponse(successPrefix, response);
      });
    } on NetworkException catch (error) {
      _setStateIfMounted(() {
        _info =
            'Request failed\nCode: ${error.code}\n'
            'Message: ${error.message}';
      });
    } catch (error) {
      _setStateIfMounted(() {
        _info = 'Unexpected error\n$error';
      });
    } finally {
      _setStateIfMounted(() {
        _isLoading = false;
      });
    }
  }

  void _setStateIfMounted(VoidCallback fn) {
    if (!mounted) {
      return;
    }

    setState(fn);
  }

  String _formatResponse<T>(String successPrefix, NetworkResponse<T> response) {
    final dynamic data = response.data;
    final String dataPreview;

    if (data is List<dynamic>) {
      dataPreview = data.isEmpty ? '[]' : data.first.toString();
    } else {
      dataPreview = data?.toString() ?? 'null';
    }

    return '$successPrefix\n'
        'Code: ${response.code}\n'
        'Message: ${response.message}\n'
        'Success: ${response.isSuccess}\n'
        'Data: $dataPreview';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: _buildBody(),
      floatingActionButton: getFAB(),
    );
  }

  Widget _buildBody() {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: SizedBox.expand(child: SingleChildScrollView(child: Text(_info))),
    );
  }

  Widget getFAB() {
    return FloatingActionButton(
      onPressed: _isLoading ? null : _handlePost,
      tooltip: 'Send POST Request',
      child: _isLoading
          ? const SizedBox(
              width: 24,
              height: 24,
              child: CircularProgressIndicator(
                color: Colors.white,
                strokeWidth: 2,
              ),
            )
          : const Icon(Icons.send),
    );
  }
}
