import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';
import 'package:flutter_vap_plus/flutter_vap_plus.dart';
import 'package:material_ui/material_ui.dart';
import 'package:path_provider/path_provider.dart';

/// VAP Alpha 视频动画播放与融合动画（社区原生封装）
/// https://pub.dev/packages/flutter_vap_plus
///
/// 核心机制与避坑点：
/// 1. 官方无 Dart 运行时：VAP 只有原生播放器，Flutter 侧靠 `AndroidView`
///    （viewType `flutter_vap`）把腾讯开源版 `AnimView` 嵌进图层，Dart 侧只有
///    MethodChannel 控制面；插件 Android 依赖 `io.github.tencent:vap:2.0.28`，
///    与 `module_anim` 同坐标，Gradle 直接去重，双端跑同一套解码器。
/// 2. 控制面比 Android 窄：无 `setLoop`、无 `OnResourceClickListener`，
///    故「循环次数」与「融合资源点击」两条 Android 轴在此无入口，不做伪装按钮；
///    播放事件只有 onStart / onComplete / onFailed / onDestroy 四个方法名回调。
/// 3. 融合素材须走缓存文件：`playAsset` 把 key 拼成 `flutter_assets/...` 用
///    AssetInputStream 读，vapx.mp4 的 vapc 配置存在截断风险（同 Android 侧结论），
///    因此融合素材先在 Dart 侧落盘再 `playPath`。
/// 4. 融合图片槽只认本地文件路径：原生侧走 `BitmapFactory.decodeFile`，
///    头像素材同样要先从 assets 拷成文件；文字槽直接传字符串。
/// 5. 控制器延迟且不可变：`onControllerCreated` 由插件硬编码延迟 1s 给出；
///    `scaleType` 只在 PlatformView 创建参数里读一次，切档必须重建视图，
///    重建期间旧控制器失效。
/// 6. 播放互斥与接力：`play` 的 Future 由原生 `onComplete` 完成，播放中再次起播会被
///    解码器忽略，故切换素材走「挂起目标 + stopPlay + 完成后接力」，与 Android 页同构；
///    `stop` 也会触发 onComplete，被重建的旧视图若不再回调，则由插件 20s 兜底超时收尾。
///
/// 素材与 Android 侧同源：demo.mp4 / vapx.mp4 / 融合头像取自 `module_anim`。
class VapDemoPage extends BasicImagePage {
  const VapDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<VapDemoPage> createState() => _VapDemoPageState();
}

class _VapDemoPageState extends BasicImagePageState<VapDemoPage> {
  static const String _demoAsset = 'assets/anim/vap/demo.mp4';
  static const String _mixAsset = 'assets/anim/vap/vapx.mp4';
  static const String _avatarAAsset = 'assets/anim/vap/avatar_a.png';
  static const String _avatarBAsset = 'assets/anim/vap/avatar_b.png';

  /// vapx.mp4 融合配置（vapc BOX）中声明的图片槽 / 文字槽 srcTag
  static const String _tagImage = '[sImg1]';
  static const String _tagText = '[sTxt1]';

  VapController? _controller;

  /// 当前素材，头像与缩放档切换后按它重播
  String _currentAsset = _demoAsset;

  VapScaleFit _fit = VapScaleFit.FIT_CENTER;

  /// 重建 PlatformView 的序号：scaleType 只在创建参数里读取
  int _viewSeq = 0;

  bool _useAvatarA = true;
  int _mixPlayCount = 0;

  /// 起播在途标记与接力目标：仅主线程读写
  bool _playing = false;
  String? _pendingAsset;

