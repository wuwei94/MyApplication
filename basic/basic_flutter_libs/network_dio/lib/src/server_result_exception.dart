/// 与 Android `ServerResultException` 对齐的业务失败原因。
class ServerResultException implements Exception {
  final int code;
  final String message;

  const ServerResultException(this.code, [this.message = '']);

  @override
  String toString() => 'ServerResultException{code: $code, message: $message}';
}
