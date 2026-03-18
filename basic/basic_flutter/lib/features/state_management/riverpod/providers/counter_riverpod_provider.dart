import 'package:flutter_riverpod/flutter_riverpod.dart';

final StateProvider<int> counterRiverpodProvider = StateProvider<int>(
  (Ref ref) => 0,
);
