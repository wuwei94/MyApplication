import 'package:flutter/material.dart';
import 'package:flutter_smart_dialog/flutter_smart_dialog.dart';

/// flutter_smart_dialog 项目级统一封装
class AppSmartDialog {
  AppSmartDialog._();

  static final TransitionBuilder initBuilder = FlutterSmartDialog.init(
    toastBuilder: (String msg) => _AppToastWidget(message: msg),
    loadingBuilder: (String msg) => _AppLoadingWidget(message: msg),
  );

  static List<NavigatorObserver> createNavigatorObservers() {
    return <NavigatorObserver>[FlutterSmartDialog.observer];
  }

  static Future<void> showToast(String message) {
    return SmartDialog.showToast(
      message,
      displayTime: const Duration(seconds: 2),
      animationTime: const Duration(milliseconds: 250),
    );
  }

  static Future<T?> showLoading<T>({
    String message = '正在处理中...',
    bool clickMaskDismiss = false,
  }) {
    return SmartDialog.showLoading<T>(
      msg: message,
      clickMaskDismiss: clickMaskDismiss,
      backType: SmartBackType.block,
    );
  }

  static Future<T?> showCustomDialog<T>({
    required WidgetBuilder builder,
    bool clickMaskDismiss = true,
    SmartBackType backType = SmartBackType.normal,
  }) {
    return SmartDialog.show<T>(
      builder: builder,
      clickMaskDismiss: clickMaskDismiss,
      backType: backType,
      animationType: SmartAnimationType.centerScale_otherSlide,
    );
  }

  static Future<bool?> showConfirm({
    required String title,
    required String message,
    String confirmText = '确认',
    String cancelText = '取消',
  }) {
    return showCustomDialog<bool>(
      clickMaskDismiss: false,
      backType: SmartBackType.block,
      builder: (BuildContext context) {
        return _AppConfirmDialog(
          title: title,
          message: message,
          confirmText: confirmText,
          cancelText: cancelText,
        );
      },
    );
  }

  static Future<void> dismiss<T>({
    SmartStatus status = SmartStatus.smart,
    T? result,
    bool force = false,
  }) {
    return SmartDialog.dismiss<T>(status: status, result: result, force: force);
  }

  static Future<void> dismissLoading() {
    return dismiss<void>(status: SmartStatus.loading);
  }

  static Future<void> dismissCustomDialog<T>({T? result}) {
    return dismiss<T>(status: SmartStatus.custom, result: result);
  }
}

class _AppToastWidget extends StatelessWidget {
  const _AppToastWidget({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      constraints: const BoxConstraints(maxWidth: 360),
      margin: const EdgeInsets.symmetric(horizontal: 24),
      decoration: BoxDecoration(
        color: const Color(0xFF152238),
        borderRadius: BorderRadius.circular(18),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x33152238),
            blurRadius: 20,
            offset: Offset(0, 10),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Container(
              width: 32,
              height: 32,
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(10),
              ),
              alignment: Alignment.center,
              child: const Icon(
                Icons.check_circle_outline_rounded,
                size: 18,
                color: Colors.white,
              ),
            ),
            const SizedBox(width: 12),
            Flexible(
              child: Text(
                message,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 14,
                  height: 1.35,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _AppLoadingWidget extends StatelessWidget {
  const _AppLoadingWidget({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 176,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x1A0F172A),
            blurRadius: 24,
            offset: Offset(0, 12),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          const SizedBox(
            width: 36,
            height: 36,
            child: CircularProgressIndicator(strokeWidth: 3),
          ),
          const SizedBox(height: 16),
          Text(
            message,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 14,
              height: 1.4,
              fontWeight: FontWeight.w600,
              color: Color(0xFF0F172A),
            ),
          ),
        ],
      ),
    );
  }
}

class _AppConfirmDialog extends StatelessWidget {
  const _AppConfirmDialog({
    required this.title,
    required this.message,
    required this.confirmText,
    required this.cancelText,
  });

  final String title;
  final String message;
  final String confirmText;
  final String cancelText;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 360),
          child: Material(
            color: Colors.white,
            borderRadius: BorderRadius.circular(28),
            clipBehavior: Clip.antiAlias,
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Container(
                    width: 52,
                    height: 52,
                    decoration: BoxDecoration(
                      color: const Color(0xFFE0F2FE),
                      borderRadius: BorderRadius.circular(16),
                    ),
                    alignment: Alignment.center,
                    child: const Icon(
                      Icons.chat_bubble_outline_rounded,
                      color: Color(0xFF0369A1),
                    ),
                  ),
                  const SizedBox(height: 18),
                  Text(
                    title,
                    style: theme.textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 10),
                  Text(
                    message,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: const Color(0xFF475569),
                      height: 1.45,
                    ),
                  ),
                  const SizedBox(height: 24),
                  Row(
                    children: <Widget>[
                      Expanded(
                        child: OutlinedButton(
                          onPressed: () {
                            AppSmartDialog.dismissCustomDialog<bool>(
                              result: false,
                            );
                          },
                          child: Text(cancelText),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: FilledButton(
                          onPressed: () {
                            AppSmartDialog.dismissCustomDialog<bool>(
                              result: true,
                            );
                          },
                          child: Text(confirmText),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
