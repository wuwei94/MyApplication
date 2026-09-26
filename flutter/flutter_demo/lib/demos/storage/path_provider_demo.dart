import 'dart:io';

import 'package:flutter_demo/core/basic/basic.dart';
import 'package:path_provider/path_provider.dart';

/// PathProvider — 系统目录查询与临时文件读写
///
/// 核心机制与避坑点：
/// 1. 平台矩阵：Library 仅 iOS、External* 仅 Android；不支持的目录抛 `UnsupportedError` 或 `MissingPlatformDirectoryException`。
/// 2. 目录寿命：temporary 可能被系统回收，长期数据写 application documents / support。
///
/// 官方参考：
/// https://pub.dev/packages/path_provider
class PathProviderDemoPage extends BasicResponsePage {
  const PathProviderDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<PathProviderDemoPage> createState() =>
      _PathProviderDemoPageState();
}

class _PathProviderDemoPageState
    extends BasicResponsePageState<PathProviderDemoPage> {
  static const String _sampleFileName = 'path_provider_demo.txt';
  String? _sampleFilePath;

  @override
  void initState() {
    super.initState();
    showDescription('PathProvider 示例：目录查询与临时文件读写');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 查询临时目录',
        '2. 查询文档目录',
        '3. 查询应用支持目录',
        '4. 查询缓存目录',
        '5. 写入示例文件',
        '6. 读取示例文件',
        '7. 删除示例文件',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _queryTemporary();
      case 1:
        _queryDocuments();
      case 2:
        _queryApplicationSupport();
      case 3:
        _queryCache();
      case 4:
        _writeSampleFile();
      case 5:
        _readSampleFile();
      case 6:
        _deleteSampleFile();
    }
  }

  Future<void> _queryTemporary() {
    return _queryDirectory('getTemporaryDirectory()', getTemporaryDirectory);
  }

  Future<void> _queryDocuments() {
    return _queryDirectory(
      'getApplicationDocumentsDirectory()',
      getApplicationDocumentsDirectory,
    );
  }

  Future<void> _queryApplicationSupport() {
    return _queryDirectory(
      'getApplicationSupportDirectory()',
      getApplicationSupportDirectory,
    );
  }

  Future<void> _queryCache() {
    return _queryDirectory(
      'getApplicationCacheDirectory()',
      getApplicationCacheDirectory,
    );
  }

  Future<void> _queryDirectory(
    String label,
    Future<Directory> Function() loader,
  ) async {
    appendLog('→ [Read] $label');
    try {
      final Directory directory = await loader();
      appendLog('✓ path = ${directory.path}');
    } on MissingPlatformDirectoryException catch (error) {
      appendLog('✗ $label: ${error.message}');
    } on UnsupportedError catch (error) {
      appendLog('✗ $label: ${error.message ?? '当前平台不支持'}');
    } catch (error) {
      appendLog('✗ $label: $error');
    }
  }

  Future<void> _writeSampleFile() async {
    appendLog('→ [Write] temporary/$_sampleFileName');
    try {
      final Directory directory = await getTemporaryDirectory();
      final String path =
          '${directory.path}${Platform.pathSeparator}$_sampleFileName';
      final File file = File(path);
      final String content =
          'PathProvider demo generated at ${DateTime.now().toIso8601String()}';
      await file.writeAsString(content, flush: true);
      _sampleFilePath = path;
      appendLog('✓ path = $path');
    } catch (error) {
      appendLog('✗ 写入失败: $error');
    }
  }

  Future<void> _readSampleFile() async {
    final String? path = _sampleFilePath;
    if (path == null) {
      appendLog('✗ 尚未写入示例文件');
      return;
    }
    appendLog('→ [Read] $path');
    try {
      final String content = await File(path).readAsString();
      appendLog('✓ content = $content');
    } catch (error) {
      appendLog('✗ 读取失败: $error');
    }
  }

  Future<void> _deleteSampleFile() async {
    final String? path = _sampleFilePath;
    if (path == null) {
      appendLog('✗ 尚未写入示例文件');
      return;
    }
    appendLog('→ [Delete] $path');
    try {
      await File(path).delete();
    } on PathNotFoundException {
      // 文件可能已被外部删除，这里按成功处理即可。
    } catch (error) {
      appendLog('✗ 删除失败: $error');
      return;
    }
    _sampleFilePath = null;
    appendLog('✓ 示例文件已删除');
  }
}
