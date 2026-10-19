import 'dart:io';
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/asr/asr_audio_source.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_merger.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_types.dart';
import 'package:test/test.dart';

/// ASR 引擎层里**不依赖 `tflite_flutter`** 的部分的对拍测试。
///
/// 为什么只测这一半：解码器 / 引擎 / worker 都要经 `ml_signature.dart` 落到原生绑定上，
/// 而 `tflite_flutter` 的绑定是**顶层 final 动态库句柄**，在 `flutter test`
/// （VM，无 native 库）里加载即失败。因此 `asr_types.dart` / `asr_tokenizer.dart` /
/// `asr_audio_source.dart` / `asr_merger.dart` 被刻意做成零原生依赖 ——
/// 本文件同时是这份约定的守门人：一旦有人往这几个文件里加了一条通向
/// `ml_signature.dart` 的 import，本文件会**直接编译不过**，而不是悄悄漏测。
///
/// 覆盖的三块逻辑都属于「错一位就静默跑偏」的类型：
/// 1. PCM 字节流 → 短整型（端序 / 对齐错了只会表现为音色怪异）；
/// 2. WAV 头解析与重叠窗口切分（缓冲左移错一位，转写结果整体错乱）；
/// 3. 编辑距离对齐与标点规范化（标点没去干净，重叠区永远对不上，界面会一直灰）。
void main() {
  group('samplesFor', () {
    test('与 Android getNumSamples 同口径（向下截断）', () {
      expect(samplesFor(16000, 5000), 80000);
      expect(samplesFor(16000, 2000), 32000);
      expect(samplesFor(16000, 1000), 16000);
      expect(samplesFor(16000, 1), 16);
      expect(samplesFor(16000, 0), 0);
      // 非整除时必须截断：22050 * 3 / 1000 = 66.15 → 66
      expect(samplesFor(22050, 3), 66);
    });
  });

  group('shortsFromPcm16Bytes', () {
    test('小端解码已知值', () {
      final Int16List samples = shortsFromPcm16Bytes(
        Uint8List.fromList(<int>[
          0x01, 0x00, // 1
          0x00, 0x80, // -32768（最小值）
          0xFF, 0x7F, // 32767（最大值）
          0x00, 0x00, // 0
          0xFF, 0xFF, // -1
        ]),
      );
      expect(samples, orderedEquals(<int>[1, -32768, 32767, 0, -1]));
    });

    test('奇数长度时丢掉最后一个残缺字节', () {
      final Int16List samples =
          shortsFromPcm16Bytes(Uint8List.fromList(<int>[0x02, 0x00, 0x07]));
      expect(samples, orderedEquals(<int>[2]));
    });

    test('空输入返回空数组', () {
      expect(shortsFromPcm16Bytes(Uint8List(0)), isEmpty);
    });

    test('缓冲区偏移非 2 字节对齐时不抛 RangeError（插件回调的真实形态）', () {
      // 真机上 `record` 给的 Uint8List 常是某个更大缓冲区的切片，
      // 直接 `bytes.buffer.asInt16List()` 会因偏移不是 2 的倍数而抛 RangeError。
      final Uint8List backing = Uint8List(12);
      backing[1] = 0x34;
      backing[2] = 0x12; // 落在「偏移 1 起」的切片里 → 小端 0x1234
      backing[3] = 0x00;
      backing[4] = 0x80; // → -32768
      final Uint8List slice = Uint8List.view(backing.buffer, 1, 4);

      expect(shortsFromPcm16Bytes(slice), orderedEquals(<int>[0x1234, -32768]));
    });

    test('与 Int16List → 字节 → Int16List 往返一致（含极值）', () {
      final Int16List source = Int16List.fromList(<int>[
        -32768,
        -1,
        0,
        1,
        12345,
        -12345,
        32767,
      ]);
      final Uint8List bytes = Uint8List(source.length * 2);
      final ByteData view = ByteData.view(bytes.buffer);
      for (int i = 0; i < source.length; i++) {
        view.setInt16(i * 2, source[i], Endian.little);
      }
      expect(shortsFromPcm16Bytes(bytes), orderedEquals(source));
    });
  });

  group('AsrMicrophoneWindowSource', () {
    // 100 ms 窗口 / 40 ms 重叠 → 1600 采样窗口、640 重叠、960 步进
    const int rate = 16000;
    const int interval = 1600;
    const int overlap = 640;
    const int step = 960;
    const int bufferLength = interval + step;

    AsrMicrophoneWindowSource build() => AsrMicrophoneWindowSource(
          samplingRate: rate,
          intervalMilliseconds: 100,
          overlapMilliseconds: 40,
        );

    Int16List constant(int value) =>
        Int16List.fromList(List<int>.filled(step, value));

    test('窗口切分参数', () {
      final AsrMicrophoneWindowSource source = build();
      expect(source.samplingRate, rate);
      expect(source.intervalSamples, interval);
      expect(source.overlapSamples, overlap);
      expect(source.stepSamples, step);
    });

    test('interval 不严格长于 overlap 时报错', () {
      expect(
        () => AsrMicrophoneWindowSource(
          samplingRate: rate,
          intervalMilliseconds: 40,
          overlapMilliseconds: 40,
        ),
        throwsA(isA<ArgumentError>()),
      );
    });

    test('攒不满一个 step 不产出窗口（推模式的核心约定）', () {
      // 单个不足一个 step 的分片
      expect(build().feed(Int16List(step - 1)), isEmpty);

      // 分片累计到刚好一个 step 才产出 —— 录音插件的回调分片通常远小于 step
      final AsrMicrophoneWindowSource source = build();
      final Int16List loud = constant(16384);
      expect(source.feed(loud.sublist(0, 500)), isEmpty);
      expect(source.feed(loud.sublist(500, step)), hasLength(1));
    });

    test('每次产出的是 interval + step 长的完整缓冲', () {
      final Float32List window = build().feed(constant(16384)).single;
      expect(window.length, bufferLength);
    });

    test('静音块整块写 0（官方底噪抑制）', () {
      final Float32List window = build().feed(Int16List(step)).single;
      _expectRange(window, 0, bufferLength, 0.0);
    });

    test('有声块按 32768 归一化并写入尾部', () {
      final Float32List window = build().feed(constant(16384)).single;
      // 新采样写在 buffer 尾部
      _expectRange(window, bufferLength - step, bufferLength, 0.5);
      // 尾部之前的区域尚未被填充
      _expectRange(window, 0, bufferLength - step, 0.0);
    });

    test('静音门限为严格小于 0.005', () {
      // 163 / 32768 = 0.004974… < 0.005 → 判为静音
      final Float32List below = build().feed(constant(163)).single;
      _expectRange(below, bufferLength - step, bufferLength, 0.0);

      // 164 / 32768 = 0.005004… > 0.005 → 保留原值
      final Float32List above = build().feed(constant(164)).single;
      _expectRange(above, bufferLength - step, bufferLength, 164 / 32768.0);
    });

    test('连续两块之间整体左移一个 step，上一块的重叠留在第二块里', () {
      final AsrMicrophoneWindowSource source = build();
      final List<Float32List> windows = source.feed(
        Int16List.fromList(<int>[
          ...List<int>.filled(step, 16384), // +0.5
          ...List<int>.filled(step, -16384), // -0.5
        ]),
      );
      expect(windows, hasLength(2));

      // 第一块：尾部为第一段，其余为 0
      _expectRange(windows[0], 0, interval, 0.0);
      _expectRange(windows[0], interval, bufferLength, 0.5);

      // 第二块：整体左移一个 step 后，第一段落到 [overlap, interval)
      _expectRange(windows[1], 0, overlap, 0.0);
      _expectRange(windows[1], overlap, interval, 0.5);
      _expectRange(windows[1], interval, bufferLength, -0.5);
    });

    test('产出的窗口是副本：后续 feed 不会改写已产出的窗口', () {
      final AsrMicrophoneWindowSource source = build();
      final Float32List first = source.feed(constant(16384)).single;
      final Float32List snapshot = Float32List.fromList(first);

      // 官方在 `yield(bufferForSamples)` 里把内部缓冲**直接**交出去（同一数组引用），
      // 于是调用方稍后再读会拿到被 shift 改写过的内容；本实现改为 fromList 拷贝。
      source.feed(constant(-16384));

      for (int i = 0; i < first.length; i++) {
        if (first[i] != snapshot[i]) {
          fail('窗口在下一次 feed 之后被改写：下标 $i，'
              '原 ${snapshot[i]}，现 ${first[i]}');
        }
      }
    });

    test('空输入 / 关闭后不再产出', () {
      final AsrMicrophoneWindowSource source = build();
      expect(source.feed(Int16List(0)), isEmpty);
      source.close();
      expect(source.feed(constant(16384)), isEmpty);
    });
  });

  group('AsrWavFileSource', () {
    const int rate = 16000;
    const int interval = 1600; // 100 ms
    const int overlap = 640; // 40 ms
    const int step = 960;

    late Directory tempDir;
    int fileSeq = 0;

    setUpAll(() {
      tempDir = Directory.systemTemp.createTempSync('asr_wav_test_');
    });

    tearDownAll(() {
      if (tempDir.existsSync()) {
        tempDir.deleteSync(recursive: true);
      }
    });

    /// 合成一个 16-bit 单声道 PCM WAV，可选在 `fmt ` 之前插入一个未知 chunk
    File writeWav(
      List<int> samples, {
      int sampleRate = rate,
      int audioFormat = 1,
      String magic = 'RIFF',
      String form = 'WAVE',
      bool withExtraChunk = false,
    }) {
      final BytesBuilder builder = BytesBuilder();
      _ascii(builder, magic);
      _u32(builder, 36 + samples.length * 2 + (withExtraChunk ? 13 : 0));
      _ascii(builder, form);

      if (withExtraChunk) {
        // 未知 chunk：解析器应整块跳过（长度故意取奇数，验证不会读歪）
        _ascii(builder, 'LIST');
        _u32(builder, 5);
        _ascii(builder, 'INFO!');
      }

      _ascii(builder, 'fmt ');
      _u32(builder, 16);
      _u16(builder, audioFormat);
      _u16(builder, 1); // 单声道
      _u32(builder, sampleRate);
      _u32(builder, sampleRate * 2); // byte rate
      _u16(builder, 2); // block align
      _u16(builder, 16); // bits per sample

      _ascii(builder, 'data');
      _u32(builder, samples.length * 2);
      for (final int sample in samples) {
        _i16(builder, sample);
      }

      final File file = File('${tempDir.path}/wav_${fileSeq++}.wav');
      file.writeAsBytesSync(builder.takeBytes());
      return file;
    }

    // 文件名逐个递增，避免 Windows 上「同名文件被前一个测试的句柄占着」
    AsrWavFileSource open(File file) {
      final AsrWavFileSource source = AsrWavFileSource.open(
        file,
        samplingRate: rate,
        intervalMilliseconds: 100,
        overlapMilliseconds: 40,
      );
      addTearDown(source.close);
      return source;
    }

    test('窗口切分参数', () {
      final AsrWavFileSource source = open(writeWav(List<int>.filled(1000, 0)));
      expect(source.samplingRate, rate);
      expect(source.intervalSamples, interval);
      expect(source.overlapSamples, overlap);
    });

    test('interval 不严格长于 overlap 时报错', () {
      final File file = writeWav(List<int>.filled(1000, 0));
      expect(
        () => AsrWavFileSource.open(
          file,
          samplingRate: rate,
          intervalMilliseconds: 40,
          overlapMilliseconds: 40,
        ),
        throwsA(isA<ArgumentError>()),
      );
    });

    test('非 RIFF/WAVE 报错', () {
      expect(
        () => open(writeWav(<int>[], magic: 'XXXX')),
        throwsA(isA<FormatException>()),
      );
      expect(
        () => open(writeWav(<int>[], form: 'AVI ')),
        throwsA(isA<FormatException>()),
      );
    });

    test('非 PCM 编码报错', () {
      // 3 = IEEE float
      expect(
        () => open(writeWav(<int>[], audioFormat: 3)),
        throwsA(isA<FormatException>()),
      );
    });

    test('采样率不符报错（而不是喂进去产出乱码）', () {
      expect(
        () => open(writeWav(<int>[], sampleRate: 44100)),
        throwsA(isA<FormatException>()),
      );
    });

    test('文件过短 / chunk 头被截断报错', () {
      final File tiny = File('${tempDir.path}/tiny.wav')
        ..writeAsBytesSync(<int>[0x52, 0x49, 0x46, 0x46]);
      expect(() => open(tiny), throwsA(isA<FormatException>()));

      // RIFF/WAVE 头齐全，但 `fmt ` chunk 头被截断
      final File truncated = File('${tempDir.path}/truncated.wav')
        ..writeAsBytesSync(<int>[
          ...'RIFF'.codeUnits,
          0x00, 0x00, 0x00, 0x00,
          ...'WAVE'.codeUnits,
          ...'fmt '.codeUnits,
        ]);
      expect(() => open(truncated), throwsA(isA<FormatException>()));
    });

    test('跳过未知 chunk 后仍能定位 data', () {
      final Int16List samples = _ramp(interval);
      final Float32List chunk = _nextChunk(
        open(writeWav(samples, withExtraChunk: true)),
      );
      _expectSamples(chunk, samples, 0, 0, interval);
    });

    test('首块读满 interval，其后按 step 步进并保留重叠', () {
      final Int16List samples = _ramp(interval + step);
      final AsrWavFileSource source = open(writeWav(samples));

      final Float32List first = _nextChunk(source);
      expect(first.length, interval);
      _expectSamples(first, samples, 0, 0, interval);

      // 第二块：前 overlap 个采样是上一块的尾部，其余是 step 个新采样
      final Float32List second = _nextChunk(source);
      expect(second.length, interval);
      _expectSamples(second, samples, 0, interval - overlap, overlap);
      _expectSamples(second, samples, overlap, interval, step);

      // 数据已耗尽
      expect(source.next(), isNull);
      expect(source.next(), isNull);
    });

    test('最后一块读不满时补零，重叠区正确保留', () {
      const int tail = 400; // < step
      final Int16List samples = _ramp(interval + tail);
      final AsrWavFileSource source = open(writeWav(samples));

      final Float32List first = _nextChunk(source);
      _expectSamples(first, samples, 0, 0, interval);

      final Float32List second = _nextChunk(source);
      // 重叠区来自上一块尾部
      _expectSamples(second, samples, 0, interval - overlap, overlap);
      // 新采样不足一个 step，紧接在重叠区之后
      _expectSamples(second, samples, overlap, interval, tail);
      // 剩余部分补零（当作静音）
      _expectRange(second, overlap + tail, interval, 0.0);

      expect(source.next(), isNull);
    });

    test('数据短于一个 interval 时首块补零', () {
      const int short = 300;
      final Int16List samples = _ramp(short);
      final AsrWavFileSource source = open(writeWav(samples));

      final Float32List chunk = _nextChunk(source);
      expect(chunk.length, interval);
      _expectSamples(chunk, samples, 0, 0, short);
      _expectRange(chunk, short, interval, 0.0);
      expect(source.next(), isNull);
    });

    test('close 之后不再产出，且可重复关闭', () {
      final AsrWavFileSource source = open(writeWav(_ramp(interval + step)));
      source.close();
      expect(source.next(), isNull);
      expect(source.close, returnsNormally);
    });
  });

  group('AsrLevenshteinMerger 纯函数', () {
    group('levenshtein', () {
      test('已知距离', () {
        expect(AsrLevenshteinMerger.levenshtein('', ''), 0);
        expect(AsrLevenshteinMerger.levenshtein('', 'abc'), 3);
        expect(AsrLevenshteinMerger.levenshtein('abc', ''), 3);
        expect(AsrLevenshteinMerger.levenshtein('kitten', 'sitting'), 3);
        expect(AsrLevenshteinMerger.levenshtein('flaw', 'lawn'), 2);
        expect(AsrLevenshteinMerger.levenshtein('abc', 'abc'), 0);
        expect(AsrLevenshteinMerger.levenshtein('ab', 'ba'), 2);
        expect(AsrLevenshteinMerger.levenshtein('日本語', '日本'), 1);
      });

      test('对称', () {
        const List<List<String>> pairs = <List<String>>[
          <String>['kitten', 'sitting'],
          <String>['', 'xyz'],
          <String>['hello world', 'hello  world'],
          <String>['ab', 'ba'],
        ];
        for (final List<String> pair in pairs) {
          expect(
            AsrLevenshteinMerger.levenshtein(pair[0], pair[1]),
            AsrLevenshteinMerger.levenshtein(pair[1], pair[0]),
            reason: '${pair[0]} / ${pair[1]}',
          );
        }
      });
    });

    group('canonicalize', () {
      test('转小写并去 ASCII 标点', () {
        expect(AsrLevenshteinMerger.canonicalize(<String>['Hello,']), 'hello');
        expect(AsrLevenshteinMerger.canonicalize(<String>['World!']), 'world');
        // `_`(0x5F) 与 `` ` ``(0x60) 也落在 Java \p{Punct} 的区间里
        expect(AsrLevenshteinMerger.canonicalize(<String>['a_b']), 'ab');
        expect(AsrLevenshteinMerger.canonicalize(<String>['a`b']), 'ab');
        // 数字与中日韩文字保留
        expect(AsrLevenshteinMerger.canonicalize(<String>['i18n']), 'i18n');
        expect(AsrLevenshteinMerger.canonicalize(<String>['日本語']), '日本語');
      });

      test('词间以空格连接，词内标点直接删除', () {
        expect(AsrLevenshteinMerger.canonicalize(<String>['a', 'b']), 'a b');
        // 词尾标点被删掉后，与「词本身」的规范化结果一致
        expect(
          AsrLevenshteinMerger.canonicalize(<String>['a-', 'b']),
          AsrLevenshteinMerger.canonicalize(<String>['a', 'b']),
        );
      });

      test('空输入', () {
        expect(AsrLevenshteinMerger.canonicalize(const <String>[]), '');
        expect(AsrLevenshteinMerger.canonicalize(<String>['']), '');
        expect(AsrLevenshteinMerger.canonicalize(<String>['...']), '');
      });
    });

    group('closeEnough', () {
      test('相同即通过', () {
        expect(AsrLevenshteinMerger.closeEnough('hello', 'hello'), isTrue);
      });

      test('较短长度 ≤3 时要求前缀完全相同（避免 a / i 互相误判）', () {
        expect(AsrLevenshteinMerger.closeEnough('abc', 'abd'), isFalse);
        expect(AsrLevenshteinMerger.closeEnough('ab', 'ab'), isTrue);
      });

      test('较短长度 >3 时允许 1 个编辑距离', () {
        expect(AsrLevenshteinMerger.closeEnough('abcd', 'abce'), isTrue);
        expect(AsrLevenshteinMerger.closeEnough('abcd', 'abxy'), isFalse);
      });

      test('长度不等时只比较「较短长度」的前缀', () {
        expect(AsrLevenshteinMerger.closeEnough('abc', 'abcde'), isTrue);
        expect(AsrLevenshteinMerger.closeEnough('abc', 'abxde'), isFalse);
        // 空串与任何输入都只比长度为 0 的前缀 → 恒真
        expect(AsrLevenshteinMerger.closeEnough('', 'anything'), isTrue);
      });
    });

    group('dedupWords', () {
      test('去掉 current 开头与 prev 尾部重复的词', () {
        expect(
          AsrLevenshteinMerger.dedupWords(
            <String>['hello', 'world'],
            <String>['world', 'again'],
          ),
          <String>['again'],
        );
      });

      test('大小写与标点不影响判重', () {
        expect(
          AsrLevenshteinMerger.dedupWords(
            <String>['World,'],
            <String>['world', 'next'],
          ),
          <String>['next'],
        );
      });

      test('无重叠时原样返回', () {
        expect(
          AsrLevenshteinMerger.dedupWords(
            <String>['aaa'],
            <String>['bbb', 'ccc'],
          ),
          <String>['bbb', 'ccc'],
        );
      });

      test('current 为空时返回空', () {
        expect(
          AsrLevenshteinMerger.dedupWords(<String>['a'], const <String>[]),
          isEmpty,
        );
      });

      test('只看最近的 searchWindow 个词', () {
        // 重复的是 prev 的**前三个**词整体，窗口放宽到 3 才够
        const List<String> prev = <String>['p', 'q', 'r'];
        const List<String> current = <String>['p', 'q', 'r', 's'];
        expect(
          AsrLevenshteinMerger.dedupWords(prev, current),
          <String>['p', 'q', 'r', 's'],
        );
        expect(
          AsrLevenshteinMerger.dedupWords(prev, current, 3),
          <String>['s'],
        );
      });
    });

    group('mergeIntoUnconfirmedText', () {
      test('无时间戳：按比例取中点，保留上一次的未确认段', () {
        // prev = p1..p10；未确认区 [4, 8) = p5..p8，pivot = 8
        final List<String> prev = _words('p', 10);
        final List<String> current = <String>[
          'c0', 'c1', 'c2', 'p7', 'p8', 'c5', 'c6', 'c7', 'c8', 'c9',
        ];

        final List<String> merged = AsrLevenshteinMerger.mergeIntoUnconfirmedText(
          prevWords: prev,
          prevWordTimestamps: const <int>[],
          currentWords: current,
          currentWordTimestamps: const <int>[],
          overlapRatio: 0.4,
          pivotFactor: 0.6,
          prevWordIndexOfUnconfirmed: 4,
          prevWordIndexOfPivot: 8,
        );

        // 当前窗口从第 3 个词（下标 3）起才算「pivot 之后」：
        // 未确认段 = 上一次的 p5..p8 + 去重后的当前窗口尾部 c5..c9
        expect(
          merged,
          <String>['p5', 'p6', 'p7', 'p8', 'c5', 'c6', 'c7', 'c8', 'c9'],
        );
      });

      test('有时间戳：按时间戳切当前窗口', () {
        final List<String> prev = _words('p', 10);
        final List<String> current = _words('c', 10);

        final List<String> merged = AsrLevenshteinMerger.mergeIntoUnconfirmedText(
          prevWords: prev,
          prevWordTimestamps: List<int>.generate(10, (int i) => i),
          currentWords: current,
          currentWordTimestamps: List<int>.generate(10, (int i) => i),
          overlapRatio: 0.4,
          pivotFactor: 0.6,
          prevWordIndexOfUnconfirmed: 4,
          prevWordIndexOfPivot: 8,
        );

        // timestampAtPivot = 8；当前窗口里时间戳 < 8 的有 8 个 → 从下标 8 起算
        expect(merged, <String>['p5', 'p6', 'p7', 'p8', 'c9', 'c10']);
      });

      test('当前窗口太短时整段沿用上一次（避免未确认区忽长忽短地跳）', () {
        final List<String> merged = AsrLevenshteinMerger.mergeIntoUnconfirmedText(
          prevWords: _words('p', 10),
          prevWordTimestamps: List<int>.filled(10, 0),
          currentWords: <String>['c0'],
          // 唯一一个词的时间戳已到 pivot → pivot 之后没有词
          currentWordTimestamps: <int>[8],
          overlapRatio: 0.4,
          pivotFactor: 0.6,
          prevWordIndexOfUnconfirmed: 4,
          prevWordIndexOfPivot: 8,
        );

        expect(merged, <String>['p5', 'p6', 'p7', 'p8', 'p9', 'p10']);
      });

      test('pivot 落在时间戳表之外时退化为「最后时间戳 + 1」', () {
        final List<String> merged = AsrLevenshteinMerger.mergeIntoUnconfirmedText(
          prevWords: _words('p', 5),
          prevWordTimestamps: <int>[0, 1, 2, 3, 4],
          currentWords: <String>['c0', 'c1', 'c2', 'c3'],
          currentWordTimestamps: <int>[0, 1, 2, 3],
          overlapRatio: 0.4,
          pivotFactor: 0.6,
          prevWordIndexOfUnconfirmed: 2,
          // 5 >= timestamps.length(5) → 取 last + 1 = 5
          prevWordIndexOfPivot: 5,
        );

        // 当前窗口时间戳全都 < 5 → 全部落在 pivot 之前，未确认段完全来自上一次
        expect(merged, <String>['p3', 'p4', 'p5']);
      });
    });
  });

  group('AsrSpeech 哨兵常量', () {
    test('与 Android SpeechRecognizer 同值', () {
      expect(AsrSpeech.endOfSequence, -1);
      expect(AsrSpeech.noTimestamp, -1);
    });
  });
}

