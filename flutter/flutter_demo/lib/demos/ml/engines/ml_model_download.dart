import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:path_provider/path_provider.dart';

/// 一个按需从远端拉取的模型资产。
///
/// 与 Android `:modules:module_ml` 的 `RemoteModel` 一一对应：官方 2.x 样例中
/// 语音识别 / 零样本分类 / 语音合成这几类任务的模型动辄数十 MB 到数 GB，
/// **官方本身就是运行时下载**，不随 APK 分发。本模块沿用同一策略：
/// 模型首次使用时下载到应用私有目录，之后完全离线可用。
///
/// @property fileName 落盘后的文件名，同时也是加载解释器时使用的名字
/// @property approxBytes 预估字节数，仅用于下载进度展示
/// @property repo 模型仓库（`owner/name`）；走 [directUrl] 时可省略
/// @property remotePath 仓库内相对路径；走 [directUrl] 时可省略
/// @property revision 仓库修订号；null 表示跟随默认分支
/// @property directUrl 绝对下载地址。上游不在 HuggingFace 时使用（例如官方
///   `image_classification` 的 `efficientnet_lite2.tflite` 由官方 Gradle 从
///   `storage.googleapis.com` 拉取），非空时**优先于** `repo + remotePath` 拼装。
class MlRemoteModel {
  const MlRemoteModel({
    required this.fileName,
    required this.approxBytes,
    this.repo = '',
    this.remotePath = '',
    this.revision,
    this.directUrl,
  });

  final String fileName;
  final int approxBytes;
  final String repo;
  final String remotePath;
  final String? revision;
  final String? directUrl;

  /// 下载地址
  ///
  /// 官方各 sample 的下载器硬编码 `https://huggingface.co`，本机该域名不可达，
  /// 改用内容一致的镜像站（与 Android 侧 `RemoteModel.MIRROR_BASE` 同口径）。
  String get url =>
      directUrl ??
      'https://hf-mirror.com/$repo/resolve/${revision ?? 'main'}/$remotePath';
}

/// 各重型任务所用的远端模型清单 —— 与 Android `RemoteModels` 逐条对应。
///
/// 仓库与文件均按官方仓库实测核对，体积为实测值。同一仓库里可能另有厂商 NPU 变体
/// （`*_MediaTek_*.tflite` / `*_Qualcomm_*.tflite`）与量化变体，那些是实验性路径，
/// **不在本清单内**。
abstract final class MlRemoteModels {
  static const String _repoMatcha = 'litert-community/Matcha-TTS';
  static const String _repoKitten = 'litert-community/kitten-tts-nano-0.8';
  static const String _repoLaya = 'litert-community/Laya-Multilingual-LiteRT';
  static const String _repoQwen = 'litert-community/Qwen3-TTS-12Hz-0.6B-Base';

  /// 图像分类的可选模型 EfficientNet-Lite2（24 MB，与 Android 侧同名条目对应）。
  ///
  /// 官方 `image_classification` 样例可在 Lite0 / Lite2 之间切换，两张图由官方
  /// Gradle 任务下载到 assets。本模块遵循「新增模型不入包、运行时下载」的既定策略：
  /// Lite0 仍随 app 资产分发（默认模型不能依赖网络），Lite2 首次选中时下载。
  ///
  /// 上游是 Google Cloud Storage 而非 HuggingFace，因此走 [MlRemoteModel.directUrl]。
  /// 实测输入 `[1, 260, 260, 3]`、输出 `[1, 1000]` FP32 Softmax —— 与 Lite0 的 224
  /// 输入不同，页面必须按模型分别取尺寸。
  static const MlRemoteModel efficientnetLite2 = MlRemoteModel(
    fileName: 'efficientnet_lite2.tflite',
    approxBytes: 24325765,
    directUrl:
        'https://storage.googleapis.com/ai-edge/interpreter-samples/'
        'image_classification/android/efficientnet_lite2.tflite',
  );

  /// 神经 G2P（DeepPhonemizer）图。
  ///
  /// **两个语音合成示例共用同一张图**：官方流式样例自己的说明里也写明它取的就是
  /// Matcha 仓库里这一份。落盘文件名一致，先做过任一页面之后另一个不会重复下载。
  static const MlRemoteModel g2p = MlRemoteModel(
    fileName: 'dp_g2p_matcha_fp16.tflite',
    repo: _repoMatcha,
    remotePath: 'dp_g2p_matcha_fp16.tflite',
    approxBytes: 25784000,
  );

