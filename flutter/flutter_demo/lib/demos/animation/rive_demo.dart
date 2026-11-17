import 'dart:async';

import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';
import 'package:material_ui/material_ui.dart';
import 'package:rive/rive.dart' as rive;

/// Rive 矢量动画与状态机播放（0.14.x 新运行时）
/// https://pub.dev/packages/rive
///
/// 核心机制与避坑点：
/// 1. 素材与 Android 侧同源：装载的是 `RiveActivity` 用的同一份 skills.riv
///    （默认画板 + "Designer's Test" 状态机），双端由同一 C++ 运行时解析，画面对齐。
/// 2. 控制器构造即要求状态机：`RiveWidgetController` 没有「只播线性动画」的入口，
///    动画分支由状态机 Entry 状态承载；找不到目标状态机时抛 `RiveStateMachineException`。
/// 3. 程序化写输入已废弃：新运行时把 `StateMachine.number/boolean/trigger` 标注
///    `@Deprecated` 并指向 Data Binding，而 skills.riv 未导出 view model，
///    故 Level（0/1/2）改由素材内按钮的指针监听驱动 —— 点击画板中的
///    Beginner / Intermediate / Expert 即切档，页面不提供伪装的输入项。
/// 4. 指针命中内建：`RiveWidget` 把 hit test 透传给状态机，画布本身即交互面，
///    预览区可操作是素材能力，不属于「入口伪装」。
/// 5. 暂停语义在控制器而非视图：`controller.active` 关闭后停止推进与绘制，
///    `RiveWidget` 无 pause/play 方法；复位无公开 `Artboard.reset`，只能重建控制器。
/// 6. 渲染轨道可选：`Factory.rive` 走 native 纹理（复杂图形、Vector Feathering 仅此轨道），
///    `Factory.flutter` 直接绘入 Flutter 画布（图形少、需与其他 Flutter 内容交织时用）；
///    切换需重新解码文件，`File` 与工厂绑定。
/// 7. native 库在构建期下载：`rive_native` 拉取预编译动态库，`RiveNative.init()`
///    首次解码时也会自动调用，显式前置可避免首帧抖动。
class RiveDemoPage extends BasicImagePage {
  const RiveDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<RiveDemoPage> createState() => _RiveDemoPageState();
}

class _RiveDemoPageState extends BasicImagePageState<RiveDemoPage> {
  static const String _sampleAsset = 'assets/anim/rive/skills.riv';

  /// 状态机名与数字输入 Level 均为设计侧命名，字符串须与素材一致
  static const String _stateMachineName = "Designer's Test";

  rive.File? _file;
  rive.RiveWidgetController? _controller;
  Object? _loadError;
  bool _isLoading = true;

  /// true = Rive Renderer（native 纹理），false = Flutter Renderer（Skia/Impeller）
  bool _useRiveRenderer = true;

  rive.Factory get _factory =>
      _useRiveRenderer ? rive.Factory.rive : rive.Factory.flutter;

  @override
  List<String> buildList() => const <String>[
    '1. 播放状态机（Designer\'s Test）',
    '2. 暂停推进',
    '3. 继续推进',
    '4. 复位状态机',
    '5. 切换渲染轨道（Rive ↔ Flutter）',
  ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _playStateMachine();
      case 1:
        _setActive(false);
      case 2:
        _setActive(true);
      case 3:
        _resetStateMachine();
      case 4:
        unawaited(_switchRenderer());
    }
  }

  @override
  void initState() {
    super.initState();
    unawaited(_loadFile());
  }

  @override
  void dispose() {
    _releaseController();
    _file?.dispose();
    super.dispose();
  }

  /// 解码素材：控制器依赖 File，装载阶段只解文件不建控制器
  Future<void> _loadFile() async {
    setState(() {
      _isLoading = true;
      _loadError = null;
    });

    try {
      await rive.RiveNative.init();
      final rive.File? file = await rive.File.asset(
        _sampleAsset,
        riveFactory: _factory,
      );
      if (!mounted) {
        file?.dispose();
        return;
      }

      setState(() {
        _file = file;
        _isLoading = false;
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _loadError = error;
        _isLoading = false;
      });
    }
  }

  void _playStateMachine() {
    final rive.File? file = _file;
    if (file == null) {
      showToast('素材尚未装载完成');
      return;
    }

    _releaseController();
    _controller = rive.RiveWidgetController(
      file,
      stateMachineSelector: rive.StateMachineSelector.byName(_stateMachineName),
    );
    setState(() {});
    showToast('已挂接状态机：$_stateMachineName（点击画板按钮切 Level）');
  }

  /// 暂停/继续：active 关掉后控制器不再推进与重绘
  void _setActive(bool active) {
    final rive.RiveWidgetController? controller = _controller;
    if (controller == null) {
      showToast('尚未挂接状态机');
      return;
    }

    controller.active = active;
    setState(() {});
    showToast(active ? '已继续推进' : '已暂停推进');
  }

  /// 复位：新运行时未公开 Artboard.reset，重建控制器即回到状态机 Entry 状态
  void _resetStateMachine() {
    _playStateMachine();
  }

  /// 切换渲染轨道：File 与解码工厂绑定，须释放旧 File 与控制器后重新解码
  Future<void> _switchRenderer() async {
    _releaseController();
    _file?.dispose();
    _file = null;

    _useRiveRenderer = !_useRiveRenderer;
    await _loadFile();
    if (!mounted) {
      return;
    }

    showToast(
      '渲染轨道：${_useRiveRenderer ? 'Rive Renderer' : 'Flutter Renderer'}',
    );
  }

  void _releaseController() {
    _controller?.dispose();
    _controller = null;
  }

  @override
  Widget buildPreview() {
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Text(
            'Local Rive sample',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          SelectableText(
            '$_sampleAsset · ${_useRiveRenderer ? 'Rive' : 'Flutter'} Renderer',
            style: Theme.of(context).textTheme.bodySmall,
          ),
          const SizedBox(height: 16),
          Expanded(child: _buildCanvas(context)),
        ],
      ),
    );
  }

  Widget _buildCanvas(BuildContext context) {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_loadError != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            const Icon(Icons.error_outline, size: 40),
            const SizedBox(height: 12),
            const Text('Failed to load Rive animation'),
            const SizedBox(height: 8),
            Text(
              '$_loadError',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
      );
    }

    final rive.RiveWidgetController? controller = _controller;
    if (controller == null) {
      return const Center(child: Text('Press 1 to play the state machine'));
    }

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
      ),
      child: Center(
        child: SizedBox(
          width: 280,
          height: 280,
          child: rive.RiveWidget(controller: controller, fit: rive.Fit.contain),
        ),
      ),
    );
  }
}
