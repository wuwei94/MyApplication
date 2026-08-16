import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';

/// Uuid
/// https://pub.dev/packages/uuid
class UuidDemoPage extends StatelessWidget {
  const UuidDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return UuidDemoView(title: title);
  }
}

class UuidDemoView extends StatefulWidget {
  const UuidDemoView({super.key, required this.title});

  final String title;

  @override
  State<UuidDemoView> createState() => _UuidDemoViewState();
}

class _UuidDemoViewState extends State<UuidDemoView> {
  static const Uuid _uuid = Uuid();
  static const String _defaultV5Name = 'flutter_demo';

  final TextEditingController _v5NameController = TextEditingController(
    text: _defaultV5Name,
  );
  final TextEditingController _validationController = TextEditingController();
  final List<_UuidRecord> _records = <_UuidRecord>[];

  @override
  void initState() {
    super.initState();
    final _UuidRecord initialRecord = _createRecord(
      version: _UuidVersion.v4,
      value: _uuid.v4(),
      summary: '初始化生成的随机 UUID',
    );
    _records.add(initialRecord);
    _validationController.text = initialRecord.value;
  }

  @override
  void dispose() {
    _v5NameController.dispose();
    _validationController.dispose();
    super.dispose();
  }

  void _generateUuid(_UuidVersion version) {
    final String v5Name = _resolveV5Name();
    final String value = switch (version) {
      _UuidVersion.v1 => _uuid.v1(),
      _UuidVersion.v4 => _uuid.v4(),
      _UuidVersion.v5 => _uuid.v5(Namespace.url.value, v5Name),
      _UuidVersion.v7 => _uuid.v7(),
    };
    final String summary = switch (version) {
      _UuidVersion.v1 => 'v1（时间戳）',
      _UuidVersion.v4 => 'v4（随机）',
      _UuidVersion.v5 => 'v5（命名空间+哈希）',
      _UuidVersion.v7 => 'v7（时间序）',
    };
    final _UuidRecord record = _createRecord(
      version: version,
      value: value,
      summary: summary,
    );

    setState(() {
      _records.insert(0, record);
      _validationController.text = value;
    });
  }

  _UuidRecord _createRecord({
    required _UuidVersion version,
    required String value,
    required String summary,
  }) {
    final Uint8List bytes = Uuid.parseAsByteList(value);
    return _UuidRecord(
      version: version,
      value: value,
      summary: summary,
      bytePreview: _formatBytes(bytes),
      createdAt: DateTime.now(),
    );
  }

  String _resolveV5Name() {
    final String name = _v5NameController.text.trim();
    return name.isEmpty ? _defaultV5Name : name;
  }

  String _formatBytes(List<int> bytes) {
    return bytes
        .map((int value) => value.toRadixString(16).padLeft(2, '0'))
        .join(' ');
  }

  Future<void> _copyUuid(String value) async {
    await Clipboard.setData(ClipboardData(text: value));
    if (!mounted) {
      return;
    }

    ScaffoldMessenger.of(
      context,
    ).showSnackBar(const SnackBar(content: Text('UUID 已复制到剪贴板')));
  }

  void _useLatestRecord() {
    if (_records.isEmpty) {
      return;
    }

    setState(() {
      _validationController.text = _records.first.value;
    });
  }

  void _clearRecords() {
    setState(() {
      _records.clear();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    final String validationInput = _validationController.text.trim();
    final bool hasValidationInput = validationInput.isNotEmpty;
    final bool isValid = hasValidationInput
        ? Uuid.isValidUUID(fromString: validationInput)
        : false;
    final Uint8List? parsedBytes = isValid
        ? Uuid.parseAsByteList(validationInput)
        : null;
    final String normalizedValue = parsedBytes == null
        ? ''
        : Uuid.unparse(parsedBytes);
    final String bytePreview = parsedBytes == null
        ? ''
        : _formatBytes(parsedBytes);
    final ThemeData theme = Theme.of(context);

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        _IntroCard(theme: theme),
        const SizedBox(height: 16),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text(
                  '生成 UUID',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  '支持演示 v1（时间戳）、v4（随机）、v5（命名空间+哈希）以及 v7（时间序）。'
                  '其中 v5 使用固定 namespace 与 name，'
                  '因此相同输入会生成相同结果。',
                  style: theme.textTheme.bodyMedium,
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: _v5NameController,
                  decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                    labelText: 'v5 名称',
                    hintText: _defaultV5Name,
                    helperText: '留空时会回退为 flutter_demo',
                  ),
                ),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 12,
                  runSpacing: 12,
                  children: <Widget>[
                    for (final _UuidVersion version in _UuidVersion.values)
                      ElevatedButton.icon(
                        onPressed: () => _generateUuid(version),
                        icon: Icon(version.icon),
                        label: Text('生成 ${version.label}'),
                      ),
                    OutlinedButton.icon(
                      onPressed: _records.isEmpty ? null : _clearRecords,
                      icon: const Icon(Icons.delete_outline_rounded),
                      label: const Text('清空记录'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    Expanded(
                      child: Text(
                        '校验与解析',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                    TextButton.icon(
                      onPressed: _records.isEmpty ? null : _useLatestRecord,
                      icon: const Icon(Icons.history_rounded),
                      label: const Text('使用最新结果'),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _validationController,
                  onChanged: (_) {
                    setState(() {});
                  },
                  decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                    labelText: '待校验的 UUID',
                    hintText: '例如：550e8400-e29b-41d4-a716-446655440000',
                  ),
                ),
                const SizedBox(height: 12),
                _ValidationStatus(
                  hasValidationInput: hasValidationInput,
                  isValid: isValid,
                ),
                if (parsedBytes != null) ...<Widget>[
                  const SizedBox(height: 12),
                  Text(
                    '16 字节内容',
                    style: theme.textTheme.labelLarge?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 4),
                  SelectableText(bytePreview),
                  const SizedBox(height: 12),
                  Text(
                    'Round Trip 结果',
                    style: theme.textTheme.labelLarge?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 4),
                  SelectableText(normalizedValue),
                ],
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),
        Text(
          '生成记录',
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 12),
        if (_records.isEmpty)
          const _EmptyHistoryCard()
        else
          for (final _UuidRecord record in _records)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: _UuidRecordCard(
                record: record,
                onCopy: () => _copyUuid(record.value),
              ),
            ),
        const SizedBox(height: 96),
      ],
    );
  }
}

enum _UuidVersion { v1, v4, v5, v7 }

extension _UuidVersionX on _UuidVersion {
  String get label => switch (this) {
    _UuidVersion.v1 => 'v1',
    _UuidVersion.v4 => 'v4',
    _UuidVersion.v5 => 'v5',
    _UuidVersion.v7 => 'v7',
  };

