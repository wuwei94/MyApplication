import 'package:basic_flutter/core/constants/urls.dart';
import 'package:basic_flutter/core/utils/network/dio_client.dart';
import 'package:basic_flutter/core/utils/network/network_exception.dart';
import 'package:basic_flutter/core/utils/network/network_response.dart';
import 'package:basic_flutter/core/utils/network/request_body_type.dart';
import 'package:flutter/material.dart';

/// dio
/// https://pub.dev/packages/dio
class DioExample extends StatelessWidget {
  const DioExample({super.key});

  @override
  Widget build(BuildContext context) {
    return const DioRoute(title: 'Dio Example');
  }
}

class DioRoute extends StatefulWidget {
  const DioRoute({super.key, required this.title});

  final String title;

  @override
  State<DioRoute> createState() => _DioRouteState();
}

class _DioRouteState extends State<DioRoute> {
  final DioClient _dioClient = DioClient();

  String _info = 'Tap the button to send a POST request.';
  bool _isLoading = false;

  Map<String, String> _buildLoginData() {
    return <String, String>{
      Urls.keyUsername: Urls.valueUsername,
      Urls.keyPassword: Urls.valuePassword,
    };
  }

  Future<void> _handlePost() async {
    await _handleRequest(
      request: () => _dioClient.post(
        Urls.login,
        body: _buildLoginData(),
        bodyType: RequestBodyType.form,
      ),
      successPrefix: 'POST success',
    );
  }

  Future<void> _handleRequest({
    required Future<NetworkResponse<dynamic>> Function() request,
    required String successPrefix,
  }) async {
    _setStateIfMounted(() {
      _isLoading = true;
      _info = 'Loading...';
    });

    try {
      final NetworkResponse<dynamic> response = await request();
      _setStateIfMounted(() {
        _info = _formatResponse(successPrefix, response);
      });
    } on NetworkException catch (error) {
      _setStateIfMounted(() {
        _info =
            'Request failed\nType: ${error.type.name}\nMessage: ${error.message}';
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
    NetworkResponse<dynamic> response,
  ) {
    final dynamic data = response.data;
    final String dataPreview;

    if (data is List<dynamic>) {
      dataPreview = data.isEmpty ? '[]' : data.first.toString();
    } else {
      dataPreview = data.toString();
    }

    return '$successPrefix\n'
        'StatusCode: ${response.statusCode ?? '-'}\n'
        'StatusMessage: ${response.statusMessage ?? '-'}\n'
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
