import 'package:logger/logger.dart';

/// Logger 工具类
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
