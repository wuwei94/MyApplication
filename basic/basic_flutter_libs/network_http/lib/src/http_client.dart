import 'dart:async';
import 'dart:convert';

import 'package:async/async.dart';
import 'package:http/http.dart' as http;
import 'package:network_http/src/network_exception.dart';
import 'package:network_http/src/network_logger.dart';
import 'package:network_http/src/network_response.dart';
import 'package:network_http/src/request_body_type.dart';
import 'package:network_http/src/response_body_decoder.dart';

/// 对 package:http 常用请求、响应和异常转换的轻量封装。
class HttpClient {
  HttpClient({
    http.Client? client,
    String? baseUrl,
    this.timeout = const Duration(seconds: 15),
    this.enableLogging = false,
  }) : _client = client ?? http.Client(),
       _ownsClient = client == null,
       baseUrl = baseUrl ?? '';

  final http.Client _client;
  final bool _ownsClient;
  final String baseUrl;
  final Duration timeout;
  final bool enableLogging;
  bool _closed = false;

  CancelableOperation<NetworkResponse<T>> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
    T Function(dynamic data)? decoder,
  }) {
    return _request<T>(
      path,
      method: 'GET',
      queryParameters: queryParameters,
      headers: headers,
      decoder: decoder,
    );
  }

  CancelableOperation<NetworkResponse<T>> post<T>(
    String path, {
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
    RequestBodyType? bodyType,
    T Function(dynamic data)? decoder,
  }) {
    return _request<T>(
      path,
      method: 'POST',
      body: body,
      queryParameters: queryParameters,
      headers: headers,
      bodyType: bodyType,
      decoder: decoder,
    );
  }

  CancelableOperation<NetworkResponse<T>> put<T>(
    String path, {
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
    RequestBodyType? bodyType,
    T Function(dynamic data)? decoder,
  }) {
    return _request<T>(
      path,
      method: 'PUT',
      body: body,
      queryParameters: queryParameters,
      headers: headers,
      bodyType: bodyType,
      decoder: decoder,
    );
  }

  CancelableOperation<NetworkResponse<T>> patch<T>(
    String path, {
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
    RequestBodyType? bodyType,
    T Function(dynamic data)? decoder,
  }) {
    return _request<T>(
      path,
      method: 'PATCH',
      body: body,
      queryParameters: queryParameters,
      headers: headers,
      bodyType: bodyType,
      decoder: decoder,
    );
  }

  CancelableOperation<NetworkResponse<T>> delete<T>(
    String path, {
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
    RequestBodyType? bodyType,
    T Function(dynamic data)? decoder,
  }) {
    return _request<T>(
      path,
      method: 'DELETE',
      body: body,
      queryParameters: queryParameters,
      headers: headers,
      bodyType: bodyType,
      decoder: decoder,
    );
  }

  CancelableOperation<NetworkResponse<T>> _request<T>(
    String path, {
    required String method,
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
    RequestBodyType? bodyType,
    T Function(dynamic data)? decoder,
  }) {
    if (_closed) {
      return CancelableOperation<NetworkResponse<T>>.fromFuture(
        Future<NetworkResponse<T>>.error(StateError('HttpClient is closed')),
      );
    }

    final Completer<void> abortController = Completer<void>();
    final Future<NetworkResponse<T>> future = _send<T>(
      path,
      method: method,
      body: body,
      queryParameters: queryParameters,
      headers: headers,
      bodyType: bodyType,
      decoder: decoder,
      abortController: abortController,
    );
    return CancelableOperation<NetworkResponse<T>>.fromFuture(
      future,
      onCancel: () {
        if (!abortController.isCompleted) abortController.complete();
      },
    );
  }

  Future<NetworkResponse<T>> _send<T>(
    String path, {
    required String method,
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
    RequestBodyType? bodyType,
    T Function(dynamic data)? decoder,
    required Completer<void> abortController,
  }) async {
    final Uri uri = _buildUri(path, queryParameters);
    try {
      final http.AbortableRequest request = http.AbortableRequest(
        method,
        uri,
        abortTrigger: abortController.future,
      );
      if (headers != null) request.headers.addAll(headers);
      if (body != null) {
        _setBody(request, body, bodyType ?? _inferBodyType(body));
      }
      _logRequest(request, body);

      final http.StreamedResponse streamed = await _client
          .send(request)
          .timeout(
            timeout,
            onTimeout: () => throw TimeoutException('Request timeout'),
          );
      final http.Response response = await http.Response.fromStream(streamed)
          .timeout(
            timeout,
            onTimeout: () => throw TimeoutException('Response timeout'),
          );
      _logResponse(response);
      return _buildResponse(response, decoder);
    } on http.RequestAbortedException {
      rethrow;
    } on TimeoutException catch (error) {
      if (!abortController.isCompleted) abortController.complete();
      throw NetworkException(
        error,
        NetworkException.timeoutError,
        message: '请求超时，请稍后再试',
      );
    } on http.ClientException catch (error) {
      throw NetworkException(
        error,
        NetworkException.connectError,
        message: '连接失败，请检查网络设置',
      );
    } on NetworkException {
      rethrow;
    } catch (error) {
      throw NetworkException(
        error,
        NetworkException.unknown,
        message: error.toString(),
      );
    }
  }

  NetworkResponse<T> _buildResponse<T>(
    http.Response response,
    T Function(dynamic data)? decoder,
  ) {
    final dynamic data = decodeNetworkResponseBody(
      response.body,
      response.headers['content-type'],
    );
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw NetworkException(
        response,
        response.statusCode,
        message: _serverMessage(data) ?? '请求错误(${response.statusCode})',
      );
    }

    try {
      return NetworkResponse<T>.fromJson(data, decoder: decoder);
    } catch (error) {
      throw NetworkException(
        error,
        NetworkException.parseError,
        message: '解析错误，请稍后再试',
      );
    }
  }

  void _setBody(
    http.AbortableRequest request,
    dynamic body,
    RequestBodyType bodyType,
  ) {
    switch (bodyType) {
      case RequestBodyType.form:
        final Map<String, String> fields = (body as Map<dynamic, dynamic>).map(
          (dynamic key, dynamic value) =>
              MapEntry(key.toString(), value.toString()),
        );
        request.body = Uri(queryParameters: fields).query;
        request.headers.putIfAbsent(
          'content-type',
          () => 'application/x-www-form-urlencoded',
        );
      case RequestBodyType.json:
        request.body = body is String ? body : jsonEncode(body);
        request.headers.putIfAbsent('content-type', () => 'application/json');
      case RequestBodyType.raw:
        if (body is List<int>) {
          request.bodyBytes = body;
        } else {
          request.body = body.toString();
        }
    }
  }

  RequestBodyType _inferBodyType(dynamic body) {
    return body is Map<dynamic, dynamic>
        ? RequestBodyType.json
        : RequestBodyType.raw;
  }

  Uri _buildUri(String path, Map<String, dynamic>? queryParameters) {
    final Uri parsed = Uri.parse(path);
    final Uri resolved;
    if (parsed.hasScheme || baseUrl.isEmpty) {
      resolved = parsed;
    } else {
      final Uri base = Uri.parse(baseUrl);
      final Uri normalized = base.path.endsWith('/')
          ? base
          : base.replace(path: '${base.path}/');
      resolved = normalized.resolveUri(parsed);
    }
    if (queryParameters == null || queryParameters.isEmpty) return resolved;
    return resolved.replace(
      queryParameters: <String, String>{
        ...resolved.queryParameters,
        for (final MapEntry<String, dynamic> entry in queryParameters.entries)
          entry.key: entry.value.toString(),
      },
    );
  }

  String? _serverMessage(dynamic data) {
    if (data is Map<dynamic, dynamic>) {
      for (final String key in <String>['message', 'msg', 'errorMsg']) {
        final dynamic value = data[key];
        if (value != null && value.toString().trim().isNotEmpty) {
          return value.toString();
        }
      }
    } else if (data != null && data.toString().trim().isNotEmpty) {
      return data.toString();
    }
    return null;
  }

  void _logRequest(http.BaseRequest request, dynamic body) {
    if (!enableLogging) return;
    logDebug(
      '--> ${request.method} ${request.url}\n'
      'headers=${request.headers}\nbody=$body',
    );
  }

  void _logResponse(http.Response response) {
    if (!enableLogging) return;
    logDebug(
      '<-- ${response.statusCode} ${response.request?.url}\n'
      'headers=${response.headers}\nbody=${response.body}',
    );
  }

  void close() {
    if (_closed) return;
    _closed = true;
    if (_ownsClient) _client.close();
  }
}
