import 'package:flutter/cupertino.dart';

/// 启动页面（无参数）
void start(BuildContext context, String routeName) {
  Navigator.of(context).pushNamed(routeName);
}

/// 启动页面（带参数）
void startWithArg(
  BuildContext context,
  String routeName,
  Map<String, dynamic>? arguments,
) {
  Navigator.of(context).pushNamed(routeName, arguments: arguments);
}
