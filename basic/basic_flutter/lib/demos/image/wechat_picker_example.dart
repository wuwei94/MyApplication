import 'package:flutter/material.dart';
import 'package:wechat_assets_picker/wechat_assets_picker.dart';
import 'package:wechat_camera_picker/wechat_camera_picker.dart';

/// WeChat Assets Picker
/// https://pub.dev/packages/wechat_assets_picker
/// WeChat Camera Picker
/// https://pub.dev/packages/wechat_camera_picker
class WechatPickerDemoPage extends StatelessWidget {
  const WechatPickerDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return WechatPickerDemoView(title: title);
  }
}

class WechatPickerDemoView extends StatefulWidget {
  const WechatPickerDemoView({super.key, required this.title});

  final String title;

  @override
  State<WechatPickerDemoView> createState() => _WechatPickerDemoViewState();
}

class _WechatPickerDemoViewState extends State<WechatPickerDemoView> {
  static const Color _accentColor = Color(0xFF1C8A63);

  bool _isLoading = false;
  String _statusMessage = '可以从微信相册选择图片，也可以直接拍照。';
  List<AssetEntity> _selectedAssets = <AssetEntity>[];

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
          title: 'wechat_picker',
          description: _statusMessage,
          accentColor: _accentColor,
          countLabel: '已选 ${_selectedAssets.length} 张',
        ),
        const SizedBox(height: 16),
        const MediaPickerSectionHeader(
          title: '快捷操作',
          subtitle: '体验微信风格拍照、单图选择和多图选择。',
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
              onPressed: _selectedAssets.isEmpty ? null : _clearSelection,
            ),
          ],
        ),
        const SizedBox(height: 20),
        MediaPickerSectionHeader(
          title: '结果预览',
          subtitle: _selectedAssets.isEmpty
              ? '选择或拍照后会在这里展示缩略图。'
              : '当前共 ${_selectedAssets.length} 张图片。',
        ),
        _SelectedAssetsSection(
          isLoading: _isLoading,
          selectedAssets: _selectedAssets,
        ),
      ],
    );
  }

  Future<void> _pickSingleImage() async {
    await _openPicker(
      pickerConfig: AssetPickerConfig(
        maxAssets: 1,
        requestType: RequestType.image,
        specialPickerType: SpecialPickerType.noPreview,
        selectedAssets: _selectedAssets,
        themeColor: const Color(0xFF1C8A63),
      ),
      successMessage: '已通过微信风格选择器选中 1 张图片。',
    );
  }

  Future<void> _pickMultipleImages() async {
    await _openPicker(
      pickerConfig: AssetPickerConfig(
        maxAssets: 9,
        requestType: RequestType.image,
        selectedAssets: _selectedAssets,
        themeColor: const Color(0xFF1C8A63),
      ),
      successMessage: '已更新图片选择结果。',
    );
  }

  Future<void> _openPicker({
    required AssetPickerConfig pickerConfig,
    required String successMessage,
  }) async {
    setState(() {
      _isLoading = true;
      _statusMessage = '正在打开选择器...';
    });

    try {
      final List<AssetEntity>? assets = await AssetPicker.pickAssets(
        context,
        pickerConfig: pickerConfig,
      );

      if (!mounted) {
        return;
      }

      if (assets == null || assets.isEmpty) {
        setState(() {
          _isLoading = false;
          _statusMessage = '已取消选择。';
        });
        return;
      }

      setState(() {
        _isLoading = false;
        _selectedAssets = assets;
        _statusMessage = successMessage;
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _isLoading = false;
        _statusMessage = '打开选择器失败：$error';
      });
    }
  }

  Future<void> _pickFromCamera() async {
    setState(() {
      _isLoading = true;
      _statusMessage = '正在打开微信风格相机...';
    });

    try {
      final AssetEntity? asset = await CameraPicker.pickFromCamera(
        context,
        pickerConfig: const CameraPickerConfig(
          enableRecording: false,
          shouldDeletePreviewFile: true,
        ),
      );

      if (!mounted) {
        return;
      }

      if (asset == null) {
        setState(() {
          _isLoading = false;
          _statusMessage = '已取消拍照。';
        });
        return;
      }

      setState(() {
        _isLoading = false;
        _selectedAssets = _mergeAssets(<AssetEntity>[
          asset,
          ..._selectedAssets,
        ]);
        _statusMessage = '已使用微信风格相机拍摄 1 张图片。';
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _isLoading = false;
        _statusMessage = '打开微信风格相机失败：$error';
      });
    }
  }

  List<AssetEntity> _mergeAssets(List<AssetEntity> assets) {
    final Map<String, AssetEntity> uniqueAssets = <String, AssetEntity>{};

    for (final AssetEntity asset in assets) {
      uniqueAssets[asset.id] = asset;
    }

    return uniqueAssets.values.toList();
  }

  void _clearSelection() {
    setState(() {
      _selectedAssets = <AssetEntity>[];
      _statusMessage = '已清空当前选择。';
    });
  }
}

class _SelectedAssetsSection extends StatelessWidget {
  const _SelectedAssetsSection({
    required this.isLoading,
    required this.selectedAssets,
  });

  final bool isLoading;
  final List<AssetEntity> selectedAssets;

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const MediaPickerLoadingState();
    }

    if (selectedAssets.isEmpty) {
      return const MediaPickerEmptyState(
        icon: Icons.perm_media_outlined,
        title: '还没有选择图片',
        description: '可以先体验微信拍照，或者从相册中单选、多选图片。',
        accentColor: _WechatPickerDemoViewState._accentColor,
      );
    }

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: selectedAssets.length,
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 12,
        crossAxisSpacing: 12,
        childAspectRatio: 0.88,
      ),
      itemBuilder: (BuildContext context, int index) {
        final AssetEntity asset = selectedAssets[index];
        return _SelectedAssetTile(asset: asset, index: index);
      },
    );
  }
}

class _SelectedAssetTile extends StatelessWidget {
  const _SelectedAssetTile({required this.asset, required this.index});

  final AssetEntity asset;
  final int index;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return MediaPickerTileFrame(
      badge: MediaPickerIndexBadge(
        index: index + 1,
        accentColor: _WechatPickerDemoViewState._accentColor,
      ),
      footer: MediaPickerFooter(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Text(
              asset.title?.isNotEmpty == true
                  ? asset.title!
                  : '图片 ${index + 1}',
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: theme.textTheme.labelLarge?.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              '${asset.width} x ${asset.height}',
              style: theme.textTheme.bodySmall?.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
      child: AssetEntityImage(
        asset,
        isOriginal: false,
        thumbnailSize: const ThumbnailSize.square(320),
        fit: BoxFit.cover,
      ),
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
