import 'package:flutter/material.dart';
import 'package:flutter_slidable/flutter_slidable.dart';

/// Slidable
/// https://pub.dev/packages/flutter_slidable
class SlidableDemoPage extends StatelessWidget {
  const SlidableDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SlidableDemoView(title: title);
  }
}

class SlidableDemoView extends StatefulWidget {
  const SlidableDemoView({super.key, required this.title});

  final String title;

  @override
  State<SlidableDemoView> createState() => _SlidableDemoViewState();
}

class _SlidableDemoViewState extends State<SlidableDemoView>
    with SingleTickerProviderStateMixin {
  late final SlidableController _featuredController;
  late List<_InboxItem> _items;

  _SlidableMotionType _motionType = _SlidableMotionType.scroll;
  bool _allowDismiss = true;
  String? _featuredItemId;
  String _statusMessage = '左右滑动卡片，体验 start/end actions 和 dismissible。';

  @override
  void initState() {
    super.initState();
    _featuredController = SlidableController(this);
    _items = _buildInitialItems();
    _featuredItemId = _pickFeaturedItemId(_items);
  }

  @override
  void dispose() {
    _featuredController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    final List<Widget> children = <Widget>[
      Padding(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
        child: _buildOverviewCard(context),
      ),
      const SizedBox(height: 12),
    ];

    if (_items.isEmpty) {
      children.add(
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: _buildEmptyState(context),
        ),
      );
    } else {
      for (int index = 0; index < _items.length; index++) {
        final _InboxItem item = _items[index];
        children.add(
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: _buildSlidableItem(item: item, index: index),
          ),
        );

        if (index != _items.length - 1) {
          children.add(const SizedBox(height: 12));
        }
      }
    }

    return ListView(
      padding: const EdgeInsets.only(bottom: 24),
      children: children,
    );
  }

  Widget _buildOverviewCard(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFDCE3F0)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120F172A),
            blurRadius: 18,
            offset: Offset(0, 10),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Container(
                  width: 52,
                  height: 52,
                  decoration: BoxDecoration(
                    color: const Color(0xFF0F766E),
                    borderRadius: BorderRadius.circular(18),
                  ),
                  alignment: Alignment.center,
                  child: const Icon(Icons.swipe_rounded, color: Colors.white),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text(
                        'flutter_slidable 4.0.3',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        '这个示例把邮件/待办列表包装成 Slidable 卡片，演示左右动作区、滑到底删除，以及通过 controller 程序化展开操作。',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: Colors.black54,
                          height: 1.45,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: <Widget>[
                _MetricChip(label: '列表项', value: '${_items.length}'),
                _MetricChip(label: '置顶', value: '$_pinnedCount'),
                _MetricChip(label: '动画', value: _motionType.label),
              ],
            ),
            const SizedBox(height: 14),
            Text(
              _statusMessage,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: const Color(0xFF115E59),
                height: 1.4,
              ),
            ),
            const SizedBox(height: 16),
            Container(
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: const Color(0xFFDCE3F0)),
              ),
              child: Theme(
                data: theme.copyWith(dividerColor: Colors.transparent),
                child: ExpansionTile(
                  tilePadding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 4,
                  ),
                  childrenPadding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                  title: Text(
                    '演示控制面板',
                    style: theme.textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  subtitle: Text(
                    '切换 dismiss、motion，并试试程序化展开操作区。',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: Colors.black54,
                      height: 1.35,
                    ),
                  ),
                  children: <Widget>[
                    Row(
                      children: <Widget>[
                        Expanded(
                          child: Text(
                            _allowDismiss
                                ? '当前开启 DismissiblePane，向左滑到底会直接移除条目。'
                                : '当前关闭 DismissiblePane，现在只保留动作按钮。',
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: Colors.black54,
                              height: 1.35,
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Switch.adaptive(
                          value: _allowDismiss,
                          onChanged: _handleDismissChanged,
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'Motion',
                      style: theme.textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      'Motion 表示动作面板展开时的动画方式，只影响按钮如何出现，不影响置顶、归档、删除这些功能本身。',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: Colors.black54,
                        height: 1.4,
                      ),
                    ),
                    const SizedBox(height: 10),
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: _SlidableMotionType.values
                          .map(
                            (_SlidableMotionType motionType) => ChoiceChip(
                              label: Text(motionType.label),
                              selected: _motionType == motionType,
                              onSelected: (bool selected) {
                                if (!selected) {
                                  return;
                                }

                                _handleMotionChanged(motionType);
                              },
                            ),
                          )
                          .toList(),
                    ),
                    const SizedBox(height: 12),
                    DecoratedBox(
                      decoration: BoxDecoration(
                        color: const Color(0xFFF8FAFC),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(color: const Color(0xFFDCE3F0)),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(12),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            Text(
                              '${_motionType.label} 是什么？',
                              style: theme.textTheme.bodyMedium?.copyWith(
                                fontWeight: FontWeight.w700,
                                color: const Color(0xFF0F172A),
                              ),
                            ),
                            const SizedBox(height: 6),
                            Text(
                              _motionType.description,
                              style: theme.textTheme.bodySmall?.copyWith(
                                color: Colors.black87,
                                height: 1.45,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    Wrap(
                      spacing: 12,
                      runSpacing: 12,
                      children: <Widget>[
                        FilledButton.icon(
                          onPressed: _openFeaturedActions,
                          icon: const Icon(
                            Icons.keyboard_double_arrow_left_rounded,
                          ),
                          label: const Text('展开推荐项操作区'),
                        ),
                        OutlinedButton.icon(
                          onPressed: _closeFeaturedActions,
                          icon: const Icon(Icons.close_fullscreen_rounded),
                          label: const Text('关闭推荐项操作区'),
                        ),
                        OutlinedButton.icon(
                          onPressed: _restoreItems,
                          icon: const Icon(Icons.restore_rounded),
                          label: const Text('恢复示例数据'),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFDCE3F0)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            const Icon(Icons.inbox_rounded, size: 48, color: Color(0xFF0F766E)),
            const SizedBox(height: 12),
            Text(
              '列表已被清空',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '你可以点击下方按钮恢复初始数据，继续体验 Slidable 的动作区和 dismiss 行为。',
              textAlign: TextAlign.center,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: Colors.black54,
                height: 1.45,
              ),
            ),
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: _restoreItems,
              icon: const Icon(Icons.restore_rounded),
              label: const Text('恢复示例数据'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSlidableItem({required _InboxItem item, required int index}) {
    final bool isFeatured = item.id == _featuredItemId;

    return Slidable(
      key: ValueKey<String>(item.id),
      controller: isFeatured ? _featuredController : null,
      startActionPane: ActionPane(
        motion: _buildMotion(),
        children: <Widget>[
          SlidableAction(
            onPressed: (BuildContext context) {
              _togglePinned(item.id);
            },
            backgroundColor: item.isPinned
                ? const Color(0xFF475569)
                : const Color(0xFF2563EB),
            foregroundColor: Colors.white,
            icon: item.isPinned
                ? Icons.push_pin_rounded
                : Icons.push_pin_outlined,
            label: item.isPinned ? '取消置顶' : '置顶',
          ),
          SlidableAction(
            onPressed: (BuildContext context) {
              _toggleRead(item.id);
            },
            backgroundColor: item.isUnread
                ? const Color(0xFFD97706)
                : const Color(0xFF0F766E),
            foregroundColor: Colors.white,
            icon: item.isUnread
                ? Icons.mark_email_read_rounded
                : Icons.mark_email_unread_rounded,
            label: item.isUnread ? '已读' : '未读',
          ),
        ],
      ),
      endActionPane: ActionPane(
        motion: _buildMotion(),
        dismissible: _allowDismiss
            ? DismissiblePane(
                onDismissed: () {
                  _removeItem(item.id, message: '已通过滑动删除 "${item.title}"。');
                },
              )
            : null,
        children: <Widget>[
          SlidableAction(
            flex: 2,
            onPressed: (BuildContext context) {
              _archiveItem(item.id);
            },
            backgroundColor: const Color(0xFF0F766E),
            foregroundColor: Colors.white,
            icon: Icons.archive_rounded,
            label: '归档',
          ),
          SlidableAction(
            onPressed: (BuildContext context) {
              _removeItem(item.id, message: '已通过动作按钮删除 "${item.title}"。');
            },
            backgroundColor: const Color(0xFFDC2626),
            foregroundColor: Colors.white,
            icon: Icons.delete_outline_rounded,
            label: '删除',
          ),
        ],
      ),
      child: _InboxItemCard(item: item, index: index, isFeatured: isFeatured),
    );
  }

  Widget _buildMotion() {
    switch (_motionType) {
      case _SlidableMotionType.scroll:
        return const ScrollMotion();
      case _SlidableMotionType.drawer:
        return const DrawerMotion();
      case _SlidableMotionType.behind:
        return const BehindMotion();
      case _SlidableMotionType.stretch:
        return const StretchMotion();
    }
  }

  void _handleDismissChanged(bool value) {
    setState(() {
      _allowDismiss = value;
      _statusMessage = value
          ? '已开启 DismissiblePane，向左滑到底会直接删除当前条目。'
          : '已关闭 DismissiblePane，现在只保留动作按钮。';
    });
  }

  void _handleMotionChanged(_SlidableMotionType motionType) {
    setState(() {
      _motionType = motionType;
      _statusMessage = '当前动作面板动画已切换为 ${motionType.label}。';
    });
  }

  void _openFeaturedActions() {
    final _InboxItem? featuredItem = _findItem(_featuredItemId);
    if (featuredItem == null) {
      _showSnackBar('当前没有可展开的推荐项。');
      return;
    }

    _featuredController.openEndActionPane();
    setState(() {
      _statusMessage =
          '已通过 SlidableController 展开 "${featuredItem.title}" 的右侧操作区。';
    });
  }

  void _closeFeaturedActions() {
    _featuredController.close();
    setState(() {
      _statusMessage = '已关闭推荐项的动作区。';
    });
  }

  void _restoreItems() {
    final List<_InboxItem> nextItems = _buildInitialItems();

    setState(() {
      _items = nextItems;
      _featuredItemId = _pickFeaturedItemId(nextItems);
      _statusMessage = '已恢复初始数据，可以重新体验各种滑动动作。';
    });
    _showSnackBar('示例数据已恢复。');
  }

  void _togglePinned(String id) {
    final _InboxItem? targetItem = _findItem(id);
    if (targetItem == null) {
      return;
    }

    final bool nextPinned = !targetItem.isPinned;
    final List<_InboxItem> nextItems = _sortItems(
      _items
          .map(
            (_InboxItem item) =>
                item.id == id ? item.copyWith(isPinned: nextPinned) : item,
          )
          .toList(),
    );

    setState(() {
      _items = nextItems;
      _featuredItemId = _pickFeaturedItemId(
        nextItems,
        preferredId: nextPinned ? id : _featuredItemId,
      );
      _statusMessage = nextPinned
          ? '已将 "${targetItem.title}" 置顶，它现在也成了推荐演示项。'
          : '已取消 "${targetItem.title}" 的置顶状态。';
    });
  }

  void _toggleRead(String id) {
    final _InboxItem? targetItem = _findItem(id);
    if (targetItem == null) {
      return;
    }

    final bool nextUnread = !targetItem.isUnread;
    final List<_InboxItem> nextItems = _items
        .map(
          (_InboxItem item) =>
              item.id == id ? item.copyWith(isUnread: nextUnread) : item,
        )
        .toList();

    setState(() {
      _items = nextItems;
      _featuredItemId = _pickFeaturedItemId(
        nextItems,
        preferredId: _featuredItemId,
      );
      _statusMessage = nextUnread
          ? '"${targetItem.title}" 已重新标记为未读。'
          : '"${targetItem.title}" 已标记为已读。';
    });
  }

  void _archiveItem(String id) {
    final _InboxItem? targetItem = _findItem(id);
    if (targetItem == null) {
      return;
    }

    _removeItem(id, message: '已将 "${targetItem.title}" 归档。');
  }

  void _removeItem(String id, {required String message}) {
    final _InboxItem? targetItem = _findItem(id);
    if (targetItem == null) {
      return;
    }

    final List<_InboxItem> nextItems = _items
        .where((_InboxItem item) => item.id != id)
        .toList();

    setState(() {
      _items = nextItems;
      _featuredItemId = _pickFeaturedItemId(
        nextItems,
        preferredId: _featuredItemId == id ? null : _featuredItemId,
      );
      _statusMessage = message;
    });
    _showSnackBar(message);
  }

  void _showSnackBar(String message) {
    final ScaffoldMessengerState scaffoldMessenger = ScaffoldMessenger.of(
      context,
    );
    scaffoldMessenger
      ..hideCurrentSnackBar()
      ..showSnackBar(
        SnackBar(content: Text(message), behavior: SnackBarBehavior.floating),
      );
  }

  int get _pinnedCount {
    return _items.where((_InboxItem item) => item.isPinned).length;
  }

  _InboxItem? _findItem(String? id) {
    if (id == null) {
      return null;
    }

    for (final _InboxItem item in _items) {
      if (item.id == id) {
        return item;
      }
    }

    return null;
  }

  String? _pickFeaturedItemId(List<_InboxItem> items, {String? preferredId}) {
    if (items.isEmpty) {
      return null;
    }

    if (preferredId != null) {
      for (final _InboxItem item in items) {
        if (item.id == preferredId) {
          return preferredId;
        }
      }
    }

    return items.first.id;
  }

  List<_InboxItem> _buildInitialItems() {
    return _sortItems(const <_InboxItem>[
      _InboxItem(
        id: 'design-review',
        order: 0,
        title: '设计评审纪要',
        preview: '把今天的结论同步给客户端同学，并在下午四点前确认剩余风险项。',
        tag: 'Inbox',
        timeLabel: '09:30',
        icon: Icons.design_services_rounded,
        accentColor: Color(0xFF0F766E),
        isPinned: true,
        isUnread: true,
      ),
      _InboxItem(
        id: 'code-review',
        order: 1,
        title: '代码走查提醒',
        preview: '左右滑动看看置顶、已读、归档和删除动作如何组合在一条列表项里。',
        tag: 'Review',
        timeLabel: '10:15',
        icon: Icons.rate_review_rounded,
        accentColor: Color(0xFF2563EB),
        isPinned: false,
        isUnread: true,
      ),
      _InboxItem(
        id: 'release-plan',
        order: 2,
        title: '版本发布清单',
        preview: '右侧动作区一般适合放归档、删除这类收尾动作，避免误触主内容。',
        tag: 'Release',
        timeLabel: '11:05',
        icon: Icons.rocket_launch_rounded,
        accentColor: Color(0xFF7C3AED),
        isPinned: false,
        isUnread: false,
      ),
      _InboxItem(
        id: 'ops-follow-up',
        order: 3,
        title: '性能巡检反馈',
        preview: '可以切换 Scroll、Drawer、Behind、Stretch 四种 motion，对比动作面板动画差异。',
        tag: 'Ops',
        timeLabel: '13:20',
        icon: Icons.monitor_heart_rounded,
        accentColor: Color(0xFFEA580C),
        isPinned: false,
        isUnread: false,
      ),
      _InboxItem(
        id: 'slidable-demo',
        order: 4,
        title: '新包接入实验',
        preview: '顶部按钮会通过 SlidableController 程序化展开推荐项，适合讲解 controller 用法。',
        tag: 'Demo',
        timeLabel: '15:40',
        icon: Icons.science_rounded,
        accentColor: Color(0xFFDC2626),
        isPinned: false,
        isUnread: true,
      ),
    ]);
  }

  List<_InboxItem> _sortItems(List<_InboxItem> items) {
    final List<_InboxItem> nextItems = List<_InboxItem>.from(items);
    nextItems.sort((_InboxItem left, _InboxItem right) {
      if (left.isPinned != right.isPinned) {
        return left.isPinned ? -1 : 1;
      }

      return left.order.compareTo(right.order);
    });
    return nextItems;
  }
}

class _MetricChip extends StatelessWidget {
  const _MetricChip({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFDCE3F0)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            label,
            style: theme.textTheme.bodySmall?.copyWith(color: Colors.black54),
          ),
          const SizedBox(height: 4),
          Text(
            value,
            style: theme.textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }
}

class _InboxItemCard extends StatelessWidget {
  const _InboxItemCard({
    required this.item,
    required this.index,
    required this.isFeatured,
  });

  final _InboxItem item;
  final int index;
  final bool isFeatured;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(
          color: isFeatured ? const Color(0xFF99F6E4) : const Color(0xFFE2E8F0),
          width: isFeatured ? 1.5 : 1,
        ),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120F172A),
            blurRadius: 16,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Container(
                  width: 44,
                  height: 44,
                  decoration: BoxDecoration(
                    color: item.accentColor.withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  alignment: Alignment.center,
                  child: Icon(item.icon, color: item.accentColor),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Row(
                        children: <Widget>[
                          Expanded(
                            child: Text(
                              item.title,
                              style: theme.textTheme.titleMedium?.copyWith(
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            item.timeLabel,
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: Colors.black45,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 6),
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        crossAxisAlignment: WrapCrossAlignment.center,
                        children: <Widget>[
                          _ItemBadge(
                            label: item.tag,
                            backgroundColor: item.accentColor.withValues(
                              alpha: 0.12,
                            ),
                            foregroundColor: item.accentColor,
                          ),
                          if (item.isPinned)
                            const _ItemBadge(
                              label: 'Pinned',
                              backgroundColor: Color(0xFFEEF2FF),
                              foregroundColor: Color(0xFF4338CA),
                            ),
                          if (item.isUnread)
                            const _ItemBadge(
                              label: 'Unread',
                              backgroundColor: Color(0xFFFFF7ED),
                              foregroundColor: Color(0xFFC2410C),
                            ),
                          if (isFeatured)
                            const _ItemBadge(
                              label: 'Controller',
                              backgroundColor: Color(0xFFECFDF5),
                              foregroundColor: Color(0xFF047857),
                            ),
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 14),
            Text(
              item.preview,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: Colors.black87,
                height: 1.45,
              ),
            ),
            const SizedBox(height: 14),
            Row(
              children: <Widget>[
                Text(
                  '条目 #${index + 1}',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: Colors.black54,
                  ),
                ),
                const Spacer(),
                const Icon(
                  Icons.swipe_rounded,
                  size: 18,
                  color: Color(0xFF94A3B8),
                ),
                const SizedBox(width: 6),
                Text(
                  '向两侧滑动',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: const Color(0xFF64748B),
                    fontWeight: FontWeight.w600,
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

class _ItemBadge extends StatelessWidget {
  const _ItemBadge({
    required this.label,
    required this.backgroundColor,
    required this.foregroundColor,
  });

  final String label;
  final Color backgroundColor;
  final Color foregroundColor;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: foregroundColor,
          fontSize: 12,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _InboxItem {
  const _InboxItem({
    required this.id,
    required this.order,
    required this.title,
    required this.preview,
    required this.tag,
    required this.timeLabel,
    required this.icon,
    required this.accentColor,
    required this.isPinned,
    required this.isUnread,
  });

  final String id;
  final int order;
  final String title;
  final String preview;
  final String tag;
  final String timeLabel;
  final IconData icon;
  final Color accentColor;
  final bool isPinned;
  final bool isUnread;

  _InboxItem copyWith({bool? isPinned, bool? isUnread}) {
    return _InboxItem(
      id: id,
      order: order,
      title: title,
      preview: preview,
      tag: tag,
      timeLabel: timeLabel,
      icon: icon,
      accentColor: accentColor,
      isPinned: isPinned ?? this.isPinned,
      isUnread: isUnread ?? this.isUnread,
    );
  }
}

enum _SlidableMotionType {
  scroll('Scroll', '按钮像从卡片外侧滑入，动作区会跟着滑动过程一起露出来，视觉最直接。'),
  drawer('Drawer', '按钮会像抽屉一样依次展开，更容易看出动作区是从边缘被拉开的。'),
  behind('Behind', '按钮固定在内容后面，前景卡片滑开后把它们露出来，层次感最明显。'),
  stretch('Stretch', '按钮会从较窄的状态被拉伸展开，展开过程会比其他模式更有形变感。');

  const _SlidableMotionType(this.label, this.description);

  final String label;
  final String description;
}
