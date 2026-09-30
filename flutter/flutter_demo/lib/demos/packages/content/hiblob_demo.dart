import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:hiblob/flutter.dart';

/// Hiblob — 由 name 确定性生成的几何 blob 头像
///
/// 核心机制与避坑点：
/// 1. 确定性映射：同一 `name` 在任意平台永远渲染同一 figure；traits / 配色 / 配饰由哈希驱动，可用 `HiblobOptions` 逐轴 pin 住。
/// 2. 静态与动画：`Hiblob` 走静态绘制；`AnimatedHiblob` 提供 breathe / bob / blink，expression 与 name 变更会交叉淡入 morph。
/// 3. 动画策略：`HiblobAnimation.always` 持续动，`hover` 仅指针悬停时渐入，列表场景优先 hover 以降低连续开销。
///
/// 官方参考：
/// https://pub.dev/packages/hiblob
class HiblobDemoPage extends BasicLayoutPage {
  const HiblobDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<HiblobDemoPage> createState() => _HiblobDemoPageState();
}

class _HiblobDemoPageState extends BasicLayoutPageState<HiblobDemoPage> {
  static const List<Expression> _expressions = expressions;
  static const List<Backdrop> _backdrops = Backdrop.values;
  static const List<HiblobAnimation> _animationModes = HiblobAnimation.values;

  static const List<String> _gridNames = <String>[
    'ada@example.com',
    'linus',
    'grace.hopper',
    'mothra-42',
    'kit@team',
    'Nyx',
    'orbit',
    '张三丰',
  ];

  static const List<String> _randomNames = <String>[
    'ada@example.com',
    'linus',
    'grace.hopper',
    'mothra-42',
    'kit@team',
    'Nyx',
    'orbit',
    '张三丰',
    'byte-bard',
    'sunny_side',
    'radar.7',
    'quiet-quark',
  ];

  final Random _random = Random();

  String _name = _gridNames.first;
  int _expressionIndex = 0;
  int _backdropIndex = 1;
  int _animationIndex = 0;

  Expression get _expression => _expressions[_expressionIndex];
  Backdrop get _backdrop => _backdrops[_backdropIndex];
  HiblobAnimation get _animation => _animationModes[_animationIndex];

  HiblobOptions get _options => HiblobOptions(
        background: _backdrop,
        expression: _expression,
      );

  @override
  List<String> buildList() => const <String>[
        '1. 切换 expression',
        '2. 切换 backdrop',
        '3. 切换动画模式',
        '4. 随机 name',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        setState(() {
          _expressionIndex = (_expressionIndex + 1) % _expressions.length;
        });
      case 1:
        setState(() {
          _backdropIndex = (_backdropIndex + 1) % _backdrops.length;
        });
      case 2:
        setState(() {
          _animationIndex = (_animationIndex + 1) % _animationModes.length;
        });
      case 3:
        setState(() {
          _name = _randomNames[_random.nextInt(_randomNames.length)];
        });
    }
  }

  @override
  Widget buildPreview() {
    final ThemeData theme = Theme.of(context);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            'name=$_name · expression=${_expression.id} · '
            'backdrop=${_backdrop.name} · animation=${_animation.name}',
            style: theme.textTheme.titleSmall,
          ),
          const SizedBox(height: 16),
          _buildSectionTitle('静态 Hiblob'),
          Center(
            child: Hiblob(
              name: _name,
              size: 96,
              semanticLabel: 'Static hiblob of $_name',
              options: _options,
            ),
          ),
          const SizedBox(height: 24),
          _buildSectionTitle('AnimatedHiblob'),
          Center(
            child: AnimatedHiblob(
              name: _name,
              size: 120,
              animation: _animation,
              options: _options,
              semanticLabel: 'Animated hiblob of $_name',
            ),
          ),
          const SizedBox(height: 24),
          _buildSectionTitle('多 name 网格'),
          Wrap(
            spacing: 12,
            runSpacing: 12,
            children: _gridNames
                .map(
                  (String gridName) => SizedBox(
                    width: 72,
                    child: Column(
                      children: <Widget>[
                        Hiblob(
                          name: gridName,
                          size: 64,
                          semanticLabel: 'Hiblob of $gridName',
                          options: HiblobOptions(
                            background: _backdrop,
                            expression: _expression,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          gridName,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: theme.textTheme.labelSmall,
                          textAlign: TextAlign.center,
                        ),
                      ],
                    ),
                  ),
                )
                .toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.bold,
          color: Colors.blue,
        ),
      ),
    );
  }
}
