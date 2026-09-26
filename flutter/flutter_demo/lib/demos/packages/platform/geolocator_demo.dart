import 'package:flutter_demo/core/basic/basic.dart';
import 'package:geolocator/geolocator.dart';

const LocationSettings _currentPositionSettings = LocationSettings(
  accuracy: LocationAccuracy.medium,
  distanceFilter: 0,
  timeLimit: Duration(seconds: 30),
);

/// Geolocator — 定位服务状态 / 权限 / 当前位置
///
/// 核心机制与避坑点：
/// 1. 调用顺序：先 `isLocationServiceEnabled` + `checkPermission`，再 `getCurrentPosition`。
/// 2. 超时兜底：实时定位超时可回退 `getLastKnownPosition`，避免无结果空转。
///
/// 官方参考：
/// https://pub.dev/packages/geolocator
class GeolocatorDemoPage extends BasicResponsePage {
  const GeolocatorDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<GeolocatorDemoPage> createState() =>
      _GeolocatorDemoPageState();
}

class _GeolocatorDemoPageState extends BasicResponsePageState<GeolocatorDemoPage> {
  @override
  void initState() {
    super.initState();
    showDescription('geolocator 示例：服务状态 / 权限 / 当前位置');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 读取定位服务与权限',
        '2. 申请权限并获取当前位置',
        '3. 读取上次缓存位置',
        '4. 打开定位设置',
        '5. 打开应用设置',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _readStatus();
      case 1:
        _requestAndLocate();
      case 2:
        _readLastKnown();
      case 3:
        _openLocationSettings();
      case 4:
        _openAppSettings();
    }
  }

  Future<void> _readStatus() async {
    appendLog('→ [status] isLocationServiceEnabled + checkPermission');
    try {
      final bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      final LocationPermission permission = await Geolocator.checkPermission();
      final LocationAccuracyStatus accuracy =
          await Geolocator.getLocationAccuracy();
      appendLog('✓ serviceEnabled=$serviceEnabled');
      appendLog('✓ permission=${permission.name}');
      appendLog('✓ accuracy=${accuracy.name}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _requestAndLocate() async {
    appendLog('→ [locate] requestPermission + getCurrentPosition');
    try {
      final bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        appendLog('✗ 系统定位服务未开启');
        return;
      }

      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }
      appendLog('✓ permission=${permission.name}');

      final Position position = await Geolocator.getCurrentPosition(
        locationSettings: _currentPositionSettings,
      );
      appendLog(
        '✓ lat=${position.latitude.toStringAsFixed(6)} '
        'lng=${position.longitude.toStringAsFixed(6)}',
      );
      appendLog('✓ accuracy=${position.accuracy.toStringAsFixed(1)}m');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _readLastKnown() async {
    appendLog('→ [lastKnown] Geolocator.getLastKnownPosition()');
    try {
      final Position? position = await Geolocator.getLastKnownPosition();
      if (position == null) {
        appendLog('✗ 暂无缓存位置');
        return;
      }
      appendLog(
        '✓ lat=${position.latitude.toStringAsFixed(6)} '
        'lng=${position.longitude.toStringAsFixed(6)}',
      );
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _openLocationSettings() async {
    appendLog('→ [settings] Geolocator.openLocationSettings()');
    try {
      await Geolocator.openLocationSettings();
      appendLog('✓ 已请求打开定位设置');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _openAppSettings() async {
    appendLog('→ [settings] Geolocator.openAppSettings()');
    try {
      await Geolocator.openAppSettings();
      appendLog('✓ 已请求打开应用设置');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }
}
