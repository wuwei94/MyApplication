import 'package:flutter/material.dart';
import 'package:slider_captcha/slider_captcha.dart';

/// Slider Captcha
/// https://pub.dev/packages/slider_captcha
class SliderCaptchaDemoPage extends StatelessWidget {
  const SliderCaptchaDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SliderCaptchaDemoView(title: title);
  }
}

class SliderCaptchaDemoView extends StatefulWidget {
  const SliderCaptchaDemoView({super.key, required this.title});

  final String title;

  @override
  State<SliderCaptchaDemoView> createState() => _SliderCaptchaDemoViewState();
}

class _SliderCaptchaDemoViewState extends State<SliderCaptchaDemoView> {
  static const Color _accentColor = Color(0xFF0F766E);
  static const List<String> _imageAssets = <String>[
    'assets/images/pic3.jpg',
    'assets/images/pic8.jpg',
    'assets/images/pic14.jpg',
    'assets/images/pic19.jpg',
  ];

  late final SliderController _controller;

  int _selectedImageIndex = 0;
  int _attemptCount = 0;
  int _successCount = 0;
  bool _isVerifying = false;
  bool? _lastVerified;
  String _statusTitle = '等待验证';
  String _statusMessage = '拖动滑块，让拼图块和缺口重合，体验本地滑块验证流程。';

