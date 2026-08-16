# lib_event_bus

MyApplication 的 Flutter Demo Catalog 使用的事件总线封装库，与 Android `lib_eventbus` 结构对齐。

公共 API 从 `package:lib_event_bus/lib_event_bus.dart` 导出，不依赖 `flutter_demo`。

## 核心契约

- `FlutterEventBus` 为全局单例（`instance` 与工厂构造返回同一实例），业务侧统一通过 `FlutterEventBus.instance` 收发事件。
- `post<T>` 发送事件；`onEvent<T>` 只接收指定类型事件，`onAllEvents` 接收任意类型事件。
- 底层基于 event_bus 包，Stream 语义：监听方需自行管理 `StreamSubscription` 生命周期（组件销毁时取消订阅）。
- 事件类型由业务侧定义，本包不提供内置事件、粘性事件或进程间通信。
