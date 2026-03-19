import 'dart:async';
import 'dart:convert';

import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:basic_flutter/core/utils/network/network_error_type.dart';
import 'package:basic_flutter/core/utils/network/network_exception.dart';
import 'package:basic_flutter/core/utils/network/network_response.dart';
import 'package:basic_flutter/core/utils/network/request_body_type.dart';
import 'package:http/http.dart' as http;

/// http 网络请求工具类
class HttpClient {
  factory HttpClient({
    http.Client? client,
    String? baseUrl,
    Duration timeout = const Duration(seconds: 15),
  }) {
    final HttpClientOptions options = HttpClientOptions(
      baseUrl: baseUrl ?? '',
      timeout: timeout,
    );

    return HttpClient._internal(
      options: options,
      client: client ?? http.Client(),
    );
  }

  HttpClient._internal({
    required http.Client client,
    required HttpClientOptions options,
  }) : _client = client,
       _options = options;

  final http.Client _client;
  final HttpClientOptions _options;

  HttpClientOptions get options {
    return _options;
  }

  Future<NetworkResponse<dynamic>> get(
    String path, {
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
  }) async {
    try {
      final Uri uri = _buildUri(path, queryParameters: queryParameters);
      final http.Response response = await _client
          .get(uri, headers: headers)
          .timeout(options.timeout);
      return _buildResponse(response);
    } on http.ClientException catch (error, stackTrace) {
      logError('HTTP GET request failed: $path', error, stackTrace);
      throw NetworkException(
        type: NetworkErrorType.connectionError,
        message: error.message,
      );
    } on FormatException catch (error, stackTrace) {
      logError('HTTP GET response parse failed: $path', error, stackTrace);
      throw NetworkException(
        type: NetworkErrorType.badResponse,
        message: error.message,
      );
    } on TimeoutException catch (error, stackTrace) {
      logError('HTTP GET request timeout: $path', error, stackTrace);
      throw NetworkException(
        type: NetworkErrorType.receiveTimeout,
        message: error.message ?? 'Request timeout',
      );
    } on Exception catch (error, stackTrace) {
      logError('HTTP GET unknown error: $path', error, stackTrace);
      throw _mapGenericException(error);
    }
  }

  Future<NetworkResponse<dynamic>> post(
    String path, {
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
    RequestBodyType? bodyType,
  }) async {
    try {
      final Uri uri = _buildUri(path, queryParameters: queryParameters);
      final RequestBodyType resolvedBodyType = _resolveRequestBodyType(
        body: body,
        headers: headers,
        bodyType: bodyType,
      );
      final Map<String, String> requestHeaders = _buildPostHeaders(
        body: body,
        headers: headers,
        bodyType: resolvedBodyType,
      );
      final http.Response response = await _client
          .post(
            uri,
            headers: requestHeaders,
            body: _encodeRequestBody(body: body, bodyType: resolvedBodyType),
          )
          .timeout(options.timeout);
      return _buildResponse(response);
    } on http.ClientException catch (error, stackTrace) {
      logError('HTTP POST request failed: $path', error, stackTrace);
      throw NetworkException(
        type: NetworkErrorType.connectionError,
        message: error.message,
      );
    } on FormatException catch (error, stackTrace) {
      logError('HTTP POST response parse failed: $path', error, stackTrace);
      throw NetworkException(
        type: NetworkErrorType.badResponse,
        message: error.message,
      );
    } on TimeoutException catch (error, stackTrace) {
      logError('HTTP POST request timeout: $path', error, stackTrace);
      throw NetworkException(
        type: NetworkErrorType.receiveTimeout,
        message: error.message ?? 'Request timeout',
      );
    } on Exception catch (error, stackTrace) {
      logError('HTTP POST unknown error: $path', error, stackTrace);
      throw _mapGenericException(error);
    }
  }

  Uri _buildUri(String path, {Map<String, dynamic>? queryParameters}) {
    final Uri uri = Uri.parse('${options.baseUrl}$path');
    if (queryParameters == null || queryParameters.isEmpty) {
      return uri;
    }

    return uri.replace(
      queryParameters: <String, String>{
        ...uri.queryParameters,
        for (final MapEntry<String, dynamic> entry in queryParameters.entries)
          entry.key: entry.value.toString(),
      },
    );
  }