  @override
  List<String> buildList() => const <String>[
    '1. 播放 Alpha 视频（demo.mp4）',
    '2. 播放融合动画（vapx.mp4）',
    '3. 切换融合头像（示例 A ↔ B）',
    '4. 切换缩放模式（FIT_CENTER / CENTER_CROP / FIT_XY）',
    '5. 停止播放',
  ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        unawaited(_play(_demoAsset));
      case 1:
        unawaited(_play(_mixAsset));
      case 2:
        _switchAvatar();
      case 3:
        _switchFit();
      case 4:
        _stop();
    }
  }

  /// 起播素材：融合动画带 srcTag 供给，其余直接播 asset
  Future<void> _play(String asset) async {
    final VapController? controller = _controller;
    if (controller == null) {
      showToast('VapView 尚未就绪');
      return;
    }

    // 播放中再次起播会被解码器忽略：先挂起目标素材并 stop，待 onComplete 接力
    if (_playing) {
      _pendingAsset = asset;
      controller.stop();
      return;
    }

    final int gen = _viewSeq;
    _currentAsset = asset;
    _playing = true;
    try {
      if (asset == _mixAsset) {
        final File video = await _copyToCache(asset);
        final File avatar = await _copyToCache(
          _useAvatarA ? _avatarAAsset : _avatarBAsset,
        );
        _mixPlayCount++;
        await controller.playPath(
          video.path,
          fetchResources: <FetchResourceModel>[
            FetchResourceModel(tag: _tagImage, resource: avatar.path),
            FetchResourceModel(
              tag: _tagText,
              resource: '恭喜 No.$_mixPlayCount 用户升神',
            ),
          ],
        );
      } else {
        await controller.playAsset(asset);
      }
    } on TimeoutException {
      // stop 与视图重建都可能不回 onComplete，插件 20s 兜底超时即此路径，不作为失败上报
    } catch (error) {
      showToast('播放失败：$error');
    } finally {
      // 视图未换代才收尾：旧控制器的超时不参与接力，避免误停新起播
      if (gen == _viewSeq) {
        _playing = false;
        final String? next = _pendingAsset;
        _pendingAsset = null;
        if (next != null && mounted) {
          unawaited(_play(next));
        }
      }
    }
  }

  /// 素材落盘：assets 不是文件路径，融合素材与头像都必须先拷到缓存目录
  Future<File> _copyToCache(String asset) async {
    final ByteData data = await rootBundle.load(asset);
    final Directory cacheDir = await getTemporaryDirectory();
    final File target = File('${cacheDir.path}/${asset.split('/').last}');
    return target.writeAsBytes(data.buffer.asUint8List(), flush: true);
  }

  void _switchAvatar() {
    _useAvatarA = !_useAvatarA;
    final String name = _useAvatarA ? 'A（橙色圆脸）' : 'B（蓝色方脸）';
    if (_currentAsset == _mixAsset) {
      unawaited(_play(_mixAsset));
      showToast('已切换头像 $name 并重播融合动画');
    } else {
      showToast('已选择头像 $name，播放融合动画时生效');
    }
  }

  void _switchFit() {
    _fit = VapScaleFit.values[(_fit.index + 1) % VapScaleFit.values.length];
    // 换档即换视图：旧控制器随 PlatformView 一起作废，在途状态一并清零
    _controller = null;
    _playing = false;
    _pendingAsset = null;
    setState(() => _viewSeq++);
    showToast('缩放模式：${_fit.name}');
  }

  void _stop() {
    final VapController? controller = _controller;
    if (controller == null) {
      showToast('VapView 尚未就绪');
      return;
    }

    _pendingAsset = null;
    controller.stop();
    showToast('已停止播放');
  }

  @override
  Widget buildPreview() {
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Text(
            'Local VAP sample',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          SelectableText(
            '$_currentAsset · ${_fit.name}',
            style: Theme.of(context).textTheme.bodySmall,
          ),
          const SizedBox(height: 16),
          Expanded(child: _buildCanvas(context)),
        ],
      ),
    );
  }

  Widget _buildCanvas(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
      ),
      child: Center(
        // 原生 AnimView 是 TextureView，缩放由外层尺寸约束，故用包一层尺寸而非定宽定高
        child: SizedBox(
          width: 280,
          height: 280,
          child: VapView(
            key: ValueKey<int>(_viewSeq),
            fit: _fit,
            onControllerCreated: _onControllerCreated,
          ),
        ),
      ),
    );
  }

  /// 平台视图创建后插件固定延迟 1s 才回调控制器，就绪即按 Android 页习惯自动起播
  void _onControllerCreated(VapController controller) {
    if (!mounted) {
      controller.dispose();
      return;
    }

    setState(() => _controller = controller);
    unawaited(_play(_currentAsset));
  }
}
