import 'package:extended_image/extended_image.dart';
import 'package:flutter/material.dart';

/// 扩展图片工具类
/// 基于 extended_image 封装的常用图片加载方式
class ExtendedImageLoader {
  ExtendedImageLoader._();

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
    return _buildNetworkImage(
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
    return _buildNetworkImage(
      url: url,
      width: width,
      height: height,
      fit: fit,
      placeholder: placeholder,
      errorWidget: errorWidget,
      fadeInDuration: fadeInDuration,
      httpHeaders: httpHeaders,
      cache: cache,
      borderRadius: BorderRadius.circular(borderRadius),
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
    return _buildNetworkImage(
      url: url,
      width: size,
      height: size,
      placeholder: placeholder,
      errorWidget: errorWidget,
      fadeInDuration: fadeInDuration,
      httpHeaders: httpHeaders,
      cache: cache,
      shape: BoxShape.circle,
      clearMemoryCacheWhenDispose: clearMemoryCacheWhenDispose,
      cacheKey: cacheKey,
    );
  }

  /// 支持手势缩放和平移的网络图片
  static Widget gesture({
    required String url,
    double? width,
    double? height,
    BoxFit fit = BoxFit.contain,
    Widget? placeholder,
    Widget? errorWidget,
    Duration fadeInDuration = const Duration(milliseconds: 300),
    Map<String, String>? httpHeaders,
    bool cache = true,
    bool clearMemoryCacheWhenDispose = false,
    String? cacheKey,
    double minScale = 0.9,
    double maxScale = 4.0,
    double animationMinScale = 0.7,
    double animationMaxScale = 4.5,
  }) {
    return _buildNetworkImage(
      url: url,
      width: width,
      height: height,
      fit: fit,
      placeholder: placeholder,
      errorWidget: errorWidget,
      fadeInDuration: fadeInDuration,
      httpHeaders: httpHeaders,
      cache: cache,
      mode: ExtendedImageMode.gesture,
      initGestureConfigHandler: (ExtendedImageState state) {
        return GestureConfig(
          minScale: minScale,
          animationMinScale: animationMinScale,
          maxScale: maxScale,
          animationMaxScale: animationMaxScale,
          speed: 1.0,
          inertialSpeed: 100.0,
          initialScale: 1.0,
          inPageView: false,
          initialAlignment: InitialAlignment.center,
          reverseMousePointerScrollDirection: true,
        );
      },
      clearMemoryCacheWhenDispose: clearMemoryCacheWhenDispose,
      cacheKey: cacheKey,
    );
  }

  /// 清除指定 URL 的图片缓存
  static Future<void> clear(
    String url, {
    Map<String, String>? httpHeaders,
    String? cacheKey,
  }) async {
    await ExtendedNetworkImageProvider(
      url,
      headers: httpHeaders,
      cache: true,
      cacheKey: cacheKey,
    ).evict();
    await clearDiskCachedImage(url, cacheKey: cacheKey);
  }

  static Widget _buildNetworkImage({
    required String url,
    double? width,
    double? height,
    BoxFit fit = BoxFit.cover,
    Widget? placeholder,
    Widget? errorWidget,
    Duration fadeInDuration = const Duration(milliseconds: 300),
    Map<String, String>? httpHeaders,
    bool cache = true,
    BoxShape? shape,
    BorderRadius? borderRadius,
    ExtendedImageMode mode = ExtendedImageMode.none,
    InitGestureConfigHandler? initGestureConfigHandler,
    bool clearMemoryCacheWhenDispose = false,
    String? cacheKey,
  }) {
    return ExtendedImage.network(
      url,
      width: width,
      height: height,
      fit: fit,
      cache: cache,
      headers: httpHeaders,
      shape: shape,
      borderRadius: borderRadius,
      mode: mode,
      initGestureConfigHandler: initGestureConfigHandler,
      clearMemoryCacheWhenDispose: clearMemoryCacheWhenDispose,
      cacheKey: cacheKey,
      loadStateChanged: (ExtendedImageState state) {
        return _buildLoadState(
          state: state,
          width: width,
          height: height,
          placeholder: placeholder,
          errorWidget: errorWidget,
          fadeInDuration: fadeInDuration,
        );
      },
    );
  }

  static Widget? _buildLoadState({
    required ExtendedImageState state,
    double? width,
    double? height,
    Widget? placeholder,
    Widget? errorWidget,
    Duration fadeInDuration = const Duration(milliseconds: 300),
  }) {
    switch (state.extendedImageLoadState) {
      case LoadState.loading:
        return placeholder ?? _defaultPlaceholder(width, height);
      case LoadState.completed:
        if (fadeInDuration <= Duration.zero || state.wasSynchronouslyLoaded) {
          return null;
        }

        return _FadeInCompletedImage(
          key: ValueKey<Object>(state.imageStreamKey ?? state.imageProvider),
          duration: fadeInDuration,
          child: state.completedWidget,
        );
      case LoadState.failed:
        return errorWidget ?? _defaultErrorWidget(width, height);
    }
  }

  /// 默认加载占位图 - 灰色背景 + 圆形进度指示器
  static Widget _defaultPlaceholder(double? width, double? height) {
    return ColoredBox(
      color: Colors.grey.shade300,
      child: SizedBox(
        width: width,
        height: height,
        child: const Center(child: CircularProgressIndicator()),
      ),
    );
  }

  /// 默认错误图 - 灰色背景 + 红色错误图标
  static Widget _defaultErrorWidget(double? width, double? height) {
    return ColoredBox(
      color: Colors.grey.shade300,
      child: SizedBox(
        width: width,
        height: height,
        child: const Center(
          child: Icon(Icons.error, color: Colors.red, size: 32),
        ),
      ),
    );
  }
}

class _FadeInCompletedImage extends StatefulWidget {
  const _FadeInCompletedImage({
    super.key,
    required this.duration,
    required this.child,
  });

  final Duration duration;
  final Widget child;

  @override
  State<_FadeInCompletedImage> createState() => _FadeInCompletedImageState();
}

class _FadeInCompletedImageState extends State<_FadeInCompletedImage> {
  double _opacity = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) {
        return;
      }

      setState(() {
        _opacity = 1;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedOpacity(
      opacity: _opacity,
      duration: widget.duration,
      child: widget.child,
    );
  }
}