  /// Matcha-TTS：文本编码器 + 扩散解码器 + 声码器 + 神经 G2P 四张图，合计约 88 MB。
  static const List<MlRemoteModel> matcha = <MlRemoteModel>[
    MlRemoteModel(
      fileName: 'matcha_textenc_fp16.tflite',
      repo: _repoMatcha,
      remotePath: 'matcha_textenc_fp16.tflite',
      approxBytes: 14772000,
    ),
    MlRemoteModel(
      fileName: 'matcha_decoder_fp16.tflite',
      repo: _repoMatcha,
      remotePath: 'matcha_decoder_fp16.tflite',
      approxBytes: 22617000,
    ),
    MlRemoteModel(
      fileName: 'matcha_vocoder_fp16.tflite',
      repo: _repoMatcha,
      remotePath: 'matcha_vocoder_fp16.tflite',
      approxBytes: 28966000,
    ),
    g2p,
  ];

  /// KittenTTS 的时长预测器（含定长 LSTM 的 variable tensors）
  static const MlRemoteModel kittenPredictor = MlRemoteModel(
    fileName: 'kitten_predictor_fp16.tflite',
    repo: _repoKitten,
    remotePath: 'kitten_predictor_fp16.tflite',
    approxBytes: 16990756,
  );

  /// KittenTTS 的韵律预测器
  static const MlRemoteModel kittenProsody = MlRemoteModel(
    fileName: 'kitten_prosody_fp16.tflite',
    repo: _repoKitten,
    remotePath: 'kitten_prosody_fp16.tflite',
    approxBytes: 1697636,
  );

  /// KittenTTS 的声码器（ISTFTNet），输出是动态形状的波形张量
  static const MlRemoteModel kittenVocoder = MlRemoteModel(
    fileName: 'kitten_vocoder_fp16.tflite',
    repo: _repoKitten,
    remotePath: 'kitten_vocoder_fp16.tflite',
    approxBytes: 13367840,
  );

  /// KittenTTS 的音色表（`voices.npz`，端侧解包）
  static const MlRemoteModel kittenVoices = MlRemoteModel(
    fileName: 'voices.npz',
    repo: _repoKitten,
    remotePath: 'voices.npz',
    approxBytes: 3278902,
  );

  /// 流式语音合成（KittenTTS nano）：三张图 + 音色表 + 共用的 G2P，合计约 61 MB。
  static const List<MlRemoteModel> kitten = <MlRemoteModel>[
    kittenPredictor,
    kittenProsody,
    kittenVocoder,
    kittenVoices,
    g2p,
  ];

  /// 官方 `model_manifest.json` 钉死的修订号。
  ///
  /// 这是**有意的**：Laya 主图 250 MB、嵌入表 393 MB，跟随默认分支一旦上游换权重，
  /// 分词器 / 嵌入表 / 图三者就可能对不上（按官方原文沿用同一修订号）。
  static const String _layaRevision =
      '058cbf34ed2c6a2854ec60e9534c229d08740387';

  /// Laya 多语言零样本分类：主图 + 动作头 + 校准表 + 分词器 + 嵌入表，合计约 679 MB。
  ///
  /// 官方清单里还有一个 `tokenizer_config.json`（524 B），但分词器只读
  /// `tokenizer.json`（其中已含 `pre_tokenizer` 与 `model.vocab` / `model.merges`），
  /// 因此不下载这个用不到的文件 —— 其余 6 项与官方清单一一对应。
  static const List<MlRemoteModel> laya = <MlRemoteModel>[
    MlRemoteModel(
      fileName: 'laya_ml_s256_embeds_wfp16.tflite',
      repo: _repoLaya,
      remotePath: 'laya_ml_s256_embeds_wfp16.tflite',
      approxBytes: 250889408,
      revision: _layaRevision,
    ),
    MlRemoteModel(
      fileName: 'laya_ml_act_head_fp32.tflite',
      repo: _repoLaya,
      remotePath: 'laya_ml_act_head_fp32.tflite',
      approxBytes: 795816,
      revision: _layaRevision,
    ),
    MlRemoteModel(
      fileName: 'laya_ml_calibration.json',
      repo: _repoLaya,
      remotePath: 'laya_ml_calibration.json',
      approxBytes: 9156,
      revision: _layaRevision,
    ),
    MlRemoteModel(
      fileName: 'tokenizer.json',
      repo: _repoLaya,
      remotePath: 'tokenizer.json',
      approxBytes: 34363188,
      revision: _layaRevision,
    ),
    MlRemoteModel(
      fileName: 'token_embeddings_fp16.bin',
      repo: _repoLaya,
      remotePath: 'token_embeddings_fp16.bin',
      approxBytes: 393216000,
      revision: _layaRevision,
    ),
    MlRemoteModel(
      fileName: 'token_embeddings.json',
      repo: _repoLaya,
      remotePath: 'token_embeddings.json',
      approxBytes: 801,
      revision: _layaRevision,
    ),
  ];

