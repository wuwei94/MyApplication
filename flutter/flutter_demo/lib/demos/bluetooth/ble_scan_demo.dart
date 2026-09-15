import 'dart:async';
import 'package:flutter/material.dart';
import 'package:lib_bluetooth/lib_bluetooth.dart';

/// BLE 设备扫描与过滤示例（调用 lib_bluetooth 本地库封装）
///
/// 演示使用 [BleClient] 进行蓝牙适配器状态监听、动态权限检查、
/// 设备扫描启停、RSSI 信号强度实时刷新与广播数据解析。
class BleScanDemoPage extends StatelessWidget {
  const BleScanDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return BleScanDemoView(title: title);
  }
}

class BleScanDemoView extends StatefulWidget {
  const BleScanDemoView({super.key, required this.title});

  final String title;

  @override
  State<BleScanDemoView> createState() => _BleScanDemoViewState();
}

class _BleScanDemoViewState extends State<BleScanDemoView> {
  BleAdapterState _adapterState = BleAdapterState.unknown;
  bool _isScanning = false;
  List<BleDeviceItem> _scanResults = <BleDeviceItem>[];
  String _filterKeyword = '';

  StreamSubscription<BleAdapterState>? _adapterStateSub;
  StreamSubscription<bool>? _isScanningSub;
  StreamSubscription<List<BleDeviceItem>>? _scanResultsSub;

  final TextEditingController _filterController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _initBluetooth();
  }

  void _initBluetooth() {
    _adapterStateSub = BleClient.instance.adapterState.listen((BleAdapterState state) {
      if (mounted) {
        setState(() {
          _adapterState = state;
        });
      }
    });

    _isScanningSub = BleClient.instance.isScanning.listen((bool scanning) {
      if (mounted) {
        setState(() {
          _isScanning = scanning;
        });
      }
    });

    _scanResultsSub = BleClient.instance.scanResults.listen((List<BleDeviceItem> results) {
      if (mounted) {
        setState(() {
          _scanResults = results;
        });
      }
    });
  }

  Future<void> _startScan() async {
    if (_adapterState != BleAdapterState.on) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('请先开启蓝牙功能 (当前状态: ${_adapterState.name})')),
      );
      return;
    }

    try {
      _scanResults.clear();
      await BleClient.instance.startScan(
        timeout: const Duration(seconds: 15),
        androidUsesFineLocation: true,
      );
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('启动扫描异常: $e')),
        );
      }
    }
  }

  Future<void> _stopScan() async {
    try {
      await BleClient.instance.stopScan();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('停止扫描异常: $e')),
        );
      }
    }
  }

  @override
  void dispose() {
    _adapterStateSub?.cancel();
    _isScanningSub?.cancel();
    _scanResultsSub?.cancel();
    _filterController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final List<BleDeviceItem> filteredList = _scanResults.where((BleDeviceItem result) {
      if (_filterKeyword.isEmpty) return true;
      return result.name.toLowerCase().contains(_filterKeyword.toLowerCase()) ||
          result.id.toLowerCase().contains(_filterKeyword.toLowerCase());
    }).toList();

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            icon: Icon(_isScanning ? Icons.stop_rounded : Icons.search_rounded),
            tooltip: _isScanning ? '停止扫描' : '开始扫描',
            onPressed: _isScanning ? _stopScan : _startScan,
          ),
        ],
      ),
      body: Column(
        children: <Widget>[
          // 状态与过滤栏
          Container(
            padding: const EdgeInsets.all(12),
            color: Theme.of(context).colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
            child: Column(
              children: <Widget>[
                Row(
                  children: <Widget>[
                    Icon(
                      _adapterState == BleAdapterState.on
                          ? Icons.bluetooth_rounded
                          : Icons.bluetooth_disabled_rounded,
                      color: _adapterState == BleAdapterState.on
                          ? Colors.blue
                          : Colors.grey,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      '蓝牙状态: ${_adapterState.name.toUpperCase()}',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                    const Spacer(),
                    if (_isScanning) ...<Widget>[
                      const SizedBox(
                        width: 14,
                        height: 14,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      ),
                      const SizedBox(width: 8),
                      const Text('扫描中...', style: TextStyle(color: Colors.blue)),
                    ] else
                      const Text('已停止', style: TextStyle(color: Colors.grey)),
                  ],
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _filterController,
                  decoration: InputDecoration(
                    hintText: '按设备名称或 MAC 地址过滤',
                    prefixIcon: const Icon(Icons.filter_list_rounded, size: 20),
                    suffixIcon: _filterKeyword.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear_rounded, size: 18),
                            onPressed: () {
                              _filterController.clear();
                              setState(() => _filterKeyword = '');
                            },
                          )
                        : null,
                    isDense: true,
                    contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                  onChanged: (String val) => setState(() => _filterKeyword = val),
                ),
              ],
            ),
          ),
          // 列表
          Expanded(
            child: filteredList.isEmpty
                ? Center(
                    child: Text(
                      _isScanning ? '正在搜索周围 BLE 设备...' : '暂无设备，点击右上角开始扫描',
                      style: const TextStyle(color: Colors.grey),
                    ),
                  )
                : ListView.separated(
                    itemCount: filteredList.length,
                    separatorBuilder: (BuildContext _, int _) => const Divider(height: 1),
                    itemBuilder: (BuildContext context, int index) {
                      final BleDeviceItem item = filteredList[index];
                      final int rssi = item.rssi;
                      final List<String> serviceUuids = item.serviceUuids
                          .map((String g) => g.length > 8 ? g.substring(0, 8) : g)
                          .toList();

                      return ListTile(
                        leading: CircleAvatar(
                          backgroundColor: Colors.blue.withValues(alpha: 0.1),
                          child: const Icon(Icons.devices_other_rounded, color: Colors.blue),
                        ),
                        title: Text(
                          item.name,
                          style: const TextStyle(fontWeight: FontWeight.w600),
                        ),
                        subtitle: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            Text('ID: ${item.id}'),
                            if (serviceUuids.isNotEmpty)
                              Text('Service UUIDs: ${serviceUuids.join(", ")}',
                                  style: const TextStyle(fontSize: 12, color: Colors.blueGrey)),
                          ],
                        ),
                        trailing: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          crossAxisAlignment: CrossAxisAlignment.end,
                          children: <Widget>[
                            Text('$rssi dBm',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: rssi > -70
                                      ? Colors.green
                                      : (rssi > -85 ? Colors.orange : Colors.red),
                                )),
                            const SizedBox(height: 2),
                            Text(
                              item.connectable ? '可连接' : '不可连接',
                              style: TextStyle(
                                fontSize: 11,
                                color: item.connectable ? Colors.green : Colors.grey,
                              ),
                            ),
                          ],
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
