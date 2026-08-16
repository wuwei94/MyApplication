// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'drift_task_database.dart';

// ignore_for_file: type=lint
class $DriftTasksTable extends DriftTasks
    with TableInfo<$DriftTasksTable, DriftTask> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $DriftTasksTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
    'id',
    aliasedName,
    false,
    hasAutoIncrement: true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'PRIMARY KEY AUTOINCREMENT',
    ),
  );
  static const VerificationMeta _titleMeta = const VerificationMeta('title');
  @override
  late final GeneratedColumn<String> title = GeneratedColumn<String>(
    'title',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _detailMeta = const VerificationMeta('detail');
  @override
  late final GeneratedColumn<String> detail = GeneratedColumn<String>(
    'detail',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _priorityMeta = const VerificationMeta(
    'priority',
  );
  @override
  late final GeneratedColumn<int> priority = GeneratedColumn<int>(
    'priority',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _isDoneMeta = const VerificationMeta('isDone');
  @override
  late final GeneratedColumn<bool> isDone = GeneratedColumn<bool>(
    'is_done',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("is_done" IN (0, 1))',
    ),
    defaultValue: const Constant(false),
  );
  static const VerificationMeta _isStarredMeta = const VerificationMeta(
    'isStarred',
  );
  @override
  late final GeneratedColumn<bool> isStarred = GeneratedColumn<bool>(
    'is_starred',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("is_starred" IN (0, 1))',
    ),
    defaultValue: const Constant(false),
  );
  static const VerificationMeta _updatedAtMeta = const VerificationMeta(
    'updatedAt',
  );
  @override
  late final GeneratedColumn<DateTime> updatedAt = GeneratedColumn<DateTime>(
    'updated_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    title,
    detail,
    priority,
    isDone,
    isStarred,
    updatedAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'drift_tasks';
  @override
  VerificationContext validateIntegrity(
    Insertable<DriftTask> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('title')) {
      context.handle(
        _titleMeta,
        title.isAcceptableOrUnknown(data['title']!, _titleMeta),
      );
    } else if (isInserting) {
      context.missing(_titleMeta);
    }
    if (data.containsKey('detail')) {
      context.handle(
        _detailMeta,
        detail.isAcceptableOrUnknown(data['detail']!, _detailMeta),
      );
    } else if (isInserting) {
      context.missing(_detailMeta);
    }
    if (data.containsKey('priority')) {
      context.handle(
        _priorityMeta,
        priority.isAcceptableOrUnknown(data['priority']!, _priorityMeta),
      );
    } else if (isInserting) {
      context.missing(_priorityMeta);
    }
    if (data.containsKey('is_done')) {
      context.handle(
        _isDoneMeta,
        isDone.isAcceptableOrUnknown(data['is_done']!, _isDoneMeta),
      );
    }
    if (data.containsKey('is_starred')) {
      context.handle(
        _isStarredMeta,
        isStarred.isAcceptableOrUnknown(data['is_starred']!, _isStarredMeta),
      );
    }
    if (data.containsKey('updated_at')) {
      context.handle(
        _updatedAtMeta,
        updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta),
      );
    } else if (isInserting) {
      context.missing(_updatedAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  DriftTask map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return DriftTask(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}id'],
      )!,
      title: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}title'],
      )!,
      detail: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}detail'],
      )!,
      priority: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}priority'],
      )!,
      isDone: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}is_done'],
      )!,
      isStarred: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}is_starred'],
      )!,
      updatedAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}updated_at'],
      )!,
    );
  }

  @override
  $DriftTasksTable createAlias(String alias) {
    return $DriftTasksTable(attachedDatabase, alias);
  }
}

