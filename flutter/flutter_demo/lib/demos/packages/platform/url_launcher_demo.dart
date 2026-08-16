import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

/// Url Launcher
/// https://pub.dev/packages/url_launcher
class UrlLauncherDemoPage extends StatelessWidget {
  const UrlLauncherDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return UrlLauncherDemoView(title: title);
  }
}

class UrlLauncherDemoView extends StatefulWidget {
  const UrlLauncherDemoView({super.key, required this.title});

  final String title;

  @override
  State<UrlLauncherDemoView> createState() => _UrlLauncherDemoViewState();
}

class _UrlLauncherDemoViewState extends State<UrlLauncherDemoView> {
  static final Uri _packagePageUri = Uri.parse(
    'https://pub.dev/packages/url_launcher',
  );
  static const double _mapLatitude = 31.2400;
  static const double _mapLongitude = 121.4900;
  static const String _mapLabel = 'The Bund Shanghai';
  static final Uri _webMapUri = Uri.parse(
    'https://www.google.com/maps/search/?api=1&query=$_mapLatitude,$_mapLongitude',
  );

  String _statusTitle = '尚未触发链接';
  String _statusMessage = '点击下方按钮，体验浏览器、邮件、电话、短信和地图应用的拉起效果。';
  bool? _lastSucceeded;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    final ThemeData theme = Theme.of(context);

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        _InfoCard(
          title: 'url_launcher 示例',
          description:
              '官方推荐优先直接调用 launchUrl，并在失败时做兜底处理。'
              '这个页面演示 https、mailto、tel、sms、地图拉起等常见场景。',
          accentColor: const Color(0xFF0F5DAA),
          child: Text(
            '说明：模拟器、桌面端或未安装对应 App 的设备，'
            '可能无法处理 tel / sms / mailto / geo 等链接，这属于正常现象。',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ),
        const SizedBox(height: 12),
        _StatusCard(
          title: _statusTitle,
          message: _statusMessage,
          succeeded: _lastSucceeded,
        ),
        const SizedBox(height: 16),
        _LaunchActionCard(
          icon: Icons.open_in_browser_rounded,
          title: '打开包文档页',
          subtitle: '使用 https 链接打开 pub.dev 包详情页',
          buttonLabel: '打开网页',
          onPressed: _openPackagePage,
        ),
        const SizedBox(height: 12),
        _LaunchActionCard(
          icon: Icons.email_outlined,
          title: '发送邮件',
          subtitle: '使用 mailto scheme，带上 subject 与 body 参数',
          buttonLabel: '打开邮件',
          onPressed: _composeEmail,
        ),
        const SizedBox(height: 12),
        _LaunchActionCard(
          icon: Icons.call_outlined,
          title: '拨打电话',
          subtitle: '使用 tel scheme 唤起系统拨号能力',
          buttonLabel: '打开拨号',
          onPressed: _dialPhoneNumber,
        ),
        const SizedBox(height: 12),
        _LaunchActionCard(
          icon: Icons.sms_outlined,
          title: '发送短信',
          subtitle: '使用 sms scheme，并预填一段短信内容',
          buttonLabel: '打开短信',
          onPressed: _sendSms,
        ),
        const SizedBox(height: 12),
        _LaunchActionCard(
          icon: Icons.map_outlined,
          title: '打开地图应用',
          subtitle: '优先尝试系统地图能力，失败时回退到网页地图',
          buttonLabel: '打开地图',
          onPressed: _openMapApp,
        ),
        const SizedBox(height: 12),
        const _InfoCard(
          title: '平台配置提示',
          description:
              '当前示例没有调用 canLaunchUrl，所以不需要额外配置白名单。'
              '如果后续要在运行时先探测能力，再决定是否展示按钮，'
              '需要按官方文档补充 iOS Info.plist 与 AndroidManifest 的 scheme 配置。',
          accentColor: Color(0xFF1C8A63),
          child: SizedBox.shrink(),
        ),
      ],
    );
  }

  Future<void> _openPackagePage() async {
    await _launchSample(
      label: '打开包文档页',
      uri: _packagePageUri,
      mode: LaunchMode.externalApplication,
    );
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

    await _launchSample(
      label: '发送邮件',
      uri: emailUri,
      fallbackUri: _packagePageUri,
      fallbackLabel: '包文档页',
    );
  }

  Future<void> _dialPhoneNumber() async {
    final Uri phoneUri = Uri(scheme: 'tel', path: '+8613800138000');

    await _launchSample(label: '拨打电话', uri: phoneUri);
  }

  Future<void> _sendSms() async {
    final Uri smsUri = Uri(
      scheme: 'sms',
      path: '13800138000',
      query: _encodeQueryParameters(<String, String>{
        'body': 'Hi from flutter_demo url_launcher demo.',
      }),
    );

    await _launchSample(label: '发送短信', uri: smsUri);
  }

  Future<void> _openMapApp() async {
    await _launchSample(
      label: '打开地图应用',
      uri: _buildMapUri(),
      mode: LaunchMode.externalApplication,
      fallbackUri: _webMapUri,
      fallbackLabel: '网页地图',
    );
  }

  Uri _buildMapUri() {
    const String coordinates = '$_mapLatitude,$_mapLongitude';

    if (kIsWeb) {
      return _webMapUri;
    }

    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return Uri(
          scheme: 'geo',
          path: coordinates,
          queryParameters: <String, String>{'q': '$coordinates($_mapLabel)'},
        );
      case TargetPlatform.iOS:
      case TargetPlatform.macOS:
        return Uri.parse(
          'https://maps.apple.com/?ll=$coordinates&q=${Uri.encodeComponent(_mapLabel)}',
        );
      case TargetPlatform.windows:
      case TargetPlatform.linux:
      case TargetPlatform.fuchsia:
        return _webMapUri;
    }
  }

  Future<void> _launchSample({
    required String label,
    required Uri uri,
    LaunchMode mode = LaunchMode.platformDefault,
    Uri? fallbackUri,
    String? fallbackLabel,
  }) async {
    final bool launched = await launchUrl(uri, mode: mode);

    if (launched) {
      _updateStatus(
        title: label,
        message: '已尝试打开 ${uri.scheme} 链接：$uri',
        succeeded: true,
      );
      return;
    }

    if (fallbackUri != null) {
      final bool fallbackLaunched = await launchUrl(
        fallbackUri,
        mode: LaunchMode.externalApplication,
      );

      if (fallbackLaunched) {
        _updateStatus(
          title: '$label 已回退',
          message:
              '当前设备未能处理 ${uri.scheme} 链接，已回退到${fallbackLabel ?? fallbackUri.scheme}。',
          succeeded: true,
        );
        return;
      }
    }

    _updateStatus(
      title: '$label 失败',
      message: '当前设备无法处理 ${uri.scheme} 链接，请检查是否安装了对应应用。',
      succeeded: false,
    );
  }

  void _updateStatus({
    required String title,
    required String message,
    required bool succeeded,
  }) {
    if (!mounted) {
      return;
    }

    setState(() {
      _statusTitle = title;
      _statusMessage = message;
      _lastSucceeded = succeeded;
    });

    final SnackBar snackBar = SnackBar(
      content: Text(message),
      behavior: SnackBarBehavior.floating,
      backgroundColor: succeeded
          ? const Color(0xFF1C8A63)
          : Theme.of(context).colorScheme.error,
    );

    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(snackBar);
  }

  String _encodeQueryParameters(Map<String, String> params) {
    return params.entries
        .map(
          (MapEntry<String, String> entry) =>
              '${Uri.encodeComponent(entry.key)}=${Uri.encodeComponent(entry.value)}',
        )
        .join('&');
  }
}

