import 'package:flutter/material.dart';
import 'package:flutter_constraintlayout/flutter_constraintlayout.dart';

/// ConstraintLayout
/// https://pub.dev/packages/flutter_constraintlayout
class ConstraintLayoutDemoPage extends StatelessWidget {
  const ConstraintLayoutDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ConstraintLayoutDemoView(title: title);
  }
}

class ConstraintLayoutDemoView extends StatelessWidget {
  const ConstraintLayoutDemoView({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: getBody(context),
    );
  }

  Widget getBody(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        _buildIntroCard(context),
        const SizedBox(height: 16),
        _SectionCard(
          title: '信息卡片',
          summary: '把头像、标题、标签和统计块放进同一个布局里，少写几层 Row/Column。',
          child: _buildProfileCard(context),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: 'Guideline 分栏',
          summary: '用一条垂直 guideline 把区域拆成 32% / 68%，非常适合做侧栏 + 内容区。',
          child: _buildGuidelineLayout(context),
        ),
      ],
    );
  }

  Widget _buildIntroCard(BuildContext context) {
    final ColorScheme colorScheme = Theme.of(context).colorScheme;

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: colorScheme.primaryContainer.withValues(alpha: 0.45),
        borderRadius: BorderRadius.circular(20),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            'flutter_constraintlayout 适合复杂卡片、海报式布局、徽标叠加等场景。',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
          ),
          SizedBox(height: 8),
          Text('你可以直接描述“谁贴着谁、谁跟着谁”，而不是一层层嵌套布局组件。'),
        ],
      ),
    );
  }

  Widget _buildProfileCard(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ColorScheme colorScheme = theme.colorScheme;
    final ConstraintId avatarId = ConstraintId('profile-avatar');
    final ConstraintId chipId = ConstraintId('profile-chip');
    final ConstraintId titleId = ConstraintId('profile-title');
    final ConstraintId subtitleId = ConstraintId('profile-subtitle');
    final ConstraintId dividerId = ConstraintId('profile-divider');
    final ConstraintId statPrimaryId = ConstraintId('profile-stat-primary');
    final ConstraintId statSecondaryId = ConstraintId('profile-stat-secondary');

    return SizedBox(
      height: 240,
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: colorScheme.surfaceContainerHighest.withValues(alpha: 0.35),
          borderRadius: BorderRadius.circular(20),
        ),
        child: ConstraintLayout(
          children: <Widget>[
            CircleAvatar(
              radius: 28,
              backgroundColor: colorScheme.primary,
              child: Icon(
                Icons.grid_view_rounded,
                color: colorScheme.onPrimary,
              ),
            ).applyConstraint(
              id: avatarId,
              width: 56,
              height: 56,
              left: parent.left,
              top: parent.top,
              margin: const EdgeInsets.only(left: 20, top: 20),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: colorScheme.secondaryContainer,
                borderRadius: BorderRadius.circular(999),
              ),
              child: Text(
                '层级更平',
                style: theme.textTheme.labelMedium?.copyWith(
                  color: colorScheme.onSecondaryContainer,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ).applyConstraint(
              id: chipId,
              right: parent.right,
              top: parent.top,
              margin: const EdgeInsets.only(top: 20, right: 20),
            ),
            Text(
              'ConstraintLayout 信息卡片',
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ).applyConstraint(
              id: titleId,
              left: avatarId.right,
              right: chipId.left,
              top: avatarId.top,
              width: matchConstraint,
              margin: const EdgeInsets.only(left: 12, right: 12),
            ),
            Text(
              '同一个布局里同时处理头像、文案、标签和底部统计区，代码更接近设计稿关系。',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: colorScheme.onSurfaceVariant,
                height: 1.4,
              ),
            ).applyConstraint(
              id: subtitleId,
              left: titleId.left,
              right: parent.right,
              top: titleId.bottom,
              width: matchConstraint,
              margin: const EdgeInsets.only(top: 8, right: 20),
            ),
            Container(color: colorScheme.outlineVariant).applyConstraint(
              id: dividerId,
              left: avatarId.left,
              right: parent.right,
              top: avatarId.bottom,
              width: matchConstraint,
              height: 1,
              margin: const EdgeInsets.only(top: 20, right: 20),
            ),
            _StatCard(
              title: '表达方式',
              value: '直接写约束',
              color: colorScheme.primaryContainer,
              textColor: colorScheme.onPrimaryContainer,
            ).applyConstraint(
              id: statPrimaryId,
              left: avatarId.left,
              top: dividerId.bottom,
              width: 120,
              height: 72,
              margin: const EdgeInsets.only(top: 16),
            ),
            _StatCard(
              title: '适合场景',
              value: '复杂卡片 / 海报',
              color: colorScheme.tertiaryContainer,
              textColor: colorScheme.onTertiaryContainer,
            ).applyConstraint(
              id: statSecondaryId,
              left: statPrimaryId.right,
              right: parent.right,
              top: statPrimaryId.top,
              width: matchConstraint,
              height: 72,
              margin: const EdgeInsets.only(left: 12, right: 20),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildGuidelineLayout(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ColorScheme colorScheme = theme.colorScheme;
    final ConstraintId guidelineId = ConstraintId('content-guideline');
    final ConstraintId sidebarId = ConstraintId('content-sidebar');
    final ConstraintId contentId = ConstraintId('content-main');

    return SizedBox(
      height: 180,
      child: ConstraintLayout(
        showHelperWidgets: true,
        children: <Widget>[
          Guideline(id: guidelineId, horizontal: false, guidelinePercent: 0.32),
          _PaneCard(
            title: '左侧导航',
            description: '32%\n筛选条件、分类入口',
            backgroundColor: colorScheme.primaryContainer.withValues(
              alpha: 0.7,
            ),
            textColor: colorScheme.onPrimaryContainer,
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
          _PaneCard(
            title: '右侧内容',
            description: '68%\n列表、图表、详情内容',
            backgroundColor: colorScheme.secondaryContainer.withValues(
              alpha: 0.7,
            ),
            textColor: colorScheme.onSecondaryContainer,
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

class _SectionCard extends StatelessWidget {
  const _SectionCard({
    required this.title,
    required this.summary,
    required this.child,
  });

  final String title;
  final String summary;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ColorScheme colorScheme = theme.colorScheme;

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: colorScheme.surface,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: colorScheme.outlineVariant),
      ),
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
            summary,
            style: theme.textTheme.bodyMedium?.copyWith(
              color: colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 16),
          child,
        ],
      ),
    );
  }
}

class _StatCard extends StatelessWidget {
  const _StatCard({
    required this.title,
    required this.value,
    required this.color,
    required this.textColor,
  });

  final String title;
  final String value;
  final Color color;
  final Color textColor;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Text(
            title,
            style: TextStyle(
              color: textColor.withValues(alpha: 0.78),
              fontSize: 12,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            value,
            style: TextStyle(
              color: textColor,
              fontSize: 15,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }
}

class _PaneCard extends StatelessWidget {
  const _PaneCard({
    required this.title,
    required this.description,
    required this.backgroundColor,
    required this.textColor,
  });

  final String title;
  final String description;
  final Color backgroundColor;
  final Color textColor;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Text(
            title,
            style: TextStyle(
              color: textColor,
              fontSize: 16,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            description,
            style: TextStyle(
              color: textColor.withValues(alpha: 0.82),
              height: 1.45,
            ),
          ),
        ],
      ),
    );
  }
}
