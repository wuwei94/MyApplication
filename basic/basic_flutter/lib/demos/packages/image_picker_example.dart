import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

/// Image Picker
/// https://pub.dev/packages/image_picker
class ImagePickerDemoPage extends StatelessWidget {
  const ImagePickerDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ImagePickerDemoView(title: title);
  }
}

class ImagePickerDemoView extends StatefulWidget {
  const ImagePickerDemoView({super.key, required this.title});

  final String title;

  @override
  State<ImagePickerDemoView> createState() => _ImagePickerDemoViewState();
}

class _ImagePickerDemoViewState extends State<ImagePickerDemoView> {
  static const Color _accentColor = Color(0xFF0F5DAA);

  final ImagePicker _picker = ImagePicker();

  bool _isLoading = false;
  String _statusMessage = '可以从系统相册选择图片，也可以直接拍照。';
  List<_PickedImageData> _pickedImages = <_PickedImageData>[];

  @override
  void initState() {
    super.initState();
    _restoreLostData();
  }

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(theme),
    );
  }

  Widget getBody(ThemeData theme) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        MediaPickerSummaryCard(
          title: 'image_picker',
          description: _statusMessage,
          accentColor: _accentColor,
          countLabel: '已选 ${_pickedImages.length} 张',
        ),
        const SizedBox(height: 16),
        const MediaPickerSectionHeader(
          title: '快捷操作',
          subtitle: '体验系统相册拍照、单图选择和多图选择。',
        ),
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: <Widget>[
            _PickerActionButton(
              icon: Icons.photo_outlined,
              label: '单选',
              accentColor: _accentColor,
              onPressed: _isLoading ? null : _pickSingleImage,
            ),
            _PickerActionButton(
              icon: Icons.collections_outlined,
              label: '多选',
              accentColor: _accentColor,
              onPressed: _isLoading ? null : _pickMultipleImages,
            ),
            _PickerActionButton(
              icon: Icons.photo_camera_outlined,
              label: '拍照',
              accentColor: _accentColor,
              onPressed: _isLoading ? null : _pickFromCamera,
            ),
            _PickerActionButton(
              icon: Icons.restart_alt,
              label: '清空',
              accentColor: _accentColor,
              onPressed: _pickedImages.isEmpty ? null : _clearSelection,
            ),
          ],
        ),
        const SizedBox(height: 20),
        MediaPickerSectionHeader(
          title: '结果预览',
          subtitle: _pickedImages.isEmpty
              ? '选择或拍照后会在这里展示缩略图。'
              : '当前共 ${_pickedImages.length} 张图片。',
        ),
        _SelectedImagesSection(
          isLoading: _isLoading,
          pickedImages: _pickedImages,
        ),
      ],
    );
  }

  Future<void> _restoreLostData() async {
    final LostDataResponse response = await _picker.retrieveLostData();

    if (response.isEmpty) {
      return;
    }

    final List<XFile>? files = response.files;
    if (files == null || files.isEmpty) {
      final String message = response.exception?.code ?? '没有需要恢复的图片数据。';
      if (!mounted) {
        return;
      }
      setState(() {
        _statusMessage = message;
      });
      return;
    }

    final List<_PickedImageData> restoredImages = await _toPickedImageData(
      files,
    );

    if (!mounted) {
      return;
    }

    setState(() {
      _pickedImages = _mergeImages(restoredImages);
      _statusMessage = '已恢复 ${restoredImages.length} 张系统回收前选择的图片。';
    });
  }

  Future<void> _pickSingleImage() async {
    await _pickImages(
      successMessage: '已通过系统相册选中 1 张图片。',
      pick: () async {
        final XFile? file = await _picker.pickImage(
          source: ImageSource.gallery,
        );
        if (file == null) {
          return <XFile>[];
        }
        return <XFile>[file];
      },
    );
  }

  Future<void> _pickFromCamera() async {
    setState(() {
      _isLoading = true;
      _statusMessage = '正在打开系统相机...';
    });

    try {
      final XFile? file = await _picker.pickImage(
        source: ImageSource.camera,
        preferredCameraDevice: CameraDevice.rear,
      );

      if (!mounted) {
        return;
      }

      if (file == null) {
        setState(() {
          _isLoading = false;
          _statusMessage = '已取消拍照。';
        });
        return;
      }

      final List<_PickedImageData> images = await _toPickedImageData(<XFile>[
        file,
      ]);

      if (!mounted) {
        return;
      }

      setState(() {
        _isLoading = false;
        _pickedImages = _mergeImages(<_PickedImageData>[
          ...images,
          ..._pickedImages,
        ]);
        _statusMessage = '已使用系统相机拍摄 1 张图片。';
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _isLoading = false;
        _statusMessage = '打开系统相机失败：$error';
      });
    }
  }

  Future<void> _pickMultipleImages() async {
    await _pickImages(
      successMessage: '已更新图片选择结果。',
      pick: () => _picker.pickMultiImage(limit: 9),
    );
  }

  Future<void> _pickImages({
    required Future<List<XFile>> Function() pick,
    required String successMessage,
  }) async {
    setState(() {
      _isLoading = true;
      _statusMessage = '正在打开系统选择器...';
    });

    try {
      final List<XFile> files = await pick();
      if (files.isEmpty) {
        if (!mounted) {
          return;
        }
        setState(() {
          _isLoading = false;
          _statusMessage = '已取消选择。';
        });
        return;
      }

      final List<_PickedImageData> images = await _toPickedImageData(files);

      if (!mounted) {
        return;
      }

      setState(() {
        _isLoading = false;
        _pickedImages = images;
        _statusMessage = successMessage;
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _isLoading = false;
        _statusMessage = '打开系统选择器失败：$error';
      });
    }
  }

  List<_PickedImageData> _mergeImages(List<_PickedImageData> images) {
    final Map<String, _PickedImageData> uniqueImages =
        <String, _PickedImageData>{};

    for (final _PickedImageData image in images) {
      uniqueImages[image.id] = image;
    }

    return uniqueImages.values.toList();
  }

  Future<List<_PickedImageData>> _toPickedImageData(List<XFile> files) async {
    final List<_PickedImageData> images = <_PickedImageData>[];

    for (final XFile file in files) {
      final Uint8List bytes = await file.readAsBytes();
      images.add(
        _PickedImageData(
          id: file.path.isNotEmpty ? file.path : file.name,
          name: file.name,
          bytes: bytes,
          sizeInBytes: bytes.length,
        ),
      );
    }

    return images;
  }

  void _clearSelection() {
    setState(() {
      _pickedImages = <_PickedImageData>[];
      _statusMessage = '已清空当前选择。';
    });
  }
}

