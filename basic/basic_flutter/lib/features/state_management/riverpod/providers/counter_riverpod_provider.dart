import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';

final StateProvider<int> counterRiverpodProvider = StateProvider<int>(
  (Ref ref) => 0,
);
