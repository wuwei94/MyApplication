// 这一组 src/ 导入是多签名图能力的唯一来源：tflite_flutter 0.12.1 的 Dart API
// **完全没有**签名（SignatureRunner）暴露，而 LiteRT 的多签名图（一个签名一张
// subgraph）只能通过签名运行器调用 —— 经典 `Interpreter.invoke()` 只跑 subgraph 0。
// 该 C API 的绑定在本包已导出（含 GetSignatureRunner / ResizeInputTensor /
// AllocateTensors / Invoke / GetInputTensor / GetOutputTensor / Delete），因此这里
// 直接按 C API 自建一套最薄的运行器封装。这是**有意**的窄面依赖，不是随手 import。
// ignore_for_file: implementation_imports
import 'dart:ffi';
import 'dart:io';
import 'dart:typed_data';

import 'package:ffi/ffi.dart';
import 'package:tflite_flutter/src/bindings/bindings.dart';
import 'package:tflite_flutter/src/bindings/tensorflow_lite_bindings_generated.dart';
import 'package:tflite_flutter/tflite_flutter.dart' as tfl;

/// 多签名 LiteRT 图 + 签名运行器的 FFI 封装
///
/// ### 为什么需要它
///
/// `tflite_flutter` 的 `Interpreter` 是「经典单图」API：`invoke()` 固定执行模型的
/// **第一个 subgraph**。而按签名导出的模型（`tf.saved_model` → TFLite signatures）
/// 每个签名占**一张独立 subgraph**，各签名的输入形状还可能互不相同
/// （如 Qwen3-TTS 的 talker：`prefill_32` 吃 `[1,32,1024]`，`decode` 吃 `[1,1,1024]`）。
/// 这种情况下 Dart 侧没有任何入口能跑到第二个签名 —— 只能下沉到 C API：
///
/// ```c
/// TfLiteInterpreterGetSignatureRunner(interpreter, "decode")
///   → TfLiteSignatureRunnerResizeInputTensor / AllocateTensors / Invoke
///   → TfLiteSignatureRunnerGet{Input,Output}Tensor(name)
/// ```
///
/// 这组函数在 litert 的 C 库里（`libtensorflowlite_c.so`）以实验接口形式导出，
/// 本包已生成的 bindings 完整覆盖；[open] 起不到符号时会在构造处直接抛错，
/// 不会把问题拖到第一次推理。
///
/// ### 生命周期
///
/// 原生对象**不受 GC 管理**，必须显式 [close]（顺序：先运行器、后解释器、再选项与模型）。
/// 与 Android 侧一致的地方是「一张图一个实例、随引擎一起建一起放」；不一致的地方是
/// C API 不提供「按名字把输入/输出映射到任意缓冲」的能力（LiteRT 2.x 的
/// `CompiledModel.run(inputs, outputs, key)` 可以），因此调用方拿到的输入/输出
/// **是图内固定张量**，需要自己负责把上一轮输出搬进下一轮输入。
class MlSignatureGraph {
  MlSignatureGraph._({
    required Pointer<TfLiteModel> model,
    required Pointer<TfLiteInterpreterOptions> options,
    required Pointer<TfLiteInterpreter> interpreter,
    required Pointer<TfLiteDelegate> delegate,
    required NativeCallable<MlErrorReporterNative>? reporter,
    required int signatureCount,
    required tfl.GpuDelegateV2? gpuDelegate,
  })  : _model = model,
        _options = options,
        _interpreter = interpreter,
        _delegate = delegate,
        _reporter = reporter,
        _signatureCount = signatureCount,
        _gpuDelegate = gpuDelegate;

  final Pointer<TfLiteModel> _model;
  final Pointer<TfLiteInterpreterOptions> _options;
  final Pointer<TfLiteInterpreter> _interpreter;

  /// 显式挂上的加速后端 delegate（未挂时为 nullptr）
  final Pointer<TfLiteDelegate> _delegate;

  /// GPU delegate 的持有者（非 GPU 后端时为 null）。
  ///
  /// 原生句柄同时存在于 [_delegate]，但释放函数不同（`TfLiteGpuDelegateV2Delete`），
  /// 因此这里单独留一份由包内实现负责释放。
  final tfl.GpuDelegateV2? _gpuDelegate;

