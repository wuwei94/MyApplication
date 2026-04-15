// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:basic_flutter/app/app.dart';
import 'package:basic_flutter/demos/examples/counter/counter_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('Counter increments smoke test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MaterialApp(home: CounterExample(title: 'Counter')));

    // Verify that our counter starts at 0.
    expect(find.text('0'), findsOneWidget);
    expect(find.text('1'), findsNothing);

    // Tap the '+' icon and trigger a frame.
    await tester.tap(find.byIcon(Icons.add));
    await tester.pump();

    // Verify that our counter has incremented.
    expect(find.text('0'), findsNothing);
    expect(find.text('1'), findsOneWidget);
  });

  testWidgets('navigates to nested layout example from demo catalog', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const DemoCatalogApp());
    await tester.pumpAndSettle();

    await tester.tap(find.text('Layout'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('容器布局'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Container'));
    await tester.pumpAndSettle();

    expect(find.text('Basic Container'), findsOneWidget);
  });
}
