import 'package:async/async.dart';
import 'package:basic_flutter/core/constants/urls.dart';
import 'package:flutter/material.dart';
import 'package:lib_network_http/lib_network_http.dart';

/// http
/// https://pub.dev/packages/http
class HttpDemoPage extends StatelessWidget {
  const HttpDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return HttpDemoView(title: title);
  }
}

class HttpDemoView extends StatefulWidget {
  const HttpDemoView({super.key, required this.title});

  final String title;

  @override
  State<HttpDemoView> createState() => _HttpDemoViewState();
}

class _HttpDemoViewState extends State<HttpDemoView> {
  final HttpClient _httpClient = HttpClient(enableLogging: true);
  CancelableOperation<NetworkResponse<Map<String, dynamic>>>? _currentOperation;

  String _info = 'Tap the button to send a POST request.';
  bool _isLoading = false;

  @override
  void dispose() {
    _currentOperation?.cancel();
    _httpClient.close();
    super.dispose();
  }

  Map<String, String> _buildLoginData() {
    return <String, String>{
      Urls.keyUsername: Urls.valueUsername,
      Urls.keyPassword: Urls.valuePassword,
    };
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
    await _handleRequest(operation: operation, successPrefix: 'POST success');
  }

  void _handleCancel() {
    _currentOperation?.cancel();
    _setStateIfMounted(() {
      _isLoading = false;
      _info = 'Request cancelled.';
    });
  }

  Future<void> _handleRequest({
    required CancelableOperation<NetworkResponse<Map<String, dynamic>>>
    operation,
    required String successPrefix,
  }) async {
    _setStateIfMounted(() {
      _isLoading = true;
      _info = 'Loading...';
    });

    try {
      final NetworkResponse<Map<String, dynamic>>? response = await operation
          .valueOrCancellation();
      if (response == null) return;
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

  String _formatResponse(
    String successPrefix,
    NetworkResponse<Map<String, dynamic>> response,
  ) {
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
    if (_isLoading) {
      return FloatingActionButton(
        onPressed: _handleCancel,
        backgroundColor: Colors.red,
        tooltip: 'Cancel Request',
        child: const Icon(Icons.cancel),
      );
    }
    return FloatingActionButton(
      onPressed: _handlePost,
      tooltip: 'Send POST Request',
      child: const Icon(Icons.send),
    );
  }
}
