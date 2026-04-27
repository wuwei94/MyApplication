import 'dart:async';
import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_luban/flutter_luban.dart';
import 'package:image_picker/image_picker.dart' as img_picker;
import 'package:path_provider/path_provider.dart';

/// flutter_luban
/// https://pub.dev/packages/flutter_luban
class FlutterLubanDemoPage extends StatelessWidget {
  const FlutterLubanDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return FlutterLubanDemoView(title: title);
  }
}

class FlutterLubanDemoView extends StatefulWidget {
  const FlutterLubanDemoView({super.key, required this.title});

  final String title;

  @override
  State<FlutterLubanDemoView> createState() => _FlutterLubanDemoViewState();
}

class _FlutterLubanDemoViewState extends State<FlutterLubanDemoView> {
  static const Color _accentColor = Color(0xFF2F6F9F);

  final img_picker.ImagePicker _picker = img_picker.ImagePicker();

  _DemoImageData? _sourceImage;
  _DemoImageData? _compressedImage;
  _LubanModeOption _mode = _LubanModeOption.auto;
  bool _useCache = false;
  bool _isPicking = false;
  bool _isCompressing = false;
  int _quality = 80;
  int _step = 6;
  String _statusMessage = '先选择一张 JPG 或 PNG 图片，再体验 flutter_luban 的智能压缩算法。';

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
        _LubanHeroCard(
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
      subtitle: '选择图片后会压缩到临时目录，Luban 根据图片比例和大小自动计算目标尺寸。',
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
                : () {
                    unawaited(_pickImage(img_picker.ImageSource.gallery));
                  },
          ),
          _ActionButton(
            icon: Icons.photo_camera_outlined,
            label: '拍照',
            accentColor: _accentColor,
            onPressed: _isBusy
                ? null
                : () {
                    unawaited(_pickImage(img_picker.ImageSource.camera));
                  },
          ),
          _ActionButton(
            icon: Icons.compress_outlined,
            label: _isCompressing ? '压缩中...' : '开始压缩',
            accentColor: _accentColor,
            onPressed: _sourceImage == null || _isBusy
                ? null
                : () {
                    unawaited(_compressCurrentImage());
                  },
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
    return _DemoSectionCard(
      title: '压缩参数',
      subtitle: '`quality` 是初始质量，`step` 控制质量搜索步长，模式决定从大到小或从小到大逼近目标体积。',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            '压缩模式',
            style: Theme.of(
              context,
            ).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: _LubanModeOption.values
                .map(
                  (_LubanModeOption mode) => ChoiceChip(
                    label: Text(mode.label),
                    selected: _mode == mode,
                    onSelected: _isBusy
                        ? null
                        : (bool selected) {
                            if (!selected) {
                              return;
                            }
                            setState(() {
                              _mode = mode;
                            });
                          },
                  ),
                )
                .toList(),
          ),
          const SizedBox(height: 20),
          _SliderTile(
            label: '初始质量',
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
          const SizedBox(height: 12),
          _SliderTile(
            label: '搜索步长',
            valueLabel: '$_step',
            value: _step.toDouble(),
            min: 1,
            max: 12,
            divisions: 11,
            onChanged: _isBusy
                ? null
                : (double value) {
                    setState(() {
                      _step = value.round();
                    });
                  },
          ),
          const SizedBox(height: 12),
          SwitchListTile.adaptive(
            contentPadding: EdgeInsets.zero,
            value: _useCache,
            activeThumbColor: _accentColor,
            title: const Text('复用同名缓存'),
            subtitle: const Text('开启后相同原图文件名会直接返回已有 luban 输出文件。'),
            onChanged: _isBusy
                ? null
                : (bool value) {
                    setState(() {
                      _useCache = value;
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
        description: '请选择 JPG 或 PNG 图片。部分相册 HEIC 图片需要先转换格式后再压缩。',
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
      subtitle: '输出文件已保存到应用临时目录，可用于上传前预处理或继续预览。',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Wrap(
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
                label: '模式',
                value: _mode.label,
                accentColor: _accentColor,
              ),
            ],
          ),
          const SizedBox(height: 14),
          Text(
            '输出路径',
            style: Theme.of(
              context,
            ).textTheme.labelLarge?.copyWith(fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 6),
          SelectableText(
            compressedImage.path,
            style: Theme.of(context).textTheme.bodySmall,
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
        title: 'Luban 压缩后',
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
            '已选中 ${image.name}，当前原图大小 ${image.formattedSize}，可以开始压缩。';
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
      _statusMessage = '正在使用 ${_mode.label} 模式压缩，初始质量 $_quality%，步长 $_step...';
    });

    try {
      final Directory temporaryDirectory = await getTemporaryDirectory();
      final Directory outputDirectory = Directory(
        '${temporaryDirectory.path}/flutter_luban_demo',
      );
      await outputDirectory.create(recursive: true);

      final CompressObject compressObject = CompressObject(
        imageFile: File(sourceImage.path),
        targetPath: outputDirectory.path,
        mode: _mode.compressMode,
        useCache: _useCache,
        quality: _quality,
        step: _step,
        autoRatio: true,
      );

      final String? outputPath = await Luban.compressImage(compressObject);
      if (outputPath == null || outputPath.isEmpty) {
        _setStateIfMounted(() {
          _isCompressing = false;
          _statusMessage = '压缩没有返回结果文件，请换一张图片再试。';
        });
        return;
      }

      final _DemoImageData image = await _createImageData(
        path: outputPath,
        name: _fileNameOf(outputPath),
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

enum _LubanModeOption {
  auto('Auto', CompressMode.AUTO),
  large2Small('Large2Small', CompressMode.LARGE2SMALL),
  small2Large('Small2Large', CompressMode.SMALL2LARGE);

  const _LubanModeOption(this.label, this.compressMode);

  final String label;
  final CompressMode compressMode;
}

class _LubanHeroCard extends StatelessWidget {
  const _LubanHeroCard({
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
    final ThemeData theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: accentColor,
        borderRadius: BorderRadius.circular(20),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x1A17324D),
            blurRadius: 24,
            offset: Offset(0, 12),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.16),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: const Icon(
                    Icons.photo_filter_outlined,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text(
                        'Luban 图片压缩',
                        style: theme.textTheme.titleLarge?.copyWith(
                          color: Colors.white,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Dart 侧压缩，无平台通道依赖',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: Colors.white.withValues(alpha: 0.78),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 18),
            Text(
              statusMessage,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: Colors.white,
                height: 1.45,
              ),
            ),
            const SizedBox(height: 18),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: <Widget>[
                _HeroMetricPill(label: '已选图片', value: '$selectedCount'),
                _HeroMetricPill(label: '压缩结果', value: '$resultCount'),
              ],
            ),
          ],
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

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFE1E7EE)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x0F17324D),
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              subtitle,
              style: theme.textTheme.bodySmall?.copyWith(
                color: const Color(0xFF667788),
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
      onPressed: onPressed,
      icon: Icon(icon),
      label: Text(label),
      style: FilledButton.styleFrom(
        backgroundColor: accentColor,
        foregroundColor: Colors.white,
        disabledBackgroundColor: const Color(0xFFE4EAF1),
        disabledForegroundColor: const Color(0xFF7D8791),
        minimumSize: const Size(128, 46),
      ),
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
                style: theme.textTheme.bodyMedium?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
            Text(
              valueLabel,
              style: theme.textTheme.labelLarge?.copyWith(
                color: const Color(0xFF2F6F9F),
                fontWeight: FontWeight.w800,
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
          onChanged: onChanged,
        ),
      ],
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
    final ThemeData theme = Theme.of(context);
    final _DemoImageData? currentImage = image;

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFE1E7EE)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Container(
                  width: 36,
                  height: 36,
                  decoration: BoxDecoration(
                    color: accentColor.withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(
                    Icons.image_outlined,
                    color: accentColor,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    title,
                    style: theme.textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            ClipRRect(
              borderRadius: BorderRadius.circular(14),
              child: AspectRatio(
                aspectRatio: 16 / 10,
                child: currentImage == null
                    ? const _PreviewPlaceholder()
                    : Image.memory(currentImage.bytes, fit: BoxFit.cover),
              ),
            ),
            const SizedBox(height: 12),
            if (currentImage == null)
              Text(
                '等待压缩结果',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: const Color(0xFF667788),
                ),
              )
            else
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    currentImage.name,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${currentImage.formattedSize}  ·  ${currentImage.dimensionLabel}',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: const Color(0xFF667788),
                    ),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}

class _PreviewPlaceholder extends StatelessWidget {
  const _PreviewPlaceholder();

  @override
  Widget build(BuildContext context) {
    return Container(
      color: const Color(0xFFF1F5F9),
      child: const Center(
        child: Icon(
          Icons.image_not_supported_outlined,
          color: Color(0xFF8FA1B3),
          size: 36,
        ),
      ),
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
    final ThemeData theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: accentColor.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: accentColor.withValues(alpha: 0.14)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Text(
            label,
            style: theme.textTheme.labelSmall?.copyWith(
              color: const Color(0xFF667788),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            value,
            style: theme.textTheme.titleSmall?.copyWith(
              color: accentColor,
              fontWeight: FontWeight.w800,
            ),
          ),
        ],
      ),
    );
  }
}

class _HeroMetricPill extends StatelessWidget {
  const _HeroMetricPill({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.14),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Text(
            label,
            style: theme.textTheme.labelMedium?.copyWith(
              color: Colors.white.withValues(alpha: 0.82),
            ),
          ),
          const SizedBox(width: 8),
          Text(
            value,
            style: theme.textTheme.labelLarge?.copyWith(
              color: Colors.white,
              fontWeight: FontWeight.w800,
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
  });

  final IconData icon;
  final String title;
  final String description;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFE1E7EE)),
      ),
      child: Column(
        children: <Widget>[
          Icon(icon, color: const Color(0xFF8FA1B3), size: 42),
          const SizedBox(height: 12),
          Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            description,
            textAlign: TextAlign.center,
            style: theme.textTheme.bodySmall?.copyWith(
              color: const Color(0xFF667788),
              height: 1.4,
            ),
          ),
        ],
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

  String get dimensionLabel {
    if (width == 0 || height == 0) {
      return '尺寸未知';
    }
    return '${width}x$height';
  }
}

class _ImageDimensions {
  const _ImageDimensions({required this.width, required this.height});

  final int width;
  final int height;
}