  /// 错误上报回调的原生包装；必须与解释器同生共死
  final NativeCallable<MlErrorReporterNative>? _reporter;

  final int _signatureCount;

  bool _closed = false;

  /// 打开模型文件并建解释器
  ///
  /// [numThreads] 直接对应 Android 侧 `TFLiteModelHelper.CPU_THREADS`
  /// （`min(核数, 4)`），保证耗时口径可横向比较。
  ///
  /// [xnnpackFlags] 非空时**显式**挂一个 XNNPACK delegate 并带上标志位 ——
  /// 官方样例对 `codec_partB` 要求的 `FORCE_FP16`（bit 2）只能从这里进：
  /// C API 的 `TfLiteInterpreterOptions` 是不透明结构，没有单独的 flags 设置函数。
  ///
  /// [gpu] 为真时改挂 GPU delegate（`TfLiteGpuDelegateV2Create`）。两者互斥：
  /// XNNPACK 与 GPU delegate 同挂只会让图被两条后端各切一半，不能横向比耗时。
  /// GPU 符号来自 `libtensorflowlite_gpu_jni.so`（tflite_flutter 单独的一个 .so，
  /// 与核心运行时解耦），该库随 APK 全 ABI 打包，因此这里直接复用包内已导出
  /// 的 `GpuDelegateV2`，不自写 FFI 结构体。
  ///
  /// [gpuAllowPrecisionLoss] 对应 `GpuDelegateOptionsV2.isPrecisionLossAllowed`：
  /// 传 `false` 时 GPU 以最大精度计算（等价于 2.x 的 `Precision.FP32`），官方 ASR
  /// 的 `gpuEnforceFloat32`（默认 true）要求的就是这一档；默认 `true` 保持原生默认，
  /// 即允许 fp16 加速。
  ///
  /// [onError] 会接管 LiteRT 的错误上报（默认是打日志），用来把「状态码 1」这类
  /// 无信息失败变成可读原因。
  static MlSignatureGraph open(
    String path, {
    required int numThreads,
    int? xnnpackFlags,
    bool gpu = false,
    bool gpuAllowPrecisionLoss = true,
    void Function(String message)? onError,
  }) {
    final Pointer<Utf8> cPath = path.toNativeUtf8();
    Pointer<TfLiteModel> model = nullptr;
    Pointer<TfLiteInterpreterOptions> options = nullptr;
    Pointer<TfLiteInterpreter> interpreter = nullptr;
    Pointer<TfLiteDelegate> delegate = nullptr;
    tfl.GpuDelegateV2? gpuDelegate;
    NativeCallable<MlErrorReporterNative>? reporter;
    try {
      model = tfliteBinding.TfLiteModelCreateFromFile(cPath.cast<Char>());
      if (model == nullptr) {
        throw StateError('LiteRT 无法打开模型文件：$path');
      }
      options = tfliteBinding.TfLiteInterpreterOptionsCreate();
      tfliteBinding.TfLiteInterpreterOptionsSetNumThreads(options, numThreads);

      if (onError != null) {
        reporter = NativeCallable<MlErrorReporterNative>.isolateLocal(
          (Pointer<Void> _, Pointer<Char> format, Pointer<Char> _) {
            onError(format.cast<Utf8>().toDartString());
          },
        );
        tfliteBinding.TfLiteInterpreterOptionsSetErrorReporter(
          options,
          reporter.nativeFunction,
          nullptr,
        );
      }

      if (gpu) {
        final tfl.GpuDelegateV2 created =
            _createGpuDelegate(allowPrecisionLoss: gpuAllowPrecisionLoss);
        gpuDelegate = created;
        delegate = created.base;
        if (delegate == nullptr) {
          throw StateError('GPU delegate 创建失败（设备无可用 GPU 后端）');
        }
        tfliteBinding.TfLiteInterpreterOptionsAddDelegate(options, delegate);
      } else if (xnnpackFlags != null) {
        final Pointer<TfLiteXNNPackDelegateOptions> xnnOptions =
            calloc<TfLiteXNNPackDelegateOptions>();
        try {
          xnnOptions.ref.num_threads = numThreads;
          xnnOptions.ref.flags = xnnpackFlags;
          delegate = tfliteBinding.TfLiteXNNPackDelegateCreate(xnnOptions);
        } finally {
          calloc.free(xnnOptions);
        }
        if (delegate == nullptr) {
          throw StateError('XNNPACK delegate 创建失败（flags=$xnnpackFlags）');
        }
        tfliteBinding.TfLiteInterpreterOptionsAddDelegate(options, delegate);
      }

      interpreter = tfliteBinding.TfLiteInterpreterCreate(model, options);
      if (interpreter == nullptr) {
        throw StateError('LiteRT 无法为 $path 建解释器（多为内存不足或算子不支持）');
      }
      return MlSignatureGraph._(
        model: model,
        options: options,
        interpreter: interpreter,
        delegate: delegate,
        reporter: reporter,
        signatureCount: tfliteBinding.TfLiteInterpreterGetSignatureCount(interpreter),
        gpuDelegate: gpuDelegate,
      );
    } catch (_) {
      if (interpreter != nullptr) {
        tfliteBinding.TfLiteInterpreterDelete(interpreter);
      }
      if (delegate != nullptr) {
        _deleteDelegate(delegate, gpuDelegate);
      }
      if (options != nullptr) {
        tfliteBinding.TfLiteInterpreterOptionsDelete(options);
      }
      if (model != nullptr) {
        tfliteBinding.TfLiteModelDelete(model);
      }
      reporter?.close();
      rethrow;
    } finally {
      malloc.free(cPath);
    }
  }

