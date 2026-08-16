import 'dart:async';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:image_cropper/image_cropper.dart';
import 'package:image_picker/image_picker.dart';

/// Image Cropper
/// https://pub.dev/packages/image_cropper
class ImageCropperDemoPage extends StatelessWidget {
  const ImageCropperDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ImageCropperDemoView(title: title);
  }
}

class ImageCropperDemoView extends StatefulWidget {
  const ImageCropperDemoView({super.key, required this.title});

  final String title;

  @override
  State<ImageCropperDemoView> createState() => _ImageCropperDemoViewState();
}

class _ImageCropperDemoViewState extends State<ImageCropperDemoView> {
  static const Color _accentColor = Color(0xFF2563EB);
  static const List<CropAspectRatioPresetData> _aspectRatioPresets =
      <CropAspectRatioPresetData>[
        CropAspectRatioPreset.original,
        CropAspectRatioPreset.square,
        CropAspectRatioPreset.ratio4x3,
        CropAspectRatioPreset.ratio16x9,
      ];

  final ImagePicker _picker = ImagePicker();
  final ImageCropper _cropper = ImageCropper();

  _CropRatioOption _ratioOption = _CropRatioOption.free;
  _CropFrameStyle _frameStyle = _CropFrameStyle.rectangle;
  _CropOutputFormat _outputFormat = _CropOutputFormat.jpg;
  _DemoImageData? _sourceImage;
  _DemoImageData? _croppedImage;
  bool _isPicking = false;
  bool _isCropping = false;
  int _quality = 92;
  String _statusMessage = '先选择一张图片，再打开系统裁剪界面体验比例、形状和压缩质量配置。';

  bool get _isBusy => _isPicking || _isCropping;