  IconData get icon => switch (this) {
    _UuidVersion.v1 => Icons.schedule_rounded,
    _UuidVersion.v4 => Icons.shuffle_rounded,
    _UuidVersion.v5 => Icons.link_rounded,
    _UuidVersion.v7 => Icons.update_rounded,
  };
}

class _IntroCard extends StatelessWidget {
  const _IntroCard({required this.theme});

  final ThemeData theme;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              'uuid package Demo',
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '这个页面用来快速验证 uuid 在 Flutter 项目中的接入结果，'
              '并展示常见生成、校验和解析能力。',
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 12),
            const Wrap(
              spacing: 8,
              runSpacing: 8,
              children: <Widget>[
                Chip(label: Text('v1（时间戳）')),
                Chip(label: Text('v4（随机）')),
                Chip(label: Text('v5（命名空间+哈希）')),
                Chip(label: Text('v7 时间序')),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _ValidationStatus extends StatelessWidget {
  const _ValidationStatus({
    required this.hasValidationInput,
    required this.isValid,
  });

  final bool hasValidationInput;
  final bool isValid;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final Color color = hasValidationInput
        ? (isValid ? theme.colorScheme.primary : theme.colorScheme.error)
        : theme.colorScheme.outline;
    final IconData icon = hasValidationInput
        ? (isValid ? Icons.verified_rounded : Icons.error_outline_rounded)
        : Icons.info_outline_rounded;
    final String text = hasValidationInput
        ? (isValid ? '当前输入是合法 UUID' : '当前输入不是合法 UUID')
        : '输入任意 UUID 进行校验';

    return Row(
      children: <Widget>[
        Icon(icon, color: color),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            text,
            style: theme.textTheme.bodyMedium?.copyWith(color: color),
          ),
        ),
      ],
    );
  }
}

class _EmptyHistoryCard extends StatelessWidget {
  const _EmptyHistoryCard();

  @override
  Widget build(BuildContext context) {
    return const Card(
      child: Padding(
        padding: EdgeInsets.all(20),
        child: Text('还没有生成记录，点击上方按钮即可创建新的 UUID。'),
      ),
    );
  }
}

class _UuidRecordCard extends StatelessWidget {
  const _UuidRecordCard({required this.record, required this.onCopy});

  final _UuidRecord record;
  final VoidCallback onCopy;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Chip(label: Text(record.version.label)),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    record.formattedTime,
                    style: theme.textTheme.bodySmall,
                  ),
                ),
                IconButton(
                  onPressed: onCopy,
                  tooltip: '复制 UUID',
                  icon: const Icon(Icons.copy_rounded),
                ),
              ],
            ),
            const SizedBox(height: 12),
            SelectableText(record.value, style: theme.textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(record.summary, style: theme.textTheme.bodyMedium),
            const SizedBox(height: 12),
            Text(
              '16 字节预览',
              style: theme.textTheme.labelLarge?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 4),
            SelectableText(
              record.bytePreview,
              style: theme.textTheme.bodySmall,
            ),
          ],
        ),
      ),
    );
  }
}

class _UuidRecord {
  const _UuidRecord({
    required this.version,
    required this.value,
    required this.summary,
    required this.bytePreview,
    required this.createdAt,
  });

  final _UuidVersion version;
  final String value;
  final String summary;
  final String bytePreview;
  final DateTime createdAt;

  String get formattedTime {
    final String hour = createdAt.hour.toString().padLeft(2, '0');
    final String minute = createdAt.minute.toString().padLeft(2, '0');
    final String second = createdAt.second.toString().padLeft(2, '0');
    return '$hour:$minute:$second';
  }
}