  @override
  void initState() {
    super.initState();
    _controller = SliderController();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: SafeArea(
        child: LayoutBuilder(
          builder: (BuildContext context, BoxConstraints constraints) {
            final bool isWide = constraints.maxWidth >= 920;
            final bool useScrollableLayout =
                !isWide && constraints.maxHeight < 760;

            return Padding(
              padding: const EdgeInsets.all(16),
              child: isWide
                  ? Row(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: <Widget>[
                        Expanded(child: _buildOverviewCard(context)),
                        const SizedBox(width: 16),
                        Expanded(child: _buildCaptchaCard()),
                      ],
                    )
                  : useScrollableLayout
                  ? ListView(
                      children: <Widget>[
                        _buildOverviewCard(context),
                        const SizedBox(height: 16),
                        SizedBox(height: 520, child: _buildCaptchaCard()),
                      ],
                    )
                  : Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: <Widget>[
                        _buildOverviewCard(context),
                        const SizedBox(height: 16),
                        Expanded(child: _buildCaptchaCard()),
                      ],
                    ),
            );
          },
        ),
      ),
    );
  }

  Widget _buildOverviewCard(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ColorScheme colorScheme = theme.colorScheme;
    final double successRate = _attemptCount == 0
        ? 0
        : (_successCount / _attemptCount) * 100;

    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF0FDFA),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFF99F6E4)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120F172A),
            blurRadius: 18,
            offset: Offset(0, 10),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Container(
                  width: 52,
                  height: 52,
                  decoration: BoxDecoration(
                    color: _accentColor,
                    borderRadius: BorderRadius.circular(18),
                  ),
                  alignment: Alignment.center,
                  child: const Icon(
                    Icons.swipe_right_alt_rounded,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text(
                        'slider_captcha 1.0.2',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        '这个示例集成了拼图滑块验证码，演示如何通过 '
                        'SliderController 重置题目，并在回调中处理校验结果。',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: Colors.black54,
                          height: 1.45,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: <Widget>[
                _MetricChip(label: '尝试次数', value: '$_attemptCount'),
                _MetricChip(label: '验证成功', value: '$_successCount'),
                _MetricChip(
                  label: '成功率',
                  value: '${successRate.toStringAsFixed(0)}%',
                ),
              ],
            ),
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: const Color(0xFFCCFBF1)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Row(
                    children: <Widget>[
                      _StatusDot(
                        color: _lastVerified == null
                            ? colorScheme.outline
                            : (_lastVerified!
                                  ? colorScheme.primary
                                  : colorScheme.error),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          _statusTitle,
                          style: theme.textTheme.titleSmall?.copyWith(
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Text(
                    _statusMessage,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: Colors.black87,
                      height: 1.45,
                    ),
                  ),
                  if (_isVerifying) ...<Widget>[
                    const SizedBox(height: 14),
                    const LinearProgressIndicator(
                      minHeight: 6,
                      borderRadius: BorderRadius.all(Radius.circular(999)),
                    ),
                  ],
                ],
              ),
            ),
            const SizedBox(height: 16),
            const _NoteCard(
              icon: Icons.info_outline_rounded,
              title: '接入提示',
              message: '这个包提供的是前端拼图交互。正式业务里建议在回调里再接一层服务端校验，不要只依赖本地结果。',
            ),
            const SizedBox(height: 12),
            const _NoteCard(
              icon: Icons.construction_rounded,
              title: '当前示例',
              message:
                  '这里用 800ms 延迟模拟服务端处理耗时，随后通过 controller.create.call() 重新生成缺口位置。',
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCaptchaCard() {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFDCE3F0)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              '验证区',
              style: Theme.of(
                context,
              ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 6),
            Text(
              '可以切换背景图、重置题目，再拖动底部滑块尝试完成拼图。',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Colors.black54,
                height: 1.45,
              ),
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: <Widget>[
                FilledButton.icon(
                  onPressed: _isVerifying ? null : _refreshCaptcha,
                  icon: const Icon(Icons.refresh_rounded),
                  label: const Text('重置拼图'),
                ),
                OutlinedButton.icon(
                  onPressed: _isVerifying ? null : _switchImage,
                  icon: const Icon(Icons.image_outlined),
                  label: const Text('切换背景图'),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Text(
              '当前图片：${_currentImageLabel()}',
              style: Theme.of(
                context,
              ).textTheme.bodySmall?.copyWith(color: const Color(0xFF475569)),
            ),
            const SizedBox(height: 20),
            Expanded(
              child: LayoutBuilder(
                builder: (BuildContext context, BoxConstraints constraints) {
                  final double captchaWidth = constraints.maxWidth > 420
                      ? 420
                      : constraints.maxWidth;

                  return Center(
                    child: SizedBox(
                      width: captchaWidth,
                      child: SliderCaptcha(
                        controller: _controller,
                        image: Image.asset(
                          _imageAssets[_selectedImageIndex],
                          fit: BoxFit.cover,
                          width: double.infinity,
                          height: 260,
                        ),
                        title: _isVerifying
                            ? 'Verifying...'
                            : 'Slide to verify',
                        titleStyle: const TextStyle(
                          color: Color(0xFF0F172A),
                          fontWeight: FontWeight.w600,
                        ),
                        colorBar: const Color(0xFFE2E8F0),
                        colorCaptChar: _accentColor,
                        borderImager: 4,
                        imageToBarPadding: 12,
                        slideContainerDecoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(16),
                          boxShadow: const <BoxShadow>[
                            BoxShadow(
                              color: Color(0x1A0F172A),
                              blurRadius: 12,
                              offset: Offset(0, 6),
                            ),
                          ],
                        ),
                        onConfirm: _handleConfirm,
                      ),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _handleConfirm(bool value) async {
    setState(() {
      _isVerifying = true;
      _attemptCount += 1;
      _statusTitle = '正在校验';
      _statusMessage = '模拟服务端确认中，请稍候...';
    });

    await Future<void>.delayed(const Duration(milliseconds: 800));

    if (!mounted) {
      return;
    }

    setState(() {
      _isVerifying = false;
      _lastVerified = value;

      if (value) {
        _successCount += 1;
        _statusTitle = '验证成功';
        _statusMessage = '拼图块已经对准缺口，当前流程允许继续下一步操作。';
      } else {
        _statusTitle = '验证失败';
        _statusMessage = '滑块位置和缺口还有偏差，我已经帮你重新生成了一道题。';
      }
    });

    _scheduleCaptchaRebuild();
  }

  void _refreshCaptcha() {
    setState(() {
      _lastVerified = null;
      _statusTitle = '拼图已重置';
      _statusMessage = '题目已刷新，可以重新拖动滑块进行验证。';
    });

    _scheduleCaptchaRebuild();
  }

  void _switchImage() {
    setState(() {
      _selectedImageIndex = (_selectedImageIndex + 1) % _imageAssets.length;
      _lastVerified = null;
      _statusTitle = '背景图已切换';
      _statusMessage = '新的图片已经加载，并且会自动生成新的缺口位置。';
    });

    _scheduleCaptchaRebuild();
  }

  void _scheduleCaptchaRebuild() {
    WidgetsBinding.instance.addPostFrameCallback((Duration _) {
      if (!mounted) {
        return;
      }

      _controller.create.call();
    });
  }

  String _currentImageLabel() {
    final String asset = _imageAssets[_selectedImageIndex];
    return asset.split('/').last;
  }
}

class _MetricChip extends StatelessWidget {
  const _MetricChip({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: const Color(0xFFCCFBF1)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        child: RichText(
          text: TextSpan(
            style: Theme.of(
              context,
            ).textTheme.bodyMedium?.copyWith(color: const Color(0xFF0F172A)),
            children: <InlineSpan>[
              TextSpan(
                text: '$label  ',
                style: const TextStyle(color: Color(0xFF475569)),
              ),
              TextSpan(
                text: value,
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _NoteCard extends StatelessWidget {
  const _NoteCard({
    required this.icon,
    required this.title,
    required this.message,
  });

  final IconData icon;
  final String title;
  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFDCE3F0)),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Icon(icon, size: 20, color: const Color(0xFF0F766E)),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text(
                  title,
                  style: Theme.of(
                    context,
                  ).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 4),
                Text(
                  message,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Colors.black54,
                    height: 1.45,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _StatusDot extends StatelessWidget {
  const _StatusDot({required this.color});

  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 12,
      height: 12,
      decoration: BoxDecoration(color: color, shape: BoxShape.circle),
    );
  }
}
