import 'dart:async';
import 'package:flutter/material.dart';
import 'package:lib_bluetooth/lib_bluetooth.dart';

/// BLE 设备连接与 GATT 交互示例（调用 lib_bluetooth 本地库封装）
///
/// 演示使用 [BleSession] 进行设备连接/断开、MTU 协商、GATT 树浏览（Services / Characteristics）、
/// 特征值读写（Read / Write）与 Notify 流式订阅。
class BleDeviceDemoPage extends StatelessWidget {
  const BleDeviceDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return BleDeviceDemoView(title: title);
  }
}

class BleDeviceDemoView extends StatefulWidget {
  const BleDeviceDemoView({super.key, required this.title});

  final String title;

  @override
  State<BleDeviceDemoView> createState() => _BleDeviceDemoViewState();
}

class _BleDeviceDemoViewState extends State<BleDeviceDemoView> {
  BleDeviceItem? _targetDevice;
  BleSession? _session;
  BleConnectionStatus _connectionState = BleConnectionStatus.disconnected;
  List<BleServiceInfo> _services = <BleServiceInfo>[];
  final List<String> _logs = <String>[];
  int _currentMtu = 23;

  StreamSubscription<BleConnectionStatus>? _connectionStateSub;
  StreamSubscription<int>? _mtuSub;

  @override
  void dispose() {
    _connectionStateSub?.cancel();
    _mtuSub?.cancel();
    _session?.disconnect();
    _session?.dispose();
    super.dispose();
  }

  void _addLog(String msg) {
    if (mounted) {
      setState(() {
        _logs.insert(0, '[${DateTime.now().toIso8601String().substring(11, 19)}] $msg');
        if (_logs.length > 50) _logs.removeLast();
      });
    }
  }

  Future<void> _scanAndSelectDevice() async {
    _addLog('正在扫描周围 BLE 设备寻找可用目标...');
    try {
      StreamSubscription<List<BleDeviceItem>>? sub;
      sub = BleClient.instance.scanResults.listen((List<BleDeviceItem> results) {
        if (results.isNotEmpty) {
          sub?.cancel();
          BleClient.instance.stopScan();
          final BleDeviceItem first = results.first;
          _addLog('发现目标设备: ${first.name} (${first.id})');
          _attachDevice(first);
        }
      });

      await BleClient.instance.startScan(timeout: const Duration(seconds: 4));
    } catch (e) {
      _addLog('扫描失败: $e');
    }
  }

  void _attachDevice(BleDeviceItem device) {
    _connectionStateSub?.cancel();
    _mtuSub?.cancel();
    _session?.dispose();

    _targetDevice = device;
    final BleSession session = BleClient.instance.createSession(device);
    _session = session;

    _connectionStateSub = session.connectionStatus.listen((BleConnectionStatus state) {
      if (mounted) {
        setState(() => _connectionState = state);
      }
      _addLog('连接状态变更: ${state.name.toUpperCase()}');
      if (state == BleConnectionStatus.connected) {
        _discoverServices();
      }
    });

    _mtuSub = session.mtuStream.listen((int mtu) {
      if (mounted) {
        setState(() => _currentMtu = mtu);
      }
      _addLog('当前 MTU 更新为: $mtu 字节');
    });

    _connectDevice();
  }

  Future<void> _connectDevice() async {
    final BleSession? session = _session;
    if (session == null) return;
    _addLog('正在连接设备: ${session.deviceId}...');
    try {
      await session.connect(timeout: const Duration(seconds: 10), autoConnect: false);
      _addLog('✓ 连接指令已发出');
    } catch (e) {
      _addLog('✗ 连接异常: $e');
    }
  }

  Future<void> _disconnectDevice() async {
    final BleSession? session = _session;
    if (session == null) return;
    _addLog('正在断开连接...');
    try {
      await session.disconnect();
      _addLog('✓ 设备已断开');
    } catch (e) {
      _addLog('✗ 断开异常: $e');
    }
  }