  /// Qwen3-TTS（语音语言模型）的宿主侧张量
  static const List<MlRemoteModel> _qwenTables = <MlRemoteModel>[
    MlRemoteModel(
      fileName: 'codec_embedding_fp32.npy',
      repo: _repoQwen,
      remotePath: 'tables/codec_embedding_fp32.npy',
      approxBytes: 12583040,
    ),
    MlRemoteModel(
      fileName: 'mtp_embeddings_fp16.npy',
      repo: _repoQwen,
      remotePath: 'tables/mtp_embeddings_fp16.npy',
      approxBytes: 62914688,
    ),
    MlRemoteModel(
      fileName: 'text_projection_fp32.npz',
      repo: _repoQwen,
      remotePath: 'tables/text_projection_fp32.npz',
      approxBytes: 25179078,
    ),
    MlRemoteModel(
      fileName: 'text_embedding_fp16.npy',
      repo: _repoQwen,
      remotePath: 'tables/text_embedding_fp16.npy',
      approxBytes: 622329984,
    ),
  ];

  /// Qwen3-TTS 三图 + 宿主表 + 分词器，合计约 **1.67 GB**（FAST 图集）。
  ///
  /// 图集按官方 `install_to_device.sh` 的默认（FAST）装：共用的 `talker_int4`，
  /// 折叠成一次调用的 `mtp_folded_int8`，以及拆成两段的 `codec_partA` + `codec_partB`。
  /// 下载顺序刻意**把最大的两个放在最后**，断点续传中断时先拿到的都是小文件。
  static const List<MlRemoteModel> qwen = <MlRemoteModel>[
    MlRemoteModel(
      fileName: 'vocab.json',
      repo: _repoQwen,
      remotePath: 'vocab.json',
      approxBytes: 2776833,
    ),
    MlRemoteModel(
      fileName: 'merges.txt',
      repo: _repoQwen,
      remotePath: 'merges.txt',
      approxBytes: 1671839,
    ),
    MlRemoteModel(
      fileName: 'demo_speaker.npy',
      repo: _repoQwen,
      remotePath: 'voices/demo_speaker.npy',
      approxBytes: 4224,
    ),
    ..._qwenTables,
    MlRemoteModel(
      fileName: 'talker_int4.tflite',
      repo: _repoQwen,
      remotePath: 'talker_int4.tflite',
      approxBytes: 255998768,
    ),
    MlRemoteModel(
      fileName: 'mtp_folded_int8.tflite',
      repo: _repoQwen,
      remotePath: 'mtp_folded_int8.tflite',
      approxBytes: 229608368,
    ),
    MlRemoteModel(
      fileName: 'codec_partA.tflite',
      repo: _repoQwen,
      remotePath: 'codec_partA.tflite',
      approxBytes: 162987924,
    ),
    MlRemoteModel(
      fileName: 'codec_partB.tflite',
      repo: _repoQwen,
      remotePath: 'codec_partB.tflite',
      approxBytes: 293705152,
    ),
  ];
}

/// 应用私有目录下的模型缓存。
///
/// 与 Android `ModelStore` 对应：路径计算不依赖页面；公共 API 只做**存在性**判定，
/// 不校验哈希 —— 完整文件的落地由 [MlModelDownloader] 用 `.part` + rename 保证，
/// 因此「目标文件存在」即等价于「下载已完成」。
abstract final class MlModelStore {
  static const String _dirName = 'models';

  /// 模型目录（不存在则创建）；懒解析，命中后缓存
  static Future<Directory> dir() async {
    final Directory base = await getApplicationDocumentsDirectory();
    final Directory models = Directory('${base.path}/$_dirName');
    // 存在性判定用同步 stat（私有目录，代价可忽略），避开 avoid_slow_async_io
    if (!models.existsSync()) {
      await models.create(recursive: true);
    }
    return models;
  }

