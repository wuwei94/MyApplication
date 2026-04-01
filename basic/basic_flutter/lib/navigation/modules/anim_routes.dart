import 'package:basic_flutter/features/anim/lottie_example.dart';
import 'package:basic_flutter/features/anim/pag_example.dart';
import 'package:basic_flutter/features/anim/svg_example.dart';
import 'package:basic_flutter/features/anim/svga_example.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

final List<RouteItem> animRoutes = [
  RouteItem(
    path: '/package/svg',
    title: 'SVG',
    subtitle: 'SVG动画示例',
    routeBuilder: (BuildContext context, _) => const SvgExample(title: 'SVG'),
  ),
  RouteItem(
    path: '/package/svga',
    title: 'SVGA',
    subtitle: 'SVGA动画示例',
    routeBuilder: (BuildContext context, _) => const SvgaExample(title: 'SVGA'),
  ),
  RouteItem(
    path: '/package/lottie',
    title: 'Lottie',
    subtitle: 'Lottie动画示例',
    routeBuilder: (BuildContext context, _) =>
        const LottieExample(title: 'Lottie'),
  ),
  RouteItem(
    path: '/package/pag',
    title: 'PAG',
    subtitle: 'PAG动画示例',
    routeBuilder: (BuildContext context, _) => const PagExample(title: 'PAG'),
  ),
];
