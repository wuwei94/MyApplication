import 'package:logger/logger.dart';

/// 全局 Logger 实例
/// 使用 final 防止意外修改
final Logger _logger = Logger(
  printer: PrettyPrinter(
    methodCount: 0,
    errorMethodCount: 5,
    lineLength: 80,
    colors: true,
    printEmojis: true,
  ),
);

/// Debug 级别日志
void logDebug(dynamic msg) {
  _logger.d(msg);
}

/// Info 级别日志
void logInfo(dynamic msg) {
  _logger.i(msg);
}

/// Warning 级别日志
void logWarning(dynamic msg) {
  _logger.w(msg);
}

/// Error 级别日志
void logError(dynamic msg, [dynamic error, StackTrace? stackTrace]) {
  _logger.e(msg, error: error, stackTrace: stackTrace);
}

/// 兼容旧代码的 log 函数（deprecated）
@Deprecated('Use logDebug instead')
void log(dynamic msg) {
  _logger.d(msg);
}
