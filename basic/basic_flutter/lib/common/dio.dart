import 'package:basic_flutter/common/log.dart';
import 'package:dio/dio.dart';

/// Dio 网络请求工具类
class DioClient {
  static DioClient? _instance;
  late final Dio _dio;

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

    // 添加日志拦截器
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
  }

  /// 获取单例实例
  static DioClient get instance {
    _instance ??= DioClient._internal();
    return _instance!;
  }

  /// Dio 实例
  Dio get dio => _dio;

  /// GET 请求
  Future<Response<T>> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) async {
    return _dio.get<T>(
      path,
      queryParameters: queryParameters,
      options: options,
    );
  }

  /// POST 请求
  Future<Response<T>> post<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) async {
    return _dio.post<T>(
      path,
      data: data,
      queryParameters: queryParameters,
      options: options,
    );
  }
}
