import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:share_plus/share_plus.dart';

/// Share Plus — 系统分享面板
///
/// 核心机制与避坑点：
/// 1. 统一入口：`SharePlus.instance.share(ShareParams(...))` 返回 `ShareResult`。
/// 2. 平台锚点：iPad / 桌面需要 `sharePositionOrigin`，否则分享弹层定位异常。
///
/// 官方参考：
/// https://pub.dev/packages/share_plus
class SharePlusDemoPage extends BasicResponsePage {
  const SharePlusDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<SharePlusDemoPage> createState() =>
      _SharePlusDemoPageState();
}

class _SharePlusDemoPageState extends BasicResponsePageState<SharePlusDemoPage> {
  static const String _sampleTitle = 'flutter_demo share_plus 示例';
  static const String _sampleText =
      '来自 flutter_demo 的 share_plus 分享内容，用于演示系统分享面板。';
  static final Uri _packageUri = Uri.parse(
    'https://pub.dev/packages/share_plus',
  );

  @override
  void initState() {
    super.initState();
    showDescription('share_plus 示例：分享文本 / 链接 / 组合内容');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 分享文本',
        '2. 分享链接',
        '3. 分享文本和链接',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _shareText();
      case 1:
        _shareUri();
      case 2:
        _shareTextAndUri();
    }
  }

  Rect? _sharePositionOrigin(BuildContext context) {
    final RenderBox? box = context.findRenderObject() as RenderBox?;
    return box == null
        ? null
        : box.localToGlobal(Offset.zero) & box.size;
  }

  Future<void> _shareText() async {
    appendLog('→ [share] text');
    await _share(
      params: ShareParams(
        title: _sampleTitle,
        subject: _sampleTitle,
        text: _sampleText,
        sharePositionOrigin: _sharePositionOrigin(context),
      ),
    );
  }

  Future<void> _shareUri() async {
    appendLog('→ [share] uri');
    await _share(
      params: ShareParams(
        title: _sampleTitle,
        uri: _packageUri,
        sharePositionOrigin: _sharePositionOrigin(context),
      ),
    );
  }

  Future<void> _shareTextAndUri() async {
    appendLog('→ [share] title + text + uri');
    await _share(
      params: ShareParams(
        title: _sampleTitle,
        subject: _sampleTitle,
        text: _sampleText,
        uri: _packageUri,
        sharePositionOrigin: _sharePositionOrigin(context),
      ),
    );
  }

  Future<void> _share({required ShareParams params}) async {
    try {
      final ShareResult result = await SharePlus.instance.share(params);
      appendLog('✓ status=${result.status.name}');
      if (result.raw.isNotEmpty) {
        appendLog('✓ raw=${result.raw}');
      }
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }
}