class _SelectedImagesSection extends StatelessWidget {
  const _SelectedImagesSection({
    required this.isLoading,
    required this.pickedImages,
  });

  final bool isLoading;
  final List<_PickedImageData> pickedImages;

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const MediaPickerLoadingState();
    }

    if (pickedImages.isEmpty) {
      return const MediaPickerEmptyState(
        icon: Icons.image_outlined,
        title: '还没有选择图片',
        description: '可以先体验系统拍照，或者从相册中单选、多选图片。',
        accentColor: _ImagePickerDemoViewState._accentColor,
      );
    }

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: pickedImages.length,
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 12,
        crossAxisSpacing: 12,
        childAspectRatio: 0.88,
      ),
      itemBuilder: (BuildContext context, int index) {
        final _PickedImageData image = pickedImages[index];
        return _PickedImageTile(image: image, index: index + 1);
      },
    );
  }
}

class _PickedImageTile extends StatelessWidget {
  const _PickedImageTile({required this.image, required this.index});

  final _PickedImageData image;
  final int index;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return MediaPickerTileFrame(
      badge: MediaPickerIndexBadge(
        index: index,
        accentColor: _ImagePickerDemoViewState._accentColor,
      ),
      footer: MediaPickerFooter(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Text(
              image.name,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: theme.textTheme.labelLarge?.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              image.formattedSize,
              style: theme.textTheme.bodySmall?.copyWith(color: Colors.white70),
            ),
          ],
        ),
      ),
      child: Image.memory(image.bytes, fit: BoxFit.cover),
    );
  }
}

class _PickerActionButton extends StatelessWidget {
  const _PickerActionButton({
    required this.icon,
    required this.label,
    required this.accentColor,
    required this.onPressed,
  });

  final IconData icon;
  final String label;
  final Color accentColor;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    return MediaPickerActionButton(
      icon: icon,
      label: label,
      accentColor: accentColor,
      onPressed: onPressed,
    );
  }
}

class _PickedImageData {
  const _PickedImageData({
    required this.id,
    required this.name,
    required this.bytes,
    required this.sizeInBytes,
  });

