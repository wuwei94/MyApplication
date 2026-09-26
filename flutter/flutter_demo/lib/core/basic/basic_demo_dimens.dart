/// 示例页尺寸常量 — 与 Android `docs/01-rules/design.md` / `dimens.xml` 对齐。
///
/// Why：操作区固定高度 200 保证「上展示 / 下操作」双区结构稳定，不随操作项数量塌缩。
abstract final class BasicDemoDimens {
  /// 页面标准边距。
  static const double pagePadding = 16;

  /// 紧凑内边距（图标与文字、列表项）。
  static const double compact = 4;

  /// 列表项 / 按钮内边距。
  static const double itemPadding = 8;

  /// 卡片 / Header 内边距。
  static const double cardPadding = 12;

  /// 区块大间距。
  static const double sectionGap = 24;

  /// 操作列表统一固定高度（Android `shared_dp_operation_height`）。
  static const double operationHeight = 200;

  /// 控制台 Header 高度。
  static const double consoleHeaderHeight = 32;

  /// 圆角：卡片。
  static const double cornerCard = 12;

  /// 圆角：小控件。
  static const double cornerSmall = 8;

  /// 控制台 / 操作项正文字号。
  static const double fontBody = 12;

  /// 页面区块标题字号。
  static const double fontTitle = 14;
}
