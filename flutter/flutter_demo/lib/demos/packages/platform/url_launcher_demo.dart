import 'package:flutter_demo/core/basic/basic.dart';
import 'package:url_launcher/url_launcher.dart';

/// Url Launcher — 拉起系统能力
///
/// 核心机制与避坑点：
/// 1. 优先直调：`launchUrl` 失败再做兜底，不必先 `canLaunchUrl`。
/// 2. Scheme 边界：`tel` / `sms` / `mailto` / `geo` 在模拟器或未装对应 App 时可能失败。
///
/// 官方参考：
/// https://pub.dev/packages/url_launcher
class UrlLauncherDemoPage extends BasicResponsePage {
  const UrlLauncherDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<UrlLauncherDemoPage> createState() =>
      _UrlLauncherDemoPageState();
}

class _UrlLauncherDemoPageState
    extends BasicResponsePageState<UrlLauncherDemoPage> {
  static final Uri _packagePageUri = Uri.parse(
    'https://pub.dev/packages/url_launcher',
  );
  static const double _mapLatitude = 31.2400;
  static const double _mapLongitude = 121.4900;
  static final Uri _webMapUri = Uri.parse(
    'https://www.google.com/maps/search/?api=1'
    '&query=$_mapLatitude,$_mapLongitude',
  );

  @override
  void initState() {
    super.initState();
    showDescription('url_launcher 示例：https / mailto / tel / sms / geo');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 打开网页',
        '2. 发送邮件',
        '3. 拨打电话',
        '4. 发送短信',
        '5. 打开地图',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _openWebPage();
      case 1:
        _composeEmail();
      case 2:
        _dialPhone();
      case 3:
        _sendSms();
      case 4:
        _openMap();
    }
  }

  Future<void> _openWebPage() async {
    await _launch(uri: _packagePageUri, mode: LaunchMode.externalApplication);
  }

  Future<void> _composeEmail() async {
    final Uri emailUri = Uri(
      scheme: 'mailto',
      path: 'flutter-demo@example.com',
      query: _encodeQueryParameters(<String, String>{
        'subject': 'flutter_demo UrlLauncher Demo',
        'body': 'Hello from the url_launcher example page.',
      }),
    );
    await _launch(uri: emailUri);
  }

  Future<void> _dialPhone() async {
    await _launch(uri: Uri(scheme: 'tel', path: '000-000-0000'));
  }

  Future<void> _sendSms() async {
    final Uri smsUri = Uri(
      scheme: 'sms',
      path: '000-000-0000',
      queryParameters: <String, String>{'body': 'Hello from url_launcher demo'},
    );
    await _launch(uri: smsUri);
  }

  Future<void> _openMap() async {
    final Uri geoUri = Uri.parse(
      'geo:$_mapLatitude,$_mapLongitude?q=$_mapLatitude,$_mapLongitude',
    );
    appendLog('→ [launch] $geoUri');
    final bool launched = await launchUrl(geoUri);
    if (launched) {
      appendLog('✓ launched=$geoUri');
      return;
    }
    appendLog('✗ geo 不可用，回退网页地图');
    await _launch(uri: _webMapUri, mode: LaunchMode.externalApplication);
  }

  Future<void> _launch({
    required Uri uri,
    LaunchMode mode = LaunchMode.platformDefault,
  }) async {
    appendLog('→ [launch] $uri mode=${mode.name}');
    try {
      final bool launched = await launchUrl(uri, mode: mode);
      if (launched) {
        appendLog('✓ launched=true');
      } else {
        appendLog('✗ launched=false');
      }
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  String _encodeQueryParameters(Map<String, String> params) {
    return params.entries
        .map(
          (MapEntry<String, String> e) =>
              '${Uri.encodeComponent(e.key)}=${Uri.encodeComponent(e.value)}',
        )
        .join('&');
  }
}
