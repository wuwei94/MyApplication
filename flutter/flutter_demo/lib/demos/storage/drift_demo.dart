import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/storage/drift/drift_task_database.dart';

/// Drift — SQL 表定义与 type-safe 查询/变更
///
/// 核心机制与避坑点：
/// 1. 生成式 ORM：表结构在 `DriftTasks`，`drift_dev` 生成 `DriftTask` 数据类；schemaVersion 变更需迁移。
/// 2. 观察查询：`watch()` 推送结果列表；变更走 `Companion` / `copyWith` + `update().replace()`。
///
/// 官方参考：
/// https://pub.dev/packages/drift
class DriftDemoPage extends BasicResponsePage {
  const DriftDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<DriftDemoPage> createState() => _DriftDemoPageState();
}

class _DriftDemoPageState extends BasicResponsePageState<DriftDemoPage> {
  @override
  void initState() {
    super.initState();
    showDescription('Drift 示例：查询 / 写入 / 更新 / 删除 / 清空');
    _queryTasks();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 查询任务列表',
        '2. 写入示例任务',
        '3. 切换首条完成状态',
        '4. 切换首条星标状态',
        '5. 删除首条任务',
        '6. 重置演示数据',
        '7. 清空全部任务',
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

  Future<DriftTaskDatabase> _requireDatabase() {
    return DriftTaskDatabase.create();
  }

  Future<DriftTask?> _firstTask(DriftTaskDatabase database) async {
    final List<DriftTask> tasks = await database.watchTasks().first;
    return tasks.isEmpty ? null : tasks.first;
  }

  Future<void> _queryTasks() async {
    appendLog('→ [Query] watchTasks()');
    try {
      final DriftTaskDatabase database = await _requireDatabase();
      final List<DriftTask> tasks = await database.watchTasks().first;
      appendLog('✓ count = ${tasks.length}');
      for (final DriftTask task in tasks) {
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
    appendLog('→ [Write] addTask(title: 示例任务 $seed)');
    try {
      final DriftTaskDatabase database = await _requireDatabase();
      await database.addTask(
        title: '示例任务 $seed',
        detail: '控制台写入的 Drift 演示记录',
        priority: 2,
      );
      appendLog('✓ 已写入示例任务');
    } catch (error) {
      appendLog('✗ 写入失败: $error');
    }
  }

  Future<void> _toggleFirstDone() async {
    appendLog('→ [Update] toggleDone(first)');
    try {
      final DriftTaskDatabase database = await _requireDatabase();
      final DriftTask? task = await _firstTask(database);
      if (task == null) {
        appendLog('✗ 无任务可操作');
        return;
      }
      await database.toggleDone(task);
      appendLog('✓ #${task.id} isDone → ${!task.isDone}');
    } catch (error) {
      appendLog('✗ 更新失败: $error');
    }
  }

  Future<void> _toggleFirstStarred() async {
    appendLog('→ [Update] toggleStarred(first)');
    try {
      final DriftTaskDatabase database = await _requireDatabase();
      final DriftTask? task = await _firstTask(database);
      if (task == null) {
        appendLog('✗ 无任务可操作');
        return;
      }
      await database.toggleStarred(task);
      appendLog('✓ #${task.id} isStarred → ${!task.isStarred}');
    } catch (error) {
      appendLog('✗ 更新失败: $error');
    }
  }

  Future<void> _removeFirstTask() async {
    appendLog('→ [Delete] removeTask(first)');
    try {
      final DriftTaskDatabase database = await _requireDatabase();
      final DriftTask? task = await _firstTask(database);
      if (task == null) {
        appendLog('✗ 无任务可操作');
        return;
      }
      await database.removeTask(task.id);
      appendLog('✓ 已删除 #${task.id}');
    } catch (error) {
      appendLog('✗ 删除失败: $error');
    }
  }

  Future<void> _resetDemoData() async {
    appendLog('→ [Reset] resetWithDemoData()');
    try {
      final DriftTaskDatabase database = await _requireDatabase();
      await database.resetWithDemoData();
      appendLog('✓ 已重置为演示数据');
    } catch (error) {
      appendLog('✗ 重置失败: $error');
    }
  }

  Future<void> _clearAll() async {
    appendLog('→ [Clear] clearAll()');
    try {
      final DriftTaskDatabase database = await _requireDatabase();
      await database.clearAll();
      appendLog('✓ 全部任务已清空');
    } catch (error) {
      appendLog('✗ 清空失败: $error');
    }
  }
}
