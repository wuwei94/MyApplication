# Flutter Skills

## 说明

本文按 6 个分类整理 `.opencode/skills/` 下的 Flutter skills，采用总表形式展示。每个 skill 都用简洁方式说明它是做什么的、主要解决什么问题，以及常见使用场景，方便快速查阅。

## 环境搭建

| Skill | 介绍 | 解决的问题 | 常见场景 |
| --- | --- | --- | --- |
| `flutter-environment-setup-windows` | 搭建 Windows 下的 Flutter 开发环境，包括 Flutter SDK、PATH、Visual Studio 工具链和 Android/Windows Desktop 配置。 | 环境缺失、工具链不完整、编译失败、`flutter doctor` 无法通过。 | 新电脑初始化、Windows 桌面开发、Android/Windows 双端调试。 |
| `flutter-environment-setup-macos` | 搭建 macOS 下的 Flutter 开发环境，包括 Flutter、Xcode、CocoaPods 和命令行工具。 | iOS/macOS 开发依赖缺失、Xcode 未配置、Pods 安装异常。 | iOS 开发机初始化、Apple 平台构建、Xcode/CocoaPods 故障排查。 |
| `flutter-environment-setup-linux` | 搭建 Linux 下的 Flutter 开发环境，重点是系统依赖、桌面工具链和 GTK 编译条件。 | Linux 桌面构建缺包、命令行环境不统一、诊断检查失败。 | Linux 工作站初始化、CI 构建环境准备、Linux 桌面应用开发。 |

## 核心开发

| Skill | 介绍 | 解决的问题 | 常见场景 |
| --- | --- | --- | --- |
| `flutter-layout` | 处理 Flutter 的布局体系，包括约束规则、常见布局组件、滚动容器和自适应界面。 | 组件溢出、尺寸异常、多设备适配困难。 | 页面开发、复杂界面编排、响应式设计。 |
| `flutter-state-management` | 管理 Flutter 应用中的局部状态和共享状态，覆盖常见状态组织方式。 | 状态分散、刷新逻辑混乱、业务逻辑和 UI 耦合过深。 | 表单、列表、用户信息、全局配置管理。 |
| `flutter-routing-and-navigation` | 实现页面跳转与路由组织，包括 Navigator、声明式路由、嵌套路由和参数传递。 | 多页面流转复杂、子流程导航混乱、深链接处理困难。 | 中大型应用、统一页面入口管理、Web 路由同步。 |
| `flutter-architecture` | 构建 Flutter 应用的整体工程结构，强调分层设计、职责分离和仓库模式。 | 项目变大后代码散乱、数据流不清晰、功能难复用。 | 多人协作项目、长期维护项目、中大型业务系统。 |
| `flutter-testing` | 建立 Flutter 自动化测试体系，包括单元测试、组件测试、集成测试和插件测试。 | 回归验证成本高、关键流程容易出错、改动后缺少可靠校验。 | 核心业务测试、复杂交互验证、发版前质量保障。 |

## 数据与并发

| Skill | 介绍 | 解决的问题 | 常见场景 |
| --- | --- | --- | --- |
| `flutter-http-and-json` | 处理 Flutter 中的网络请求和 JSON 数据，包括 HTTP 调用、模型解析和错误处理。 | 接口对接混乱、响应解析不规范、大 JSON 数据处理卡顿。 | REST API 对接、远程列表加载、表单提交。 |
| `flutter-databases` | 实现本地数据持久化，包括键值存储、关系型数据库和仓库层封装。 | 数据如何落地、如何离线访问、如何本地高效查询更新。 | 离线记录、消息缓存、复杂表单、本地业务数据管理。 |
| `flutter-caching` | 设计和实现缓存策略，包括内存缓存、文件缓存、本地持久化缓存和图片缓存。 | 重复请求过多、首屏慢、弱网体验差、数据反复加载。 | 首页缓存、配置缓存、媒体资源缓存、离线优先展示。 |
| `flutter-concurrency` | 处理 Flutter 中的并发任务，包括异步执行、Isolate 和重计算拆分。 | 主线程阻塞、耗时计算掉帧、后台任务难组织。 | 大 JSON 解析、批量数据处理、计算密集型任务。 |

