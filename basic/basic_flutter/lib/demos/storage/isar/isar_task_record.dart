import 'package:isar_community/isar.dart';

part 'isar_task_record.g.dart';

@collection
class IsarTaskRecord {
  Id id = Isar.autoIncrement;

  late String title;
  late String detail;
  late int priority;
  bool isDone = false;
  bool isStarred = false;
  late DateTime updatedAt;
}
