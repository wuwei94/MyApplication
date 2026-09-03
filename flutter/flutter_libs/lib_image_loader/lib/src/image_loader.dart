import 'package:flutter/widgets.dart';
import 'package:lib_image_loader/src/cached_network_image_loader.dart';
import 'package:lib_image_loader/src/i_image_loader.dart';

/// 图片加载统一门面，与 Android lib_image_loader 对齐。
/// 业务侧只依赖 [ImageLoader]，切换内核仅需替换 [kernel]，调用方零改动。
class ImageLoader {
  ImageLoader._();

  /// 当前内核实现，默认 cached_network_image。
  static IImageLoader kernel = const CachedNetworkImageLoader();

  /// 基础网络图片
  static Widget load({
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
  }) {
    return kernel.load(
      url: url,
      width: width,
      height: height,
      fit: fit,
      placeholder: placeholder,
      errorWidget: errorWidget,
      fadeInDuration: fadeInDuration,
      httpHeaders: httpHeaders,
      cache: cache,
      clearMemoryCacheWhenDispose: clearMemoryCacheWhenDispose,
      cacheKey: cacheKey,
    );
  }

  /// 圆角图片
  static Widget radius({
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
  }) {
    return kernel.radius(
      url: url,
      width: width,
      height: height,
      borderRadius: borderRadius,
      fit: fit,
      placeholder: placeholder,
      errorWidget: errorWidget,
      fadeInDuration: fadeInDuration,
      httpHeaders: httpHeaders,
      cache: cache,
      clearMemoryCacheWhenDispose: clearMemoryCacheWhenDispose,
      cacheKey: cacheKey,
    );
  }

  /// 圆形图片
  static Widget round({
    required String url,
    double size = 48,
    Widget? placeholder,
    Widget? errorWidget,
    Duration fadeInDuration = const Duration(milliseconds: 300),
    Map<String, String>? httpHeaders,
    bool cache = true,
    bool clearMemoryCacheWhenDispose = false,
    String? cacheKey,
  }) {
    return kernel.round(
      url: url,
      size: size,
      placeholder: placeholder,
      errorWidget: errorWidget,
      fadeInDuration: fadeInDuration,
      httpHeaders: httpHeaders,
      cache: cache,
      clearMemoryCacheWhenDispose: clearMemoryCacheWhenDispose,
      cacheKey: cacheKey,
    );
  }

  /// 返回 ImageProvider，用于 CircleAvatar / FadeInImage / Hero 等组件
  static ImageProvider<Object> provider(
    String url, {
    Map<String, String>? httpHeaders,
    String? cacheKey,
  }) {
    return kernel.provider(url, httpHeaders: httpHeaders, cacheKey: cacheKey);
  }

  /// 清除指定 URL 的图片缓存
  static Future<void> clear(
    String url, {
    Map<String, String>? httpHeaders,
    String? cacheKey,
  }) {
    return kernel.clear(url, httpHeaders: httpHeaders, cacheKey: cacheKey);
  }
}
