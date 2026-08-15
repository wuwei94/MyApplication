/// 与 Android `ApiException` 对齐的网络异常。
class NetworkException implements Exception {
  static const String defaultMessage = '未知错误';
  static const int unknown = 1000;
  static const int connectError = 1001;
  static const int timeoutError = 1002;
  static const int sslError = 1003;
  static const int parseError = 1004;

  /// 触发当前异常的底层错误。
  final Object cause;
  final int code;
  final String message;

  NetworkException(this.cause, this.code, {String? message})
    : message = _normalizeMessage(message ?? cause.toString());

  @override
  String toString() => 'NetworkException{code: $code, message: $message}';

  static String _normalizeMessage(String value) {
    final String normalized = value.trim();
    return normalized.isEmpty ? defaultMessage : normalized;
  }
}