  final String id;
  final String name;
  final Uint8List bytes;
  final int sizeInBytes;

  String get formattedSize {
    final double sizeInKb = sizeInBytes / 1024;
    if (sizeInKb < 1024) {
      return '${sizeInKb.toStringAsFixed(1)} KB';
    }

    final double sizeInMb = sizeInKb / 1024;
    return '${sizeInMb.toStringAsFixed(1)} MB';
  }
}

class MediaPickerSummaryCard extends StatelessWidget {
  const MediaPickerSummaryCard({
    super.key,
    required this.title,
    required this.description,
    required this.accentColor,
    required this.countLabel,
  });

  final String title;
  final String description;
  final Color accentColor;
  final String countLabel;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: accentColor.withValues(alpha: 0.18)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120A2533),
            blurRadius: 22,
            offset: Offset(0, 10),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Expanded(
                  child: Text(
                    title,
                    style: theme.textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                DecoratedBox(
                  decoration: BoxDecoration(
                    color: accentColor.withValues(alpha: 0.10),
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 6,
                    ),
                    child: Text(
                      countLabel,
                      style: theme.textTheme.labelLarge?.copyWith(
                        fontWeight: FontWeight.w700,
                        color: accentColor,
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Text(
              description,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
                height: 1.4,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class MediaPickerSectionHeader extends StatelessWidget {
  const MediaPickerSectionHeader({
    super.key,
    required this.title,
    required this.subtitle,
  });

  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            subtitle,
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }
}

class MediaPickerActionButton extends StatelessWidget {
  const MediaPickerActionButton({
    super.key,
    required this.icon,
    required this.label,
    required this.accentColor,
    required this.onPressed,
  });

  final IconData icon;
  final String label;
  final Color accentColor;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 156,
      child: FilledButton.tonalIcon(
        style: FilledButton.styleFrom(
          foregroundColor: accentColor,
          backgroundColor: accentColor.withValues(alpha: 0.10),
        ),
        onPressed: onPressed,
        icon: Icon(icon),
        label: Padding(
          padding: const EdgeInsets.symmetric(vertical: 14),
          child: Text(label),
        ),
      ),
    );
  }
}

class MediaPickerEmptyState extends StatelessWidget {
  const MediaPickerEmptyState({
    super.key,
    required this.icon,
    required this.title,
    required this.description,
    required this.accentColor,
  });

  final IconData icon;
  final String title;
  final String description;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.all(28),
      decoration: BoxDecoration(
        color: accentColor.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(24),
      ),
      child: Column(
        children: <Widget>[
          Icon(icon, size: 40, color: accentColor),
          const SizedBox(height: 12),
          Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            description,
            textAlign: TextAlign.center,
            style: theme.textTheme.bodyMedium?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }
}

class MediaPickerLoadingState extends StatelessWidget {
  const MediaPickerLoadingState({super.key});

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.symmetric(vertical: 48),
      child: Center(child: CircularProgressIndicator()),
    );
  }
}

class MediaPickerTileFrame extends StatelessWidget {
  const MediaPickerTileFrame({
    super.key,
    required this.child,
    this.badge,
    this.footer,
  });

  final Widget child;
  final Widget? badge;
  final Widget? footer;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        color: Colors.white,
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120A2533),
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(20),
        child: Stack(
          fit: StackFit.expand,
          children: <Widget>[
            child,
            if (badge != null) Positioned(top: 12, right: 12, child: badge!),
            if (footer != null)
              Positioned(left: 0, right: 0, bottom: 0, child: footer!),
          ],
        ),
      ),
    );
  }
}

class MediaPickerIndexBadge extends StatelessWidget {
  const MediaPickerIndexBadge({
    super.key,
    required this.index,
    required this.accentColor,
  });

  final int index;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return CircleAvatar(
      radius: 14,
      backgroundColor: accentColor.withValues(alpha: 0.82),
      child: Text(
        '$index',
        style: theme.textTheme.labelMedium?.copyWith(
          color: Colors.white,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class MediaPickerFooter extends StatelessWidget {
  const MediaPickerFooter({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.bottomCenter,
          end: Alignment.topCenter,
          colors: <Color>[Color(0xCC0E1C26), Color(0x000E1C26)],
        ),
      ),
      child: Padding(padding: const EdgeInsets.all(12), child: child),
    );
  }
}