  /// 模型里声明的签名（subgraph）个数
  int get signatureCount => _signatureCount;

  /// 全部签名键，按模型内的声明顺序
  List<String> get signatureKeys => List<String>.generate(
        _signatureCount,
        (int i) => tfliteBinding
            .TfLiteInterpreterGetSignatureKey(_interpreter, i)
            .cast<Utf8>()
            .toDartString(),
      );

  /// 取某个签名的运行器；同一个键重复调用返回同一个实例（内部缓存）
  MlSignatureRunner runner(String key) => _runners.putIfAbsent(key, () {
        final Pointer<Utf8> cKey = key.toNativeUtf8();
        try {
          final Pointer<TfLiteSignatureRunner> handle =
              tfliteBinding.TfLiteInterpreterGetSignatureRunner(
            _interpreter,
            cKey.cast<Char>(),
          );
          if (handle == nullptr) {
            throw StateError('模型里没有签名「$key」，可选：${signatureKeys.join(', ')}');
          }
          return MlSignatureRunner._(handle, key);
        } finally {
          malloc.free(cKey);
        }
      });

  final Map<String, MlSignatureRunner> _runners = <String, MlSignatureRunner>{};

  /// 释放全部原生对象。运行器先于解释器，否则运行器持有的是已释放的 subgraph
  void close() {
    if (_closed) {
      return;
    }
    _closed = true;
    for (final MlSignatureRunner runner in _runners.values) {
      runner.delete();
    }
    _runners.clear();
    tfliteBinding.TfLiteInterpreterDelete(_interpreter);
    tfliteBinding.TfLiteInterpreterOptionsDelete(_options);
    if (_delegate != nullptr) {
      _deleteDelegate(_delegate, _gpuDelegate);
    }
    tfliteBinding.TfLiteModelDelete(_model);
    _reporter?.close();
  }

  /// 创建 GPU delegate
  ///
  /// [allowPrecisionLoss] 为 true 时不传 options，直接走原生默认
  /// （`TfLiteGpuDelegateOptionsV2Default()`，允许 fp16 加速）；
  /// 为 false 时显式构造 `GpuDelegateOptionsV2(isPrecisionLossAllowed: false)`，
  /// 让 GPU 以最大精度计算 —— 1.4.0 的 delegate 没有 2.x `constantTensorSharing`
  /// 那个开关，这里是它能表达的最接近 `Precision.FP32` 的档位。
  static tfl.GpuDelegateV2 _createGpuDelegate({
    required bool allowPrecisionLoss,
  }) {
    if (allowPrecisionLoss) {
      return tfl.GpuDelegateV2();
    }
    final tfl.GpuDelegateOptionsV2 gpuOptions =
        tfl.GpuDelegateOptionsV2(isPrecisionLossAllowed: false);
    try {
      // TfLiteGpuDelegateV2Create 按值拷贝选项，建完 delegate 即可释放这块 native 内存
      return tfl.GpuDelegateV2(options: gpuOptions);
    } finally {
      gpuOptions.delete();
    }
  }