  /// 某个远端模型落盘后的目标文件
  static Future<File> fileFor(MlRemoteModel model) async =>
      File('${(await dir()).path}/${model.fileName}');

  /// 下载中的临时文件；命名固定，中断后仍可被下次续传复用
  static Future<File> partFileFor(MlRemoteModel model) async =>
      File('${(await dir()).path}/${model.fileName}.part');

  /// 是否已经下载完成
  static Future<bool> isReady(MlRemoteModel model) async {
    final File file = await fileFor(model);
    // 元数据判定用同步 IO（私有目录上的 stat，代价可忽略），避开 avoid_slow_async_io
    return file.existsSync() && file.lengthSync() > 0;
  }

  /// 尚未就绪的模型合计还要下载多少字节（用于「首次使用需下载约 xx MB」提示）
  static Future<int> pendingBytes(List<MlRemoteModel> models) async {
    int total = 0;
    for (final MlRemoteModel model in models) {
      if (!await isReady(model)) {
        total += model.approxBytes;
      }
    }
    return total;
  }

  /// 删除模型本体与残留的临时文件
  static Future<void> delete(MlRemoteModel model) async {
    final File target = await fileFor(model);
    if (target.existsSync()) {
      await target.delete();
    }
    final File part = await partFileFor(model);
    if (part.existsSync()) {
      await part.delete();
    }
  }

  /// 按文件名取绝对路径。
  ///
  /// 落盘文件名与远端文件名一致，因此引擎侧可以直接用文件名拼路径，
  /// 不必反过来持有 [MlRemoteModel] 实例。
  static Future<String> pathIn(String fileName) async =>
      '${(await dir()).path}/$fileName';
}

/// 下载进度回调：`(该文件已下字节, 该文件总字节)`
typedef MlDownloadProgress = void Function(int downloaded, int total);

/// 整组下载进度回调：`(当前第几个, 该文件已下, 该文件总, 全部已下, 全部总)`
typedef MlEnsureAllProgress = void Function(
    int index, int downloaded, int total, int overall, int overallTotal);

/// 支持断点续传的模型下载器。
///
/// 与 Android `ModelDownloader` 同口径，三点取舍：
/// 1. **断点续传**：先写 `<name>.part`，用 `Range` 请求续下；服务端忽略 Range 返回 200
///    时从头重写，不会写出错位数据；
/// 2. **原子落地**：只有完整写完后才 rename 成正式文件名，[MlModelStore.isReady]
///    因此只需判存在性，不会把半截文件误当成可用模型；
/// 3. **可取消**：页面销毁时调 [cancel]，流式写循环内检查标记并中断，
///    `.part` 保留供下次续传。
class MlModelDownloader {
  MlModelDownloader({Dio? dio}) : _dio = dio ?? Dio();

  final Dio _dio;
  final CancelToken _cancelToken = CancelToken();
  bool _cancelled = false;

  /// 取消当前下载；已写的 `.part` 会被保留
  void cancel() {
    _cancelled = true;
    _cancelToken.cancel('模型下载已取消');
  }

  /// 下载单个模型；已存在则直接返回。
  ///
  /// 抛出 [DioException] / [MlDownloadException]；期间调用了 [cancel] 时中断。
  Future<File> download(
    MlRemoteModel model, {
    MlDownloadProgress? onProgress,
  }) async {
    final File target = await MlModelStore.fileFor(model);
    // 元数据判定用同步 IO：这些都是私有目录上的 stat 调用，代价可忽略，
    // 且能避开 avoid_slow_async_io（项目 lint 强制）
    if (target.existsSync() && target.lengthSync() > 0) {
      final int size = target.lengthSync();
      onProgress?.call(size, size);
      return target;
    }

    final File part = await MlModelStore.partFileFor(model);
    return _downloadFrom(model.url, model, part, target, onProgress);
  }

