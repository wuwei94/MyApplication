import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';

/// Path Provider
/// https://pub.dev/packages/path_provider
class PathProviderDemoPage extends StatelessWidget {
  const PathProviderDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PathProviderDemoView(title: title);
  }
}

class PathProviderDemoView extends StatefulWidget {
  const PathProviderDemoView({super.key, required this.title});

  final String title;

  @override
  State<PathProviderDemoView> createState() => _PathProviderDemoViewState();
}

class _PathProviderDemoViewState extends State<PathProviderDemoView> {
  static const String _sampleFileName = 'path_provider_demo.txt';

  bool _isLoadingDirectories = false;
  bool _isWritingSampleFile = false;
  String _statusMessage = '页面会自动读取常用目录，也可以手动刷新。';
  String? _sampleFilePath;
  String _sampleFileContent = '尚未创建示例文件。';
  List<_DirectoryProbeResult> _directoryResults =
      const <_DirectoryProbeResult>[];

  bool get _isBusy => _isLoadingDirectories || _isWritingSampleFile;

  @override
  void initState() {
    super.initState();
    unawaited(_loadDirectories());
  }

  @override
  Widget build(BuildContext context) {
    final int availableCount = _directoryResults
        .where((_DirectoryProbeResult result) => result.isAvailable)
        .length;

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            onPressed: _isLoadingDirectories ? null : _loadDirectories,
            tooltip: '刷新目录',
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: getBody(availableCount),
    );
  }

  Widget getBody(int availableCount) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        _buildOverviewCard(availableCount),
        const SizedBox(height: 16),
        _buildActionCard(),
        const SizedBox(height: 16),
        _buildSampleFileCard(),
        const SizedBox(height: 16),
        _buildDirectorySection(),
      ],
    );
  }

  Widget _buildOverviewCard(int availableCount) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              'path_provider 已接入',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Text(
              '用于查询临时目录、文档目录、缓存目录等系统路径。当前示例按 Android / iOS 的支持矩阵顺序展示。',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 12),
            Text('当前状态：$_statusMessage'),
            const SizedBox(height: 8),
            Text('可用目录：$availableCount / ${_directoryResults.length}'),
            if (_isBusy) ...<Widget>[
              const SizedBox(height: 12),
              const LinearProgressIndicator(),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildActionCard() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text('快捷操作', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 8),
            const Text('刷新目录结果，并在临时目录中创建一个示例文件做读写验证。'),
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: <Widget>[
                FilledButton.icon(
                  onPressed: _isLoadingDirectories ? null : _loadDirectories,
                  icon: const Icon(Icons.folder_open_outlined),
                  label: const Text('刷新目录'),
                ),
                FilledButton.icon(
                  onPressed: _isBusy ? null : _writeSampleFile,
                  icon: const Icon(Icons.edit_note_outlined),
                  label: const Text('写入示例文件'),
                ),
                OutlinedButton.icon(
                  onPressed: _sampleFilePath == null || _isBusy
                      ? null
                      : _deleteSampleFile,
                  icon: const Icon(Icons.delete_outline),
                  label: const Text('删除示例文件'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSampleFileCard() {
    final bool hasSampleFile = _sampleFilePath != null;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text('示例文件', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(
              hasSampleFile
                  ? '文件已写入临时目录，可用来验证路径是否真实可写。'
                  : '点击上方按钮后，会在 temporary directory 下创建一个文本文件。',
            ),
            const SizedBox(height: 12),
            _InfoBlock(label: '文件路径', value: _sampleFilePath ?? '暂无'),
            const SizedBox(height: 12),
            _InfoBlock(label: '文件内容', value: _sampleFileContent),
          ],
        ),
      ),
    );
  }

  Widget _buildDirectorySection() {
    if (_directoryResults.isEmpty && _isLoadingDirectories) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.symmetric(vertical: 32),
          child: CircularProgressIndicator(),
        ),
      );
    }

    if (_directoryResults.isEmpty) {
      return const Card(
        child: Padding(padding: EdgeInsets.all(16), child: Text('还没有读取到目录信息。')),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: _directoryResults
          .map(_buildDirectoryCard)
          .toList(growable: false),
    );
  }

  Widget _buildDirectoryCard(_DirectoryProbeResult result) {
    final Color toneColor = result.isAvailable
        ? Colors.green.shade700
        : Colors.orange.shade800;

    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                children: <Widget>[
                  Icon(
                    result.isAvailable
                        ? Icons.check_circle_outline
                        : Icons.info_outline,
                    color: toneColor,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      result.label,
                      style: Theme.of(context).textTheme.titleSmall,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(result.description),
              const SizedBox(height: 12),
              if (result.isAvailable)
                _InfoBlock(
                  label: result.paths.length > 1 ? '目录列表' : '目录路径',
                  value: result.paths.join('\n'),
                )
              else
                Text(result.errorMessage, style: TextStyle(color: toneColor)),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _loadDirectories() async {
    setState(() {
      _isLoadingDirectories = true;
      _statusMessage = '正在读取系统目录...';
    });

    final List<_DirectoryProbeResult> results = await _collectDirectories();
    final int availableCount = results
        .where((_DirectoryProbeResult result) => result.isAvailable)
        .length;

    if (!mounted) {
      return;
    }

    setState(() {
      _directoryResults = results;
      _isLoadingDirectories = false;
      _statusMessage = '已读取 $availableCount 个可用目录。';
    });
  }

  Future<void> _writeSampleFile() async {
    setState(() {
      _isWritingSampleFile = true;
      _statusMessage = '正在写入临时文件...';
    });

    try {
      final Directory temporaryDirectory = await getTemporaryDirectory();
      final String filePath =
          '${temporaryDirectory.path}${Platform.pathSeparator}$_sampleFileName';
      final File sampleFile = File(filePath);
      final String content =
          'PathProvider demo generated at ${DateTime.now().toIso8601String()}';

      await sampleFile.writeAsString(content, flush: true);
      final String readBack = await sampleFile.readAsString();

      if (!mounted) {
        return;
      }

      setState(() {
        _sampleFilePath = filePath;
        _sampleFileContent = readBack;
        _isWritingSampleFile = false;
        _statusMessage = '已写入并回读示例文件。';
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _isWritingSampleFile = false;
        _statusMessage = '写入示例文件失败：$error';
      });
    }
  }

  Future<void> _deleteSampleFile() async {
    final String? currentSampleFilePath = _sampleFilePath;
    if (currentSampleFilePath == null) {
      return;
    }

    setState(() {
      _isWritingSampleFile = true;
      _statusMessage = '正在删除示例文件...';
    });

    try {
      final File sampleFile = File(currentSampleFilePath);
      try {
        await sampleFile.delete();
      } on PathNotFoundException {
        // 文件可能已被外部删除，这里按成功处理即可。
      }

      if (!mounted) {
        return;
      }

      setState(() {
        _sampleFilePath = null;
        _sampleFileContent = '示例文件已删除。';
        _isWritingSampleFile = false;
        _statusMessage = '示例文件已删除。';
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _isWritingSampleFile = false;
        _statusMessage = '删除示例文件失败：$error';
      });
    }
  }

  Future<List<_DirectoryProbeResult>> _collectDirectories() async {
    final bool isAndroid = Platform.isAndroid;
    final bool isIOS = Platform.isIOS;

    return <_DirectoryProbeResult>[
      await _probeDirectory(
        label: 'Temporary Directory',
        description: '临时目录，适合图片缓存、临时导出文件等可被系统回收的数据。',
        loader: getTemporaryDirectory,
      ),
      await _probeDirectory(
        label: 'Application Support Directory',
        description: '支持目录，适合应用内部配置、数据库和后台支撑文件。',
        loader: getApplicationSupportDirectory,
      ),
      if (isIOS)
        await _probeDirectory(
          label: 'Application Library Directory',
          description: 'iOS Library 目录，适合应用内部文件和框架支撑文件。',
          loader: getLibraryDirectory,
        )
      else
        _unsupportedResult(
          label: 'Application Library Directory',
          description: 'iOS 专属目录，用于放置应用内部文件。',
          errorMessage: '当前平台不支持，仅 iOS 可用。',
        ),
      await _probeDirectory(
        label: 'Application Documents Directory',
        description: '文档目录，适合长期保存且希望继续读取的用户数据。',
        loader: getApplicationDocumentsDirectory,
      ),
      await _probeDirectory(
        label: 'Application Cache Directory',
        description: '缓存目录，适合可重复生成的数据。',
        loader: getApplicationCacheDirectory,
      ),
      if (isAndroid)
        await _probeOptionalDirectory(
          label: 'External Storage Directory',
          description: 'Android 外部存储目录，适合和系统文件目录协作。',
          loader: getExternalStorageDirectory,
        )
      else
        _unsupportedResult(
          label: 'External Storage Directory',
          description: 'Android 专属外部存储目录。',
          errorMessage: '当前平台不支持，仅 Android 可用。',
        ),
      if (isAndroid)
        await _probeOptionalDirectories(
          label: 'External Cache Directories',
          description: 'Android 外部缓存目录列表，常见于不同挂载卷。',
          loader: getExternalCacheDirectories,
        )
      else
        _unsupportedResult(
          label: 'External Cache Directories',
          description: 'Android 专属的外部缓存目录列表。',
          errorMessage: '当前平台不支持，仅 Android 可用。',
        ),
      if (isAndroid)
        await _probeOptionalDirectories(
          label: 'External Storage Directories',
          description: 'Android 外部存储目录列表，可用于不同挂载卷或 app-specific 外部目录。',
          loader: getExternalStorageDirectories,
        )
      else
        _unsupportedResult(
          label: 'External Storage Directories',
          description: 'Android 专属的外部存储目录列表。',
          errorMessage: '当前平台不支持，仅 Android 可用。',
        ),
      await _probeOptionalDirectory(
        label: 'Downloads Directory',
        description: '下载目录，适合下载文件、导出文件等场景。',
        loader: getDownloadsDirectory,
      ),
    ];
  }

  Future<_DirectoryProbeResult> _probeDirectory({
    required String label,
    required String description,
    required Future<Directory> Function() loader,
  }) async {
    try {
      final Directory directory = await loader();
      return _DirectoryProbeResult.available(
        label: label,
        description: description,
        paths: <String>[directory.path],
      );
    } on MissingPlatformDirectoryException catch (error) {
      return _DirectoryProbeResult.unavailable(
        label: label,
        description: description,
        errorMessage: _messageOrFallback(error.message, '当前平台没有返回可用目录。'),
      );
    } on UnsupportedError catch (error) {
      return _DirectoryProbeResult.unavailable(
        label: label,
        description: description,
        errorMessage: _messageOrFallback(error.message, '当前平台暂不支持该目录。'),
      );
    } catch (error) {
      return _DirectoryProbeResult.unavailable(
        label: label,
        description: description,
        errorMessage: '$error',
      );
    }
  }

  Future<_DirectoryProbeResult> _probeOptionalDirectory({
    required String label,
    required String description,
    required Future<Directory?> Function() loader,
  }) async {
    try {
      final Directory? directory = await loader();
      if (directory == null) {
        return _DirectoryProbeResult.unavailable(
          label: label,
          description: description,
          errorMessage: '当前平台没有返回目录。',
        );
      }

      return _DirectoryProbeResult.available(
        label: label,
        description: description,
        paths: <String>[directory.path],
      );
    } on MissingPlatformDirectoryException catch (error) {
      return _DirectoryProbeResult.unavailable(
        label: label,
        description: description,
        errorMessage: _messageOrFallback(error.message, '当前平台没有返回可用目录。'),
      );
    } on UnsupportedError catch (error) {
      return _DirectoryProbeResult.unavailable(
        label: label,
        description: description,
        errorMessage: _messageOrFallback(error.message, '当前平台暂不支持该目录。'),
      );
    } catch (error) {
      return _DirectoryProbeResult.unavailable(
        label: label,
        description: description,
        errorMessage: '$error',
      );
    }
  }

  Future<_DirectoryProbeResult> _probeOptionalDirectories({
    required String label,
    required String description,
    required Future<List<Directory>?> Function() loader,
  }) async {
    try {
      final List<Directory>? directories = await loader();
      if (directories == null || directories.isEmpty) {
        return _DirectoryProbeResult.unavailable(
          label: label,
          description: description,
          errorMessage: '当前平台没有返回目录列表。',
        );
      }

      return _DirectoryProbeResult.available(
        label: label,
        description: description,
        paths: directories
            .map((Directory directory) => directory.path)
            .toList(),
      );
    } on MissingPlatformDirectoryException catch (error) {
      return _DirectoryProbeResult.unavailable(
        label: label,
        description: description,
        errorMessage: _messageOrFallback(error.message, '当前平台没有返回可用目录。'),
      );
    } on UnsupportedError catch (error) {
      return _DirectoryProbeResult.unavailable(
        label: label,
        description: description,
        errorMessage: _messageOrFallback(error.message, '当前平台暂不支持该目录。'),
      );
    } catch (error) {
      return _DirectoryProbeResult.unavailable(
        label: label,
        description: description,
        errorMessage: '$error',
      );
    }
  }

  String _messageOrFallback(String? message, String fallback) {
    if (message == null || message.isEmpty) {
      return fallback;
    }

    return message;
  }

  _DirectoryProbeResult _unsupportedResult({
    required String label,
    required String description,
    required String errorMessage,
  }) {
    return _DirectoryProbeResult.unavailable(
      label: label,
      description: description,
      errorMessage: errorMessage,
    );
  }
}

class _InfoBlock extends StatelessWidget {
  const _InfoBlock({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(label, style: Theme.of(context).textTheme.labelLarge),
        const SizedBox(height: 6),
        DecoratedBox(
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surfaceContainerHighest,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: SelectionArea(child: Text(value)),
          ),
        ),
      ],
    );
  }
}

class _DirectoryProbeResult {
  const _DirectoryProbeResult._({
    required this.label,
    required this.description,
    required this.paths,
    required this.errorMessage,
  });

  factory _DirectoryProbeResult.available({
    required String label,
    required String description,
    required List<String> paths,
  }) {
    return _DirectoryProbeResult._(
      label: label,
      description: description,
      paths: paths,
      errorMessage: '',
    );
  }

  factory _DirectoryProbeResult.unavailable({
    required String label,
    required String description,
    required String errorMessage,
  }) {
    return _DirectoryProbeResult._(
      label: label,
      description: description,
      paths: const <String>[],
      errorMessage: errorMessage,
    );
  }

  final String label;
  final String description;
  final List<String> paths;
  final String errorMessage;

  bool get isAvailable => paths.isNotEmpty;
}
