import 'package:flutter/material.dart';
import 'package:lib_storage/lib_storage.dart';

/// lib_storage
/// 本地 package：../flutter_libs/lib_storage
/// 演示 IStorage 统一接口与 Storage 门面内核切换。
class LibStorageDemoPage extends StatelessWidget {
  const LibStorageDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return LibStorageDemoView(title: title);
  }
}

class LibStorageDemoView extends StatefulWidget {
  const LibStorageDemoView({super.key, required this.title});

  final String title;

  @override
  State<LibStorageDemoView> createState() => _LibStorageDemoViewState();
}

class _LibStorageDemoViewState extends State<LibStorageDemoView> {
  static const String _counterKey = 'lib_storage_counter';
  int _counter = 0;

  String get _kernelName {
    return Storage.kernel is SharedPreferencesStorage
        ? 'SharedPreferencesStorage'
        : 'HiveStorage';
  }

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    // 示例页退出后恢复默认内核，避免影响其它示例页面。
    Storage.kernel = const HiveStorage();
    super.dispose();
  }

  Future<void> _load() async {
    final int counter = await Storage.getValue<int>(_counterKey, 0);

    if (!mounted) return;
    setState(() {
      _counter = counter;
    });
  }

  Future<void> _increment() async {
    final int counter = await Storage.getValue<int>(_counterKey, 0);
    await Storage.setValue(_counterKey, counter + 1);

    if (!mounted) return;
    setState(() {
      _counter = counter + 1;
    });
  }

  Future<void> _clear() async {
    await Storage.clearAll();

    if (!mounted) return;
    setState(() {
      _counter = 0;
    });
  }

  void _switchKernel() {
    setState(() {
      Storage.kernel = Storage.kernel is SharedPreferencesStorage
          ? const HiveStorage()
          : const SharedPreferencesStorage();
    });
  }

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: <Widget>[
          _KernelCard(kernelName: _kernelName, onSwitch: _switchKernel),
          const SizedBox(height: 16),
          _CounterCard(
            counter: _counter,
            theme: theme,
            onIncrement: _increment,
            onClear: _clear,
          ),
        ],
      ),
    );
  }
}

class _KernelCard extends StatelessWidget {
  const _KernelCard({required this.kernelName, required this.onSwitch});

  final String kernelName;
  final VoidCallback onSwitch;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: <Widget>[
            const Icon(Icons.swap_horiz_rounded),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text('当前内核：$kernelName'),
                  const SizedBox(height: 4),
                  Text(
                    'Storage.kernel 可整体替换，调用方 API 不变；'
                    '切换后计数器读写自不同后端。',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            FilledButton.tonal(onPressed: onSwitch, child: const Text('切换内核')),
          ],
        ),
      ),
    );
  }
}

class _CounterCard extends StatelessWidget {
  const _CounterCard({
    required this.counter,
    required this.theme,
    required this.onIncrement,
    required this.onClear,
  });

  final int counter;
  final ThemeData theme;
  final VoidCallback onIncrement;
  final VoidCallback onClear;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              'Storage 门面（getValue / setValue / clearAll）',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 12),
            Text('计数器：$counter', style: theme.textTheme.titleLarge),
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              children: <Widget>[
                FilledButton.icon(
                  onPressed: onIncrement,
                  icon: const Icon(Icons.add),
                  label: const Text('计数 +1'),
                ),
                OutlinedButton.icon(
                  onPressed: onClear,
                  icon: const Icon(Icons.delete_outline_rounded),
                  label: const Text('清空'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