  /// 释放加速后端 delegate
  ///
  /// GPU 走包内 `GpuDelegateV2.delete()`（`TfLiteGpuDelegateV2Delete`）。
  ///
  /// XNNPACK 的 `TfLiteXNNPackDelegateDelete` **没有**被 tflite_flutter 生成的
  /// bindings 覆盖（该文件只生成了 `Create`），但符号就在同一个 .so 里，这里自己查一次。
  /// 查不到时静默跳过：delegate 会随进程一起结束，不至于让整页失败。
  static void _deleteDelegate(
    Pointer<TfLiteDelegate> delegate,
    tfl.GpuDelegateV2? gpuDelegate,
  ) {
    if (gpuDelegate != null) {
      gpuDelegate.delete();
      return;
    }
    try {
      _delegateDelete ??= DynamicLibrary.open(_tfliteLibName).lookupFunction<
          Void Function(Pointer<TfLiteDelegate>),
          void Function(Pointer<TfLiteDelegate>)>('TfLiteXNNPackDelegateDelete');
      _delegateDelete!(delegate);
    } catch (_) {
      // 符号缺失：放弃释放，不影响功能
    }
  }

  static void Function(Pointer<TfLiteDelegate>)? _delegateDelete;

  /// 与 tflite_flutter 的 `bindings.dart` 同口径的库名
  ///
  /// Android 上是 `libLiteRt.so` —— 这**不是**笔误：`tflite_flutter` 的运行时已由
  /// `tools/android/apply_android_tflite_flutter.dart` 从 LiteRT 1.4.0
  /// （`libtensorflowlite_jni.so`）升到 2.3.0（`libLiteRt.so`），APK 里已经没有前者。
  /// 这里若仍写旧名，`DynamicLibrary.open` 会一直失败、`TfLiteXNNPackDelegateDelete`
  /// 永远查不到，delegate 只能随进程结束才回收（不崩，但泄漏）。
  /// 其他平台仍是 tflite_flutter 自带的那几个名字，未打补丁。
  static String get _tfliteLibName {
    if (Platform.isAndroid) {
      return 'libLiteRt.so';
    }
    if (Platform.isIOS || Platform.isMacOS) {
      return 'libtensorflowlite_c-mac.dylib';
    }
    if (Platform.isLinux) {
      return 'libtensorflowlite_c-linux.so';
    }
    return 'libtensorflowlite_c-win.dll';
  }
}

/// LiteRT 错误上报回调的原生签名（`TfLiteErrorReporter`）
///
/// 第三个参数是 `va_list`（本包的 bindings 里就是 `Pointer<Char>`）：Dart 侧读不到
/// 变参内容，因此只取格式串 —— 足以定位「哪种算子/张量不受支持」这类失败。
typedef MlErrorReporterNative = Void Function(
  Pointer<Void> userData,
  Pointer<Char> format,
  Pointer<Char> arguments,
);

/// 单个签名的运行器：输入/输出的按名寻址、形状调整、数据读写与调用
///
/// 一个签名内**同名张量可以既是输入又是输出**（KV cache 就是这种形态：读旧 cache、
/// 写新 cache），此时两者的下标与内存都不同，因此读写分别走 `input` / `output` 侧。
class MlSignatureRunner {
  MlSignatureRunner._(this._runner, this.key);

  final Pointer<TfLiteSignatureRunner> _runner;

  /// 对应签名键
  final String key;

  bool _deleted = false;

  /// 输入张量个数
  int get inputCount => tfliteBinding.TfLiteSignatureRunnerGetInputCount(_runner);

  /// 输出张量个数
  int get outputCount => tfliteBinding.TfLiteSignatureRunnerGetOutputCount(_runner);

