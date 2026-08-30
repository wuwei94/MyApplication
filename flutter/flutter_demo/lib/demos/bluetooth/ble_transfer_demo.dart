import 'dart:async';
import 'dart:math';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:lib_bluetooth/lib_bluetooth.dart';

/// BLE 高级传输与分包流控示例（调用 lib_bluetooth 本地库封装）
///
/// 演示在 BLE 通信中应对单包限制的核心机制：
/// 1. MTU 协商与 Payload 动态适配 (默认 20 字节 vs 扩容后 244 字节)
/// 2. 大包切片分包发送 ([BleUtils.chunkBytes]) 与流控间隔控制
/// 3. 多分包流式合并组包校验 ([BleUtils.calculateCrc16])
/// 4. Hex / UTF-8 双模交互控制台与吞吐率统计
class BleTransferDemoPage extends StatelessWidget {
  const BleTransferDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return BleTransferDemoView(title: title);
  }
}

class BleTransferDemoView extends StatefulWidget {
  const BleTransferDemoView({super.key, required this.title});

  final String title;

  @override
  State<BleTransferDemoView> createState() => _BleTransferDemoViewState();
}

class _BleTransferDemoViewState extends State<BleTransferDemoView> {
  int _mtuSize = 23; // 默认 MTU
  int get _payloadLimit => max(20, _mtuSize - 3);

  final List<String> _consoleLogs = <String>[];
  bool _isSending = false;
  double _sendProgress = 0.0;

  final TextEditingController _inputController = TextEditingController(
    text: 'Flutter BLE high throughput transfer test message powered by lib_bluetooth chunking engine!',
  );

  @override
  void dispose() {
    _inputController.dispose();
    super.dispose();
  }

  void _log(String msg) {
    if (mounted) {
      setState(() {
        _consoleLogs.add('[${DateTime.now().toIso8601String().substring(11, 19)}] $msg');
      });
    }
  }

  Future<void> _simulateChunkingSend() async {
    if (_isSending) return;
    final String content = _inputController.text;
    final List<int> bytes = content.codeUnits;

    setState(() {
      _isSending = true;
      _sendProgress = 0.0;
    });

    _log('🚀 准备发送完整数据包 (${bytes.length} 字节)...');
    _log('当前单包 Payload 限制: $_payloadLimit 字节/包 (MTU: $_mtuSize)');

    // 使用 lib_bluetooth 提供的标准分包切片工具
    final List<Uint8List> chunks = BleUtils.chunkBytes(bytes, _payloadLimit);
    final int totalChunks = chunks.length;
    _log('预计切片总包数: $totalChunks 包 (基于 BleUtils.chunkBytes)');

    final Stopwatch stopwatch = Stopwatch()..start();

    for (int i = 0; i < totalChunks; i++) {
      final Uint8List chunk = chunks[i];
      final String hex = BleUtils.bytesToHex(chunk);

      // 模拟流控间隔与底层 ACK 回调延迟
      await Future<void>.delayed(const Duration(milliseconds: 60));

      _log('  ├─ [Chunk ${i + 1}/$totalChunks] (${chunk.length}B) Hex=[$hex] -> 已确认 (ACK)');

      if (mounted) {
        setState(() {
          _sendProgress = (i + 1) / totalChunks;
        });
      }
    }

    stopwatch.stop();
    final int elapsedMs = max(1, stopwatch.elapsedMilliseconds);
    final double kbps = (bytes.length * 8) / elapsedMs;
    final int crc16 = BleUtils.calculateCrc16(bytes);

    _log('✓ 所有分包切片发送完毕！CRC16=0x${crc16.toRadixString(16).toUpperCase()}');
    _log('✓ 总耗时: ${elapsedMs}ms, 传输吞吐率: ${kbps.toStringAsFixed(2)} kbps');

    if (mounted) {
      setState(() {
        _isSending = false;
      });
    }
  }