class _LaunchActionCard extends StatelessWidget {
  const _LaunchActionCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.buttonLabel,
    required this.onPressed,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final String buttonLabel;
  final Future<void> Function() onPressed;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120A2533),
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: <Widget>[
            DecoratedBox(
              decoration: const BoxDecoration(
                color: Color(0xFFF3F7FF),
                shape: BoxShape.circle,
              ),
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Icon(icon, color: const Color(0xFF0F5DAA)),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    title,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    subtitle,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            FilledButton(onPressed: onPressed, child: Text(buttonLabel)),
          ],
        ),
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  const _InfoCard({
    required this.title,
    required this.description,
    required this.accentColor,
    required this.child,
  });

  final String title;
  final String description;
  final Color accentColor;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: accentColor.withValues(alpha: 0.16)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120A2533),
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
                color: accentColor,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              description,
              style: theme.textTheme.bodyMedium?.copyWith(height: 1.5),
            ),
            const SizedBox(height: 12),
            child,
          ],
        ),
      ),
    );
  }
}

class _StatusCard extends StatelessWidget {
  const _StatusCard({
    required this.title,
    required this.message,
    required this.succeeded,
  });

  final String title;
  final String message;
  final bool? succeeded;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final Color accentColor;

    if (succeeded == null) {
      accentColor = const Color(0xFF0F5DAA);
    } else if (succeeded!) {
      accentColor = const Color(0xFF1C8A63);
    } else {
      accentColor = theme.colorScheme.error;
    }

    return DecoratedBox(
      decoration: BoxDecoration(
        color: accentColor.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: accentColor.withValues(alpha: 0.18)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
                color: accentColor,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              message,
              style: theme.textTheme.bodyMedium?.copyWith(height: 1.45),
            ),
          ],
        ),
      ),
    );
  }
}
