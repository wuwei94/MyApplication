import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:image_loader/image_loader.dart';

void main() {
  tearDown(() {
    ImageLoader.kernel = const CachedNetworkImageLoader();
  });

  test('默认内核为 cached_network_image 实现', () {
    expect(ImageLoader.kernel, isA<CachedNetworkImageLoader>());
  });

  test('切换内核后门面转发到新内核，调用方 API 不变', () async {
    final _FakeImageLoader fake = _FakeImageLoader();
    ImageLoader.kernel = fake;

    ImageLoader.load(url: 'https://example.com/a.png');
    ImageLoader.radius(url: 'https://example.com/b.png');
    ImageLoader.round(url: 'https://example.com/c.png');
    ImageLoader.provider('https://example.com/d.png');
    await ImageLoader.clear('https://example.com/e.png');

    expect(
      fake.calls,
      <String>['load', 'radius', 'round', 'provider', 'clear'],
    );
  });
}

class _FakeImageLoader implements IImageLoader {
  final List<String> calls = <String>[];

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
    calls.add('load');
    return const SizedBox.shrink();
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
    calls.add('radius');
    return const SizedBox.shrink();
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
    calls.add('round');
    return const SizedBox.shrink();
  }

  @override
  ImageProvider<Object> provider(
    String url, {
    Map<String, String>? httpHeaders,
    String? cacheKey,
  }) {
    calls.add('provider');
    return const NetworkImage('https://example.com/provider.png');
  }

  @override
  Future<void> clear(
    String url, {
    Map<String, String>? httpHeaders,
    String? cacheKey,
  }) async {
    calls.add('clear');
  }
}
