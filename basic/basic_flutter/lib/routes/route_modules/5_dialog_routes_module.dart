import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/features/dialog/my_dialog.dart';
import 'package:go_router/go_router.dart';

final List<GoRoute> dialogRoutes = [
  GoRoute(
    path: DialogRoutes.dialog,
    builder: (context, state) => const MyDialog(),
  ),
];
