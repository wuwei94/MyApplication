import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/animations_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/async_programming_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/decoration_effects_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/dialogs_sheets_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/flow_layout_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/gesture_interaction_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/layout_builder_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/layout_containers_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/linear_layout_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/scroll_widgets_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/sliver_widgets_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/stack_positioning_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/state_driven_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/state_management_routes.dart';

final List<RouteItem> layoutRoutes = [
  ...layoutContainersRoutes,
  ...linearLayoutRoutes,
  ...stackPositioningRoutes,
  ...flowLayoutRoutes,
  ...scrollWidgetsRoutes,
  ...sliverWidgetsRoutes,
  ...decorationEffectsRoutes,
  ...gestureInteractionRoutes,
  ...stateDrivenRoutes,
  ...dialogsSheetsRoutes,
  ...animationsRoutes,
  ...layoutBuilderRoutes,
  ...stateManagementRoutes,
  ...asyncProgrammingRoutes,
];