/// 取下一下块；已结束则直接让用例失败
Float32List _nextChunk(AsrAudioSource source) {
  final Float32List? chunk = source.next();
  if (chunk == null) {
    fail('期望还有下一个音频块，实际已读到结尾');
  }
  return chunk;
}

/// 断言 [values] 的 `[start, end)` 区间内每个元素都约等于 [expected]
void _expectRange(
  Float32List values,
  int start,
  int end,
  double expected, {
  double tolerance = 1e-9,
}) {
  for (int i = start; i < end; i++) {
    if ((values[i] - expected).abs() > tolerance) {
      fail('下标 $i 期望 $expected，实际 ${values[i]}（区间 [$start, $end)）');
    }
  }
}

/// 断言 [actual] 的 `[offset, offset + count)` 等于 [source] 从 [sourceOffset] 起的
/// `count` 个采样按 32768 归一化后的值
void _expectSamples(
  Float32List actual,
  Int16List source,
  int offset,
  int sourceOffset,
  int count,
) {
  for (int i = 0; i < count; i++) {
    final double expected = source[sourceOffset + i] / 32768.0;
    if ((actual[offset + i] - expected).abs() > 1e-9) {
      fail(
        '下标 ${offset + i} 期望 $expected（采样 ${source[sourceOffset + i]}），'
        '实际 ${actual[offset + i]}',
      );
    }
  }
}

/// `count` 个递增采样（0, 1, 2, …），让错位一眼可见
Int16List _ramp(int count) =>
    Int16List.fromList(List<int>.generate(count, (int i) => i));

/// `prefix` + 1..count 的词表（如 `p1`、`p2`…），便于把期望直接列出来
List<String> _words(String prefix, int count) =>
    List<String>.generate(count, (int i) => '$prefix${i + 1}');

void _ascii(BytesBuilder builder, String value) => builder.add(value.codeUnits);

void _u16(BytesBuilder builder, int value) {
  final ByteData data = ByteData(2)..setUint16(0, value, Endian.little);
  builder.add(data.buffer.asUint8List());
}

void _u32(BytesBuilder builder, int value) {
  final ByteData data = ByteData(4)..setUint32(0, value, Endian.little);
  builder.add(data.buffer.asUint8List());
}

void _i16(BytesBuilder builder, int value) {
  final ByteData data = ByteData(2)..setInt16(0, value, Endian.little);
  builder.add(data.buffer.asUint8List());
}
