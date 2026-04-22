import 'dart:async';

import 'package:basic_flutter/demos/storage/objectbox/objectbox_task_entity.dart';
import 'package:basic_flutter/objectbox/objectbox.g.dart';
import 'package:flutter/widgets.dart';

class ObjectBoxTaskStore {
  ObjectBoxTaskStore._(this.store) : _taskBox = Box<ObjectBoxTaskEntity>(store);

  static Future<ObjectBoxTaskStore>? _createFuture;

  final Store store;
  final Box<ObjectBoxTaskEntity> _taskBox;

  static Future<ObjectBoxTaskStore> create() {
    return _createFuture ??= _open();
  }

  static Future<ObjectBoxTaskStore> _open() async {
    WidgetsFlutterBinding.ensureInitialized();
    final Store store = await openStore();
    final ObjectBoxTaskStore taskStore = ObjectBoxTaskStore._(store);
    taskStore.ensureDemoData();
    return taskStore;
  }

  Stream<List<ObjectBoxTaskEntity>> watchTasks() {
    final QueryBuilder<ObjectBoxTaskEntity> builder =
        _taskBox.query()
          ..order(
            ObjectBoxTaskEntity_.updatedAt,
            flags: Order.descending,
          );
    return builder
        .watch(triggerImmediately: true)
        .map((Query<ObjectBoxTaskEntity> query) => query.find());
  }

  bool isEmpty() {
    return _taskBox.isEmpty();
  }

  void ensureDemoData() {
    if (!_taskBox.isEmpty()) {
      return;
    }

    _taskBox.putMany(_buildDemoTasks());
  }

  void addTask({
    required String title,
    required String detail,
    required int priority,
  }) {
    final ObjectBoxTaskEntity task = ObjectBoxTaskEntity(
      title: title,
      detail: detail,
      priority: priority,
    );
    _taskBox.put(task);
  }

  void toggleDone(ObjectBoxTaskEntity task) {
    task.isDone = !task.isDone;
    task.updatedAt = DateTime.now();
    _taskBox.put(task);
  }

  void toggleStarred(ObjectBoxTaskEntity task) {
    task.isStarred = !task.isStarred;
    task.updatedAt = DateTime.now();
    _taskBox.put(task);
  }

  void removeTask(int id) {
    _taskBox.remove(id);
  }

  void clearAll() {
    _taskBox.removeAll();
  }

  void resetWithDemoData() {
    _taskBox.removeAll();
    _taskBox.putMany(_buildDemoTasks());
  }

  List<ObjectBoxTaskEntity> _buildDemoTasks() {
    final DateTime now = DateTime.now();
    return <ObjectBoxTaskEntity>[
      ObjectBoxTaskEntity(
        title: '完善帖子草稿箱',
        detail: '把本地草稿、封面图路径和最近编辑时间一起存进 ObjectBox。',
        priority: 3,
        isStarred: true,
        updatedAt: now.subtract(const Duration(minutes: 18)),
      ),
      ObjectBoxTaskEntity(
        title: '同步消息红点状态',
        detail: '演示对象查询与 watch()，红点被消费后列表会自动刷新。',
        priority: 2,
        updatedAt: now.subtract(const Duration(hours: 2)),
      ),
      ObjectBoxTaskEntity(
        title: '缓存社区首页卡片',
        detail: '适合离线展示标题、摘要、作者和交互状态等对象数据。',
        priority: 2,
        isDone: true,
        updatedAt: now.subtract(const Duration(days: 1, hours: 3)),
      ),
      ObjectBoxTaskEntity(
        title: '记录图片上传任务',
        detail: '上传成功后更新任务状态，失败时保留本地对象便于重试。',
        priority: 1,
        updatedAt: now.subtract(const Duration(days: 2, hours: 5)),
      ),
    ];
  }
}
