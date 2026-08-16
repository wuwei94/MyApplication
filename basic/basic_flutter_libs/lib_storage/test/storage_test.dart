import 'package:flutter_test/flutter_test.dart';
import 'package:lib_storage/lib_storage.dart';

void main() {
  tearDown(() {
    Storage.kernel = const HiveStorage();
  });

  test('默认内核为 Hive 实现', () {
    expect(Storage.kernel, isA<HiveStorage>());
  });

  test('切换内核后门面转发到新内核，调用方 API 不变', () async {
    final _FakeStorage fake = _FakeStorage();
    Storage.kernel = fake;

    await Storage.setValue('a', 1);
    await Storage.getValue<int>('a', 0);
    await Storage.remove('a');
    await Storage.clearAll();

    expect(fake.calls, <String>['setValue', 'getValue', 'remove', 'clearAll']);
  });
}

class _FakeStorage implements IStorage {
  final List<String> calls = <String>[];

  @override
  Future<bool> setValue(String key, Object value) async {
    calls.add('setValue');
    return true;
  }

  @override
  Future<T> getValue<T>(String key, T defaultValue) async {
    calls.add('getValue');
    return defaultValue;
  }

  @override
  Future<bool> remove(String key) async {
    calls.add('remove');
    return true;
  }

  @override
  Future<bool> clearAll() async {
    calls.add('clearAll');
    return true;
  }
}