  Future<void> _discoverServices() async {
    final BleSession? session = _session;
    if (session == null) return;
    _addLog('正在发起服务发现 (discoverServices)...');
    try {
      final List<BleServiceInfo> s = await session.discoverServices();
      if (mounted) {
        setState(() => _services = s);
      }
      _addLog('✓ 发现 ${s.length} 个 GATT 服务');
    } catch (e) {
      _addLog('✗ 服务发现失败: $e');
    }
  }

  Future<void> _requestMtu() async {
    final BleSession? session = _session;
    if (session == null) return;
    _addLog('正在请求扩展 MTU 至 512 字节...');
    try {
      final int mtu = await session.requestMtu(512);
      _addLog('✓ MTU 协商成功: $mtu 字节');
    } catch (e) {
      _addLog('✗ 请求 MTU 失败: $e');
    }
  }

  Future<void> _readCharacteristic(BleCharInfo char) async {
    final BleSession? session = _session;
    if (session == null) return;
    _addLog('正在读取特征值: ${char.uuid.substring(0, 8)}...');
    try {
      final List<int> val = await session.read(characteristicUuid: char.uuid);
      final String hex = BleUtils.bytesToHex(val);
      _addLog('✓ 读取成功: Hex=[$hex] (长度: ${val.length} 字节)');
    } catch (e) {
      _addLog('✗ 读取失败: $e');
    }
  }

  Future<void> _writeCharacteristic(BleCharInfo char) async {
    final BleSession? session = _session;
    if (session == null) return;
    final List<int> data = 'Hello BLE from lib_bluetooth!'.codeUnits;
    _addLog('正在写入数据 (${data.length} 字节) 到特征值: ${char.uuid.substring(0, 8)}...');
    try {
      await session.write(
        characteristicUuid: char.uuid,
        data: data,
        withoutResponse: false,
      );
      _addLog('✓ 写入成功并收到响应');
    } catch (e) {
      _addLog('✗ 写入失败: $e');
    }
  }

