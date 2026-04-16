import 'package:basic_flutter/core/utils/network/image_loader.dart';
import 'package:flutter/material.dart';

/// cached_network_image
/// https://pub.dev/packages/cached_network_image
class ImageDemoPage extends StatelessWidget {
  const ImageDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ImageDemoView(title: title);
  }
}

class ImageDemoView extends StatelessWidget {
  const ImageDemoView({super.key, required this.title});

  final String title;

  static const String _sampleImageUrl =
      'https://picsum.photos/seed/flutter/400/400';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSectionTitle('基础用法 - ImageUtils.load'),
          _buildBasicExample(),
          const SizedBox(height: 24),
          _buildSectionTitle('圆角图片 - ImageUtils.radius'),
          _buildRoundedImage(),
          const SizedBox(height: 24),
          _buildSectionTitle('圆形头像 - ImageUtils.round'),
          _buildCircleAvatar(),
          const SizedBox(height: 24),
          _buildSectionTitle('清除缓存功能'),
          _buildClearCacheExample(),
        ],
      ),
    );
  }

  Widget _buildSectionTitle(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        text,
        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
      ),
    );
  }

  Widget _buildBasicExample() {
    return ImageLoader.load(
      url: _sampleImageUrl,
      width: double.infinity,
      height: 200,
    );
  }

  Widget _buildRoundedImage() {
    return ImageLoader.radius(
      url: '$_sampleImageUrl?random=1',
      width: double.infinity,
      height: 200,
      borderRadius: 16,
    );
  }

  Widget _buildCircleAvatar() {
    return Center(
      child: ImageLoader.round(url: '$_sampleImageUrl?random=2', size: 120),
    );
  }

  Widget _buildClearCacheExample() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          '点击下方按钮清除上面图片的缓存，然后重新加载时会重新下载图片',
          style: TextStyle(fontSize: 14, color: Colors.grey),
        ),
        const SizedBox(height: 12),
        ElevatedButton.icon(
          onPressed: () async {
            await ImageLoader.clear(_sampleImageUrl);
          },
          icon: const Icon(Icons.delete_outline),
          label: const Text('清除缓存'),
        ),
      ],
    );
  }
}
