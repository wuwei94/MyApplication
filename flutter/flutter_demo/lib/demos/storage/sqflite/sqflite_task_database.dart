import 'dart:io';

import 'package:path_provider/path_provider.dart';
import 'package:sqflite/sqflite.dart';

/// sqflite 任务表：建表、迁移与 CRUD 全部是手写 SQL。
///
/// 与 Drift 示例共用同一套 task 字段与演示数据，便于横向对照两种写法：
/// Drift 走代码生成 + 类型安全 DSL，这里走 `CREATE TABLE` + `db.query/rawQuery`。
class SqfliteTaskDatabase {
  SqfliteTaskDatabase._(this._db);

  static const String _table = 'tasks';
  static const String _fileName = 'sqflite_task_demo.db';

  /// 版本号变更即触发 [openDatabase] 的 onCreate / onUpgrade。
  /// v2 追加 is_starred 列，用于演示 ALTER TABLE 迁移。
  static const int _version = 2;

  final Database _db;

  static Future<SqfliteTaskDatabase>? _createFuture;

  /// 进程内单例：首次调用完成建表、迁移与演示数据写入。
  static Future<SqfliteTaskDatabase> create() {
    return _createFuture ??= _open();
  }

  static Future<SqfliteTaskDatabase> _open() async {
    final Directory directory = await getApplicationSupportDirectory();
    final String path = '${directory.path}/$_fileName';
    final Database database = await openDatabase(
      path,
      version: _version,
      onCreate: (Database db, int version) async {
        await db.execute('''
CREATE TABLE $_table (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  detail TEXT NOT NULL,
  priority INTEGER NOT NULL,
  is_done INTEGER NOT NULL DEFAULT 0,
  is_starred INTEGER NOT NULL DEFAULT 0,
  updated_at INTEGER NOT NULL
)
''');
      },
      onUpgrade: (Database db, int oldVersion, int newVersion) async {
        if (oldVersion < 2) {
          // 只追加列、不重建表，既有数据自动补默认值。
          await db.execute(
            'ALTER TABLE $_table '
            'ADD COLUMN is_starred INTEGER NOT NULL DEFAULT 0',
          );
        }
      },
    );

    final SqfliteTaskDatabase store = SqfliteTaskDatabase._(database);
    await store.ensureDemoData();
    return store;
  }

  /// 全量查询：`query()` 是 sqflite 对 `SELECT *` 的封装，排序仍需写列名。
  Future<List<SqfliteTask>> queryAll() async {
    final List<Map<String, Object?>> rows = await _db.query(
      _table,
      orderBy: 'updated_at DESC',
    );
    return rows.map(SqfliteTask.fromMap).toList(growable: false);
  }

  /// 参数化查询：值通过占位符绑定，不要拼进 SQL 字符串。
  Future<List<SqfliteTask>> queryByDone({required bool isDone}) async {
    final List<Map<String, Object?>> rows = await _db.rawQuery(
      'SELECT * FROM $_table WHERE is_done = ? '
      'ORDER BY priority DESC, updated_at DESC',
      <Object?>[isDone ? 1 : 0],
    );
    return rows.map(SqfliteTask.fromMap).toList(growable: false);
  }

  Future<int> count() async {
    final List<Map<String, Object?>> rows = await _db.rawQuery(
      'SELECT COUNT(*) AS total FROM $_table',
    );
    return (rows.first['total'] as int?) ?? 0;
  }

  /// 插入一条，返回自增主键；布尔值在 SQLite 里存 0 / 1。
  Future<int> insertTask({
    required String title,
    required String detail,
    required int priority,
    bool isStarred = false,
  }) {
    final int now = DateTime.now().millisecondsSinceEpoch;
    return _db.insert(_table, <String, Object?>{
      'title': title,
      'detail': detail,
      'priority': priority,
      'is_done': 0,
      'is_starred': isStarred ? 1 : 0,
      'updated_at': now,
    });
  }

