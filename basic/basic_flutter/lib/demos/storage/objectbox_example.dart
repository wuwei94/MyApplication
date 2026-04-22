import 'dart:async';

import 'package:basic_flutter/demos/storage/objectbox/objectbox_task_entity.dart';
import 'package:basic_flutter/demos/storage/objectbox/objectbox_task_store.dart';
import 'package:flutter/material.dart';
import 'package:timeago/timeago.dart' as timeago;

/// ObjectBox
/// https://pub.dev/packages/objectbox
class ObjectBoxDemoPage extends StatelessWidget {
  const ObjectBoxDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ObjectBoxDemoView(title: title);
  }
}

class ObjectBoxDemoView extends StatefulWidget {
  const ObjectBoxDemoView({super.key, required this.title});

  final String title;

  @override
  State<ObjectBoxDemoView> createState() => _ObjectBoxDemoViewState();
}

class _ObjectBoxDemoViewState extends State<ObjectBoxDemoView> {
  static const Color _accentColor = Color(0xFF2563EB);
  static const List<_PriorityOption> _priorityOptions = <_PriorityOption>[
    _PriorityOption(value: 1, label: 'Low', description: '适合低频背景任务'),
    _PriorityOption(value: 2, label: 'Medium', description: '日常页面功能默认级别'),
    _PriorityOption(value: 3, label: 'High', description: '优先处理的本地数据'),
  ];

  final TextEditingController _titleController = TextEditingController();
  final TextEditingController _detailController = TextEditingController();
  late final Future<ObjectBoxTaskStore> _storeFuture;

  _ObjectBoxTaskFilter _selectedFilter = _ObjectBoxTaskFilter.all;
  int _selectedPriority = 2;
  bool _isMutating = false;
  String _statusMessage = '正在准备 ObjectBox Store，稍后会自动加载演示数据。';

  @override
  void initState() {
    super.initState();
    timeago.setLocaleMessages('zh', timeago.ZhMessages());
    timeago.setLocaleMessages('zh_CN', timeago.ZhCnMessages());
    _storeFuture = _prepareStore();
  }

  @override
  void dispose() {
    _titleController.dispose();
    _detailController.dispose();
    super.dispose();
  }

  Future<ObjectBoxTaskStore> _prepareStore() async {
    final ObjectBoxTaskStore store = await ObjectBoxTaskStore.create();
    if (!mounted) {
      return store;
    }

    setState(() {
      _statusMessage =
          'ObjectBox 已就绪，当前示例通过 watch() 自动监听对象变化。';
    });
    return store;
  }

  Future<void> _runMutation({
    required String successMessage,
    required void Function(ObjectBoxTaskStore store) mutation,
  }) async {
    if (_isMutating) {
      return;
    }

    setState(() {
      _isMutating = true;
    });

    try {
      final ObjectBoxTaskStore store = await _storeFuture;
      mutation(store);
      if (!mounted) {
        return;
      }

      setState(() {
        _statusMessage = successMessage;
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _statusMessage = 'ObjectBox 操作失败：$error';
      });
    } finally {
      if (mounted) {
        setState(() {
          _isMutating = false;
        });
      }
    }
  }

  Future<void> _createTask() async {
    final String title = _titleController.text.trim();
    final String detail = _detailController.text.trim();

    if (title.isEmpty) {
      setState(() {
        _statusMessage = '先输入一个标题，再把对象写入 ObjectBox。';
      });
      return;
    }

    await _runMutation(
      successMessage: '已创建一条新任务对象，列表会立即自动刷新。',
      mutation: (ObjectBoxTaskStore store) {
        store.addTask(
          title: title,
          detail: detail.isEmpty ? '这是一条刚写入 ObjectBox 的示例记录。' : detail,
          priority: _selectedPriority,
        );
      },
    );

    if (!mounted) {
      return;
    }

    _titleController.clear();
    _detailController.clear();
  }