  /// 输入名字，按签名内的声明顺序
  List<String> get inputNames => List<String>.generate(
        inputCount,
        (int i) => tfliteBinding
            .TfLiteSignatureRunnerGetInputName(_runner, i)
            .cast<Utf8>()
            .toDartString(),
      );

  /// 输出名字，按签名内的声明顺序
  List<String> get outputNames => List<String>.generate(
        outputCount,
        (int i) => tfliteBinding
            .TfLiteSignatureRunnerGetOutputName(_runner, i)
            .cast<Utf8>()
            .toDartString(),
      );

  /// 输入张量当前形状（未分配时返回声明形状；分配后返回实际形状）
  List<int> inputShape(String name) => _shapeOf(inputTensor(name));

  /// 输出张量当前形状（动态维在 [allocateTensors] 之后才落定）
  List<int> outputShape(String name) => _shapeOf(outputTensor(name));

  /// 输入张量字节数
  int inputByteSize(String name) => tfliteBinding.TfLiteTensorByteSize(inputTensor(name));

  /// 输出张量字节数
  int outputByteSize(String name) => tfliteBinding.TfLiteTensorByteSize(outputTensor(name));

  /// 张量的元素类型码（`TfLiteType`，即 `TfLiteType.kTfLiteFloat32` 等常量）。
  ///
  /// 返回的是**内部绑定**的常量值，不在本类的公开 API 里暴露类型名 ——
  /// 调用方一律用 [isFloat32] / [isInt32] 判定，避免把 implementation_imports
  /// 扩散到页面与引擎代码里。
  int typeOf(String name, {required bool input}) =>
      tfliteBinding.TfLiteTensorType(input ? inputTensor(name) : outputTensor(name));

  /// 是否是 float32 张量
  bool isFloat32(String name, {required bool input}) =>
      typeOf(name, input: input) == TfLiteType.kTfLiteFloat32;

  /// 是否是 int32 张量
  bool isInt32(String name, {required bool input}) =>
      typeOf(name, input: input) == TfLiteType.kTfLiteInt32;

  /// 张量的元素个数（按类型宽度换算；不支持的类型返回 -1）
  int elementCountOf(String name, {required bool input}) {
    final int bytes = input ? inputByteSize(name) : outputByteSize(name);
    final int width = switch (typeOf(name, input: input)) {
      TfLiteType.kTfLiteFloat32 || TfLiteType.kTfLiteInt32 => 4,
      TfLiteType.kTfLiteFloat64 || TfLiteType.kTfLiteInt64 => 8,
      TfLiteType.kTfLiteInt16 || TfLiteType.kTfLiteFloat16 => 2,
      TfLiteType.kTfLiteInt8 ||
      TfLiteType.kTfLiteUInt8 ||
      TfLiteType.kTfLiteBool =>
        1,
      _ => -1,
    };
    return width > 0 && bytes % width == 0 ? bytes ~/ width : -1;
  }

  /// 调整输入形状。**只有模型里标了 -1 的维可以改**（`ResizeInputTensorStrict` 语义），
  /// 改完必须接一次 [allocateTensors] 才生效。
  void resizeInput(String name, List<int> dims) {
    final Pointer<Utf8> cName = name.toNativeUtf8();
    final Pointer<Int> cDims = calloc<Int>(dims.length);
    try {
      for (int i = 0; i < dims.length; i++) {
        cDims[i] = dims[i];
      }
      final int status = tfliteBinding.TfLiteSignatureRunnerResizeInputTensor(
        _runner,
        cName.cast<Char>(),
        cDims,
        dims.length,
      );
      if (status != 0) {
        throw StateError('resizeInput 失败（$key.$name -> $dims），状态码 $status');
      }
    } finally {
      calloc.free(cDims);
      malloc.free(cName);
    }
  }

  /// 按调整后的输入形状重新分配张量内存
  void allocateTensors() {
    final int status = tfliteBinding.TfLiteSignatureRunnerAllocateTensors(_runner);
    if (status != 0) {
      throw StateError('allocateTensors 失败（$key），状态码 $status');
    }
  }