  /// 批量写入：`batch` 在单个事务内提交，比逐条 insert 少一次通道往返。
  Future<void> insertBatch(List<SqfliteTask> tasks) async {
    final Batch batch = _db.batch();
    for (final SqfliteTask task in tasks) {
      batch.insert(_table, task.toMap());
    }
    await batch.commit(noResult: true);
  }

  /// 显式事务：回调内必须使用参数给出的 [Transaction]，用外层 db 会绕过事务。
  Future<void> insertInTransaction(List<SqfliteTask> tasks) {
    return _db.transaction((Transaction txn) async {
      for (final SqfliteTask task in tasks) {
        await txn.insert(_table, task.toMap());
      }
    });
  }

  Future<int> updateDone({required int id, required bool isDone}) {
    return _db.update(
      _table,
      <String, Object?>{
        'is_done': isDone ? 1 : 0,
        'updated_at': DateTime.now().millisecondsSinceEpoch,
      },
      where: 'id = ?',
      whereArgs: <Object?>[id],
    );
  }

  Future<int> deleteTask(int id) {
    return _db.delete(_table, where: 'id = ?', whereArgs: <Object?>[id]);
  }

  Future<void> clearAll() async {
    await _db.delete(_table);
  }

  Future<void> ensureDemoData() async {
    if (await count() > 0) {
      return;
    }
    await insertBatch(_buildDemoTasks());
  }

  Future<void> resetWithDemoData() async {
    await clearAll();
    await insertBatch(_buildDemoTasks());
  }

  static List<SqfliteTask> _buildDemoTasks() {
    final DateTime base = DateTime.now();
    return <SqfliteTask>[
      SqfliteTask(
        title: '整理 sqflite 与 Drift 的对照笔记',
        detail: '同一份 task 表，一边手写 SQL 一边写 DSL',
        priority: 3,
        isStarred: true,
        updatedAt: base.subtract(const Duration(minutes: 3)),
      ),
      SqfliteTask(
        title: '验证 onUpgrade 迁移',
        detail: 'v1 → v2 只用 ALTER TABLE 追加 is_starred 列',
        priority: 2,
        updatedAt: base.subtract(const Duration(minutes: 2)),
      ),
      SqfliteTask(
        title: '补充事务写入示例',
        detail: 'transaction 内必须使用回调给出的 txn',
        priority: 1,
        isDone: true,
        updatedAt: base.subtract(const Duration(minutes: 1)),
      ),
    ];
  }
}

/// 任务记录：SQLite 只有整数，布尔值通过 0 / 1 映射。
class SqfliteTask {
  const SqfliteTask({
    this.id,
    required this.title,
    required this.detail,
    required this.priority,
    this.isDone = false,
    this.isStarred = false,
    required this.updatedAt,
  });

  factory SqfliteTask.fromMap(Map<String, Object?> map) {
    return SqfliteTask(
      id: map['id'] as int?,
      title: map['title'] as String? ?? '',
      detail: map['detail'] as String? ?? '',
      priority: map['priority'] as int? ?? 0,
      isDone: (map['is_done'] as int? ?? 0) == 1,
      isStarred: (map['is_starred'] as int? ?? 0) == 1,
      updatedAt: DateTime.fromMillisecondsSinceEpoch(
        map['updated_at'] as int? ?? 0,
      ),
    );
  }

  final int? id;
  final String title;
  final String detail;
  final int priority;
  final bool isDone;
  final bool isStarred;
  final DateTime updatedAt;

  Map<String, Object?> toMap() {
    return <String, Object?>{
      if (id != null) 'id': id,
      'title': title,
      'detail': detail,
      'priority': priority,
      'is_done': isDone ? 1 : 0,
      'is_starred': isStarred ? 1 : 0,
      'updated_at': updatedAt.millisecondsSinceEpoch,
    };
  }
}
