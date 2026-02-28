
import 'package:basic_flutter/routes/models/route_item_model.dart';

class RouteGroup {
  final String name;
  final List<RouteItem> routes;

  const RouteGroup({
    required this.name,
    required this.routes,
  });
}