  Future<void> _resetDemoData() async {
    await _runMutation(
      successMessage: '已重置为一组演示数据，方便继续体验查询和更新。',
      mutation: (ObjectBoxTaskStore store) {
        store.resetWithDemoData();
      },
    );
  }

  Future<void> _clearAllTasks() async {
    await _runMutation(
      successMessage: 'ObjectBox 中的演示对象已清空。',
      mutation: (ObjectBoxTaskStore store) {
        store.clearAll();
      },
    );
  }

  Future<void> _toggleDone(ObjectBoxTaskEntity task) async {
    final bool nextState = !task.isDone;
    await _runMutation(
      successMessage: nextState ? '任务已标记完成。' : '任务已恢复为待处理。',
      mutation: (ObjectBoxTaskStore store) {
        store.toggleDone(task);
      },
    );
  }

  Future<void> _toggleStarred(ObjectBoxTaskEntity task) async {
    final bool nextState = !task.isStarred;
    await _runMutation(
      successMessage: nextState ? '这条任务已加入星标。' : '这条任务已取消星标。',
      mutation: (ObjectBoxTaskStore store) {
        store.toggleStarred(task);
      },
    );
  }

  Future<void> _deleteTask(ObjectBoxTaskEntity task) async {
    await _runMutation(
      successMessage: '已删除一条对象记录。',
      mutation: (ObjectBoxTaskStore store) {
        store.removeTask(task.id);
      },
    );
  }

  List<ObjectBoxTaskEntity> _applyFilter(List<ObjectBoxTaskEntity> tasks) {
    return tasks
        .where((ObjectBoxTaskEntity task) => _selectedFilter.matches(task))
        .toList();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    return FutureBuilder<ObjectBoxTaskStore>(
      future: _storeFuture,
      builder: (
        BuildContext context,
        AsyncSnapshot<ObjectBoxTaskStore> storeSnapshot,
      ) {
        if (storeSnapshot.connectionState != ConnectionState.done) {
          return const Center(child: CircularProgressIndicator());
        }

        if (storeSnapshot.hasError) {
          return _buildCenteredState(
            icon: Icons.error_outline,
            title: 'ObjectBox 初始化失败',
            description: '${storeSnapshot.error}',
          );
        }

        final ObjectBoxTaskStore store = storeSnapshot.data!;
        return StreamBuilder<List<ObjectBoxTaskEntity>>(
          stream: store.watchTasks(),
          builder: (
            BuildContext context,
            AsyncSnapshot<List<ObjectBoxTaskEntity>> snapshot,
          ) {
            final List<ObjectBoxTaskEntity> tasks =
                snapshot.data ?? <ObjectBoxTaskEntity>[];
            final List<ObjectBoxTaskEntity> filteredTasks = _applyFilter(tasks);
            final int completedCount = tasks
                .where((ObjectBoxTaskEntity task) => task.isDone)
                .length;
            final int starredCount = tasks
                .where((ObjectBoxTaskEntity task) => task.isStarred)
                .length;

            return ListView(
              padding: const EdgeInsets.all(16),
              children: <Widget>[
                _ObjectBoxHeroCard(
                  accentColor: _accentColor,
                  statusMessage: _statusMessage,
                  totalCount: tasks.length,
                  completedCount: completedCount,
                  starredCount: starredCount,
                ),
                const SizedBox(height: 16),
                _buildComposerSection(),
                const SizedBox(height: 16),
                _buildFilterSection(
                  totalCount: tasks.length,
                  filteredCount: filteredTasks.length,
                ),
                const SizedBox(height: 16),
                _buildTaskListSection(filteredTasks),
              ],
            );
          },
        );
      },
    );
  }