  @override
  void initState() {
    super.initState();
    unawaited(_restoreLostData());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        _CropperHeroCard(
          statusMessage: _statusMessage,
          sourceImage: _sourceImage,
          croppedImage: _croppedImage,
          quality: _quality,
          outputFormat: _outputFormat,
          accentColor: _accentColor,
        ),
        const SizedBox(height: 16),
        _buildActionSection(),
        const SizedBox(height: 16),
        _buildSettingsSection(),
        const SizedBox(height: 16),
        _buildPreviewSection(),
      ],
    );
  }

  Widget _buildActionSection() {
    return _DemoSectionCard(
      title: '快捷操作',
      subtitle: '从系统相册或相机取得源图，再调用 image_cropper 打开原生裁剪界面。',
      child: Wrap(
        spacing: 12,
        runSpacing: 12,
        children: <Widget>[
          _ActionButton(
            icon: Icons.photo_library_outlined,
            label: '相册选图',
            accentColor: _accentColor,
            onPressed: _isBusy ? null : () => _pickImage(ImageSource.gallery),
          ),
          _ActionButton(
            icon: Icons.photo_camera_outlined,
            label: '拍照',
            accentColor: _accentColor,
            onPressed: _isBusy ? null : () => _pickImage(ImageSource.camera),
          ),
          _ActionButton(
            icon: Icons.crop_outlined,
            label: _isCropping ? '裁剪中...' : '开始裁剪',
            accentColor: _accentColor,
            onPressed: _sourceImage == null || _isBusy
                ? null
                : () => _cropCurrentImage(context),
          ),
          _ActionButton(
            icon: Icons.swap_horiz_outlined,
            label: '结果作源图',
            accentColor: _accentColor,
            onPressed: _croppedImage == null || _isBusy
                ? null
                : _useCroppedAsSource,
          ),
          _ActionButton(
            icon: Icons.restart_alt,
            label: '重置',
            accentColor: _accentColor,
            onPressed: _sourceImage == null && _croppedImage == null
                ? null
                : _reset,
          ),
        ],
      ),
    );
  }

  Widget _buildSettingsSection() {
    return _DemoSectionCard(
      title: '裁剪参数',
      subtitle: '常见上传头像、封面图、内容配图场景会用到这些参数。',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _ChoiceGroup<_CropRatioOption>(
            label: '裁剪比例',
            values: _CropRatioOption.values,
            selectedValue: _ratioOption,
            labelFor: (_CropRatioOption option) => option.label,
            onSelected: _isBusy
                ? null
                : (_CropRatioOption option) {
                    setState(() {
                      _ratioOption = option;
                    });
                  },
          ),
          const SizedBox(height: 16),
          _ChoiceGroup<_CropFrameStyle>(
            label: '裁剪形状',
            values: _CropFrameStyle.values,
            selectedValue: _frameStyle,
            labelFor: (_CropFrameStyle option) => option.label,
            onSelected: _isBusy
                ? null
                : (_CropFrameStyle option) {
                    setState(() {
                      _frameStyle = option;
                    });
                  },
          ),
          const SizedBox(height: 16),
          _ChoiceGroup<_CropOutputFormat>(
            label: '输出格式',
            values: _CropOutputFormat.values,
            selectedValue: _outputFormat,
            labelFor: (_CropOutputFormat option) => option.label,
            onSelected: _isBusy
                ? null
                : (_CropOutputFormat option) {
                    setState(() {
                      _outputFormat = option;
                    });
                  },
          ),
          const SizedBox(height: 16),
          _SliderTile(
            label: '压缩质量',
            valueLabel: '$_quality%',
            value: _quality.toDouble(),
            min: 40,
            max: 100,
            divisions: 12,
            onChanged: _isBusy
                ? null
                : (double value) {
                    setState(() {
                      _quality = value.round();
                    });
                  },
          ),
        ],
      ),
    );
  }

  Widget _buildPreviewSection() {
    if (_sourceImage == null && _croppedImage == null) {
      return const _EmptyStateCard(
        icon: Icons.crop_free_outlined,
        title: '还没有可裁剪的图片',
        description: '选择相册图片或拍照后，会在这里展示源图和裁剪结果。',
      );
    }

    final List<Widget> previews = <Widget>[
      _ImagePreviewCard(
        title: '源图',
        image: _sourceImage,
        accentColor: _accentColor,
      ),
      _ImagePreviewCard(
        title: '裁剪结果',
        image: _croppedImage,
        accentColor: _accentColor,
      ),
    ];

    return LayoutBuilder(
      builder: (BuildContext context, BoxConstraints constraints) {
        if (constraints.maxWidth >= 860) {
          return Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Expanded(child: previews[0]),
              const SizedBox(width: 16),
              Expanded(child: previews[1]),
            ],
          );
        }

        return Column(
          children: <Widget>[
            previews[0],
            const SizedBox(height: 16),
            previews[1],
          ],
        );
      },
    );
  }

  Future<void> _restoreLostData() async {
    try {
      final CroppedFile? recoveredCrop = await _cropper.recoverImage();
      if (recoveredCrop != null) {
        final _DemoImageData image = await _createImageDataFromCroppedFile(
          recoveredCrop,
        );
        _setStateIfMounted(() {
          _sourceImage = image;
          _croppedImage = null;
          _statusMessage = '已恢复上次被系统回收前完成的裁剪结果，可以继续作为源图裁剪。';
        });
        return;
      }

      final LostDataResponse response = await _picker.retrieveLostData();
      if (response.isEmpty) {
        return;
      }

      final List<XFile>? files = response.files;
      if (files == null || files.isEmpty) {
        final String message = response.exception?.code ?? '没有需要恢复的图片数据。';
        _setStateIfMounted(() {
          _statusMessage = message;
        });
        return;
      }

      final XFile file = files.first;
      final _DemoImageData image = await _createImageDataFromXFile(file);
      _setStateIfMounted(() {
        _sourceImage = image;
        _croppedImage = null;
        _statusMessage = '已恢复系统回收前选择的图片，可以继续打开裁剪。';
      });
    } catch (error) {
      _setStateIfMounted(() {
        _statusMessage = '恢复上次图片处理数据失败：$error';
      });
    }
  }

  Future<void> _pickImage(ImageSource source) async {
    setState(() {
      _isPicking = true;
      _statusMessage = source == ImageSource.camera
          ? '正在打开系统相机...'
          : '正在打开系统相册...';
    });

    try {
      final XFile? file = await _picker.pickImage(source: source);
      if (file == null) {
        _setStateIfMounted(() {
          _isPicking = false;
          _statusMessage = '已取消选择图片。';
        });
        return;
      }

      final _DemoImageData image = await _createImageDataFromXFile(file);
      _setStateIfMounted(() {
        _isPicking = false;
        _sourceImage = image;
        _croppedImage = null;
        _statusMessage =
            '已选中 ${image.name}，尺寸 ${image.dimensionsLabel}，可以开始裁剪。';
      });
    } catch (error) {
      _setStateIfMounted(() {
        _isPicking = false;
        _statusMessage = '选择图片失败：$error';
      });
    }
  }

  Future<void> _cropCurrentImage(BuildContext context) async {
    final _DemoImageData? sourceImage = _sourceImage;
    if (sourceImage == null) {
      return;
    }

    setState(() {
      _isCropping = true;
      _statusMessage =
          '正在打开裁剪器：${_ratioOption.label} / ${_frameStyle.label} / ${_outputFormat.label}。';
    });

    try {
      final CroppedFile? croppedFile = await _cropper.cropImage(
        sourcePath: sourceImage.path,
        maxWidth: 2048,
        maxHeight: 2048,
        aspectRatio: _ratioOption.aspectRatio,
        compressFormat: _outputFormat.format,
        compressQuality: _quality,
        uiSettings: _buildUiSettings(context),
      );

      if (croppedFile == null) {
        _setStateIfMounted(() {
          _isCropping = false;
          _statusMessage = '已取消裁剪。';
        });
        return;
      }

      await _cropper.recoverImage();
      final _DemoImageData image = await _createImageDataFromCroppedFile(
        croppedFile,
      );
      _setStateIfMounted(() {
        _isCropping = false;
        _croppedImage = image;
        _statusMessage =
            '裁剪完成，输出 ${image.name}，大小 ${image.formattedSize}，尺寸 ${image.dimensionsLabel}。';
      });
    } catch (error) {
      _setStateIfMounted(() {
        _isCropping = false;
        _statusMessage = '裁剪失败：$error';
      });
    }
  }

  List<PlatformUiSettings> _buildUiSettings(BuildContext context) {
    final Color surfaceColor = Theme.of(context).colorScheme.surface;

    return <PlatformUiSettings>[
      AndroidUiSettings(
        toolbarTitle: 'ImageCropper',
        toolbarColor: _accentColor,
        toolbarWidgetColor: Colors.white,
        backgroundColor: surfaceColor,
        activeControlsWidgetColor: _accentColor,
        cropFrameColor: Colors.white,
        cropGridColor: Colors.white70,
        initAspectRatio: _ratioOption.initialPreset,
        lockAspectRatio: _ratioOption.lockAspectRatio,
        cropStyle: _frameStyle.cropStyle,
        aspectRatioPresets: _aspectRatioPresets,
      ),
      IOSUiSettings(
        title: 'ImageCropper',
        doneButtonTitle: '完成',
        cancelButtonTitle: '取消',
        showCancelConfirmationDialog: true,
        aspectRatioLockEnabled: _ratioOption.lockAspectRatio,
        cropStyle: _frameStyle.cropStyle,
        aspectRatioPresets: _aspectRatioPresets,
      ),
      WebUiSettings(
        context: context,
        presentStyle: WebPresentStyle.dialog,
        size: const CropperSize(width: 520, height: 520),
      ),
    ];
  }

  Future<_DemoImageData> _createImageDataFromXFile(XFile file) async {
    final Uint8List bytes = await file.readAsBytes();
    final _ImageDimensions dimensions = await _readDimensions(bytes);

    return _DemoImageData(
      path: file.path,
      name: file.name.isNotEmpty ? file.name : _fileNameOf(file.path),
      bytes: bytes,
      sizeInBytes: bytes.length,
      width: dimensions.width,
      height: dimensions.height,
    );
  }

  Future<_DemoImageData> _createImageDataFromCroppedFile(
    CroppedFile file,
  ) async {
    final Uint8List bytes = await file.readAsBytes();
    final _ImageDimensions dimensions = await _readDimensions(bytes);

    return _DemoImageData(
      path: file.path,
      name: _fileNameOf(file.path),
      bytes: bytes,
      sizeInBytes: bytes.length,
      width: dimensions.width,
      height: dimensions.height,
    );
  }

  Future<_ImageDimensions> _readDimensions(Uint8List bytes) async {
    try {
      final ui.Image image = await _decodeImageFromList(bytes);
      final _ImageDimensions dimensions = _ImageDimensions(
        width: image.width,
        height: image.height,
      );
      image.dispose();
      return dimensions;
    } catch (_) {
      return const _ImageDimensions(width: 0, height: 0);
    }
  }

  Future<ui.Image> _decodeImageFromList(Uint8List bytes) {
    final Completer<ui.Image> completer = Completer<ui.Image>();
    ui.decodeImageFromList(bytes, (ui.Image image) {
      completer.complete(image);
    });
    return completer.future;
  }

  String _fileNameOf(String path) {
    final List<String> segments = path.split('/');
    return segments.lastWhere(
      (String segment) => segment.isNotEmpty,
      orElse: () => 'cropped_image.${_outputFormat.fileExtension}',
    );
  }

  void _useCroppedAsSource() {
    final _DemoImageData? croppedImage = _croppedImage;
    if (croppedImage == null) {
      return;
    }

    setState(() {
      _sourceImage = croppedImage;
      _croppedImage = null;
      _statusMessage = '已将裁剪结果作为新的源图，可以继续二次裁剪。';
    });
  }

  void _reset() {
    setState(() {
      _sourceImage = null;
      _croppedImage = null;
      _statusMessage = '已重置，可以重新选择图片。';
    });
  }

  void _setStateIfMounted(VoidCallback fn) {
    if (!mounted) {
      return;
    }
    setState(fn);
  }
}

