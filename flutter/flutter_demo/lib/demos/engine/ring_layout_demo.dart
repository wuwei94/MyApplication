import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';

/// 环形布局示例
///
/// 手写 [RenderBox] 实现自定义布局协议：
/// 子组件在 [RenderRingLayout.performLayout] 中被均匀排布在圆周上。
class RingLayoutDemoPage extends StatelessWidget {
  const RingLayoutDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return RingLayoutDemoView(title: title);
  }
}

class RingLayoutDemoView extends StatefulWidget {
  const RingLayoutDemoView({super.key, required this.title});

  final String title;

  @override
  State<RingLayoutDemoView> createState() => _RingLayoutDemoViewState();
}

class _RingLayoutDemoViewState extends State<RingLayoutDemoView> {
  static const int _itemCount = 8;

  double _radius = 110;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              '自定义 RenderObject 将 $_itemCount 个子组件均匀排列在圆周上，'
              '拖动滑块可动态改变半径。',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
          Expanded(
            child: Center(
              child: RingLayout(
                radius: _radius,
                children: List<Widget>.generate(
                  _itemCount,
                  (int index) => _RingItem(index: index),
                ),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                const Icon(Icons.zoom_out),
                Expanded(
                  child: Slider(
                    min: 40,
                    max: 140,
                    value: _radius,
                    onChanged: (double value) {
                      setState(() {
                        _radius = value;
                      });
                    },
                  ),
                ),
                const Icon(Icons.zoom_in),
              ],
            ),
          ),
          const SizedBox(height: 8),
        ],
      ),
    );
  }
}

class _RingItem extends StatelessWidget {
  const _RingItem({required this.index});

  final int index;

  @override
  Widget build(BuildContext context) {
    final Color color = Colors.primaries[index % Colors.primaries.length];
    return Container(
      width: 56,
      height: 56,
      alignment: Alignment.center,
      decoration: BoxDecoration(color: color, shape: BoxShape.circle),
      child: Text(
        '${index + 1}',
        style: const TextStyle(
          color: Colors.white,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
}

/// 将子组件均匀排列在圆周上的布局组件。
class RingLayout extends MultiChildRenderObjectWidget {
  const RingLayout({
    super.key,
    required this.radius,
    required super.children,
  });

  final double radius;

  @override
  RenderRingLayout createRenderObject(BuildContext context) {
    return RenderRingLayout(radius: radius);
  }

  @override
  void updateRenderObject(
    BuildContext context,
    RenderRingLayout renderObject,
  ) {
    renderObject.radius = radius;
  }
}

class RingParentData extends ContainerBoxParentData<RenderBox> {}

class RenderRingLayout extends RenderBox
    with
        ContainerRenderObjectMixin<RenderBox, RingParentData>,
        RenderBoxContainerDefaultsMixin<RenderBox, RingParentData> {
  RenderRingLayout({required double radius}) : _radius = radius;

  double _radius;

  double get radius => _radius;

  set radius(double value) {
    if (_radius == value) {
      return;
    }
    _radius = value;
    markNeedsLayout();
  }

  @override
  void setupParentData(RenderObject child) {
    if (child.parentData is! RingParentData) {
      child.parentData = RingParentData();
    }
  }

  @override
  void performLayout() {
    // 第一遍：测量所有子组件，得到最大子半径。
    double maxChildRadius = 0;
    RenderBox? child = firstChild;
    while (child != null) {
      child.layout(constraints.loosen(), parentUsesSize: true);

      final double halfDiagonal = math.sqrt(
            child.size.width * child.size.width +
                child.size.height * child.size.height,
          ) /
          2;
      if (halfDiagonal > maxChildRadius) {
        maxChildRadius = halfDiagonal;
      }
      child = childAfter(child);
    }

    // 自身尺寸为圆环外接正方形，圆心即自身中心。
    size = constraints.constrain(Size.square(2 * (_radius + maxChildRadius)));
    final Offset center = Offset(size.width / 2, size.height / 2);

    // 第二遍：按角度把子组件定位到圆周上。
    int index = 0;
    child = firstChild;
    while (child != null) {
      final double angle = _angleFor(index);
      final Offset target = Offset(
        center.dx + math.cos(angle) * _radius,
        center.dy + math.sin(angle) * _radius,
      );
      final ParentData? parentData = child.parentData;
      if (parentData is RingParentData) {
        parentData.offset = target -
            Offset(child.size.width / 2, child.size.height / 2);
      }
      index++;
      child = childAfter(child);
    }
  }

  double _angleFor(int index) {
    final int count = childCount;
    if (count == 0) {
      return 0;
    }
    return -math.pi / 2 + 2 * math.pi * index / count;
  }

  @override
  void paint(PaintingContext context, Offset offset) {
    defaultPaint(context, offset);
  }

  @override
  bool hitTestChildren(BoxHitTestResult result, {required Offset position}) {
    return defaultHitTestChildren(result, position: position);
  }
}