  Widget _buildComposerSection() {
    return _SectionCard(
      title: '对象写入',
      subtitle: '输入标题和描述后直接写入 ObjectBox，下面的列表会通过 watch() 自动刷新。',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          TextField(
            controller: _titleController,
            textInputAction: TextInputAction.next,
            decoration: const InputDecoration(
              labelText: '标题',
              hintText: '例如：离线缓存帖子详情',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _detailController,
            minLines: 2,
            maxLines: 3,
            decoration: const InputDecoration(
              labelText: '描述',
              hintText: '可选：这条对象记录要保存什么内容',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 16),
          Text(
            '优先级',
            style: Theme.of(
              context,
            ).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 10),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: _priorityOptions
                .map(
                  (_PriorityOption option) => ChoiceChip(
                    selected: _selectedPriority == option.value,
                    label: Text(option.label),
                    onSelected: _isMutating
                        ? null
                        : (bool selected) {
                            if (!selected) {
                              return;
                            }
                            setState(() {
                              _selectedPriority = option.value;
                            });
                          },
                  ),
                )
                .toList(),
          ),
          const SizedBox(height: 12),
          Text(
            _priorityOptions
                .firstWhere(
                  (_PriorityOption option) => option.value == _selectedPriority,
                )
                .description,
            style: Theme.of(
              context,
            ).textTheme.bodySmall?.copyWith(color: Colors.black54),
          ),
          const SizedBox(height: 16),
          Wrap(
            spacing: 12,
            runSpacing: 12,
            children: <Widget>[
              FilledButton.icon(
                onPressed: _isMutating ? null : _createTask,
                icon: const Icon(Icons.add_task_outlined),
                label: Text(_isMutating ? '处理中...' : '写入对象'),
                style: FilledButton.styleFrom(
                  backgroundColor: _accentColor,
                  foregroundColor: Colors.white,
                ),
              ),
              OutlinedButton.icon(
                onPressed: _isMutating ? null : _resetDemoData,
                icon: const Icon(Icons.auto_fix_high_outlined),
                label: const Text('重置演示数据'),
              ),
              OutlinedButton.icon(
                onPressed: _isMutating ? null : _clearAllTasks,
                icon: const Icon(Icons.delete_outline),
                label: const Text('清空数据'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildFilterSection({
    required int totalCount,
    required int filteredCount,
  }) {
    return _SectionCard(
      title: '实时查询视图',
      subtitle:
          '当前展示 $filteredCount / $totalCount 条记录。切换筛选时，仍然沿用同一条 ObjectBox 数据流。',
      child: Wrap(
        spacing: 8,
        runSpacing: 8,
        children: _ObjectBoxTaskFilter.values
            .map(
              (_ObjectBoxTaskFilter filter) => ChoiceChip(
                selected: _selectedFilter == filter,
                label: Text(filter.label),
                onSelected: (bool selected) {
                  if (!selected) {
                    return;
                  }
                  setState(() {
                    _selectedFilter = filter;
                    _statusMessage = '已切换到“${filter.label}”视图。';
                  });
                },
              ),
            )
            .toList(),
      ),
    );
  }

  Widget _buildTaskListSection(List<ObjectBoxTaskEntity> tasks) {
    if (tasks.isEmpty) {
      return _SectionCard(
        title: '对象列表',
        subtitle: '当前筛选条件下还没有记录，可以先写入一条对象或恢复演示数据。',
        child: _buildCenteredState(
          icon: Icons.inbox_outlined,
          title: '列表为空',
          description: 'ObjectBox Store 已准备好，现在可以体验新增、更新和删除操作。',
        ),
      );
    }

    return _SectionCard(
      title: '对象列表',
      subtitle: '每一张卡片都对应一个本地对象，支持更新状态、星标和删除。',
      child: Column(
        children: tasks
            .map(
              (ObjectBoxTaskEntity task) => Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: _ObjectBoxTaskCard(
                  task: task,
                  accentColor: _accentColor,
                  relativeTime: timeago.format(task.updatedAt, locale: 'zh_CN'),
                  onToggleDone: _isMutating ? null : () => _toggleDone(task),
                  onToggleStarred: _isMutating
                      ? null
                      : () => _toggleStarred(task),
                  onDelete: _isMutating ? null : () => _deleteTask(task),
                ),
              ),
            )
            .toList(),
      ),
    );
  }

  Widget _buildCenteredState({
    required IconData icon,
    required String title,
    required String description,
  }) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 28),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Icon(icon, size: 36, color: _accentColor),
          const SizedBox(height: 12),
          Text(
            title,
            style: Theme.of(
              context,
            ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 8),
          Text(
            description,
            style: Theme.of(
              context,
            ).textTheme.bodyMedium?.copyWith(color: Colors.black54),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}

enum _ObjectBoxTaskFilter {
  all('全部'),
  pending('待处理'),
  completed('已完成'),
  starred('星标');

  const _ObjectBoxTaskFilter(this.label);

  final String label;

  bool matches(ObjectBoxTaskEntity task) {
    switch (this) {
      case _ObjectBoxTaskFilter.all:
        return true;
      case _ObjectBoxTaskFilter.pending:
        return !task.isDone;
      case _ObjectBoxTaskFilter.completed:
        return task.isDone;
      case _ObjectBoxTaskFilter.starred:
        return task.isStarred;
    }
  }
}

class _PriorityOption {
  const _PriorityOption({
    required this.value,
    required this.label,
    required this.description,
  });

  final int value;
  final String label;
  final String description;
}

class _ObjectBoxHeroCard extends StatelessWidget {
  const _ObjectBoxHeroCard({
    required this.accentColor,
    required this.statusMessage,
    required this.totalCount,
    required this.completedCount,
    required this.starredCount,
  });

  final Color accentColor;
  final String statusMessage;
  final int totalCount;
  final int completedCount;
  final int starredCount;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        gradient: const LinearGradient(
          colors: <Color>[Color(0xFF1D4ED8), Color(0xFF0F172A)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x220F172A),
            blurRadius: 24,
            offset: Offset(0, 14),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.16),
              borderRadius: BorderRadius.circular(999),
            ),
            child: const Text(
              'ObjectBox Demo',
              style: TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
          const SizedBox(height: 14),
          const Text(
            '对象写入后，页面会马上跟着数据库变化一起刷新。',
            style: TextStyle(
              color: Colors.white,
              fontSize: 20,
              fontWeight: FontWeight.w700,
              height: 1.35,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            statusMessage,
            style: const TextStyle(
              color: Color(0xFFDCE8FF),
              fontSize: 14,
              height: 1.5,
            ),
          ),
          const SizedBox(height: 18),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: <Widget>[
              _SummaryChip(
                label: '总记录',
                value: '$totalCount',
                accentColor: accentColor,
              ),
              _SummaryChip(
                label: '已完成',
                value: '$completedCount',
                accentColor: accentColor,
              ),
              _SummaryChip(
                label: '星标',
                value: '$starredCount',
                accentColor: accentColor,
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _SummaryChip extends StatelessWidget {
  const _SummaryChip({
    required this.label,
    required this.value,
    required this.accentColor,
  });

  final String label;
  final String value;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    return Container(
      constraints: const BoxConstraints(minWidth: 92),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.14),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: Colors.white.withValues(alpha: 0.12)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            value,
            style: TextStyle(
              color: Colors.white,
              fontSize: 20,
              fontWeight: FontWeight.w800,
              shadows: <Shadow>[
                Shadow(
                  color: accentColor.withValues(alpha: 0.18),
                  blurRadius: 12,
                ),
              ],
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: const TextStyle(color: Color(0xFFDCE8FF), fontSize: 12),
          ),
        ],
      ),
    );
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({
    required this.title,
    required this.subtitle,
    required this.child,
  });

  final String title;
  final String subtitle;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x140F172A),
            blurRadius: 20,
            offset: Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            title,
            style: Theme.of(
              context,
            ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 6),
          Text(
            subtitle,
            style: Theme.of(
              context,
            ).textTheme.bodyMedium?.copyWith(color: Colors.black54, height: 1.5),
          ),
          const SizedBox(height: 16),
          child,
        ],
      ),
    );
  }
}

class _ObjectBoxTaskCard extends StatelessWidget {
  const _ObjectBoxTaskCard({
    required this.task,
    required this.accentColor,
    required this.relativeTime,
    required this.onToggleDone,
    required this.onToggleStarred,
    required this.onDelete,
  });

  final ObjectBoxTaskEntity task;
  final Color accentColor;
  final String relativeTime;
  final VoidCallback? onToggleDone;
  final VoidCallback? onToggleStarred;
  final VoidCallback? onDelete;

  Color get _priorityColor {
    switch (task.priority) {
      case 3:
        return const Color(0xFFDC2626);
      case 2:
        return const Color(0xFFF59E0B);
      case 1:
        return const Color(0xFF0F766E);
    }
    return accentColor;
  }

  String get _priorityLabel {
    switch (task.priority) {
      case 3:
        return 'High';
      case 2:
        return 'Medium';
      case 1:
        return 'Low';
    }
    return 'Unknown';
  }

  @override
  Widget build(BuildContext context) {
    final TextStyle? titleStyle = Theme.of(context).textTheme.titleMedium
        ?.copyWith(
          fontWeight: FontWeight.w700,
          decoration: task.isDone ? TextDecoration.lineThrough : null,
          color: task.isDone ? Colors.black45 : const Color(0xFF0F172A),
        );

    return AnimatedContainer(
      duration: const Duration(milliseconds: 220),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: task.isDone ? const Color(0xFFF8FAFC) : const Color(0xFFFCFDFF),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: task.isStarred ? accentColor.withValues(alpha: 0.25) : const Color(0xFFE2E8F0),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: <Widget>[
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 5,
                          ),
                          decoration: BoxDecoration(
                            color: _priorityColor.withValues(alpha: 0.12),
                            borderRadius: BorderRadius.circular(999),
                          ),
                          child: Text(
                            _priorityLabel,
                            style: TextStyle(
                              color: _priorityColor,
                              fontWeight: FontWeight.w700,
                              fontSize: 12,
                            ),
                          ),
                        ),
                        if (task.isDone)
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 10,
                              vertical: 5,
                            ),
                            decoration: BoxDecoration(
                              color: const Color(0xFFDCFCE7),
                              borderRadius: BorderRadius.circular(999),
                            ),
                            child: const Text(
                              'Done',
                              style: TextStyle(
                                color: Color(0xFF166534),
                                fontWeight: FontWeight.w700,
                                fontSize: 12,
                              ),
                            ),
                          ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Text(task.title, style: titleStyle),
                    const SizedBox(height: 8),
                    Text(
                      task.detail,
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: Colors.black54,
                        height: 1.5,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 12),
              Column(
                children: <Widget>[
                  IconButton(
                    onPressed: onToggleStarred,
                    tooltip: task.isStarred ? '取消星标' : '设为星标',
                    icon: Icon(
                      task.isStarred ? Icons.star_rounded : Icons.star_outline_rounded,
                      color: task.isStarred ? const Color(0xFFF59E0B) : Colors.black38,
                    ),
                  ),
                  IconButton(
                    onPressed: onDelete,
                    tooltip: '删除',
                    icon: const Icon(
                      Icons.delete_outline_rounded,
                      color: Colors.black45,
                    ),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 14),
          Row(
            children: <Widget>[
              Text(
                '更新于 $relativeTime',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: Colors.black45,
                ),
              ),
              const Spacer(),
              TextButton.icon(
                onPressed: onToggleDone,
                icon: Icon(
                  task.isDone
                      ? Icons.restart_alt_rounded
                      : Icons.check_circle_outline_rounded,
                  color: accentColor,
                ),
                label: Text(task.isDone ? '恢复' : '完成'),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