  NetworkResponse<dynamic> _buildResponse(http.Response response) {
    return NetworkResponse<dynamic>(
      data: _decodeResponseBody(response.body),
      statusCode: response.statusCode,
      statusMessage: response.reasonPhrase,
      headers: <String, dynamic>{
        for (final MapEntry<String, String> entry in response.headers.entries)
          entry.key: entry.value,
      },
    );
  }

  dynamic _decodeResponseBody(String responseBody) {
    if (responseBody.isEmpty) {
      return responseBody;
    }

    try {
      return jsonDecode(responseBody);
    } on FormatException {
      return responseBody;
    }
  }

  Object? _encodeBody(dynamic body) {
    if (body == null || body is String || body is List<int>) {
      return body;
    }
    return jsonEncode(body);
  }

  Object? _encodeRequestBody({
    required dynamic body,
    required RequestBodyType bodyType,
  }) {
    if (bodyType != RequestBodyType.form) {
      return _encodeBody(body);
    }

    if (body == null || body is String || body is List<int>) {
      return body;
    }
    if (body is Map<String, String>) {
      return body;
    }
    if (body is Map<String, dynamic>) {
      return <String, String>{
        for (final MapEntry<String, dynamic> entry in body.entries)
          entry.key: entry.value.toString(),
      };
    }

    return body.toString();
  }

  Map<String, String> _buildPostHeaders({
    required dynamic body,
    required Map<String, String>? headers,
    required RequestBodyType bodyType,
  }) {
    final Map<String, String> requestHeaders = <String, String>{
      if (headers != null) ...headers,
    };

    if (body == null || _containsContentType(headers)) {
      return requestHeaders;
    }

    if (body is String || body is List<int>) {
      return requestHeaders;
    }

    requestHeaders['Content-Type'] = bodyType == RequestBodyType.form
        ? 'application/x-www-form-urlencoded; charset=UTF-8'
        : 'application/json';
    return requestHeaders;
  }

  RequestBodyType _resolveRequestBodyType({
    required dynamic body,
    required Map<String, String>? headers,
    required RequestBodyType? bodyType,
  }) {
    if (bodyType != null) {
      return bodyType;
    }

    if (_isFormUrlEncoded(headers)) {
      return RequestBodyType.form;
    }

    return _shouldSkipAutoContentType(body)
        ? RequestBodyType.raw
        : RequestBodyType.json;
  }

  bool _containsContentType(Map<String, String>? headers) {
    if (headers == null) {
      return false;
    }

    return headers.keys.any(
      (String key) => key.toLowerCase() == 'content-type',
    );
  }

  bool _isFormUrlEncoded(Map<String, String>? headers) {
    if (headers == null) {
      return false;
    }

    final String contentType = headers.entries
        .firstWhere(
          (MapEntry<String, String> entry) =>
              entry.key.toLowerCase() == 'content-type',
          orElse: () => const MapEntry<String, String>('', ''),
        )
        .value;

    return contentType.toLowerCase().contains(
      'application/x-www-form-urlencoded',
    );
  }

  bool _shouldSkipAutoContentType(dynamic body) {
    return body == null || body is String || body is List<int>;
  }

  NetworkException _mapGenericException(Object error) {
    if (error is http.ClientException) {
      return NetworkException(
        type: NetworkErrorType.connectionError,
        message: error.message,
      );
    }
    if (error is FormatException) {
      return NetworkException(
        type: NetworkErrorType.badResponse,
        message: error.message,
      );
    }
    if (error is TimeoutException) {
      return NetworkException(
        type: NetworkErrorType.receiveTimeout,
        message: error.message ?? 'Request timeout',
      );
    }

    return NetworkException(
      type: NetworkErrorType.unknown,
      message: error.toString(),
    );
  }
}

class HttpClientOptions {
  const HttpClientOptions({
    this.baseUrl = '',
    this.timeout = const Duration(seconds: 15),
  });

  final String baseUrl;
  final Duration timeout;
}