enum _CropRatioOption {
  free(
    label: '自由',
    aspectRatio: null,
    initialPreset: CropAspectRatioPreset.original,
    lockAspectRatio: false,
  ),
  square(
    label: '1:1',
    aspectRatio: CropAspectRatio(ratioX: 1, ratioY: 1),
    initialPreset: CropAspectRatioPreset.square,
    lockAspectRatio: true,
  ),
  ratio4x3(
    label: '4:3',
    aspectRatio: CropAspectRatio(ratioX: 4, ratioY: 3),
    initialPreset: CropAspectRatioPreset.ratio4x3,
    lockAspectRatio: true,
  ),
  ratio16x9(
    label: '16:9',
    aspectRatio: CropAspectRatio(ratioX: 16, ratioY: 9),
    initialPreset: CropAspectRatioPreset.ratio16x9,
    lockAspectRatio: true,
  );

  const _CropRatioOption({
    required this.label,
    required this.aspectRatio,
    required this.initialPreset,
    required this.lockAspectRatio,
  });

  final String label;
  final CropAspectRatio? aspectRatio;
  final CropAspectRatioPresetData initialPreset;
  final bool lockAspectRatio;
}

enum _CropFrameStyle {
  rectangle('矩形', CropStyle.rectangle),
  circle('圆形', CropStyle.circle);

