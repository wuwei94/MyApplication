import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// FutureBuilder
/// Demonstrates async data handling with FutureBuilder
class FutureBuilderDemoPage extends BasicLayoutPage {
  const FutureBuilderDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<FutureBuilderDemoPage> createState() =>
      _FutureBuilderDemoPageState();
}

class _FutureBuilderDemoPageState
    extends BasicLayoutPageState<FutureBuilderDemoPage> {
  Future<String>? _future;

  @override
  List<String> buildList() => const <String>[
        '1. 加载成功数据',
        '2. 加载失败数据',
        '3. 重置状态',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _loadData();
      case 1:
        _loadError();
      case 2:
        _reset();
    }
  }

  Future<String> _fetchData() async {
    await Future<void>.delayed(const Duration(seconds: 2));
    return 'Data loaded successfully!';
  }

  Future<String> _fetchError() async {
    await Future<void>.delayed(const Duration(seconds: 1));
    throw Exception('Failed to load data');
  }

  void _loadData() {
    setState(() {
      _future = _fetchData();
    });
  }

  void _loadError() {
    setState(() {
      _future = _fetchError();
    });
  }

  void _reset() {
    setState(() {
      _future = null;
    });
  }

  @override
  Widget buildPreview() {
    final Future<String>? future = _future;
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Container(
        width: double.infinity,
        decoration: BoxDecoration(
          color: Colors.blue.shade50,
          borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
          border: Border.all(color: Colors.blue.shade200),
        ),
        child: future == null
            ? const Center(child: Text('Tap an action to start'))
            : FutureBuilder<String>(
                future: future,
                builder: (
                  BuildContext context,
                  AsyncSnapshot<String> snapshot,
                ) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return const Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: <Widget>[
                          CircularProgressIndicator(),
                          SizedBox(height: 16),
                          Text('Loading...'),
                        ],
                      ),
                    );
                  } else if (snapshot.hasError) {
                    return Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: <Widget>[
                          const Icon(
                            Icons.error,
                            color: Colors.red,
                            size: 48,
                          ),
                          const SizedBox(height: 16),
                          Text('Error: ${snapshot.error}'),
                        ],
                      ),
                    );
                  } else if (snapshot.hasData) {
                    return Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: <Widget>[
                          const Icon(
                            Icons.check_circle,
                            color: Colors.green,
                            size: 48,
                          ),
                          const SizedBox(height: 16),
                          Text(
                            snapshot.data ?? '',
                            style: const TextStyle(fontSize: 18),
                          ),
                        ],
                      ),
                    );
                  }
                  return const SizedBox.shrink();
                },
              ),
      ),
    );
  }
}
