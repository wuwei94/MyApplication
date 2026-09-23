/// 编译期注入的敏感配置（禁止在源码中写入真实密钥）
///
/// 与 Android 侧共用工程根 `local.properties`：
/// - Android：`deepseek.api.key` → Gradle `buildConfigField` → `BuildConfig.DEEPSEEK_API_KEY`
/// - Flutter：`dart tools/sync_dart_defines.dart` 生成 `dart_defines.json`，
///   再以 `--dart-define-from-file=dart_defines.json`（或 `--dart-define=DEEPSEEK_API_KEY=...`）编译
class Secrets {
  Secrets._();

  /// DeepSeek API Key；空字符串表示未配置
  static const String deepSeekApiKey = String.fromEnvironment(
    'DEEPSEEK_API_KEY',
  );
}
