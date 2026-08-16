import 'dart:async';

import 'package:flutter/widgets.dart' show WidgetsFlutterBinding;
import 'package:flutter_demo/demos/storage/isar/isar_task_record.dart';
import 'package:isar_community/isar.dart';
import 'package:path_provider/path_provider.dart';

class IsarTaskStore {
  IsarTaskStore._(this.isar);

  static const String _databaseName = 'isar_task_demo';
  static IsarTaskStore? _instance;
  static Future<IsarTaskStore>? _createFuture;

  final Isar isar;

  static Future<IsarTaskStore> create() {
    return _createFuture ??= _open();
  }

  static Future<IsarTaskStore> _open() async {
    WidgetsFlutterBinding.ensureInitialized();
    final directory = await getApplicationSupportDirectory();
    final Isar isar = await Isar.open(
      <CollectionSchema<dynamic>>[IsarTaskRecordSchema],
      directory: directory.path,
      name: _databaseName,
    );
    final IsarTaskStore store = _instance ??= IsarTaskStore._(isar);
    await store.ensureDemoData();
    return store;
  }

  Stream<List<IsarTaskRecord>> watchTasks() async* {
    yield await _loadTasks();
    await for (final _ in isar.isarTaskRecords.watchLazy()) {
      yield await _loadTasks();
    }
  }

  Future<bool> isEmpty() async {
    return await isar.isarTaskRecords.count() == 0;
  }

  Future<void> ensureDemoData() async {
    if (!await isEmpty()) {
      return;
    }

    await isar.writeTxn(() async {
      await isar.isarTaskRecords.putAll(_buildDemoTasks());
    });
  }

  Future<void> addTask({
    required String title,
    required String detail,
    required int priority,
  }) async {
    final IsarTaskRecord task = IsarTaskRecord()
      ..title = title
      ..detail = detail
      ..priority = priority
      ..updatedAt = DateTime.now();

    await isar.writeTxn(() async {
      await isar.isarTaskRecords.put(task);
    });
  }

  Future<void> toggleDone(IsarTaskRecord task) async {
    task
      ..isDone = !task.isDone
      ..updatedAt = DateTime.now();

    await isar.writeTxn(() async {
      await isar.isarTaskRecords.put(task);
    });
  }

  Future<void> toggleStarred(IsarTaskRecord task) async {
    task
      ..isStarred = !task.isStarred
      ..updatedAt = DateTime.now();

    await isar.writeTxn(() async {
      await isar.isarTaskRecords.put(task);
    });
  }

  Future<void> removeTask(int id) async {
    await isar.writeTxn(() async {
      await isar.isarTaskRecords.delete(id);
    });
  }

  Future<void> clearAll() async {
    await isar.writeTxn(() async {
      await isar.isarTaskRecords.clear();
    });
  }

  Future<void> resetWithDemoData() async {
    await isar.writeTxn(() async {
      await isar.isarTaskRecords.clear();
      await isar.isarTaskRecords.putAll(_buildDemoTasks());
    });
  }

  Future<List<IsarTaskRecord>> _loadTasks() async {
    final List<IsarTaskRecord> tasks = await isar.isarTaskRecords
        .where()
        .findAll();
    tasks.sort(
      (IsarTaskRecord left, IsarTaskRecord right) =>
          right.updatedAt.compareTo(left.updatedAt),
    );
    return tasks;
  }

  List<IsarTaskRecord> _buildDemoTasks() {
    final DateTime now = DateTime.now();

    final IsarTaskRecord draftTask = IsarTaskRecord()
      ..title = '完善帖子草稿箱'
      ..detail = '把本地草稿、封面图路径和最近编辑时间一起存进 Isar。'
      ..priority = 3
      ..isStarred = true
      ..updatedAt = now.subtract(const Duration(minutes: 18));

    final IsarTaskRecord badgeTask = IsarTaskRecord()
      ..title = '同步消息红点状态'
      ..detail = '演示 watchLazy()，红点被消费后列表会自动刷新。'
      ..priority = 2
      ..updatedAt = now.subtract(const Duration(hours: 2));

    final IsarTaskRecord feedTask = IsarTaskRecord()
      ..title = '缓存社区首页卡片'
      ..detail = '适合离线保存标题、摘要、作者和交互状态等对象数据。'
      ..priority = 2
      ..isDone = true
      ..updatedAt = now.subtract(const Duration(days: 1, hours: 3));

    final IsarTaskRecord uploadTask = IsarTaskRecord()
      ..title = '记录图片上传任务'
      ..detail = '上传成功后更新状态，失败时保留本地对象便于继续重试。'
      ..priority = 1
      ..updatedAt = now.subtract(const Duration(days: 2, hours: 5));

    return <IsarTaskRecord>[
      draftTask,
      badgeTask,
      feedTask,
      uploadTask,
    ];
  }
}