  const _CropFrameStyle(this.label, this.cropStyle);

  final String label;
  final CropStyle cropStyle;
}

enum _CropOutputFormat {
  jpg('JPG', 'jpg', ImageCompressFormat.jpg),
  png('PNG', 'png', ImageCompressFormat.png);

  const _CropOutputFormat(this.label, this.fileExtension, this.format);

  final String label;
  final String fileExtension;
  final ImageCompressFormat format;
}

class _CropperHeroCard extends StatelessWidget {
  const _CropperHeroCard({
    required this.statusMessage,
    required this.sourceImage,
    required this.croppedImage,
    required this.quality,
    required this.outputFormat,
    required this.accentColor,
  });

  final String statusMessage;
  final _DemoImageData? sourceImage;
  final _DemoImageData? croppedImage;
  final int quality;
  final _CropOutputFormat outputFormat;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    return _DemoSectionCard(
      title: 'image_cropper',
      subtitle: statusMessage,
      child: Wrap(
        spacing: 10,
        runSpacing: 10,
        children: <Widget>[
          _StatChip(
            label: '源图',
            value: sourceImage == null ? '未选择' : sourceImage!.formattedSize,
            accentColor: accentColor,
          ),
          _StatChip(
            label: '结果',
            value: croppedImage == null ? '待裁剪' : croppedImage!.formattedSize,
            accentColor: accentColor,
          ),
          _StatChip(label: '质量', value: '$quality%', accentColor: accentColor),
          _StatChip(
            label: '格式',
            value: outputFormat.label,
            accentColor: accentColor,
          ),
        ],
      ),
    );
  }
}

class _DemoSectionCard extends StatelessWidget {
  const _DemoSectionCard({
    required this.title,
    required this.subtitle,
    required this.child,
  });

  final String title;
  final String subtitle;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outlineVariant),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              subtitle,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
                height: 1.4,
              ),
            ),
            const SizedBox(height: 16),
            child,
          ],
        ),
      ),
    );
  }
}

class _ChoiceGroup<T extends Object> extends StatelessWidget {
  const _ChoiceGroup({
    required this.label,
    required this.values,
    required this.selectedValue,
    required this.labelFor,
    required this.onSelected,
  });

  final String label;
  final List<T> values;
  final T selectedValue;
  final String Function(T value) labelFor;
  final ValueChanged<T>? onSelected;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        _SettingLabel(text: label),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: values
              .map(
                (T value) => ChoiceChip(
                  label: Text(labelFor(value)),
                  selected: value == selectedValue,
                  onSelected: onSelected == null
                      ? null
                      : (bool selected) {
                          if (!selected) {
                            return;
                          }
                          onSelected!(value);
                        },
                ),
              )
              .toList(),
        ),
      ],
    );
  }
}

