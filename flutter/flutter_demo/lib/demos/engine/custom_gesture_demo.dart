import 'dart:math' as math;

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';

/// 自定义手势识别示例
///
/// 自定义 [ScaleRotateGestureRecognizer] 识别双指缩放与旋转手势，
/// 通过 [RawGestureDetector] 挂载到手势竞技场，展示多指手势的底层识别协议。
class CustomGestureDemoPage extends StatelessWidget {
  const CustomGestureDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return CustomGestureDemoView(title: title);
  }
}

class CustomGestureDemoView extends StatefulWidget {
  const CustomGestureDemoView({super.key, required this.title});

  final String title;

  @override
  State<CustomGestureDemoView> createState() => _CustomGestureDemoViewState();
}

class _CustomGestureDemoViewState extends State<CustomGestureDemoView> {
  double _scale = 1;
  double _rotation = 0;

  void _onUpdate(double scaleDelta, double rotationDelta) {
    setState(() {
      _scale = (_scale * scaleDelta).clamp(0.2, 6.0).toDouble();
      _rotation += rotationDelta;
    });
  }

  void _reset() {
    setState(() {
      _scale = 1;
      _rotation = 0;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: [
          IconButton(
            onPressed: _reset,
            icon: const Icon(Icons.refresh),
            tooltip: '重置',
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              '用两根手指在盒子上缩放或旋转，自定义 GestureRecognizer 会识别'
              '双指距离（缩放）与角度（旋转）的变化。\n'
              '缩放 ${_scale.toStringAsFixed(2)}x · '
              '旋转 ${(_rotation * 180 / math.pi).toStringAsFixed(0)}°',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    color: Theme.of(context).colorScheme.surfaceContainerHighest,
                  ),
                  child: SizedBox.expand(
                    child: RawGestureDetector(
                      gestures: <Type, GestureRecognizerFactory>{
                        ScaleRotateGestureRecognizer:
                            GestureRecognizerFactoryWithHandlers<
                                ScaleRotateGestureRecognizer>(
                          () => ScaleRotateGestureRecognizer(),
                          (ScaleRotateGestureRecognizer instance) {
                            instance.onUpdate = _onUpdate;
                          },
                        ),
                      },
                      child: Center(
                        child: Transform.rotate(
                          angle: _rotation,
                          child: Transform.scale(
                            scale: _scale,
                            child: const _Box(),
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Box extends StatelessWidget {
  const _Box();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 140,
      height: 140,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: Colors.deepOrange,
        borderRadius: BorderRadius.circular(20),
        boxShadow: const [
          BoxShadow(
            color: Colors.black26,
            blurRadius: 16,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: const Text('🚀', style: TextStyle(fontSize: 64)),
    );
  }
}

/// 双指缩放旋转手势识别器。
///
/// 跟踪多个指针，在第二个指针落下时进入 accepted 状态，
/// 之后通过两指距离变化计算缩放增量、两指连线角度变化计算旋转增量。
class ScaleRotateGestureRecognizer extends OneSequenceGestureRecognizer {
  /// 每次产生增量时回调（缩放倍率与旋转弧度）。
  void Function(double scaleDelta, double rotationDelta)? onUpdate;

  final Map<int, Offset> _pointers = <int, Offset>{};
  double _baselineDistance = 0;
  double _baselineAngle = 0;

  @override
  void addAllowedPointer(PointerDownEvent event) {
    _pointers[event.pointer] = event.position;
    startTrackingPointer(event.pointer);
    if (_pointers.length == 2) {
      _resetBaseline();
      resolve(GestureDisposition.accepted);
    }
  }

  @override
  void handleEvent(PointerEvent event) {
    if (event is PointerMoveEvent) {
      if (!_pointers.containsKey(event.pointer)) {
        return;
      }
      _pointers[event.pointer] = event.position;
      if (_pointers.length >= 2) {
        _emitUpdate();
      }
    } else if (event is PointerUpEvent || event is PointerCancelEvent) {
      _pointers.remove(event.pointer);
      stopTrackingPointer(event.pointer);
    }
  }

  void _resetBaseline() {
    final List<Offset> positions = _pointers.values.toList();
    _baselineDistance = (positions[0] - positions[1]).distance;
    _baselineAngle = math.atan2(
      positions[1].dy - positions[0].dy,
      positions[1].dx - positions[0].dx,
    );
  }

  void _emitUpdate() {
    final List<Offset> positions = _pointers.values.toList();
    final double distance = (positions[0] - positions[1]).distance;
    final double angle = math.atan2(
      positions[1].dy - positions[0].dy,
      positions[1].dx - positions[0].dx,
    );
    if (_baselineDistance == 0) {
      _resetBaseline();
      return;
    }
    final double scaleDelta = distance / _baselineDistance;
    final double rotationDelta = angle - _baselineAngle;
    _baselineDistance = distance;
    _baselineAngle = angle;
    onUpdate?.call(scaleDelta, rotationDelta);
  }

  @override
  void didStopTrackingLastPointer(int pointer) {
    _pointers.clear();
  }

  @override
  String get debugDescription => 'scale-rotate';
}
