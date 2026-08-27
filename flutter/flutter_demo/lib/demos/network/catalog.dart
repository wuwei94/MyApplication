import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/network/dio_demo.dart';
import 'package:flutter_demo/demos/network/http_demo.dart';
import 'package:flutter_demo/demos/network/mqtt_demo.dart';
import 'package:flutter_demo/demos/network/websocket_demo.dart';

/// Network 模块
///
/// 包含：Dio、Http、WebSocket 等网络请求与通信示例
class NetworkCatalog extends CatalogSection {
  const NetworkCatalog._();

  @override
  String get path => 'network';

  @override
  String get title => 'Network';

  @override
  String get subtitle => '请求发送、响应展示与实时通信';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'dio',
      title: 'Dio',
      subtitle: '封装 POST 请求、响应解析与异常处理',
      pageBuilder: (BuildContext context) => const DioDemoPage(title: 'Dio'),
    ),
    CatalogEntry.page(
      path: 'http',
      title: 'Http',
      subtitle: '基础 POST 请求与统一响应结果展示',
      pageBuilder: (BuildContext context) => const HttpDemoPage(title: 'Http'),
    ),
    CatalogEntry.page(
      path: 'websocket',
      title: 'WebSocket',
      subtitle: 'WebSocket 连接、消息收发与连接状态管理',
      pageBuilder: (BuildContext context) =>
          const WebSocketDemoPage(title: 'WebSocket'),
    ),
    CatalogEntry.page(
      path: 'mqtt',
      title: 'MQTT',
      subtitle: 'MQTT 连接、订阅、发布（QoS 0/1/2）与消息接收',
      pageBuilder: (BuildContext context) => const MqttDemoPage(title: 'MQTT'),
    ),
  ];
}

/// 单例实例
const NetworkCatalog networkCatalog = NetworkCatalog._();
