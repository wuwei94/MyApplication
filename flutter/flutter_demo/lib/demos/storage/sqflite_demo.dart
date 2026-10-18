import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/storage/sqflite/sqflite_task_database.dart';

/// Sqflite — 手写 SQL 的关系型存储（与 Drift 页同轴对照）
///
/// 核心机制与避坑点：
/// 1. 无代码生成：建表、迁移、CRUD 全部手写 SQL；布尔值在 SQLite 里是 0 / 1。
/// 2. 事务内必须使用回调给出的 `txn`，用外层 `db` 操作会绕过事务。
/// 3. 仅支持 Android / iOS / macOS；Windows / Linux / Web 需 `sqflite_common_ffi`。
///
/// 官方参考：
/// https://pub.dev/packages/sqflite
class SqfliteDemoPage extends BasicResponsePage {
  const SqfliteDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<SqfliteDemoPage> createState() =>
      _SqfliteDemoPageState();
}

class _SqfliteDemoPageState extends BasicResponsePageState<SqfliteDemoPage> {
  @override
  void initState() {
    super.initState();
    showDescription('Sqflite 示例：手写 SQL 的查询 / 写入 / 批量 / 事务 / 删除');
    _queryTasks();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 查询任务列表',
        '2. 参数化查询未完成任务',
        '3. 写入示例任务',
        '4. 批量写入三条',
        '5. 切换首条完成状态',
        '6. 事务写入两条',
        '7. 删除首条任务',
        '8. 清空全部任务',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _queryTasks();
      case 1:
        _queryUndoneTasks();
      case 2:
        _insertTask();
      case 3:
        _insertBatch();
      case 4:
        _toggleFirstDone();
      case 5:
        _insertInTransaction();
      case 6:
        _removeFirstTask();
      case 7:
        _clearAll();
    }
  }

  Future<SqfliteTaskDatabase> _requireDatabase() {
    return SqfliteTaskDatabase.create();
  }

  Future<SqfliteTask?> _firstTask(SqfliteTaskDatabase database) async {
    final List<SqfliteTask> tasks = await database.queryAll();
    return tasks.isEmpty ? null : tasks.first;
  }

  void _logTasks(List<SqfliteTask> tasks) {
    appendLog('✓ count = ${tasks.length}');
    for (final SqfliteTask task in tasks) {
      appendLog(
        '  #${task.id} ${task.title} done=${task.isDone} '
        'starred=${task.isStarred} priority=${task.priority}',
      );
    }
  }

  Future<void> _queryTasks() async {
    appendLog('→ [Query] db.query(tasks)');
    try {
      final SqfliteTaskDatabase database = await _requireDatabase();
      _logTasks(await database.queryAll());
    } catch (error) {
      appendLog('✗ 查询失败: $error');
    }
  }

  Future<void> _queryUndoneTasks() async {
    appendLog("→ [Query] db.rawQuery(WHERE is_done = ?), args = [0]");
    try {
      final SqfliteTaskDatabase database = await _requireDatabase();
      _logTasks(await database.queryByDone(isDone: false));
    } catch (error) {
      appendLog('✗ 查询失败: $error');
    }
  }

  Future<void> _insertTask() async {
    final int seed = DateTime.now().millisecondsSinceEpoch % 10000;
    appendLog('→ [Insert] db.insert(tasks, title: 示例任务 $seed)');
    try {
      final SqfliteTaskDatabase database = await _requireDatabase();
      final int rowId = await database.insertTask(
        title: '示例任务 $seed',
        detail: '控制台写入的 sqflite 演示记录',
        priority: 2,
      );
      appendLog('✓ rowId = $rowId');
    } catch (error) {
      appendLog('✗ 写入失败: $error');
    }
  }

  Future<void> _insertBatch() async {
    appendLog('→ [Batch] batch.insert × 3 + commit()');
    try {
      final SqfliteTaskDatabase database = await _requireDatabase();
      final DateTime now = DateTime.now();
      await database.insertBatch(<SqfliteTask>[
        SqfliteTask(
          title: '批量任务 A',
          detail: 'batch 内多条 insert 一次提交',
          priority: 3,
          updatedAt: now,
        ),
        SqfliteTask(
          title: '批量任务 B',
          detail: '减少跨通道往返次数',
          priority: 2,
          updatedAt: now,
        ),
        SqfliteTask(
          title: '批量任务 C',
          detail: 'commit(noResult: true) 只返回成功与否',
          priority: 1,
          isStarred: true,
          updatedAt: now,
        ),
      ]);
      appendLog('✓ 已批量写入 3 条');
    } catch (error) {
      appendLog('✗ 批量写入失败: $error');
    }
  }

  Future<void> _toggleFirstDone() async {
    appendLog('→ [Update] db.update(tasks, WHERE id = ?)');
    try {
      final SqfliteTaskDatabase database = await _requireDatabase();
      final SqfliteTask? task = await _firstTask(database);
      if (task == null || task.id == null) {
        appendLog('✗ 无任务可操作');
        return;
      }
      final int rows = await database.updateDone(
        id: task.id!,
        isDone: !task.isDone,
      );
      appendLog('✓ affected rows = $rows, isDone → ${!task.isDone}');
    } catch (error) {
      appendLog('✗ 更新失败: $error');
    }
  }

  Future<void> _insertInTransaction() async {
    appendLog('→ [Transaction] db.transaction((txn) => txn.insert × 2)');
    try {
      final SqfliteTaskDatabase database = await _requireDatabase();
      final DateTime now = DateTime.now();
      await database.insertInTransaction(<SqfliteTask>[
        SqfliteTask(
          title: '事务任务 A',
          detail: '事务内要么全部成功，要么全部回滚',
          priority: 2,
          updatedAt: now,
        ),
        SqfliteTask(
          title: '事务任务 B',
          detail: '回调里必须用 txn 而不是外层 db',
          priority: 2,
          updatedAt: now,
        ),
      ]);
      appendLog('✓ 事务已提交 2 条');
    } catch (error) {
      appendLog('✗ 事务回滚: $error');
    }
  }

  Future<void> _removeFirstTask() async {
    appendLog('→ [Delete] db.delete(tasks, WHERE id = ?)');
    try {
      final SqfliteTaskDatabase database = await _requireDatabase();
      final SqfliteTask? task = await _firstTask(database);
      if (task == null || task.id == null) {
        appendLog('✗ 无任务可操作');
        return;
      }
      final int rows = await database.deleteTask(task.id!);
      appendLog('✓ affected rows = $rows, 已删除 #${task.id}');
    } catch (error) {
      appendLog('✗ 删除失败: $error');
    }
  }

  Future<void> _clearAll() async {
    appendLog('→ [Clear] db.delete(tasks)');
    try {
      final SqfliteTaskDatabase database = await _requireDatabase();
      await database.clearAll();
      appendLog('✓ 全部任务已清空');
    } catch (error) {
      appendLog('✗ 清空失败: $error');
    }
  }
}
