import 'package:network_http/src/network_exception.dart';
import 'package:network_http/src/server_result_exception.dart';

/// 与 Android `RetrofitResponse` 对齐的业务响应。
class NetworkResponse<T> {
  static const int successCode = 0;

  final int code;
  final String message;
  final T? data;

  const NetworkResponse._({required this.code, this.message = '', this.data});

  bool get isSuccess => code == successCode;

  /// 需要将业务失败进入异常链时显式调用。
  NetworkResponse<T> requireSuccess() {
    if (isSuccess) return this;
    throw NetworkException(
      ServerResultException(code, message),
      code,
      message: message,
    );
  }

  factory NetworkResponse.fromJson(
    dynamic json, {
    T Function(dynamic data)? decoder,
  }) {
    if (json is! Map<dynamic, dynamic>) {
      throw const FormatException('业务响应必须是 JSON 对象');
    }
    final dynamic rawCode = json['errorCode'];
    if (rawCode is! int) {
      throw const FormatException('业务响应缺少整数类型 errorCode');
    }
    final dynamic rawData = json['data'];
    return NetworkResponse<T>._(
      code: rawCode,
      message: json['errorMsg']?.toString() ?? '',
      data: rawData == null
          ? null
          : decoder != null
          ? decoder(rawData)
          : rawData as T,
    );
  }
}
