import 'dart:async';
import 'dart:io';

import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:dio/dio.dart';

/// 网络异常类型
enum NetworkErrorType {
  connectionTimeout,
  sendTimeout,
  receiveTimeout,
  badResponse,
  cancel,
  connectionError,
  unknown,
}

/// 网络异常
class NetworkException implements Exception {
  final NetworkErrorType type;
  final String message;
  final int? statusCode;
  final dynamic originalError;

  NetworkException({
    required this.type,
    required this.message,
    this.statusCode,
    this.originalError,
  });

  @override
  String toString() =>
      'NetworkException{type: $type, message: $message, statusCode: $statusCode}';
}

/// Dio 网络请求工具类
class DioClient {
  static DioClient? _instance;
  late final Dio _dio;

  /// 重试次数
  static const int maxRetries = 3;

  /// 重试延迟
  static const Duration retryDelay = Duration(seconds: 1);

  /// 初始化 Dio 实例
  DioClient._internal() {
    _dio = Dio(
      BaseOptions(
        connectTimeout: const Duration(seconds: 10), // 连接超时
        receiveTimeout: const Duration(seconds: 10), // 接收超时
        sendTimeout: const Duration(seconds: 10), // 发送超时
        contentType: Headers.formUrlEncodedContentType, // 表单类型
      ),
    );

    _setupInterceptors();
  }

  /// 获取单例实例
  static DioClient get instance {
    _instance ??= DioClient._internal();
    return _instance!;
  }

  /// Dio 实例
  Dio get dio => _dio;

  /// 设置拦截器
  void _setupInterceptors() {
    // 日志拦截器
    _dio.interceptors.add(
      LogInterceptor(
        request: true,
        requestHeader: true,
        requestBody: true,
        responseHeader: true,
        responseBody: true,
        error: true,
        logPrint: (obj) => logDebug(obj),
      ),
    );

    // 错误处理拦截器
    _dio.interceptors.add(
      InterceptorsWrapper(
        onError: (error, handler) {
          final exception = _handleError(error);
          logError('Request error: ${exception.message}', exception);
          return handler.next(error);
        },
      ),
    );
  }

  /// 错误处理
  NetworkException _handleError(DioException error) {
    switch (error.type) {
      case DioExceptionType.connectionTimeout:
        return NetworkException(
          type: NetworkErrorType.connectionTimeout,
          message: '连接超时，请检查网络',
          originalError: error,
        );
      case DioExceptionType.sendTimeout:
        return NetworkException(
          type: NetworkErrorType.sendTimeout,
          message: '发送请求超时',
          originalError: error,
        );
      case DioExceptionType.receiveTimeout:
        return NetworkException(
          type: NetworkErrorType.receiveTimeout,
          message: '接收响应超时',
          originalError: error,
        );
      case DioExceptionType.badResponse:
        return NetworkException(
          type: NetworkErrorType.badResponse,
          message: '服务器响应错误: ${error.response?.statusMessage}',
          statusCode: error.response?.statusCode,
          originalError: error,
        );
      case DioExceptionType.cancel:
        return NetworkException(
          type: NetworkErrorType.cancel,
          message: '请求被取消',
          originalError: error,
        );
      case DioExceptionType.connectionError:
        return NetworkException(
          type: NetworkErrorType.connectionError,
          message: '网络连接错误，请检查网络设置',
          originalError: error,
        );
      case DioExceptionType.badCertificate:
        return NetworkException(
          type: NetworkErrorType.connectionError,
          message: 'SSL证书验证失败',
          originalError: error,
        );
      case DioExceptionType.unknown:
        if (error.error is SocketException) {
          return NetworkException(
            type: NetworkErrorType.connectionError,
            message: '网络连接失败，请检查网络',
            originalError: error,
          );
        }
        return NetworkException(
          type: NetworkErrorType.unknown,
          message: '未知错误: ${error.message}',
          originalError: error,
        );
    }
  }

  /// 带重试的请求执行
  Future<Response<T>> _executeWithRetry<T>(
    Future<Response<T>> Function() request,
  ) async {
    int attempts = 0;

    while (attempts < maxRetries) {
      try {
        final response = await request();
        return response;
      } on DioException catch (error) {
        attempts++;

        // 不适用于重试的错误类型
        if (error.type == DioExceptionType.cancel ||
            (error.type == DioExceptionType.badResponse &&
                error.response?.statusCode != null &&
                error.response!.statusCode! >= 400 &&
                error.response!.statusCode! < 500)) {
          throw _handleError(error);
        }

        if (attempts >= maxRetries) {
          throw _handleError(error);
        }

        logWarning('Request failed, retrying... ($attempts/$maxRetries)');
        await Future.delayed(retryDelay * attempts);
      }
    }

    throw NetworkException(
      type: NetworkErrorType.unknown,
      message: '请求失败，已重试 $maxRetries 次',
    );
  }

  /// GET 请求
  Future<Response<T>> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Options? options,
    bool useRetry = true,
  }) async {
    final request = () => _dio.get<T>(
          path,
          queryParameters: queryParameters,
          options: options,
        );

    return useRetry
        ? await _executeWithRetry(request)
        : await request();
  }

  /// POST 请求
  Future<Response<T>> post<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    bool useRetry = true,
  }) async {
    final request = () => _dio.post<T>(
          path,
          data: data,
          queryParameters: queryParameters,
          options: options,
        );

    return useRetry
        ? await _executeWithRetry(request)
        : await request();
  }

  /// PUT 请求
  Future<Response<T>> put<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    bool useRetry = true,
  }) async {
    final request = () => _dio.put<T>(
          path,
          data: data,
          queryParameters: queryParameters,
          options: options,
        );

    return useRetry
        ? await _executeWithRetry(request)
        : await request();
  }

  /// DELETE 请求
  Future<Response<T>> delete<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    bool useRetry = true,
  }) async {
    final request = () => _dio.delete<T>(
          path,
          data: data,
          queryParameters: queryParameters,
          options: options,
        );

    return useRetry
        ? await _executeWithRetry(request)
        : await request();
  }

  /// 上传文件
  Future<Response<T>> uploadFile<T>(
    String path, {
    required File file,
    String fileKey = 'file',
    Map<String, dynamic>? data,
    Options? options,
    ProgressCallback? onSendProgress,
    bool useRetry = false,
  }) async {
    final formData = FormData.fromMap({
      ...?data,
      fileKey: await MultipartFile.fromFile(file.path),
    });

    final request = () => _dio.post<T>(
          path,
          data: formData,
          options: options,
          onSendProgress: onSendProgress,
        );

    return useRetry
        ? await _executeWithRetry(request)
        : await request();
  }

  /// 下载文件
  Future<Response> download(
    String urlPath,
    String savePath, {
    ProgressCallback? onReceiveProgress,
    Map<String, dynamic>? queryParameters,
    CancelToken? cancelToken,
    bool deleteOnError = true,
    String lengthHeader = Headers.contentLengthHeader,
    Object? data,
    Options? options,
  }) async {
    try {
      return await _dio.download(
        urlPath,
        savePath,
        onReceiveProgress: onReceiveProgress,
        queryParameters: queryParameters,
        cancelToken: cancelToken,
        deleteOnError: deleteOnError,
        lengthHeader: lengthHeader,
        data: data,
        options: options,
      );
    } on DioException catch (error) {
      throw _handleError(error);
    }
  }
}
