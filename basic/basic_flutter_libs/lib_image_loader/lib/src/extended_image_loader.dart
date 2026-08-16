import 'package:extended_image/extended_image.dart';
import 'package:flutter/material.dart';
import 'package:lib_image_loader/src/i_image_loader.dart';

/// extended_image 内核实现。
/// 覆盖基础、圆角、圆形与缓存清理场景；手势缩放等查看器能力属于
/// extended_image 的扩展场景，由业务侧直接使用 extended_image 实现。
class ExtendedImageLoader implements IImageLoader {
  const ExtendedImageLoader();

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

  @override
  ImageProvider<Object> provider(
    String url, {
    Map<String, String>? httpHeaders,
    String? cacheKey,
  }) {
    return ExtendedNetworkImageProvider(
      url,
      headers: httpHeaders,
      cache: true,
      cacheKey: cacheKey,
    );
  }

  @override
  Future<void> clear(
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