class DriftTask extends DataClass implements Insertable<DriftTask> {
  final int id;
  final String title;
  final String detail;
  final int priority;
  final bool isDone;
  final bool isStarred;
  final DateTime updatedAt;
  const DriftTask({
    required this.id,
    required this.title,
    required this.detail,
    required this.priority,
    required this.isDone,
    required this.isStarred,
    required this.updatedAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['title'] = Variable<String>(title);
    map['detail'] = Variable<String>(detail);
    map['priority'] = Variable<int>(priority);
    map['is_done'] = Variable<bool>(isDone);
    map['is_starred'] = Variable<bool>(isStarred);
    map['updated_at'] = Variable<DateTime>(updatedAt);
    return map;
  }

  DriftTasksCompanion toCompanion(bool nullToAbsent) {
    return DriftTasksCompanion(
      id: Value(id),
      title: Value(title),
      detail: Value(detail),
      priority: Value(priority),
      isDone: Value(isDone),
      isStarred: Value(isStarred),
      updatedAt: Value(updatedAt),
    );
  }

  factory DriftTask.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return DriftTask(
      id: serializer.fromJson<int>(json['id']),
      title: serializer.fromJson<String>(json['title']),
      detail: serializer.fromJson<String>(json['detail']),
      priority: serializer.fromJson<int>(json['priority']),
      isDone: serializer.fromJson<bool>(json['isDone']),
      isStarred: serializer.fromJson<bool>(json['isStarred']),
      updatedAt: serializer.fromJson<DateTime>(json['updatedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'title': serializer.toJson<String>(title),
      'detail': serializer.toJson<String>(detail),
      'priority': serializer.toJson<int>(priority),
      'isDone': serializer.toJson<bool>(isDone),
      'isStarred': serializer.toJson<bool>(isStarred),
      'updatedAt': serializer.toJson<DateTime>(updatedAt),
    };
  }

  DriftTask copyWith({
    int? id,
    String? title,
    String? detail,
    int? priority,
    bool? isDone,
    bool? isStarred,
    DateTime? updatedAt,
  }) => DriftTask(
    id: id ?? this.id,
    title: title ?? this.title,
    detail: detail ?? this.detail,
    priority: priority ?? this.priority,
    isDone: isDone ?? this.isDone,
    isStarred: isStarred ?? this.isStarred,
    updatedAt: updatedAt ?? this.updatedAt,
  );
  DriftTask copyWithCompanion(DriftTasksCompanion data) {
    return DriftTask(
      id: data.id.present ? data.id.value : this.id,
      title: data.title.present ? data.title.value : this.title,
      detail: data.detail.present ? data.detail.value : this.detail,
      priority: data.priority.present ? data.priority.value : this.priority,
      isDone: data.isDone.present ? data.isDone.value : this.isDone,
      isStarred: data.isStarred.present ? data.isStarred.value : this.isStarred,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('DriftTask(')
          ..write('id: $id, ')
          ..write('title: $title, ')
          ..write('detail: $detail, ')
          ..write('priority: $priority, ')
          ..write('isDone: $isDone, ')
          ..write('isStarred: $isStarred, ')
          ..write('updatedAt: $updatedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(id, title, detail, priority, isDone, isStarred, updatedAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is DriftTask &&
          other.id == this.id &&
          other.title == this.title &&
          other.detail == this.detail &&
          other.priority == this.priority &&
          other.isDone == this.isDone &&
          other.isStarred == this.isStarred &&
          other.updatedAt == this.updatedAt);
}

class DriftTasksCompanion extends UpdateCompanion<DriftTask> {
  final Value<int> id;
  final Value<String> title;
  final Value<String> detail;
  final Value<int> priority;
  final Value<bool> isDone;
  final Value<bool> isStarred;
  final Value<DateTime> updatedAt;
  const DriftTasksCompanion({
    this.id = const Value.absent(),
    this.title = const Value.absent(),
    this.detail = const Value.absent(),
    this.priority = const Value.absent(),
    this.isDone = const Value.absent(),
    this.isStarred = const Value.absent(),
    this.updatedAt = const Value.absent(),
  });
  DriftTasksCompanion.insert({
    this.id = const Value.absent(),
    required String title,
    required String detail,
    required int priority,
    this.isDone = const Value.absent(),
    this.isStarred = const Value.absent(),
    required DateTime updatedAt,
  }) : title = Value(title),
       detail = Value(detail),
       priority = Value(priority),
       updatedAt = Value(updatedAt);
  static Insertable<DriftTask> custom({
    Expression<int>? id,
    Expression<String>? title,
    Expression<String>? detail,
    Expression<int>? priority,
    Expression<bool>? isDone,
    Expression<bool>? isStarred,
    Expression<DateTime>? updatedAt,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (title != null) 'title': title,
      if (detail != null) 'detail': detail,
      if (priority != null) 'priority': priority,
      if (isDone != null) 'is_done': isDone,
      if (isStarred != null) 'is_starred': isStarred,
      if (updatedAt != null) 'updated_at': updatedAt,
    });
  }

  DriftTasksCompanion copyWith({
    Value<int>? id,
    Value<String>? title,
    Value<String>? detail,
    Value<int>? priority,
    Value<bool>? isDone,
    Value<bool>? isStarred,
    Value<DateTime>? updatedAt,
  }) {
    return DriftTasksCompanion(
      id: id ?? this.id,
      title: title ?? this.title,
      detail: detail ?? this.detail,
      priority: priority ?? this.priority,
      isDone: isDone ?? this.isDone,
      isStarred: isStarred ?? this.isStarred,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (title.present) {
      map['title'] = Variable<String>(title.value);
    }
    if (detail.present) {
      map['detail'] = Variable<String>(detail.value);
    }
    if (priority.present) {
      map['priority'] = Variable<int>(priority.value);
    }
    if (isDone.present) {
      map['is_done'] = Variable<bool>(isDone.value);
    }
    if (isStarred.present) {
      map['is_starred'] = Variable<bool>(isStarred.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<DateTime>(updatedAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('DriftTasksCompanion(')
          ..write('id: $id, ')
          ..write('title: $title, ')
          ..write('detail: $detail, ')
          ..write('priority: $priority, ')
          ..write('isDone: $isDone, ')
          ..write('isStarred: $isStarred, ')
          ..write('updatedAt: $updatedAt')
          ..write(')'))
        .toString();
  }
}

abstract class _$DriftTaskDatabase extends GeneratedDatabase {
  _$DriftTaskDatabase(QueryExecutor e) : super(e);
  $DriftTaskDatabaseManager get managers => $DriftTaskDatabaseManager(this);
  late final $DriftTasksTable driftTasks = $DriftTasksTable(this);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [driftTasks];
}

typedef $$DriftTasksTableCreateCompanionBuilder =
    DriftTasksCompanion Function({
      Value<int> id,
      required String title,
      required String detail,
      required int priority,
      Value<bool> isDone,
      Value<bool> isStarred,
      required DateTime updatedAt,
    });
typedef $$DriftTasksTableUpdateCompanionBuilder =
    DriftTasksCompanion Function({
      Value<int> id,
      Value<String> title,
      Value<String> detail,
      Value<int> priority,
      Value<bool> isDone,
      Value<bool> isStarred,
      Value<DateTime> updatedAt,
    });

class $$DriftTasksTableFilterComposer
    extends Composer<_$DriftTaskDatabase, $DriftTasksTable> {
  $$DriftTasksTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get detail => $composableBuilder(
    column: $table.detail,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get priority => $composableBuilder(
    column: $table.priority,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get isDone => $composableBuilder(
    column: $table.isDone,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get isStarred => $composableBuilder(
    column: $table.isStarred,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $$DriftTasksTableOrderingComposer
    extends Composer<_$DriftTaskDatabase, $DriftTasksTable> {
  $$DriftTasksTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get detail => $composableBuilder(
    column: $table.detail,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get priority => $composableBuilder(
    column: $table.priority,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get isDone => $composableBuilder(
    column: $table.isDone,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get isStarred => $composableBuilder(
    column: $table.isStarred,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$DriftTasksTableAnnotationComposer
    extends Composer<_$DriftTaskDatabase, $DriftTasksTable> {
  $$DriftTasksTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get title =>
      $composableBuilder(column: $table.title, builder: (column) => column);

  GeneratedColumn<String> get detail =>
      $composableBuilder(column: $table.detail, builder: (column) => column);

  GeneratedColumn<int> get priority =>
      $composableBuilder(column: $table.priority, builder: (column) => column);

  GeneratedColumn<bool> get isDone =>
      $composableBuilder(column: $table.isDone, builder: (column) => column);

  GeneratedColumn<bool> get isStarred =>
      $composableBuilder(column: $table.isStarred, builder: (column) => column);

  GeneratedColumn<DateTime> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);
}

class $$DriftTasksTableTableManager
    extends
        RootTableManager<
          _$DriftTaskDatabase,
          $DriftTasksTable,
          DriftTask,
          $$DriftTasksTableFilterComposer,
          $$DriftTasksTableOrderingComposer,
          $$DriftTasksTableAnnotationComposer,
          $$DriftTasksTableCreateCompanionBuilder,
          $$DriftTasksTableUpdateCompanionBuilder,
          (
            DriftTask,
            BaseReferences<_$DriftTaskDatabase, $DriftTasksTable, DriftTask>,
          ),
          DriftTask,
          PrefetchHooks Function()
        > {
  $$DriftTasksTableTableManager(_$DriftTaskDatabase db, $DriftTasksTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$DriftTasksTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$DriftTasksTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$DriftTasksTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                Value<String> title = const Value.absent(),
                Value<String> detail = const Value.absent(),
                Value<int> priority = const Value.absent(),
                Value<bool> isDone = const Value.absent(),
                Value<bool> isStarred = const Value.absent(),
                Value<DateTime> updatedAt = const Value.absent(),
              }) => DriftTasksCompanion(
                id: id,
                title: title,
                detail: detail,
                priority: priority,
                isDone: isDone,
                isStarred: isStarred,
                updatedAt: updatedAt,
              ),
          createCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                required String title,
                required String detail,
                required int priority,
                Value<bool> isDone = const Value.absent(),
                Value<bool> isStarred = const Value.absent(),
                required DateTime updatedAt,
              }) => DriftTasksCompanion.insert(
                id: id,
                title: title,
                detail: detail,
                priority: priority,
                isDone: isDone,
                isStarred: isStarred,
                updatedAt: updatedAt,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$DriftTasksTableProcessedTableManager =
    ProcessedTableManager<
      _$DriftTaskDatabase,
      $DriftTasksTable,
      DriftTask,
      $$DriftTasksTableFilterComposer,
      $$DriftTasksTableOrderingComposer,
      $$DriftTasksTableAnnotationComposer,
      $$DriftTasksTableCreateCompanionBuilder,
      $$DriftTasksTableUpdateCompanionBuilder,
      (
        DriftTask,
        BaseReferences<_$DriftTaskDatabase, $DriftTasksTable, DriftTask>,
      ),
      DriftTask,
      PrefetchHooks Function()
    >;

class $DriftTaskDatabaseManager {
  final _$DriftTaskDatabase _db;
  $DriftTaskDatabaseManager(this._db);
  $$DriftTasksTableTableManager get driftTasks =>
      $$DriftTasksTableTableManager(_db, _db.driftTasks);
}
