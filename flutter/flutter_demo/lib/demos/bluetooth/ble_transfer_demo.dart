import 'dart:math';
import 'dart:typed_data';

import 'package:flutter_demo/core/basic/basic.dart';
import 'package:lib_bluetooth/lib_bluetooth.dart';

/// BLE 传输 — 大包分包、流控与组包校验
///
/// 核心机制与避坑点：
/// 1. 单包限制：Payload = max(20, MTU - 3)，超长数据必须 `BleUtils.chunkBytes` 切片。
/// 2. 流控间隔：分包之间插入延迟，避免底层蓝牙栈拥塞丢包。
/// 3. 完整性：多包拼回后用 `BleUtils.calculateCrc16`（CRC16-CCITT）校验。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_blue_plus
class BleTransferDemoPage extends BasicResponsePage {
  const BleTransferDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<BleTransferDemoPage> createState() =>
      _BleTransferDemoPageState();
}

class _BleTransferDemoPageState
    extends BasicResponsePageState<BleTransferDemoPage> {
  static const String _progressKey = 'progress';
  static const int _mtuDefault = 23;
  static const int _mtuStandard = 247;
  static const int _mtuMax = 512;
  static const Duration _chunkInterval = Duration(milliseconds: 60);
  static const String _sampleText =
      'Flutter BLE high throughput transfer test message powered by lib_bluetooth chunking engine!';

  int _mtuSize = _mtuDefault;
  bool _isSending = false;

  int get _payloadLimit => max(20, _mtuSize - 3);

  @override
  void initState() {
    super.initState();
    showDescription(
      'BLE 传输示例：按 MTU 切片分包发送、流控间隔与 CRC16 组包校验',
    );
  }

  @override
  List<String> buildList() => const <String>[
        '1. 设置 MTU 23（默认）',
        '2. 设置 MTU 247（标准）',
        '3. 设置 MTU 512（最大）',
        '4. 分包流控发送',
        '5. 模拟拼包合并',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _setMtu(_mtuDefault);
      case 1:
        _setMtu(_mtuStandard);
      case 2:
        _setMtu(_mtuMax);
      case 3:
        _simulateChunkingSend();
      case 4:
        _simulatePacketMergerReceive();
    }
  }

  void _setMtu(int mtu) {
    _mtuSize = mtu;
    appendLog('→ [MTU] 设置 MTU=$_mtuSize，单包 Payload 限制=$_payloadLimit 字节');
  }

  Future<void> _simulateChunkingSend() async {
    if (_isSending) {
      appendLog('✗ [Send] 已有发送任务进行中');
      return;
    }
    final List<int> bytes = _sampleText.codeUnits;
    _isSending = true;

    appendLog('→ [Send] 准备发送完整数据包（${bytes.length} 字节）');
    appendLog(
      '→ [Send] 当前单包 Payload 限制: $_payloadLimit 字节/包（MTU: $_mtuSize）',
    );

    final List<Uint8List> chunks = BleUtils.chunkBytes(bytes, _payloadLimit);
    final int totalChunks = chunks.length;
    appendLog('→ [Send] 预计切片总包数: $totalChunks 包（BleUtils.chunkBytes）');

    final Stopwatch stopwatch = Stopwatch()..start();
    try {
      for (int i = 0; i < totalChunks; i++) {
        final Uint8List chunk = chunks[i];
        final String hex = BleUtils.bytesToHex(chunk);
        await Future<void>.delayed(_chunkInterval);
        updateLog(
          _progressKey,
          'Chunk ${i + 1}/$totalChunks（${chunk.length}B）Hex=[$hex] ACK',
        );
      }
      stopwatch.stop();
      final int elapsedMs = max(1, stopwatch.elapsedMilliseconds);
      final double kbps = (bytes.length * 8) / elapsedMs;
      final int crc16 = BleUtils.calculateCrc16(bytes);
      removeUpdatingLog(_progressKey);
      appendLog(
        '✓ [Send] 分包发送完毕，CRC16=0x${crc16.toRadixString(16).toUpperCase()}',
      );
      appendLog(
        '✓ [Send] 总耗时 ${elapsedMs}ms，吞吐率 ${kbps.toStringAsFixed(2)} kbps',
      );
    } finally {
      _isSending = false;
    }
  }

  void _simulatePacketMergerReceive() {
    const List<String> chunks = <String>[
      'Chunk-1:[Header:0xAA,Len:48] ',
      'Chunk-2:[SensorData:25.6C,Hum:60%] ',
      'Chunk-3:[Checksum:0x5F,Tail:0x55]',
    ];
    appendLog('→ [Merge] 接收端收到 ${chunks.length} 个切片包');
    for (int i = 0; i < chunks.length; i++) {
      appendLog(
        '→ [Merge] 切片 #${i + 1}（${chunks[i].length} 字节）: "${chunks[i]}"',
      );
    }
    final String merged = chunks.join();
    appendLog('✓ [Merge] 拼包完成，完整帧（${merged.length} 字节）: "$merged"');
  }
}
