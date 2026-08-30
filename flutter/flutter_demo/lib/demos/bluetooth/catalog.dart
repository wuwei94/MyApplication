import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/bluetooth/ble_device_demo.dart';
import 'package:flutter_demo/demos/bluetooth/ble_scan_demo.dart';
import 'package:flutter_demo/demos/bluetooth/ble_transfer_demo.dart';

/// Bluetooth 模块
///
/// 包含：BLE 扫描过滤、GATT 连接读写与分包流控传输示例
class BluetoothCatalog extends CatalogSection {
  const BluetoothCatalog._();

  @override
  String get path => 'bluetooth';

  @override
  String get title => 'Bluetooth';

  @override
  String get subtitle => 'BLE 设备扫描、GATT 交互与分包传输';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = <CatalogEntry>[
    CatalogEntry.page(
      path: 'scan',
      title: 'BLE 扫描与过滤',
      subtitle: '适配器状态监听、动态权限与 RSSI 实时刷新',
      pageBuilder: (BuildContext context) =>
          const BleScanDemoPage(title: 'BLE 扫描与过滤'),
    ),
    CatalogEntry.page(
      path: 'device',
      title: 'BLE 连接与 GATT 交互',
      subtitle: '服务发现、特征读写与 Notify 数据流订阅',
      pageBuilder: (BuildContext context) =>
          const BleDeviceDemoPage(title: 'BLE 连接与 GATT 交互'),
    ),
    CatalogEntry.page(
      path: 'transfer',
      title: 'BLE 传输与分包流控',
      subtitle: 'MTU 协商、大数据 Chunking 切片与拼包组装',
      pageBuilder: (BuildContext context) =>
          const BleTransferDemoPage(title: 'BLE 传输与分包流控'),
    ),
  ];
}

/// 单例实例
const BluetoothCatalog bluetoothCatalog = BluetoothCatalog._();