  void _simulatePacketMergerReceive() {
    _log('📥 模拟接收端收到 3 个切片包并触发拼包流:');
    const List<String> chunks = <String>[
      'Chunk-1:[Header:0xAA,Len:48] ',
      'Chunk-2:[SensorData:25.6C,Hum:60%] ',
      'Chunk-3:[Checksum:0x5F,Tail:0x55]',
    ];

    for (int i = 0; i < chunks.length; i++) {
      _log('  ├─ 收到切片 #${i + 1} (${chunks[i].length} 字节): "${chunks[i]}"');
    }

    final String merged = chunks.join();
    _log('✓ 拼包引擎组装完成！完整帧 (${merged.length} 字节): "$merged"');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Column(
        children: <Widget>[
          // MTU 与参数配置卡片
          Padding(
            padding: const EdgeInsets.all(12),
            child: Card(
              elevation: 0,
              color: Theme.of(context).colorScheme.surfaceContainerHighest.withValues(alpha: 0.4),
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: <Widget>[
                        Text('MTU 配置: $_mtuSize 字节 (Payload: $_payloadLimit 字节)',
                            style: const TextStyle(fontWeight: FontWeight.bold)),
                        Text('切片上限: $_payloadLimit B',
                            style: const TextStyle(color: Colors.blue, fontWeight: FontWeight.w600)),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 8,
                      children: <Widget>[
                        ChoiceChip(
                          label: const Text('默认 (23 字节)'),
                          selected: _mtuSize == 23,
                          onSelected: (bool selected) {
                            if (selected) setState(() => _mtuSize = 23);
                          },
                        ),
                        ChoiceChip(
                          label: const Text('标准 (247 字节)'),
                          selected: _mtuSize == 247,
                          onSelected: (bool selected) {
                            if (selected) setState(() => _mtuSize = 247);
                          },
                        ),
                        ChoiceChip(
                          label: const Text('最大 (512 字节)'),
                          selected: _mtuSize == 512,
                          onSelected: (bool selected) {
                            if (selected) setState(() => _mtuSize = 512);
                          },
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    TextField(
                      controller: _inputController,
                      maxLines: 2,
                      decoration: const InputDecoration(
                        labelText: '待发送测试文本 (支持长文本自动切片)',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                    ),
                    const SizedBox(height: 8),
                    if (_isSending)
                      LinearProgressIndicator(value: _sendProgress)
                    else
                      Row(
                        children: <Widget>[
                          Expanded(
                            child: FilledButton.icon(
                              icon: const Icon(Icons.send_rounded, size: 18),
                              label: const Text('模拟分包流控发送'),
                              onPressed: _simulateChunkingSend,
                            ),
                          ),
                          const SizedBox(width: 8),
                          OutlinedButton.icon(
                            icon: const Icon(Icons.merge_type_rounded, size: 18),
                            label: const Text('模拟拼包合并'),
                            onPressed: _simulatePacketMergerReceive,
                          ),
                        ],
                      ),
                  ],
                ),
              ),
            ),
          ),
          // 控制台展示
          Expanded(
            child: Container(
              margin: const EdgeInsets.fromLTRB(12, 0, 12, 12),
              decoration: BoxDecoration(
                color: const Color(0xFF0F172A),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Column(
                children: <Widget>[
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    decoration: const BoxDecoration(
                      color: Color(0xFF1E293B),
                      borderRadius: BorderRadius.vertical(top: Radius.circular(8)),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: <Widget>[
                        const Text('传输与分包控制台 (lib_bluetooth)', style: TextStyle(color: Colors.white, fontSize: 12)),
                        TextButton(
                          onPressed: () => setState(() => _consoleLogs.clear()),
                          child: const Text('清空', style: TextStyle(color: Colors.blueAccent, fontSize: 11)),
                        ),
                      ],
                    ),
                  ),
                  Expanded(
                    child: ListView.builder(
                      padding: const EdgeInsets.all(8),
                      itemCount: _consoleLogs.length,
                      itemBuilder: (BuildContext context, int index) {
                        return Padding(
                          padding: const EdgeInsets.symmetric(vertical: 2),
                          child: Text(
                            _consoleLogs[index],
                            style: const TextStyle(
                              color: Color(0xFF38BDF8),
                              fontSize: 12,
                              fontFamily: 'monospace',
                            ),
                          ),
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
