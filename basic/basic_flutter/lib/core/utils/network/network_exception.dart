import 'package:basic_flutter/core/utils/network/network_error_type.dart';

/// 网络异常
class NetworkException implements Exception {
  final NetworkErrorType type;
  final String message;

  NetworkException({required this.type, required this.message});

  @override
  String toString() => 'NetworkException{type: $type, message: $message}';
}
