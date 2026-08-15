import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:network_dio/src/network_exception.dart';
import 'package:network_dio/src/network_response.dart';
import 'package:network_dio/src/request_body_type.dart';

/// 对 Dio 常用请求、响应和异常转换的轻量封装。
class DioClient {
  factory DioClient({Dio? dio, String? baseUrl, Duration? timeout}) {
    final BaseOptions options = dio?.options.copyWith() ?? BaseOptions();
    if (baseUrl != null) options.baseUrl = baseUrl;
    final Duration? defaultTimeout =
        timeout ?? (dio == null ? const Duration(seconds: 15) : null);
    if (defaultTimeout != null) {
      options.connectTimeout = defaultTimeout;
      options.receiveTimeout = defaultTimeout;
      options.sendTimeout = defaultTimeout;
    }

    final Dio resolved = dio?.clone(options: options) ?? Dio(options);
    return DioClient._(resolved, ownsDio: dio == null);
  }

  DioClient._(this._dio, {required bool ownsDio}) : _ownsDio = ownsDio;

  final Dio _dio;
  final bool _ownsDio;
  bool _closed = false;

  Future<NetworkResponse<T>> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Map<String, dynamic>? headers,
    CancelToken? cancelToken,
    T Function(dynamic data)? decoder,
  }) {
    return _request<T>(
      path,
      method: 'GET',
      queryParameters: queryParameters,
      headers: headers,
      cancelToken: cancelToken,
      decoder: decoder,
    );
  }

  Future<NetworkResponse<T>> post<T>(
    String path, {
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, dynamic>? headers,
    RequestBodyType? bodyType,
    CancelToken? cancelToken,
    T Function(dynamic data)? decoder,
  }) {
    return _request<T>(
      path,
      method: 'POST',
      body: body,
      queryParameters: queryParameters,
      headers: headers,
      bodyType: bodyType,
      cancelToken: cancelToken,
      decoder: decoder,
    );
  }

  Future<NetworkResponse<T>> put<T>(
    String path, {
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, dynamic>? headers,
    RequestBodyType? bodyType,
    CancelToken? cancelToken,
    T Function(dynamic data)? decoder,
  }) {
    return _request<T>(
      path,
      method: 'PUT',
      body: body,
      queryParameters: queryParameters,
      headers: headers,
      bodyType: bodyType,
      cancelToken: cancelToken,
      decoder: decoder,
    );
  }

  Future<NetworkResponse<T>> patch<T>(
    String path, {
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, dynamic>? headers,
    RequestBodyType? bodyType,
    CancelToken? cancelToken,
    T Function(dynamic data)? decoder,
  }) {
    return _request<T>(
      path,
      method: 'PATCH',
      body: body,
      queryParameters: queryParameters,
      headers: headers,
      bodyType: bodyType,
      cancelToken: cancelToken,
      decoder: decoder,
    );
  }

  Future<NetworkResponse<T>> delete<T>(
    String path, {
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, dynamic>? headers,
    RequestBodyType? bodyType,
    CancelToken? cancelToken,
    T Function(dynamic data)? decoder,
  }) {
    return _request<T>(
      path,
      method: 'DELETE',
      body: body,
      queryParameters: queryParameters,
      headers: headers,
      bodyType: bodyType,
      cancelToken: cancelToken,
      decoder: decoder,
    );
  }

  Future<NetworkResponse<T>> _request<T>(
    String path, {
    required String method,
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, dynamic>? headers,
    RequestBodyType? bodyType,
    CancelToken? cancelToken,
    T Function(dynamic data)? decoder,
  }) async {
    if (_closed) throw StateError('DioClient is closed');

    try {
      final RequestBodyType resolvedType = bodyType ?? _inferBodyType(body);
      final Response<dynamic> response = await _dio.request<dynamic>(
        path,
        data: body,
        queryParameters: queryParameters,
        options: Options(
          method: method,
          headers: headers,
          contentType: _contentType(resolvedType),
        ),
        cancelToken: cancelToken,
      );
      return _buildResponse(response, decoder);
    } on DioException catch (error) {
      if (error.type == DioExceptionType.cancel) rethrow;
      throw _mapDioError(error);
    } on NetworkException {
      rethrow;
    } catch (error) {
      throw NetworkException(error, NetworkException.unknown);
    }
  }

  NetworkResponse<T> _buildResponse<T>(
    Response<dynamic> response,
    T Function(dynamic data)? decoder,
  ) {
    final int? statusCode = response.statusCode;
    if (statusCode == null || statusCode < 200 || statusCode >= 300) {
      throw NetworkException(
        response,
        statusCode ?? NetworkException.unknown,
        message: _serverMessage(response.data) ?? '请求错误(${statusCode ?? '-'})',
      );
    }

    try {
      return NetworkResponse<T>.fromJson(response.data, decoder: decoder);
    } catch (error) {
      throw NetworkException(
        error,
        NetworkException.parseError,
        message: '解析错误，请稍后再试',
      );
    }
  }

  NetworkException _mapDioError(DioException error) {
    final Response<dynamic>? response = error.response;
    final int code = switch (error.type) {
      DioExceptionType.connectionTimeout ||
      DioExceptionType.sendTimeout ||
      DioExceptionType.receiveTimeout ||
      DioExceptionType.transformTimeout => NetworkException.timeoutError,
      DioExceptionType.badCertificate => NetworkException.sslError,
      DioExceptionType.badResponse =>
        response?.statusCode ?? NetworkException.unknown,
      DioExceptionType.connectionError => NetworkException.connectError,
      DioExceptionType.cancel ||
      DioExceptionType.unknown => NetworkException.unknown,
    };
    return NetworkException(
      error,
      code,
      message:
          _serverMessage(response?.data) ??
          error.message ??
          NetworkException.defaultMessage,
    );
  }

  RequestBodyType _inferBodyType(dynamic body) {
    return body is Map<String, dynamic>
        ? RequestBodyType.json
        : RequestBodyType.raw;
  }

  String? _contentType(RequestBodyType type) {
    return switch (type) {
      RequestBodyType.form => Headers.formUrlEncodedContentType,
      RequestBodyType.json => Headers.jsonContentType,
      RequestBodyType.raw => null,
    };
  }

  String? _serverMessage(dynamic data) {
    if (data is Map<dynamic, dynamic>) {
      for (final String key in <String>['message', 'msg', 'errorMsg']) {
        final dynamic value = data[key];
        if (value != null && value.toString().trim().isNotEmpty) {
          return value.toString();
        }
      }
    } else if (data is String && data.trim().isNotEmpty) {
      try {
        return _serverMessage(jsonDecode(data)) ?? data;
      } on FormatException {
        return data;
      }
    }
    return null;
  }

  void close() {
    if (_closed) return;
    _closed = true;
    if (_ownsDio) _dio.close(force: true);
  }
}
