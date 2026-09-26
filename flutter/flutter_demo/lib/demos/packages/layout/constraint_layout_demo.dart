import 'package:flutter/material.dart';
import 'package:flutter_constraintlayout/flutter_constraintlayout.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// ConstraintLayout — 声明式相对布局
///
/// 核心机制与避坑点：
/// 1. 约束关系：`applyConstraint` 描述“谁贴着谁”，避免 Row/Column 多层嵌套。
/// 2. Guideline：`Guideline(guidelinePercent:)` 可按百分比拆分区域。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_constraintlayout
class ConstraintLayoutDemoPage extends BasicLayoutPage {
  const ConstraintLayoutDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ConstraintLayoutDemoPage> createState() =>
      _ConstraintLayoutDemoPageState();
}

class _ConstraintLayoutDemoPageState
    extends BasicLayoutPageState<ConstraintLayoutDemoPage> {
  @override
  Widget buildPreview() {
    final ColorScheme colorScheme = Theme.of(context).colorScheme;
    return ListView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      children: <Widget>[
        _buildProfileCard(colorScheme),
        const SizedBox(height: BasicDemoDimens.sectionGap),
        _buildGuidelineLayout(colorScheme),
      ],
    );
  }

  Widget _buildProfileCard(ColorScheme colorScheme) {
    final ConstraintId avatarId = ConstraintId('profile-avatar');
    final ConstraintId titleId = ConstraintId('profile-title');
    final ConstraintId subtitleId = ConstraintId('profile-subtitle');
    final ConstraintId chipId = ConstraintId('profile-chip');

    return SizedBox(
      height: 180,
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: colorScheme.surfaceContainerHighest.withValues(alpha: 0.35),
          borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        ),
        child: ConstraintLayout(
          children: <Widget>[
            CircleAvatar(
              radius: 28,
              backgroundColor: colorScheme.primary,
              child: Icon(Icons.grid_view_rounded, color: colorScheme.onPrimary),
            ).applyConstraint(
              id: avatarId,
              width: 56,
              height: 56,
              left: parent.left,
              top: parent.top,
              margin: const EdgeInsets.only(left: 20, top: 20),
            ),
            Text(
              'ConstraintLayout 信息卡片',
              style: TextStyle(
                fontWeight: FontWeight.w700,
                color: colorScheme.onSurface,
              ),
            ).applyConstraint(
              id: titleId,
              left: avatarId.right,
              right: parent.right,
              top: avatarId.top,
              width: matchConstraint,
              margin: const EdgeInsets.only(left: 12, right: 20),
            ),
            Text(
              '头像、标题、标签共处一层约束',
              style: TextStyle(color: colorScheme.onSurfaceVariant),
            ).applyConstraint(
              id: subtitleId,
              left: titleId.left,
              right: parent.right,
              top: titleId.bottom,
              width: matchConstraint,
              margin: const EdgeInsets.only(top: 8, right: 20),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: colorScheme.secondaryContainer,
                borderRadius: BorderRadius.circular(999),
              ),
              child: Text(
                '层级更平',
                style: TextStyle(
                  color: colorScheme.onSecondaryContainer,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ).applyConstraint(
              id: chipId,
              left: avatarId.left,
              top: avatarId.bottom,
              margin: const EdgeInsets.only(left: 4, top: 16),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildGuidelineLayout(ColorScheme colorScheme) {
    final ConstraintId guidelineId = ConstraintId('content-guideline');
    final ConstraintId sidebarId = ConstraintId('content-sidebar');
    final ConstraintId contentId = ConstraintId('content-main');

    return SizedBox(
      height: 180,
      child: ConstraintLayout(
        children: <Widget>[
          Guideline(id: guidelineId, horizontal: false, guidelinePercent: 0.32),
          Container(
            alignment: Alignment.center,
            color: colorScheme.primaryContainer.withValues(alpha: 0.7),
            child: const Text('侧栏 32%'),
          ).applyConstraint(
            id: sidebarId,
            left: parent.left,
            right: guidelineId.left,
            top: parent.top,
            bottom: parent.bottom,
            width: matchConstraint,
            height: matchConstraint,
            margin: const EdgeInsets.only(right: 12),
          ),
          Container(
            alignment: Alignment.center,
            color: colorScheme.secondaryContainer.withValues(alpha: 0.7),
            child: const Text('内容区 68%'),
          ).applyConstraint(
            id: contentId,
            left: guidelineId.right,
            right: parent.right,
            top: parent.top,
            bottom: parent.bottom,
            width: matchConstraint,
            height: matchConstraint,
            margin: const EdgeInsets.only(left: 12),
          ),
        ],
      ),
    );
  }
}