  /// 从单个地址把模型取到 `.part` 并原子落地
  Future<File> _downloadFrom(
    String url,
    MlRemoteModel model,
    File part,
    File target,
    MlDownloadProgress? onProgress,
  ) async {
    int downloaded = part.existsSync() ? part.lengthSync() : 0;

    final Response<ResponseBody> response =
        await _dio.get<ResponseBody>(
      url,
      options: Options(
        responseType: ResponseType.stream,
        headers: downloaded > 0 ? <String, String>{'Range': 'bytes=$downloaded-'} : null,
        sendTimeout: const Duration(seconds: 15),
        receiveTimeout: const Duration(seconds: 30),
      ),
      cancelToken: _cancelToken,
    );

    final int status = response.statusCode ?? 0;
    if (status == 200) {
      // 服务端忽略 Range：从文件头重写
      downloaded = 0;
      await part.writeAsBytes(const <int>[]);
    } else if (status == 206) {
      // 续传：保留 .part 现有长度
    } else if (status == 416) {
      // Range 不被满足：.part 长度与服务端不符（内容变了或上次写坏）。
      // 删掉残留，让下次重试时从头下，避免永久卡在 416。
      await part.delete();
      throw MlDownloadException('HTTP 416（残留 .part 与远端不符，已清理，请重试）');
    } else {
      throw MlDownloadException('HTTP $status');
    }

    final int declared =
        int.tryParse(response.headers.value('content-length') ?? '') ?? -1;
    final int total = declared > 0 ? declared + downloaded : model.approxBytes;
    // 响应头的 Content-Length 是**本次要传的字节数**：206 时它只是剩余部分，不是文件总长。
    // 校验必须用「本次实际写入量」，否则续传会被误判成截断（downloaded 是含已续传部分的累计值）。
    final int startAtResponse = downloaded;

    // 本机 Dart SDK 的 File.openWrite 已改为同步返回 IOSink（句柄惰性打开），
    // 没有 await 的必要；flush / close 仍是 Future，保留 await。
    final IOSink sink = part.openWrite(mode: FileMode.append);
    int lastReport = 0;
    try {
      await for (final List<int> chunk in response.data!.stream) {
        if (_cancelled) {
          throw MlDownloadException('模型下载已取消');
        }
        sink.add(chunk);
        downloaded += chunk.length;
        final int now = DateTime.now().millisecondsSinceEpoch;
        if (now - lastReport >= 120) {
          lastReport = now;
          onProgress?.call(downloaded, total);
        }
      }
      await sink.flush();
    } finally {
      await sink.close();
    }

    // 长度校验：服务端声明了 Content-Length 时，本次实收字节数必须与之一致，否则是被
    // 截断的残次品，不能 rename 落地（否则 ModelStore 会把它当成可用模型）。
    if (declared > 0 && downloaded - startAtResponse != declared) {
      await part.delete();
      throw MlDownloadException(
        '下载被截断：期望 $declared 字节，实得 ${downloaded - startAtResponse}',
      );
    }
    onProgress?.call(downloaded, total);

    if (target.existsSync()) {
      await target.delete();
    }
    await part.rename(target.path);
    return target;
  }

  /// 按顺序补齐一组模型，任一失败即抛出（已完成的不会重下）。
  Future<List<File>> ensureAll(
    List<MlRemoteModel> models, {
    MlEnsureAllProgress? onProgress,
  }) async {
    final List<int> sizes = <int>[];
    for (final MlRemoteModel model in models) {
      sizes.add(await MlModelStore.isReady(model) ? 0 : model.approxBytes);
    }
    final int overallTotal = sizes.fold(0, (int sum, int size) => sum + size);
    int doneBefore = 0;
    final List<File> files = <File>[];
    for (int index = 0; index < models.length; index++) {
      final MlRemoteModel model = models[index];
      final File file = await download(
        model,
        onProgress: (int downloaded, int total) =>
            onProgress?.call(index, downloaded, total, doneBefore + downloaded, overallTotal),
      );
      doneBefore += sizes[index];
      files.add(file);
    }
    return files;
  }
}

/// 下载类失败的统一异常类型（HTTP 状态异常、长度不符、取消等）
class MlDownloadException implements Exception {
  MlDownloadException(this.message);

  final String message;

  @override
  String toString() => 'MlDownloadException: $message';
}

/// 字节数的人类可读格式化（下载提示用）
String mlFormatBytes(int bytes) {
  if (bytes <= 0) return '0 B';
  const List<String> units = <String>['B', 'KB', 'MB', 'GB'];
  int value = bytes;
  int unit = 0;
  while (value >= 1024 && unit < units.length - 1) {
    value ~/= 1024;
    unit++;
  }
  return '$value ${units[unit]}';
}

/// 调试输出（与现有 helper 的 [debugPrint] 用法保持一致）
void mlLog(String message) => debugPrint(message);
