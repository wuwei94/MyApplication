import 'dart:typed_data';

import 'package:basic_flutter/features/packages/widgets/media_picker_example_widgets.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

/// Image Picker
/// https://pub.dev/packages/image_picker
class ImagePickerExample extends StatelessWidget {
  const ImagePickerExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ImagePickerRoute(title: title);
  }
}

class ImagePickerRoute extends StatefulWidget {
  const ImagePickerRoute({super.key, required this.title});

  final String title;

  @override
  State<ImagePickerRoute> createState() => _ImagePickerRouteState();
}

class _ImagePickerRouteState extends State<ImagePickerRoute> {
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
        accentColor: _ImagePickerRouteState._accentColor,
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
        accentColor: _ImagePickerRouteState._accentColor,
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
