import 'package:flutter/material.dart';

/// FutureBuilder
/// Demonstrates async data handling with FutureBuilder
class FutureBuilderDemoPage extends StatelessWidget {
  const FutureBuilderDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return FutureBuilderDemoView(title: title);
  }
}

class FutureBuilderDemoView extends StatefulWidget {
  const FutureBuilderDemoView({super.key, required this.title});

  final String title;

  @override
  State<FutureBuilderDemoView> createState() => _FutureBuilderDemoViewState();
}

class _FutureBuilderDemoViewState extends State<FutureBuilderDemoView> {
  Future<String>? _future;

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
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('FutureBuilder Demo'),
            Row(
              children: [
                ElevatedButton(
                  onPressed: _loadData,
                  child: const Text('Load Data'),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: _loadError,
                  style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                  child: const Text('Load Error'),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: _reset,
                  style: ElevatedButton.styleFrom(backgroundColor: Colors.grey),
                  child: const Text('Reset'),
                ),
              ],
            ),
            const SizedBox(height: 24),
            _buildSectionTitle('Result'),
            Expanded(
              child: Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  color: Colors.blue.shade50,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.blue.shade200),
                ),
                child: _future == null
                    ? const Center(child: Text('Press a button to start'))
                    : FutureBuilder<String>(
                        future: _future,
                        builder: (context, snapshot) {
                          if (snapshot.connectionState ==
                              ConnectionState.waiting) {
                            return const Center(
                              child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
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
                                children: [
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
                                children: [
                                  const Icon(
                                    Icons.check_circle,
                                    color: Colors.green,
                                    size: 48,
                                  ),
                                  const SizedBox(height: 16),
                                  Text(
                                    snapshot.data!,
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
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.bold,
          color: Colors.blue,
        ),
      ),
    );
  }
}
