import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/storage/objectbox/objectbox_task_entity.dart';
import 'package:flutter_demo/demos/storage/objectbox/objectbox_task_store.dart';

/// ObjectBox — 嵌入式对象库 Box 读写与 Query watch
///
/// 核心机制与避坑点：
/// 1. 同步写：`Box.put/remove` 为同步 API，写完立即可查；`watch(triggerImmediately: true)` 自带首帧。
/// 2. 实体注解：`@Entity` + `@Id` 由 `objectbox_generator` 生成绑定；日期字段需 `@Property(type: PropertyType.date)`。
///
/// 官方参考：
/// https://pub.dev/packages/objectbox
class ObjectBoxDemoPage extends BasicResponsePage {
  const ObjectBoxDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<ObjectBoxDemoPage> createState() =>
      _ObjectBoxDemoPageState();
}

class _ObjectBoxDemoPageState
    extends BasicResponsePageState<ObjectBoxDemoPage> {
  @override
  void initState() {
    super.initState();
    showDescription('ObjectBox 示例：查询 / 写入 / 更新 / 删除 / 清空');
    _queryTasks();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 查询任务对象',
        '2. 写入示例对象',
        '3. 切换首条完成状态',
        '4. 切换首条星标状态',
        '5. 删除首条对象',
        '6. 重置演示数据',
        '7. 清空全部对象',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _queryTasks();
      case 1:
        _addTask();
      case 2:
        _toggleFirstDone();
      case 3:
        _toggleFirstStarred();
      case 4:
        _removeFirstTask();
      case 5:
        _resetDemoData();
      case 6:
        _clearAll();
    }
  }

  Future<ObjectBoxTaskStore> _requireStore() {
    return ObjectBoxTaskStore.create();
  }

  Future<ObjectBoxTaskEntity?> _firstTask(ObjectBoxTaskStore store) async {
    final List<ObjectBoxTaskEntity> tasks = await store.watchTasks().first;
    return tasks.isEmpty ? null : tasks.first;
  }

  Future<void> _queryTasks() async {
    appendLog('→ [Query] watchTasks()');
    try {
      final ObjectBoxTaskStore store = await _requireStore();
      final List<ObjectBoxTaskEntity> tasks = await store.watchTasks().first;
      appendLog('✓ count = ${tasks.length}');
      for (final ObjectBoxTaskEntity task in tasks) {
        appendLog(
          '  #${task.id} ${task.title} done=${task.isDone} '
          'starred=${task.isStarred} priority=${task.priority}',
        );
      }
    } catch (error) {
      appendLog('✗ 查询失败: $error');
    }
  }

  Future<void> _addTask() async {
    final int seed = DateTime.now().millisecondsSinceEpoch % 10000;
    appendLog('→ [Write] addTask(title: 示例对象 $seed)');
    try {
      final ObjectBoxTaskStore store = await _requireStore();
      store.addTask(
        title: '示例对象 $seed',
        detail: '控制台写入的 ObjectBox 演示对象',
        priority: 2,
      );
      appendLog('✓ 已写入示例对象');
    } catch (error) {
      appendLog('✗ 写入失败: $error');
    }
  }

  Future<void> _toggleFirstDone() async {
    appendLog('→ [Update] toggleDone(first)');
    try {
      final ObjectBoxTaskStore store = await _requireStore();
      final ObjectBoxTaskEntity? task = await _firstTask(store);
      if (task == null) {
        appendLog('✗ 无对象可操作');
        return;
      }
      store.toggleDone(task);
      appendLog('✓ #${task.id} isDone → ${task.isDone}');
    } catch (error) {
      appendLog('✗ 更新失败: $error');
    }
  }

  Future<void> _toggleFirstStarred() async {
    appendLog('→ [Update] toggleStarred(first)');
    try {
      final ObjectBoxTaskStore store = await _requireStore();
      final ObjectBoxTaskEntity? task = await _firstTask(store);
      if (task == null) {
        appendLog('✗ 无对象可操作');
        return;
      }
      store.toggleStarred(task);
      appendLog('✓ #${task.id} isStarred → ${task.isStarred}');
    } catch (error) {
      appendLog('✗ 更新失败: $error');
    }
  }

  Future<void> _removeFirstTask() async {
    appendLog('→ [Delete] removeTask(first)');
    try {
      final ObjectBoxTaskStore store = await _requireStore();
      final ObjectBoxTaskEntity? task = await _firstTask(store);
      if (task == null) {
        appendLog('✗ 无对象可操作');
        return;
      }
      store.removeTask(task.id);
      appendLog('✓ 已删除 #${task.id}');
    } catch (error) {
      appendLog('✗ 删除失败: $error');
    }
  }

  Future<void> _resetDemoData() async {
    appendLog('→ [Reset] resetWithDemoData()');
    try {
      final ObjectBoxTaskStore store = await _requireStore();
      store.resetWithDemoData();
      appendLog('✓ 已重置为演示数据');
    } catch (error) {
      appendLog('✗ 重置失败: $error');
    }
  }

  Future<void> _clearAll() async {
    appendLog('→ [Clear] clearAll()');
    try {
      final ObjectBoxTaskStore store = await _requireStore();
      store.clearAll();
      appendLog('✓ 全部对象已清空');
    } catch (error) {
      appendLog('✗ 清空失败: $error');
    }
  }
}
