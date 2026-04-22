import 'dart:async';
import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:flutter/widgets.dart' show WidgetsFlutterBinding;
import 'package:path_provider/path_provider.dart';

part 'drift_task_database.g.dart';

class DriftTasks extends Table {
  IntColumn get id => integer().autoIncrement()();

  TextColumn get title => text()();

  TextColumn get detail => text()();

  IntColumn get priority => integer()();

  BoolColumn get isDone => boolean().withDefault(const Constant(false))();

  BoolColumn get isStarred => boolean().withDefault(const Constant(false))();

  DateTimeColumn get updatedAt => dateTime()();
}

@DriftDatabase(tables: [DriftTasks])
class DriftTaskDatabase extends _$DriftTaskDatabase {
  DriftTaskDatabase._() : super(_openConnection());

  static DriftTaskDatabase? _instance;
  static Future<DriftTaskDatabase>? _createFuture;

  static Future<DriftTaskDatabase> create() {
    return _createFuture ??= _open();
  }

  static Future<DriftTaskDatabase> _open() async {
    final DriftTaskDatabase database = _instance ??= DriftTaskDatabase._();
    await database.ensureDemoData();
    return database;
  }

  @override
  int get schemaVersion => 1;

  Stream<List<DriftTask>> watchTasks() {
    final Selectable<DriftTask> query = (select(driftTasks)
      ..orderBy(<OrderingTerm Function(DriftTasks)>[
        (DriftTasks table) => OrderingTerm.desc(table.updatedAt),
      ]));
    return query.watch();
  }

  Future<bool> isEmpty() async {
    final Expression<int> countExpression = driftTasks.id.count();
    final Selectable<TypedResult> countQuery = selectOnly(driftTasks)
      ..addColumns(<Expression<Object>>[countExpression]);
    final TypedResult result = await countQuery.getSingle();
    return (result.read(countExpression) ?? 0) == 0;
  }

  Future<void> ensureDemoData() async {
    if (!await isEmpty()) {
      return;
    }

    await batch((Batch batch) {
      batch.insertAll(driftTasks, _buildDemoTasks());
    });
  }

  Future<void> addTask({
    required String title,
    required String detail,
    required int priority,
  }) async {
    await into(driftTasks).insert(
      DriftTasksCompanion.insert(
        title: title,
        detail: detail,
        priority: priority,
        updatedAt: DateTime.now(),
      ),
    );
  }

  Future<void> toggleDone(DriftTask task) async {
    await update(driftTasks).replace(
      task.copyWith(
        isDone: !task.isDone,
        updatedAt: DateTime.now(),
      ),
    );
  }

  Future<void> toggleStarred(DriftTask task) async {
    await update(driftTasks).replace(
      task.copyWith(
        isStarred: !task.isStarred,
        updatedAt: DateTime.now(),
      ),
    );
  }

  Future<void> removeTask(int id) async {
    await (delete(driftTasks)
      ..where((DriftTasks table) => table.id.equals(id))).go();
  }

  Future<void> clearAll() async {
    await delete(driftTasks).go();
  }

  Future<void> resetWithDemoData() async {
    await transaction(() async {
      await clearAll();
      await batch((Batch batch) {
        batch.insertAll(driftTasks, _buildDemoTasks());
      });
    });
  }

  List<DriftTasksCompanion> _buildDemoTasks() {
    final DateTime now = DateTime.now();
    return <DriftTasksCompanion>[
      DriftTasksCompanion.insert(
        title: '完善帖子草稿箱',
        detail: '把本地草稿、封面图路径和最近编辑时间一起存进 Drift。',
        priority: 3,
        isStarred: const Value<bool>(true),
        updatedAt: now.subtract(const Duration(minutes: 18)),
      ),
      DriftTasksCompanion.insert(
        title: '同步消息红点状态',
        detail: '演示 SQL 查询与 watch()，红点被消费后列表会自动刷新。',
        priority: 2,
        updatedAt: now.subtract(const Duration(hours: 2)),
      ),
      DriftTasksCompanion.insert(
        title: '缓存社区首页卡片',
        detail: '适合离线展示标题、摘要、作者和交互状态等结构化数据。',
        priority: 2,
        isDone: const Value<bool>(true),
        updatedAt: now.subtract(const Duration(days: 1, hours: 3)),
      ),
      DriftTasksCompanion.insert(
        title: '记录图片上传任务',
        detail: '上传成功后更新状态，失败时保留本地记录便于继续重试。',
        priority: 1,
        updatedAt: now.subtract(const Duration(days: 2, hours: 5)),
      ),
    ];
  }
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    WidgetsFlutterBinding.ensureInitialized();
    final Directory directory = await getApplicationSupportDirectory();
    final File databaseFile = File('${directory.path}/drift_task_demo.sqlite');
    return NativeDatabase.createInBackground(databaseFile);
  });
}
