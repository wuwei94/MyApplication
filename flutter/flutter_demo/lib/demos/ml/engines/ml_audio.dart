/// 端侧合成音频的共享播放工具 —— 三个 TTS 页（Matcha / Kitten / Qwen3-LM）共用。
library;

import 'dart:async';
import 'dart:typed_data';

import 'package:audioplayers/audioplayers.dart';

/// 把 float PCM 波形包成 16-bit PCM 单声道 WAV 容器。
///
/// 合成链路的输出是 [-1, 1] 的 float 波形； audioplayers 的字节源需要完整容器格式，
/// 这里选最通用的 16-bit PCM（format code 1），任何播放器都能解。
/// 采样 16-bit 而非 24/32 是刻意取舍：端侧 TTS 的听感瓶颈在声码器而非量化位深。
Uint8List wavFromFloats(Float32List samples, int sampleRate) {
  final int dataBytes = samples.length * 2;
  final ByteData header = ByteData(44);
  // RIFF 头
  for (int i = 0; i < 4; i++) {
    header.setUint8(i, 'RIFF'.codeUnitAt(i));
  }
  header.setUint32(4, 36 + dataBytes, Endian.little);
  for (int i = 0; i < 4; i++) {
    header.setUint8(8 + i, 'WAVE'.codeUnitAt(i));
  }
  // fmt 段（PCM = 1）
  for (int i = 0; i < 4; i++) {
    header.setUint8(12 + i, 'fmt '.codeUnitAt(i));
  }
  header.setUint32(16, 16, Endian.little); // fmt 段长度
  header.setUint16(20, 1, Endian.little); // PCM
  header.setUint16(22, 1, Endian.little); // 单声道
  header.setUint32(24, sampleRate, Endian.little);
  header.setUint32(28, sampleRate * 2, Endian.little); // 字节率
  header.setUint16(32, 2, Endian.little); // 块对齐
  header.setUint16(34, 16, Endian.little); // 位深
  // data 段
  for (int i = 0; i < 4; i++) {
    header.setUint8(36 + i, 'data'.codeUnitAt(i));
  }
  header.setUint32(40, dataBytes, Endian.little);

  final ByteData pcm = ByteData(dataBytes);
  for (int i = 0; i < samples.length; i++) {
    final double clamped = samples[i].clamp(-1.0, 1.0);
    pcm.setInt16(i * 2, (clamped * 32767.0).round(), Endian.little);
  }

  final Uint8List out = Uint8List(44 + dataBytes);
  out.setRange(0, 44, header.buffer.asUint8List());
  out.setRange(44, 44 + dataBytes, pcm.buffer.asUint8List());
  return out;
}

/// float 波形时长（秒）
double durationOf(Float32List samples, int sampleRate) =>
    sampleRate > 0 ? samples.length / sampleRate : 0.0;

/// 合成结果的播放器封装。
///
/// 与 Android 页 `play()` / `stopPlayback()` 对应：再次合成前先停掉上一次播放，
/// 页面销毁时释放。audioplayers 的 `AudioPlayer` 一次只能有一个 source，
/// 因此每次播放都 `setSourceBytes` 后 `resume`。
///
/// ### 完成语义：等到真正播完，而不是「已恢复播放」
///
/// audioplayers 的 `resume()` 在**音频已恢复播放**时就完成，不等音频结束；
/// 而调用方（尤其是流式页的播放泵）需要知道「这一句什么时候放完」。
/// 因此 [playWav] 显式订阅 `onPlayerComplete`（广播流，可随取随弃），
/// 把完成时机对齐到音频自然结束。**`stop()` 不会触发 `onPlayerComplete`**
/// （audioplayers 文档明确说明停止/暂停不发完成事件），所以 [stop] 与 [dispose]
/// 里手动补全完成信号，避免等待方永远挂起。
class MlAudioPlayer {
  MlAudioPlayer();

  final AudioPlayer _player = AudioPlayer();

  bool _playing = false;

  /// 当前播放的完成信号；音频播完、被停止、或播放出错时补全
  Completer<void>? _playbackDone;

  /// 是否正在播放
  bool get isPlaying => _playing;

  /// 播放一段 WAV 容器字节。
  ///
  /// 返回的 Future 在**音频真正播完**（或被 [stop] / [dispose] 打断）时完成；
  /// 若上一段还在播，先停掉再播（与 Android 页「再次朗读前先停」一致）。
  Future<void> playWav(Uint8List wavBytes) async {
    await _settleAndStop();
    final Completer<void> done = Completer<void>();
    _playbackDone = done;
    final StreamSubscription<void> subscription =
        _player.onPlayerComplete.listen((_) => _completePlayback());
    try {
      await _player.setSourceBytes(wavBytes);
      _playing = true;
      await _player.resume();
      await done.future;
    } catch (error) {
      _completePlayback();
      rethrow;
    } finally {
      await subscription.cancel();
      // 只清理「自己这一轮」的信号；期间若又开始了新一轮播放，不动它的
      if (_playbackDone == done) {
        _playbackDone = null;
      }
    }
  }

  /// 停止当前播放（没有在播时是空操作）
  Future<void> stop() async {
    if (!_playing && _playbackDone == null) return;
    _playing = false;
    _completePlayback();
    await _player.stop();
  }

  Future<void> dispose() async {
    _playing = false;
    _completePlayback();
    await _player.stop();
    await _player.dispose();
  }

  /// 补全当前播放的完成信号（自然结束 / 停止 / 出错三条路径共用）
  void _completePlayback() {
    _playing = false;
    final Completer<void>? done = _playbackDone;
    _playbackDone = null;
    if (done != null && !done.isCompleted) {
      done.complete();
    }
  }

  /// 停掉上一段播放并等它的等待方收尾，再开始下一轮
  Future<void> _settleAndStop() async {
    final Completer<void>? pending = _playbackDone;
    _playing = false;
    _completePlayback();
    await _player.stop();
    if (pending != null) {
      // 上一轮的 subscription 由其自身的 finally 注销，这里只等信号
      await pending.future;
    }
  }
}
