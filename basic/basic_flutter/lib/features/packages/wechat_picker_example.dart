import 'package:basic_flutter/features/packages/widgets/media_picker_example_widgets.dart';
import 'package:flutter/material.dart';
import 'package:wechat_assets_picker/wechat_assets_picker.dart';
import 'package:wechat_camera_picker/wechat_camera_picker.dart';

/// WeChat Assets Picker
/// https://pub.dev/packages/wechat_assets_picker
/// WeChat Camera Picker
/// https://pub.dev/packages/wechat_camera_picker
class WechatPickerExample extends StatelessWidget {
  const WechatPickerExample({super.key});

  @override
  Widget build(BuildContext context) {
    return const WechatPickerRoute(title: 'WechatPicker 图片选择示例');
  }
}

class WechatPickerRoute extends StatefulWidget {
  const WechatPickerRoute({super.key, required this.title});

  final String title;

  @override
  State<WechatPickerRoute> createState() => _WechatPickerRouteState();
}

class _WechatPickerRouteState extends State<WechatPickerRoute> {
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
        accentColor: _WechatPickerRouteState._accentColor,
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
        accentColor: _WechatPickerRouteState._accentColor,
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
