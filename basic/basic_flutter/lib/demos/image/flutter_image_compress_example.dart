import 'dart:async';
import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_image_compress/flutter_image_compress.dart' as fic;
import 'package:image_picker/image_picker.dart' as img_picker;
import 'package:path_provider/path_provider.dart';

/// flutter_image_compress
/// https://pub.dev/packages/flutter_image_compress
class FlutterImageCompressDemoPage extends StatelessWidget {
  const FlutterImageCompressDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return FlutterImageCompressDemoView(title: title);
  }
}

class FlutterImageCompressDemoView extends StatefulWidget {
  const FlutterImageCompressDemoView({super.key, required this.title});

  final String title;

  @override
  State<FlutterImageCompressDemoView> createState() =>
      _FlutterImageCompressDemoViewState();
}

class _FlutterImageCompressDemoViewState
    extends State<FlutterImageCompressDemoView> {
  static const Color _accentColor = Color(0xFF0F766E);
  static const int _defaultTargetSide = 1440;

  final img_picker.ImagePicker _picker = img_picker.ImagePicker();

  _CompressOutputFormat _outputFormat = _CompressOutputFormat.jpeg;
  _DemoImageData? _sourceImage;
  _DemoImageData? _compressedImage;
  bool _keepExif = false;
  bool _isPicking = false;
  bool _isCompressing = false;
  int _quality = 82;
  int _targetSide = _defaultTargetSide;
  String _statusMessage = '先选择一张图片，再体验 flutter_image_compress 的质量压缩与临时文件输出。';

  bool get _isBusy => _isPicking || _isCompressing;

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
        _CompressHeroCard(
          statusMessage: _statusMessage,
          accentColor: _accentColor,
          sourceImage: _sourceImage,
          compressedImage: _compressedImage,
        ),
        const SizedBox(height: 16),
        _buildActionSection(),
        const SizedBox(height: 16),
        _buildSettingsSection(),
        const SizedBox(height: 16),
        _buildResultSection(),
      ],
    );
  }

  Widget _buildActionSection() {
    return _DemoSectionCard(
      title: '快捷操作',
      subtitle: '支持从系统相册选图、打开相机拍照，并将压缩结果输出到临时目录。',
      child: Wrap(
        spacing: 12,
        runSpacing: 12,
        children: <Widget>[
          _ActionButton(
            icon: Icons.photo_library_outlined,
            label: '相册选图',
            accentColor: _accentColor,
            onPressed: _isBusy
                ? null
                : () => _pickImage(img_picker.ImageSource.gallery),
          ),
          _ActionButton(
            icon: Icons.photo_camera_outlined,
            label: '拍照',
            accentColor: _accentColor,
            onPressed: _isBusy
                ? null
                : () => _pickImage(img_picker.ImageSource.camera),
          ),
          _ActionButton(
            icon: Icons.compress_outlined,
            label: _isCompressing ? '压缩中...' : '开始压缩',
            accentColor: _accentColor,
            onPressed: _sourceImage == null || _isBusy
                ? null
                : _compressCurrentImage,
          ),
          _ActionButton(
            icon: Icons.restart_alt,
            label: '重置',
            accentColor: _accentColor,
            onPressed: _sourceImage == null && _compressedImage == null
                ? null
                : _reset,
          ),
        ],
      ),
    );
  }

  Widget _buildSettingsSection() {
    final String targetSideLabel = '${_targetSide}px';

    return _DemoSectionCard(
      title: '压缩参数',
      subtitle:
          '这里演示 `quality`、`minWidth/minHeight`、`format` 和 `keepExif` 的常见组合。',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            '输出格式',
            style: Theme.of(
              context,
            ).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: _CompressOutputFormat.values
                .map(
                  (_CompressOutputFormat format) => ChoiceChip(
                    label: Text(format.label),
                    selected: _outputFormat == format,
                    onSelected: _isBusy
                        ? null
                        : (bool selected) {
                            if (!selected) {
                              return;
                            }
                            setState(() {
                              _outputFormat = format;
                            });
                          },
                  ),
                )
                .toList(),
          ),
          const SizedBox(height: 20),
          _SliderTile(
            label: '图片质量',
            valueLabel: '$_quality%',
            value: _quality.toDouble(),
            min: 30,
            max: 100,
            divisions: 14,
            onChanged: _isBusy
                ? null
                : (double value) {
                    setState(() {
                      _quality = value.round();
                    });
                  },
          ),
          const SizedBox(height: 12),
          _SliderTile(
            label: '目标最长边',
            valueLabel: targetSideLabel,
            value: _targetSide.toDouble(),
            min: 720,
            max: 2160,
            divisions: 12,
            onChanged: _isBusy
                ? null
                : (double value) {
                    setState(() {
                      _targetSide = value.round();
                    });
                  },
          ),
          const SizedBox(height: 12),
          SwitchListTile.adaptive(
            contentPadding: EdgeInsets.zero,
            value: _keepExif,
            activeThumbColor: _accentColor,
            title: const Text('保留 EXIF'),
            subtitle: const Text('适合上传前仍需保留拍摄信息的场景。'),
            onChanged: _isBusy
                ? null
                : (bool value) {
                    setState(() {
                      _keepExif = value;
                    });
                  },
          ),
        ],
      ),
    );
  }

  Widget _buildResultSection() {
    if (_sourceImage == null) {
      return const _EmptyStateCard(
        icon: Icons.image_search_outlined,
        title: '还没有选择图片',
        description: '先从相册选择一张图，或者直接拍一张照片，再体验压缩效果。',
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        if (_compressedImage != null) ...<Widget>[
          _buildSummarySection(),
          const SizedBox(height: 16),
        ],
        _buildPreviewSection(),
      ],
    );
  }

  Widget _buildSummarySection() {
    final _DemoImageData sourceImage = _sourceImage!;
    final _DemoImageData compressedImage = _compressedImage!;
    final int savedBytes =
        sourceImage.sizeInBytes - compressedImage.sizeInBytes;
    final double reductionRatio = sourceImage.sizeInBytes == 0
        ? 0
        : (savedBytes / sourceImage.sizeInBytes * 100).clamp(-999, 999);

    return _DemoSectionCard(
      title: '压缩结果',
      subtitle: '输出文件已经保存到临时目录，可以直接拿去上传、预览或继续处理。',
      child: Wrap(
        spacing: 12,
        runSpacing: 12,
        children: <Widget>[
          _SummaryStatChip(
            label: '体积变化',
            value: _formatSizeDelta(savedBytes),
            accentColor: _accentColor,
          ),
          _SummaryStatChip(
            label: '压缩比例',
            value: '${reductionRatio.toStringAsFixed(1)}%',
            accentColor: _accentColor,
          ),
          _SummaryStatChip(
            label: '输出格式',
            value: _outputFormat.label,
            accentColor: _accentColor,
          ),
        ],
      ),
    );
  }

  Widget _buildPreviewSection() {
    final List<Widget> cards = <Widget>[
      _PreviewCard(
        title: '原图',
        accentColor: _accentColor,
        image: _sourceImage!,
      ),
      _PreviewCard(
        title: '压缩后',
        accentColor: _accentColor,
        image: _compressedImage,
      ),
    ];

    return LayoutBuilder(
      builder: (BuildContext context, BoxConstraints constraints) {
        if (constraints.maxWidth >= 860) {
          return Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Expanded(child: cards[0]),
              const SizedBox(width: 16),
              Expanded(child: cards[1]),
            ],
          );
        }

        return Column(
          children: <Widget>[cards[0], const SizedBox(height: 16), cards[1]],
        );
      },
    );
  }

  Future<void> _pickImage(img_picker.ImageSource source) async {
    setState(() {
      _isPicking = true;
      _statusMessage = source == img_picker.ImageSource.camera
          ? '正在打开系统相机...'
          : '正在打开系统相册...';
    });

    try {
      final img_picker.XFile? file = await _picker.pickImage(source: source);
      if (file == null) {
        _setStateIfMounted(() {
          _isPicking = false;
          _statusMessage = '已取消选择图片。';
        });
        return;
      }

      final _DemoImageData image = await _createImageData(
        path: file.path,
        name: file.name,
      );

      _setStateIfMounted(() {
        _isPicking = false;
        _sourceImage = image;
        _compressedImage = null;
        _statusMessage =
            '已选中 ${image.name}，当前原图大小 ${image.formattedSize}，可以直接开始压缩。';
      });
    } catch (error) {
      _setStateIfMounted(() {
        _isPicking = false;
        _statusMessage = '选择图片失败：$error';
      });
    }
  }

  Future<void> _compressCurrentImage() async {
    final _DemoImageData? sourceImage = _sourceImage;
    if (sourceImage == null) {
      return;
    }

    setState(() {
      _isCompressing = true;
      _statusMessage =
          '正在以 ${_outputFormat.label} 格式压缩，质量 $_quality%，目标最长边 ${_targetSide}px...';
    });

    try {
      final Directory temporaryDirectory = await getTemporaryDirectory();
      final Directory outputDirectory = Directory(
        '${temporaryDirectory.path}/flutter_image_compress_demo',
      );
      await outputDirectory.create(recursive: true);

      final String fileName =
          '${DateTime.now().millisecondsSinceEpoch}_$_quality.${_outputFormat.fileExtension}';
      final String targetPath = '${outputDirectory.path}/$fileName';

      final fic.XFile? compressedFile =
          await fic.FlutterImageCompress.compressAndGetFile(
            sourceImage.path,
            targetPath,
            quality: _quality,
            minWidth: _targetSide,
            minHeight: _targetSide,
            format: _outputFormat.compressFormat,
            keepExif: _keepExif,
            autoCorrectionAngle: true,
          );

      if (compressedFile == null) {
        _setStateIfMounted(() {
          _isCompressing = false;
          _statusMessage = '压缩没有返回结果文件，请换一张图片再试。';
        });
        return;
      }

      final _DemoImageData image = await _createImageData(
        path: compressedFile.path,
        name: compressedFile.name,
      );

      final int savedBytes = sourceImage.sizeInBytes - image.sizeInBytes;

      _setStateIfMounted(() {
        _isCompressing = false;
        _compressedImage = image;
        _statusMessage =
            '压缩完成，输出文件 ${image.name}，相比原图 ${_formatSizeDelta(savedBytes)}。';
      });
    } catch (error) {
      _setStateIfMounted(() {
        _isCompressing = false;
        _statusMessage = '压缩失败：$error';
      });
    }
  }

  Future<_DemoImageData> _createImageData({
    required String path,
    required String name,
  }) async {
    final File file = File(path);
    final Uint8List bytes = await file.readAsBytes();
    final _ImageDimensions dimensions = await _readDimensions(bytes);

    return _DemoImageData(
      path: path,
      name: name.isNotEmpty ? name : _fileNameOf(path),
      bytes: bytes,
      sizeInBytes: bytes.length,
      width: dimensions.width,
      height: dimensions.height,
    );
  }

  Future<_ImageDimensions> _readDimensions(Uint8List bytes) async {
    try {
      final ui.Image image = await decodeImageFromList(bytes);
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

  Future<ui.Image> decodeImageFromList(Uint8List bytes) {
    final Completer<ui.Image> completer = Completer<ui.Image>();
    ui.decodeImageFromList(bytes, (ui.Image image) {
      completer.complete(image);
    });
    return completer.future;
  }

  String _fileNameOf(String path) {
    final List<String> segments = path.split(Platform.pathSeparator);
    return segments.lastWhere((String segment) => segment.isNotEmpty);
  }

  String _formatSizeDelta(int bytes) {
    final int absBytes = bytes.abs();
    final String prefix = bytes >= 0 ? '-' : '+';
    return '$prefix${_formatBytes(absBytes)}';
  }

  String _formatBytes(int bytes) {
    final double sizeInKb = bytes / 1024;
    if (sizeInKb < 1024) {
      return '${sizeInKb.toStringAsFixed(1)} KB';
    }

    final double sizeInMb = sizeInKb / 1024;
    return '${sizeInMb.toStringAsFixed(1)} MB';
  }

  void _reset() {
    setState(() {
      _sourceImage = null;
      _compressedImage = null;
      _statusMessage = '已重置，可以重新选择图片体验压缩。';
    });
  }

  void _setStateIfMounted(VoidCallback fn) {
    if (!mounted) {
      return;
    }
    setState(fn);
  }
}

enum _CompressOutputFormat {
  jpeg('JPEG', 'jpg', fic.CompressFormat.jpeg),
  png('PNG', 'png', fic.CompressFormat.png),
  webp('WebP', 'webp', fic.CompressFormat.webp);

  const _CompressOutputFormat(
    this.label,
    this.fileExtension,
    this.compressFormat,
  );

  final String label;
  final String fileExtension;
  final fic.CompressFormat compressFormat;
}

class _CompressHeroCard extends StatelessWidget {
  const _CompressHeroCard({
    required this.statusMessage,
    required this.accentColor,
    required this.sourceImage,
    required this.compressedImage,
  });

  final String statusMessage;
  final Color accentColor;
  final _DemoImageData? sourceImage;
  final _DemoImageData? compressedImage;

  @override
  Widget build(BuildContext context) {
    final int selectedCount = sourceImage == null ? 0 : 1;
    final int resultCount = compressedImage == null ? 0 : 1;

    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: <Color>[
            accentColor.withValues(alpha: 0.18),
            accentColor.withValues(alpha: 0.06),
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: accentColor.withValues(alpha: 0.20)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            _PackageBadge(accentColor: accentColor),
            const SizedBox(height: 16),
            Text(
              '适合上传前压缩、生成临时文件、调节质量和尺寸控制的图片处理场景。',
              style: Theme.of(
                context,
              ).textTheme.bodyLarge?.copyWith(height: 1.6),
            ),
            const SizedBox(height: 14),
            Text(
              statusMessage,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Theme.of(context).colorScheme.onSurfaceVariant,
                height: 1.5,
              ),
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: <Widget>[
                _SummaryStatChip(
                  label: '已选图片',
                  value: '$selectedCount 张',
                  accentColor: accentColor,
                ),
                _SummaryStatChip(
                  label: '输出结果',
                  value: '$resultCount 张',
                  accentColor: accentColor,
                ),
                _SummaryStatChip(
                  label: '返回类型',
                  value: 'XFile?',
                  accentColor: accentColor,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _PackageBadge extends StatelessWidget {
  const _PackageBadge({required this.accentColor});

  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: accentColor.withValues(alpha: 0.24)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        child: Text(
          'flutter_image_compress',
          style: Theme.of(context).textTheme.labelLarge?.copyWith(
            fontWeight: FontWeight.w700,
            color: accentColor,
          ),
        ),
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
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0x140A2533)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120A2533),
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(18),
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
                height: 1.5,
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
    return FilledButton.icon(
      style: FilledButton.styleFrom(
        backgroundColor: accentColor,
        disabledBackgroundColor: accentColor.withValues(alpha: 0.24),
        foregroundColor: Colors.white,
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      ),
      onPressed: onPressed,
      icon: Icon(icon),
      label: Text(label),
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
            Expanded(
              child: Text(
                label,
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
            Text(
              valueLabel,
              style: theme.textTheme.labelLarge?.copyWith(
                color: theme.colorScheme.primary,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
        Slider(
          value: value.clamp(min, max),
          min: min,
          max: max,
          divisions: divisions,
          label: valueLabel,
          onChanged: onChanged,
        ),
      ],
    );
  }
}

class _SummaryStatChip extends StatelessWidget {
  const _SummaryStatChip({
    required this.label,
    required this.value,
    required this.accentColor,
  });

  final String label;
  final String value;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: accentColor.withValues(alpha: 0.18)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Text(
              label,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: Theme.of(context).colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              value,
              style: Theme.of(context).textTheme.titleSmall?.copyWith(
                fontWeight: FontWeight.w700,
                color: accentColor,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PreviewCard extends StatelessWidget {
  const _PreviewCard({
    required this.title,
    required this.accentColor,
    required this.image,
  });

  final String title;
  final Color accentColor;
  final _DemoImageData? image;

  @override
  Widget build(BuildContext context) {
    return _DemoSectionCard(
      title: title,
      subtitle: image == null ? '完成压缩后，这里会展示处理后的图片预览和文件信息。' : '当前图片信息与输出路径如下。',
      child: image == null
          ? const _EmptyStateCard(
              icon: Icons.auto_awesome_motion_outlined,
              title: '暂无压缩结果',
              description: '调整参数后点击“开始压缩”，这里会展示输出文件。',
              isEmbedded: true,
            )
          : Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                ClipRRect(
                  borderRadius: BorderRadius.circular(20),
                  child: AspectRatio(
                    aspectRatio: image!.aspectRatio,
                    child: Image.memory(image!.bytes, fit: BoxFit.cover),
                  ),
                ),
                const SizedBox(height: 14),
                Wrap(
                  spacing: 12,
                  runSpacing: 12,
                  children: <Widget>[
                    _MetaItem(label: '文件名', value: image!.name),
                    _MetaItem(label: '文件大小', value: image!.formattedSize),
                    _MetaItem(label: '尺寸', value: image!.dimensionLabel),
                    _MetaItem(label: '文件路径', value: image!.path),
                  ],
                ),
                const SizedBox(height: 14),
                DecoratedBox(
                  decoration: BoxDecoration(
                    color: accentColor.withValues(alpha: 0.08),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: Text(
                      '适合上传前直接使用：compressAndGetFile 会把结果写入临时目录，并返回 XFile 供后续处理。',
                      style: Theme.of(
                        context,
                      ).textTheme.bodyMedium?.copyWith(height: 1.5),
                    ),
                  ),
                ),
              ],
            ),
    );
  }
}

class _MetaItem extends StatelessWidget {
  const _MetaItem({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return SizedBox(
      width: 260,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            label,
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            value,
            style: theme.textTheme.bodyMedium?.copyWith(
              fontWeight: FontWeight.w600,
              height: 1.4,
            ),
          ),
        ],
      ),
    );
  }
}

class _EmptyStateCard extends StatelessWidget {
  const _EmptyStateCard({
    required this.icon,
    required this.title,
    required this.description,
    this.isEmbedded = false,
  });

  final IconData icon;
  final String title;
  final String description;
  final bool isEmbedded;

  @override
  Widget build(BuildContext context) {
    final Widget content = DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0x140A2533)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(28),
        child: Column(
          children: <Widget>[
            Icon(icon, size: 42, color: const Color(0xFF64748B)),
            const SizedBox(height: 14),
            Text(
              title,
              style: Theme.of(
                context,
              ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 8),
            Text(
              description,
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Theme.of(context).colorScheme.onSurfaceVariant,
                height: 1.5,
              ),
            ),
          ],
        ),
      ),
    );

    if (isEmbedded) {
      return content;
    }

    return Padding(padding: const EdgeInsets.only(bottom: 8), child: content);
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

  String get dimensionLabel =>
      width > 0 && height > 0 ? '$width x $height' : '-';

  String get formattedSize {
    final double sizeInKb = sizeInBytes / 1024;
    if (sizeInKb < 1024) {
      return '${sizeInKb.toStringAsFixed(1)} KB';
    }

    final double sizeInMb = sizeInKb / 1024;
    return '${sizeInMb.toStringAsFixed(1)} MB';
  }

  double get aspectRatio {
    if (width <= 0 || height <= 0) {
      return 1;
    }
    return width / height;
  }
}

class _ImageDimensions {
  const _ImageDimensions({required this.width, required this.height});

  final int width;
  final int height;
}