  Future<void> _toggleNotification(BleCharInfo char) async {
    final BleSession? session = _session;
    if (session == null) return;
    _addLog('正在监听 Notify 通知: ${char.uuid.substring(0, 8)}...');
    try {
      final Stream<List<int>> stream = session.listenNotification(
        characteristicUuid: char.uuid,
        enable: true,
      );
      stream.listen((List<int> value) {
        final String hex = BleUtils.bytesToHex(value);
        _addLog('🔔 收到 Notify 推送 [${char.uuid.substring(0, 8)}]: Hex=[$hex]');
      });
      _addLog('✓ Notify 数据流已就绪');
    } catch (e) {
      _addLog('✗ Notify 设置失败: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            icon: const Icon(Icons.search_rounded),
            tooltip: '扫描并选择首个设备',
            onPressed: _scanAndSelectDevice,
          ),
        ],
      ),
      body: Column(
        children: <Widget>[
          // 设备与状态头部
          Container(
            padding: const EdgeInsets.all(12),
            color: Theme.of(context).colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    Icon(
                      _connectionState == BleConnectionStatus.connected
                          ? Icons.link_rounded
                          : Icons.link_off_rounded,
                      color: _connectionState == BleConnectionStatus.connected
                          ? Colors.green
                          : Colors.grey,
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _targetDevice != null
                            ? '${_targetDevice!.name} (${_targetDevice!.id})'
                            : '尚未选择设备（点击右上角扫描）',
                        style: const TextStyle(fontWeight: FontWeight.bold),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: _connectionState == BleConnectionStatus.connected
                            ? Colors.green.withValues(alpha: 0.1)
                            : Colors.grey.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: Text(
                        _connectionState.name.toUpperCase(),
                        style: TextStyle(
                          fontSize: 12,
                          color: _connectionState == BleConnectionStatus.connected
                              ? Colors.green
                              : Colors.grey,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Row(
                  children: <Widget>[
                    Text('当前 MTU: $_currentMtu 字节', style: const TextStyle(fontSize: 12)),
                    const Spacer(),
                    if (_connectionState == BleConnectionStatus.connected) ...<Widget>[
                      OutlinedButton(
                        onPressed: _requestMtu,
                        child: const Text('协商 MTU (512)', style: TextStyle(fontSize: 12)),
                      ),
                      const SizedBox(width: 8),
                      OutlinedButton(
                        onPressed: _disconnectDevice,
                        child: const Text('断开', style: TextStyle(fontSize: 12)),
                      ),
                    ],
                  ],
                ),
              ],
            ),
          ),
          // GATT 树展示区
          Expanded(
            flex: 6,
            child: _services.isEmpty
                ? const Center(
                    child: Text('暂无 GATT 服务数据，连接成功后自动展示',
                        style: TextStyle(color: Colors.grey)),
                  )
                : ListView.builder(
                    itemCount: _services.length,
                    itemBuilder: (BuildContext context, int sIndex) {
                      final BleServiceInfo service = _services[sIndex];
                      final String shortUuid = service.uuid.length > 8
                          ? service.uuid.substring(0, 8)
                          : service.uuid;
                      return ExpansionTile(
                        leading: const Icon(Icons.folder_open_rounded, color: Colors.blue),
                        title: Text('Service: $shortUuid...'),
                        subtitle: Text('Full UUID: ${service.uuid}', style: const TextStyle(fontSize: 11)),
                        children: service.characteristics.map((BleCharInfo char) {
                          final String cShortUuid = char.uuid.length > 8
                              ? char.uuid.substring(0, 8)
                              : char.uuid;
                          return ListTile(
                            contentPadding: const EdgeInsets.only(left: 32, right: 16),
                            title: Text('Char: $cShortUuid...'),
                            subtitle: Text(
                              '属性: [${<String>[
                                if (char.canRead) 'Read',
                                if (char.canWrite) 'Write',
                                if (char.canWriteWithoutResponse) 'WriteNoResp',
                                if (char.canNotify) 'Notify',
                                if (char.canIndicate) 'Indicate',
                              ].join('/')}]',
                              style: const TextStyle(fontSize: 11),
                            ),
                            trailing: Wrap(
                              spacing: 4,
                              children: <Widget>[
                                if (char.canRead)
                                  IconButton(
                                    icon: const Icon(Icons.download_rounded, size: 18),
                                    tooltip: '读取',
                                    onPressed: () => _readCharacteristic(char),
                                  ),
                                if (char.canWrite || char.canWriteWithoutResponse)
                                  IconButton(
                                    icon: const Icon(Icons.upload_rounded, size: 18),
                                    tooltip: '写入',
                                    onPressed: () => _writeCharacteristic(char),
                                  ),
                                if (char.canNotify || char.canIndicate)
                                  IconButton(
                                    icon: const Icon(
                                      Icons.notifications_none_rounded,
                                      size: 18,
                                    ),
                                    tooltip: '开启 Notify',
                                    onPressed: () => _toggleNotification(char),
                                  ),
                              ],
                            ),
                          );
                        }).toList(),
                      );
                    },
                  ),
          ),
          const Divider(height: 1),
          // 日志控制台
          Expanded(
            flex: 4,
            child: Container(
              color: const Color(0xFF1E293B),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: <Widget>[
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                    color: const Color(0xFF0F172A),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: <Widget>[
                        const Text('通信与交互日志', style: TextStyle(color: Colors.white70, fontSize: 12)),
                        TextButton(
                          onPressed: () => setState(() => _logs.clear()),
                          child: const Text('清空日志', style: TextStyle(fontSize: 11, color: Colors.blueAccent)),
                        ),
                      ],
                    ),
                  ),
                  Expanded(
                    child: ListView.builder(
                      reverse: true,
                      padding: const EdgeInsets.all(8),
                      itemCount: _logs.length,
                      itemBuilder: (BuildContext context, int index) {
                        return Text(
                          _logs[index],
                          style: const TextStyle(color: Color(0xFF38BDF8), fontSize: 12, fontFamily: 'monospace'),
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
