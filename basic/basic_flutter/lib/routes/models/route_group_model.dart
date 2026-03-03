
import 'package:basic_flutter/routes/models/route_item_model.dart';

class RouteGroup {
  final String name;
  final String describe;
  final List<RouteItem> routeItems;

  const RouteGroup({
    required this.name,
    required this.describe,
    required this.routeItems,
  });
}
