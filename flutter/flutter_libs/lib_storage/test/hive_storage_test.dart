import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:hive/hive.dart';
import 'package:lib_storage/lib_storage.dart';

void main() {
  late Directory tempDir;

  setUpAll(() async {
    tempDir = Directory.systemTemp.createTempSync('lib_storage_hive_test');
    await HiveStorage.initForTesting(tempDir.path);
  });

  tearDownAll(() async {
    await Hive.close();
    tempDir.deleteSync(recursive: true);
  });

  test('setValue / getValue 按类型往返', () async {
    const HiveStorage storage = HiveStorage();

    await storage.setValue('count', 42);
    await storage.setValue('name', 'Ada');

    expect(await storage.getValue<int>('count', 0), 42);
    expect(await storage.getValue<String>('name', ''), 'Ada');
  });

  test('缺失 key 返回默认值', () async {
    const HiveStorage storage = HiveStorage();

    expect(await storage.getValue<int>('missing', 7), 7);
  });

  test('List<dynamic> 可转换为 List<String>', () async {
    const HiveStorage storage = HiveStorage();

    await storage.setValue('tags', <String>['a', 'b']);

    expect(
      await storage.getValue<List<String>>('tags', <String>[]),
      <String>['a', 'b'],
    );
  });

  test('remove 删除指定 key', () async {
    const HiveStorage storage = HiveStorage();

    await storage.setValue('temp', 1);
    await storage.remove('temp');

    expect(await storage.getValue<int>('temp', 0), 0);
  });

  test('clearAll 清空全部数据', () async {
    const HiveStorage storage = HiveStorage();

    await storage.setValue('k1', 1);
    await storage.setValue('k2', 2);
    await storage.clearAll();

    expect(await storage.getValue<int>('k1', 0), 0);
    expect(await storage.getValue<int>('k2', 0), 0);
  });
}
