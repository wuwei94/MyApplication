import 'dart:async';

import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:lib_image_loader/src/i_image_loader.dart';

/// cached_network_image 内核实现
/// 基于 cached_network_image 封装的常用图片加载方式，
/// 覆盖普通封面图、列表图与头像等常规场景。
class CachedNetworkImageLoader implements IImageLoader {
  const CachedNetworkImageLoader();

  @override
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
  }) {
    final Widget child = cache
        ? CachedNetworkImage(
            imageUrl: url,
            width: width,
            height: height,
            fit: fit,
            fadeInDuration: fadeInDuration,
            httpHeaders: httpHeaders,
            cacheKey: cacheKey,
            placeholder: placeholder != null
                ? (BuildContext context, String imageUrl) => placeholder
                : (BuildContext context, String imageUrl) =>
                      _defaultPlaceholder(width, height),
            errorWidget: errorWidget != null
                ? (BuildContext context, String imageUrl, Object error) =>
                      errorWidget
                : (BuildContext context, String imageUrl, Object error) =>
                      _defaultErrorWidget(width, height),
          )
        : Image.network(
            url,
            width: width,
            height: height,
            fit: fit,
            headers: httpHeaders,
            frameBuilder:
                (
                  BuildContext context,
                  Widget child,
                  int? frame,
                  bool wasSynchronouslyLoaded,
                ) {
                  if (wasSynchronouslyLoaded ||
                      fadeInDuration <= Duration.zero) {
                    return child;
                  }

                  return AnimatedOpacity(
                    opacity: frame == null ? 0 : 1,
                    duration: fadeInDuration,
                    child: child,
                  );
                },
            loadingBuilder:
                (
                  BuildContext context,
                  Widget child,
                  ImageChunkEvent? loadingProgress,
                ) {
                  if (loadingProgress == null) {
                    return child;
                  }

                  return placeholder ?? _defaultPlaceholder(width, height);
                },
            errorBuilder:
                (BuildContext context, Object error, StackTrace? stackTrace) {
                  return errorWidget ?? _defaultErrorWidget(width, height);
                },
          );

    return _wrapWithMemoryCleanupIfNeeded(
      child: child,
      url: url,
      httpHeaders: httpHeaders,
      cache: cache,
      clearMemoryCacheWhenDispose: clearMemoryCacheWhenDispose,
      cacheKey: cacheKey,
    );
  }

  @override
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
        fadeInDuration: fadeInDuration,
        httpHeaders: httpHeaders,
        cache: cache,
        clearMemoryCacheWhenDispose: clearMemoryCacheWhenDispose,
        cacheKey: cacheKey,
      ),
    );
  }

  @override
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
  }) {
    return ClipOval(
      child: load(
        url: url,
        width: size,
        height: size,
        placeholder: placeholder,
        errorWidget: errorWidget,
        fadeInDuration: fadeInDuration,
        httpHeaders: httpHeaders,
        cache: cache,
        clearMemoryCacheWhenDispose: clearMemoryCacheWhenDispose,
        cacheKey: cacheKey,
      ),
    );
  }

  @override
  ImageProvider<Object> provider(
    String url, {
    Map<String, String>? httpHeaders,
    String? cacheKey,
  }) {
    return CachedNetworkImageProvider(
      url,
      headers: httpHeaders,
      cacheKey: cacheKey,
    );
  }

  @override
  Future<void> clear(
    String url, {
    Map<String, String>? httpHeaders,
    String? cacheKey,
  }) async {
    await CachedNetworkImage.evictFromCache(url, cacheKey: cacheKey);
    if (httpHeaders != null) {
      await NetworkImage(url, headers: httpHeaders).evict();
    }
  }

  Widget _wrapWithMemoryCleanupIfNeeded({
    required Widget child,
    required String url,
    required bool cache,
    required bool clearMemoryCacheWhenDispose,
    Map<String, String>? httpHeaders,
    String? cacheKey,
  }) {
    if (!clearMemoryCacheWhenDispose) {
      return child;
    }

    return _DisposeCallbackWidget(
      onDispose: () async {
        if (cache) {
          await CachedNetworkImageProvider(
            url,
            headers: httpHeaders,
            cacheKey: cacheKey,
          ).evict();
          return;
        }

        await NetworkImage(url, headers: httpHeaders).evict();
      },
      child: child,
    );
  }

  // ============== 默认组件 ==============

  /// 默认加载占位图 - 灰色背景 + 圆形进度指示器
  Widget _defaultPlaceholder(double? width, double? height) {
    return Container(
      width: width,
      height: height,
      color: Colors.grey[300],
      child: const Center(child: CircularProgressIndicator()),
    );
  }

  /// 默认错误图 - 灰色背景 + 红色错误图标
  Widget _defaultErrorWidget(double? width, double? height) {
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

class _DisposeCallbackWidget extends StatefulWidget {
  const _DisposeCallbackWidget({required this.onDispose, required this.child});

  final Future<void> Function() onDispose;
  final Widget child;

  @override
  State<_DisposeCallbackWidget> createState() => _DisposeCallbackWidgetState();
}

class _DisposeCallbackWidgetState extends State<_DisposeCallbackWidget> {
  @override
  void dispose() {
    unawaited(widget.onDispose());
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return widget.child;
  }
}
