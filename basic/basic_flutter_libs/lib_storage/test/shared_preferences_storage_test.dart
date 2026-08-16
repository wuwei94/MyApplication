import 'package:flutter_test/flutter_test.dart';
import 'package:lib_storage/lib_storage.dart';
import 'package:shared_preferences_platform_interface/in_memory_shared_preferences_async.dart';
import 'package:shared_preferences_platform_interface/shared_preferences_async_platform_interface.dart';

void main() {
  setUpAll(() {
    SharedPreferencesAsyncPlatform.instance = InMemorySharedPreferencesAsync.empty();
  });

  test('setValue / getValue 按类型往返', () async {
    const SharedPreferencesStorage storage = SharedPreferencesStorage();

    await storage.setValue('count', 42);
    await storage.setValue('pi', 3.14);
    await storage.setValue('enabled', true);
    await storage.setValue('name', 'Ada');
    await storage.setValue('tags', <String>['a', 'b']);

    expect(await storage.getValue<int>('count', 0), 42);
    expect(await storage.getValue<double>('pi', 0), 3.14);
    expect(await storage.getValue<bool>('enabled', false), isTrue);
    expect(await storage.getValue<String>('name', ''), 'Ada');
    expect(
      await storage.getValue<List<String>>('tags', <String>[]),
      <String>['a', 'b'],
    );
  });

  test('缺失 key 返回默认值', () async {
    const SharedPreferencesStorage storage = SharedPreferencesStorage();

    expect(await storage.getValue<int>('missing', 7), 7);
  });

  test('不支持的类型抛出 ArgumentError', () async {
    const SharedPreferencesStorage storage = SharedPreferencesStorage();

    await expectLater(
      storage.setValue('bad', <int>[1, 2]),
      throwsA(isA<ArgumentError>()),
    );
  });

  test('remove 删除指定 key', () async {
    const SharedPreferencesStorage storage = SharedPreferencesStorage();

    await storage.setValue('temp', 1);
    await storage.remove('temp');

    expect(await storage.getValue<int>('temp', 0), 0);
  });

  test('clearAll 清空全部数据', () async {
    const SharedPreferencesStorage storage = SharedPreferencesStorage();

    await storage.setValue('k1', 1);
    await storage.setValue('k2', 2);
    await storage.clearAll();

    expect(await storage.getValue<int>('k1', 0), 0);
    expect(await storage.getValue<int>('k2', 0), 0);
  });
}
