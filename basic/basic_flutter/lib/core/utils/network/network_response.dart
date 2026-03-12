/// 通用网络响应
class NetworkResponse<T> {
  final T data;
  final int? statusCode;
  final String? statusMessage;
  final Map<String, dynamic> headers;

  NetworkResponse({
    required this.data,
    this.statusCode,
    this.statusMessage,
    this.headers = const <String, dynamic>{},
  });
}
