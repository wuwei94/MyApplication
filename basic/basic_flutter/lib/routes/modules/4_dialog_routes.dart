import 'package:basic_flutter/features/4_dialog/my_dialog.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Dialog 对话框路由
final List<RouteItem> dialogRoutes = [
  RouteItem(
    name: 'Dialog',
    path: '/dialog',
    describe: 'Dialog',
    builder: (BuildContext context, _) => const MyDialog(),
  ),
];
