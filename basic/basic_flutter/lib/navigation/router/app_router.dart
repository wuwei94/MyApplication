import 'package:flutter/widgets.dart';
import 'package:basic_flutter/navigation/router/auto_app_router.dart';
import 'package:basic_flutter/navigation/router/go_app_router.dart';

export 'auto_app_router.dart';
export 'go_app_router.dart';

enum AppRouterType { goRouter, autoRoute }

const AppRouterType currentAppRouterType = AppRouterType.autoRoute;

final RouterConfig<Object> appRouter = switch (currentAppRouterType) {
  AppRouterType.goRouter => goAppRouter,
  AppRouterType.autoRoute => autoAppRouter,
};
