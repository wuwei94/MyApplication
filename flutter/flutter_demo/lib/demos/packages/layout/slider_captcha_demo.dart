import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:slider_captcha/slider_captcha.dart';

/// SliderCaptcha — 滑动拼图校验
///
/// 纯布局预览，无操作列表；画布直接展示 SliderCaptcha 交互控件。
///
/// 官方参考：
/// https://pub.dev/packages/slider_captcha
class SliderCaptchaDemoPage extends BasicLayoutPage {
  const SliderCaptchaDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<SliderCaptchaDemoPage> createState() =>
      _SliderCaptchaDemoPageState();
}

class _SliderCaptchaDemoPageState
    extends BasicLayoutPageState<SliderCaptchaDemoPage> {
  final SliderController _controller = SliderController();
  bool? _result;

  @override
  Widget buildPreview() {
    final ThemeData theme = Theme.of(context);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            _result == null
                ? '拖动滑块完成拼图校验'
                : (_result! ? '校验通过' : '校验失败，请重试'),
            style: theme.textTheme.titleSmall,
          ),
          const SizedBox(height: 12),
          SliderCaptcha(
            controller: _controller,
            image: Container(
              color: Colors.blueGrey,
              alignment: Alignment.center,
              child: const Text(
                'captcha image',
                style: TextStyle(color: Colors.white),
              ),
            ),
            colorBar: Colors.blue,
            colorCaptChar: Colors.black26,
            onConfirm: _handleConfirm,
            title: '向右滑动',
            titleStyle: const TextStyle(color: Colors.white),
          ),
        ],
      ),
    );
  }

  Future<void> _handleConfirm(bool value) async {
    if (!mounted) {
      return;
    }
    setState(() {
      _result = value;
    });
    if (!value) {
      await Future<void>.delayed(const Duration(milliseconds: 600));
      _controller.create();
    }
  }
}