## 高级特性

| Skill | 介绍 | 解决的问题 | 常见场景 |
| --- | --- | --- | --- |
| `flutter-performance` | 分析和优化 Flutter 性能，包括 DevTools、帧时间、重建范围和渲染开销识别。 | 页面卡顿、滚动掉帧、复杂页面性能不稳定。 | 性能排查、列表优化、复杂界面调优。 |
| `flutter-animation` | 实现 Flutter 动画效果，包括隐式动画、显式动画、共享元素动画和物理动画。 | 交互反馈生硬、状态切换不自然、复杂动效难组织。 | 引导页、页面转场、卡片展开、拖拽回弹。 |
| `flutter-app-size` | 分析和优化 Flutter 应用体积，包括构建产物分析、包体拆解和瘦身策略。 | 安装包过大、下载成本高、难定位体积来源。 | 发版前优化、渠道包控制、多平台构建治理。 |
| `flutter-accessibility` | 提升 Flutter 应用的无障碍能力和自适应体验，包括语义标注、焦点控制和可读性规范。 | 屏幕阅读器不可用、键盘操作差、控件可访问性不足。 | 政企项目、公共服务项目、面向广泛用户群体的产品。 |

## 原生交互

| Skill | 介绍 | 解决的问题 | 常见场景 |
| --- | --- | --- | --- |
| `flutter-plugins` | 开发 Flutter 插件本身，包含插件模板、平台结构、Method Channel、FFI 和联邦化插件。 | Flutter 无法直接提供的平台能力如何封装和复用。 | 封装内部 SDK、沉淀基础设施、构建跨项目通用能力。 |
| `flutter-native-interop` | 实现 Flutter 与原生平台之间的互操作，包括 Platform Channel、FFI、原生 API 调用和 Web JS 互通。 | Flutter 无法直接访问底层系统能力或复用现有原生库。 | 接入系统功能、第三方原生 SDK、硬件能力、Web 原生 API。 |
| `flutter-platform-views` | 把原生视图嵌入 Flutter 界面，如 Android View、iOS UIView 或 Web 宿主页元素。 | 复杂原生控件难以用 Flutter 完整重建。 | 地图、视频、广告、富文本编辑器、系统级控件嵌入。 |

## 主题与本地

| Skill | 介绍 | 解决的问题 | 常见场景 |
| --- | --- | --- | --- |
| `flutter-theming` | 统一 Flutter 应用视觉风格，包括主题配置、Material 3、颜色体系和组件样式规范。 | 页面风格不一致、样式分散、主题难统一管理。 | 设计系统建设、品牌换肤、暗黑模式支持。 |
| `flutter-localization` | 实现 Flutter 应用的国际化与本地化，包括多语言资源、Locale 配置和本地化组织方式。 | 文本硬编码、语言切换不便、多地区内容维护困难。 | 国际化产品、多地区运营、多语言支持。 |

## 分类概览

| 分类 | 说明 |
| --- | --- |
| 环境搭建 | 解决开发环境安装、工具链准备和编译前置问题，是 Flutter 开发的起点。 |
| 核心开发 | 覆盖布局、状态、路由、架构、测试，是日常开发最核心的一组能力。 |
| 数据与并发 | 关注数据获取、存储、缓存和后台计算，决定业务数据流和运行效率。 |
| 高级特性 | 侧重性能、动画、包体积和无障碍，用于提升产品质量与体验。 |
| 原生交互 | 让 Flutter 与平台能力深度结合，适合设备能力接入和原生资产复用。 |
| 主题与本地 | 统一视觉风格和语言环境，支撑设计一致性与国际化能力。 |
