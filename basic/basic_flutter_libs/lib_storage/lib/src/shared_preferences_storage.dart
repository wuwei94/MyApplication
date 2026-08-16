import 'package:lib_storage/src/i_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// SharedPreferences 内核实现，适合保存普通本地配置和非敏感数据。
class SharedPreferencesStorage implements IStorage {
  const SharedPreferencesStorage();

  static final SharedPreferencesAsync _prefs = SharedPreferencesAsync();

  @override
  Future<bool> setValue(String key, Object value) async {
    if (value is int) {
      await _prefs.setInt(key, value);
      return true;
    }

    if (value is bool) {
      await _prefs.setBool(key, value);
      return true;
    }

    if (value is double) {
      await _prefs.setDouble(key, value);
      return true;
    }

    if (value is String) {
      await _prefs.setString(key, value);
      return true;
    }

    if (value is List<String>) {
      await _prefs.setStringList(key, value);
      return true;
    }

    throw ArgumentError('Unsupported type: ${value.runtimeType}');
  }

  @override
  Future<T> getValue<T>(String key, T defaultValue) async {
    final Map<String, Object?> values = await _prefs.getAll(
      allowList: <String>{key},
    );
    final Object? value = values[key];

    if (value == null) {
      return defaultValue;
    }

    if (value is T) {
      return value as T;
    }

    return defaultValue;
  }

  @override
  Future<bool> remove(String key) async {
    await _prefs.remove(key);
    return true;
  }

  @override
  Future<bool> clearAll() async {
    await _prefs.clear();
    return true;
  }
}
