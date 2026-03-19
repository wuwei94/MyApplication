import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:basic_flutter/core/utils/network/network_error_type.dart';
import 'package:basic_flutter/core/utils/network/network_exception.dart';
import 'package:basic_flutter/core/utils/network/network_response.dart';
import 'package:basic_flutter/core/utils/network/request_body_type.dart';
import 'package:dio/dio.dart';

/// Dio 网络请求工具类
class DioClient {
  factory DioClient({
    Dio? dio,
    String? baseUrl,
    Duration timeout = const Duration(seconds: 15),
  }) {
    final BaseOptions options = BaseOptions(
      baseUrl: baseUrl ?? '',
      connectTimeout: timeout,
      receiveTimeout: timeout,
      sendTimeout: timeout,
    );

    return DioClient._internal(options: options, dio: dio ?? Dio(options));
  }

  DioClient._internal({required Dio dio, required BaseOptions options})
    : _options = options,
      _dio = dio;

  final Dio _dio;
  final BaseOptions _options;

  BaseOptions get baseOptions {
    return _options;
  }

  Future<NetworkResponse<dynamic>> get(
    String path, {
    Map<String, dynamic>? queryParameters,
    Map<String, dynamic>? headers,
  }) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>(
        path,
        queryParameters: queryParameters,
        options: Options(headers: headers),
      );
      return _buildResponse(response);
    } on DioException catch (error, stackTrace) {
      logError('Dio GET request failed: $path', error, stackTrace);
      throw _mapDioException(error);
    } catch (error, stackTrace) {
      logError('Dio GET unknown error: $path', error, stackTrace);
      throw NetworkException(
        type: NetworkErrorType.unknown,
        message: error.toString(),
      );
    }
  }

  Future<NetworkResponse<dynamic>> post(
    String path, {
    dynamic body,
    Map<String, dynamic>? queryParameters,
    Map<String, dynamic>? headers,
    RequestBodyType? bodyType,
  }) async {
    try {
      final RequestBodyType resolvedBodyType = _resolveRequestBodyType(
        body: body,
        headers: headers,
        bodyType: bodyType,
      );
      final Options requestOptions = _buildPostOptions(
        body: body,
        headers: headers,
        bodyType: resolvedBodyType,
      );
      final Response<dynamic> response = await _dio.post<dynamic>(
        path,
        data: _encodeRequestBody(body: body, bodyType: resolvedBodyType),
        queryParameters: queryParameters,
        options: requestOptions,
      );
      return _buildResponse(response);
    } on DioException catch (error, stackTrace) {
      logError('Dio POST request failed: $path', error, stackTrace);
      throw _mapDioException(error);
    } catch (error, stackTrace) {
      logError('Dio POST unknown error: $path', error, stackTrace);
      throw NetworkException(
        type: NetworkErrorType.unknown,
        message: error.toString(),
      );
    }
  }

  NetworkResponse<dynamic> _buildResponse(Response<dynamic> response) {
    return NetworkResponse<dynamic>(
      data: response.data,
      statusCode: response.statusCode,
      statusMessage: response.statusMessage,
      headers: <String, dynamic>{
        for (final MapEntry<String, List<String>> entry
            in response.headers.map.entries)
          entry.key: entry.value,
      },
    );
  }

  Options _buildPostOptions({
    required dynamic body,
    required Map<String, dynamic>? headers,
    required RequestBodyType bodyType,
  }) {
    return Options(
      headers: headers,
      contentType: _resolveContentType(
        body: body,
        headers: headers,
        bodyType: bodyType,
      ),
    );
  }

  dynamic _encodeRequestBody({
    required dynamic body,
    required RequestBodyType bodyType,
  }) {
    if (bodyType != RequestBodyType.form) {
      return body;
    }

    if (body == null ||
        body is String ||
        body is List<int> ||
        body is FormData) {
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

  String? _resolveContentType({
    required dynamic body,
    required Map<String, dynamic>? headers,
    required RequestBodyType bodyType,
  }) {
    if (body == null || _containsContentType(headers)) {
      return null;
    }

    if (body is String || body is List<int> || body is FormData) {
      return null;
    }

    if (bodyType == RequestBodyType.form) {
      return Headers.formUrlEncodedContentType;
    }

    return Headers.jsonContentType;
  }

  RequestBodyType _resolveRequestBodyType({
    required dynamic body,
    required Map<String, dynamic>? headers,
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

  bool _containsContentType(Map<String, dynamic>? headers) {
    if (headers == null) {
      return false;
    }

    return headers.keys.any(
      (dynamic key) => key.toString().toLowerCase() == 'content-type',
    );
  }

  bool _isFormUrlEncoded(Map<String, dynamic>? headers) {
    if (headers == null) {
      return false;
    }

    for (final MapEntry<String, dynamic> entry in headers.entries) {
      if (entry.key.toString().toLowerCase() == 'content-type') {
        return entry.value.toString().toLowerCase().contains(
          Headers.formUrlEncodedContentType,
        );
      }
    }

    return false;
  }

  bool _shouldSkipAutoContentType(dynamic body) {
    return body == null ||
        body is String ||
        body is List<int> ||
        body is FormData;
  }

  NetworkException _mapDioException(DioException error) {
    final String message =
        error.message ?? error.response?.statusMessage ?? 'Unknown Dio error';

    switch (error.type) {
      case DioExceptionType.connectionTimeout:
        return NetworkException(
          type: NetworkErrorType.connectionTimeout,
          message: message,
        );
      case DioExceptionType.sendTimeout:
        return NetworkException(
          type: NetworkErrorType.sendTimeout,
          message: message,
        );
      case DioExceptionType.receiveTimeout:
        return NetworkException(
          type: NetworkErrorType.receiveTimeout,
          message: message,
        );
      case DioExceptionType.badResponse:
        return NetworkException(
          type: NetworkErrorType.badResponse,
          message: message,
        );
      case DioExceptionType.cancel:
        return NetworkException(
          type: NetworkErrorType.cancel,
          message: message,
        );
      case DioExceptionType.connectionError:
        return NetworkException(
          type: NetworkErrorType.connectionError,
          message: message,
        );
      case DioExceptionType.badCertificate:
      case DioExceptionType.unknown:
        return NetworkException(
          type: NetworkErrorType.unknown,
          message: message,
        );
    }
  }
}