class _SliderTile extends StatelessWidget {
  const _SliderTile({
    required this.label,
    required this.valueLabel,
    required this.value,
    required this.min,
    required this.max,
    required this.divisions,
    required this.onChanged,
  });

  final String label;
  final String valueLabel;
  final double value;
  final double min;
  final double max;
  final int divisions;
  final ValueChanged<double>? onChanged;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Row(
          children: <Widget>[
            Expanded(child: _SettingLabel(text: label)),
            Text(
              valueLabel,
              style: theme.textTheme.labelLarge?.copyWith(
                color: _ImageCropperDemoViewState._accentColor,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
        Slider(
          value: value,
          min: min,
          max: max,
          divisions: divisions,
          label: valueLabel,
          activeColor: _ImageCropperDemoViewState._accentColor,
          onChanged: onChanged,
        ),
      ],
    );
  }
}

class _SettingLabel extends StatelessWidget {
  const _SettingLabel({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: Theme.of(
        context,
      ).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
    );
  }
}

class _ActionButton extends StatelessWidget {
  const _ActionButton({
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
    return OutlinedButton.icon(
      onPressed: onPressed,
      icon: Icon(icon),
      label: Text(label),
      style: OutlinedButton.styleFrom(
        foregroundColor: accentColor,
        side: BorderSide(color: accentColor.withValues(alpha: 0.45)),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
    );
  }
}

class _ImagePreviewCard extends StatelessWidget {
  const _ImagePreviewCard({
    required this.title,
    required this.image,
    required this.accentColor,
  });

  final String title;
  final _DemoImageData? image;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final _DemoImageData? image = this.image;

    return DecoratedBox(
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outlineVariant),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: theme.textTheme.titleSmall?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 12),
            AspectRatio(
              aspectRatio: 16 / 10,
              child: ClipRRect(
                borderRadius: BorderRadius.circular(6),
                child: image == null
                    ? _PreviewPlaceholder(accentColor: accentColor)
                    : Image.memory(image.bytes, fit: BoxFit.cover),
              ),
            ),
            const SizedBox(height: 12),
            if (image == null)
              Text(
                '等待裁剪结果',
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              )
            else
              _ImageMeta(image: image),
          ],
        ),
      ),
    );
  }
}

class _PreviewPlaceholder extends StatelessWidget {
  const _PreviewPlaceholder({required this.accentColor});

  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    return ColoredBox(
      color: accentColor.withValues(alpha: 0.08),
      child: Center(
        child: Icon(
          Icons.image_not_supported_outlined,
          size: 40,
          color: accentColor,
        ),
      ),
    );
  }
}

class _ImageMeta extends StatelessWidget {
  const _ImageMeta({required this.image});

  final _DemoImageData image;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          image.name,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: theme.textTheme.labelLarge?.copyWith(
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          '${image.formattedSize} · ${image.dimensionsLabel}',
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
      ],
    );
  }
}

class _EmptyStateCard extends StatelessWidget {
  const _EmptyStateCard({
    required this.icon,
    required this.title,
    required this.description,
  });

  final IconData icon;
  final String title;
  final String description;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest.withValues(
          alpha: 0.45,
        ),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: <Widget>[
            Icon(
              icon,
              size: 44,
              color: _ImageCropperDemoViewState._accentColor,
            ),
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
      ),
    );
  }
}

class _StatChip extends StatelessWidget {
  const _StatChip({
    required this.label,
    required this.value,
    required this.accentColor,
  });

  final String label;
  final String value;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: accentColor.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Text(
              label,
              style: theme.textTheme.labelSmall?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 2),
            Text(
              value,
              style: theme.textTheme.labelLarge?.copyWith(
                color: accentColor,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _DemoImageData {
  const _DemoImageData({
    required this.path,
    required this.name,
    required this.bytes,
    required this.sizeInBytes,
    required this.width,
    required this.height,
  });

  final String path;
  final String name;
  final Uint8List bytes;
  final int sizeInBytes;
  final int width;
  final int height;

  String get formattedSize {
    final double sizeInKb = sizeInBytes / 1024;
    if (sizeInKb < 1024) {
      return '${sizeInKb.toStringAsFixed(1)} KB';
    }

    final double sizeInMb = sizeInKb / 1024;
    return '${sizeInMb.toStringAsFixed(1)} MB';
  }

  String get dimensionsLabel {
    if (width == 0 || height == 0) {
      return '未知尺寸';
    }
    return '$width×$height';
  }
}

class _ImageDimensions {
  const _ImageDimensions({required this.width, required this.height});

  final int width;
  final int height;
}
