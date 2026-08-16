import 'package:objectbox/objectbox.dart';

@Entity()
class ObjectBoxTaskEntity {
  ObjectBoxTaskEntity({
    this.id = 0,
    required this.title,
    required this.detail,
    required this.priority,
    this.isDone = false,
    this.isStarred = false,
    DateTime? updatedAt,
  }) : updatedAt = updatedAt ?? DateTime.now();

  @Id()
  int id;

  String title;
  String detail;
  int priority;
  bool isDone;
  bool isStarred;

  @Property(type: PropertyType.date)
  DateTime updatedAt;
}