  /// 执行一次推理
  void invoke() {
    final int status = tfliteBinding.TfLiteSignatureRunnerInvoke(_runner);
    if (status != 0) {
      throw StateError('invoke 失败（$key），状态码 $status');
    }
  }

  /// 写入 float32 输入（就地写入图内输入张量，字节数必须完全吻合）
  void writeFloatInput(String name, Float32List data) {
    final Float32List target = floatView(name, input: true);
    if (target.length != data.length) {
      throw StateError('输入 $name 需要 ${target.length} 个 float，实得 ${data.length}');
    }
    target.setAll(0, data);
  }

  /// 写入 int32 输入
  void writeIntInput(String name, Int32List data) {
    final Int32List target = intView(name, input: true);
    if (target.length != data.length) {
      throw StateError('输入 $name 需要 ${target.length} 个 int32，实得 ${data.length}');
    }
    target.setAll(0, data);
  }

  /// 读取 float32 输出（**拷出**；就地视图会在下一次 invoke 时被覆盖）
  Float32List readFloatOutput(String name) =>
      Float32List.fromList(floatView(name, input: false));

  /// 读取 int32 输出（**拷出**）
  Int32List readIntOutput(String name) => Int32List.fromList(intView(name, input: false));

  /// 输入张量上的 float32 就地视图（零拷贝；写 KV cache 这类大块数据时必需）
  Float32List floatView(String name, {required bool input}) {
    final Pointer<TfLiteTensor> tensor = input ? inputTensor(name) : outputTensor(name);
    final int bytes = tfliteBinding.TfLiteTensorByteSize(tensor);
    if (bytes % 4 != 0) {
      throw StateError('张量 $name 的字节数 $bytes 不是 4 的倍数，不能按 float32 读');
    }
    return _dataPointer(tensor).cast<Float>().asTypedList(bytes ~/ 4);
  }

  /// 输入张量上的 int32 就地视图（零拷贝）
  Int32List intView(String name, {required bool input}) {
    final Pointer<TfLiteTensor> tensor = input ? inputTensor(name) : outputTensor(name);
    final int bytes = tfliteBinding.TfLiteTensorByteSize(tensor);
    if (bytes % 4 != 0) {
      throw StateError('张量 $name 的字节数 $bytes 不是 4 的倍数，不能按 int32 读');
    }
    return _dataPointer(tensor).cast<Int32>().asTypedList(bytes ~/ 4);
  }

  /// 输入张量按名字取句柄（每次调用重新查一遍；名字表由 C 侧维护，开销可忽略）
  Pointer<TfLiteTensor> inputTensor(String name) => _withCString(
        name,
        (Pointer<Char> cName) =>
            tfliteBinding.TfLiteSignatureRunnerGetInputTensor(_runner, cName),
      );

  /// 输出张量按名字取句柄
  Pointer<TfLiteTensor> outputTensor(String name) => _withCString(
        name,
        (Pointer<Char> cName) =>
            tfliteBinding.TfLiteSignatureRunnerGetOutputTensor(_runner, cName),
      );

  Pointer<Void> _dataPointer(Pointer<TfLiteTensor> tensor) {
    final Pointer<Void> data = tfliteBinding.TfLiteTensorData(tensor);
    if (data == nullptr) {
      throw StateError('张量数据指针为空（是否忘了 allocateTensors）');
    }
    return data;
  }

  List<int> _shapeOf(Pointer<TfLiteTensor> tensor) {
    final int dims = tfliteBinding.TfLiteTensorNumDims(tensor);
    return List<int>.generate(dims, (int i) => tfliteBinding.TfLiteTensorDim(tensor, i));
  }

  T _withCString<T>(String value, T Function(Pointer<Char>) body) {
    final Pointer<Utf8> cValue = value.toNativeUtf8();
    try {
      return body(cValue.cast<Char>());
    } finally {
      malloc.free(cValue);
    }
  }

  /// 释放运行器（幂等）。必须在所属解释器释放之前调用
  void delete() {
    if (_deleted) {
      return;
    }
    _deleted = true;
    tfliteBinding.TfLiteSignatureRunnerDelete(_runner);
  }
}
