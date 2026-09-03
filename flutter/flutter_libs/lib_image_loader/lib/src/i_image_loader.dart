import 'package:flutter/widgets.dart';

/// 图片加载统一接口
/// 与 Android lib_image_loader 的 IImageLoader 对齐：业务侧只依赖接口，
/// 内核实现可以整体替换。
abstract class IImageLoader {
  /// 基础网络图片
  Widget load({
    required String url,
    double? width,
    double? height,
    BoxFit fit = BoxFit.cover,
    Widget? placeholder,
    Widget? errorWidget,
    Duration fadeInDuration = const Duration(milliseconds: 300),
    Map<String, String>? httpHeaders,
    bool cache = true,
    bool clearMemoryCacheWhenDispose = false,
    String? cacheKey,
  });

  /// 圆角图片
  Widget radius({
    required String url,
    double? width,
    double? height,
    double borderRadius = 8,
    BoxFit fit = BoxFit.cover,
    Widget? placeholder,
    Widget? errorWidget,
    Duration fadeInDuration = const Duration(milliseconds: 300),
    Map<String, String>? httpHeaders,
    bool cache = true,
    bool clearMemoryCacheWhenDispose = false,
    String? cacheKey,
  });

  /// 圆形图片
  Widget round({
    required String url,
    double size = 48,
    Widget? placeholder,
    Widget? errorWidget,
    Duration fadeInDuration = const Duration(milliseconds: 300),
    Map<String, String>? httpHeaders,
    bool cache = true,
    bool clearMemoryCacheWhenDispose = false,
    String? cacheKey,
  });

  /// 返回 ImageProvider，用于 CircleAvatar / FadeInImage / Hero 等组件
  ImageProvider<Object> provider(
    String url, {
    Map<String, String>? httpHeaders,
    String? cacheKey,
  });

  /// 清除指定 URL 的图片缓存
  Future<void> clear(
    String url, {
    Map<String, String>? httpHeaders,
    String? cacheKey,
  });
}
