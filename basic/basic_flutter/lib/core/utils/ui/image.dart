import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

/// 缓存图片工具类
/// 基于 cached_network_image 封装的常用图片加载方式
class ImageUtils {
  ImageUtils._();

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
  }) {
    return CachedNetworkImage(
      imageUrl: url,
      width: width,
      height: height,
      fit: fit,
      fadeInDuration: fadeInDuration,
      httpHeaders: httpHeaders,
      placeholder: placeholder != null
          ? (context, url) => placeholder
          : (context, url) => _defaultPlaceholder(width, height),
      errorWidget: errorWidget != null
          ? (context, url, error) => errorWidget
          : (context, url, error) => _defaultErrorWidget(width, height),
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
  }) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(borderRadius),
      child: load(
        url: url,
        width: width,
        height: height,
        fit: fit,
        placeholder: placeholder,
        errorWidget: errorWidget,
      ),
    );
  }

  /// 圆形图片
  static Widget round({
    required String url,
    double size = 48,
    Widget? placeholder,
    Widget? errorWidget,
  }) {
    return CachedNetworkImage(
      imageUrl: url,
      imageBuilder: (context, imageProvider) => Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          image: DecorationImage(image: imageProvider, fit: BoxFit.cover),
        ),
      ),
      placeholder: (context, url) =>
          placeholder ?? _defaultPlaceholder(size, size),
      errorWidget: (context, url, error) =>
          errorWidget ?? _defaultErrorWidget(size, size),
    );
  }

  /// 清除指定 URL 的图片缓存
  static Future<void> clear(String url) async {
    await CachedNetworkImage.evictFromCache(url);
  }

  // ============== 默认组件 ==============

  /// 默认加载占位图 - 灰色背景 + 圆形进度指示器
  static Widget _defaultPlaceholder(double? width, double? height) {
    return Container(
      width: width,
      height: height,
      color: Colors.grey[300],
      child: const Center(child: CircularProgressIndicator()),
    );
  }

  /// 默认错误图 - 灰色背景 + 红色错误图标
  static Widget _defaultErrorWidget(double? width, double? height) {
    return Container(
      width: width,
      height: height,
      color: Colors.grey[300],
      child: const Center(
        child: Icon(Icons.error, color: Colors.red, size: 32),
      ),
    );
  }
}
