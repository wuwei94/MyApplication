# 设计规范

> 基于 Material Design 规范，定义项目统一的尺寸、间距和文字大小。
> 所有尺寸定义在 `basic_shared/src/main/res/values/dimens.xml`。

## 尺寸体系

### 间距（dp）

基于 4dp 网格系统，所有间距为 4 的倍数。

| 尺寸 | 资源名 | 用途 |
|------|--------|------|
| 4dp | `shared_dp_4` | 最小间距、图标与文字间距、紧凑内边距 |
| 8dp | `shared_dp_8` | 列表项内边距、按钮内边距 |
| 12dp | `shared_dp_12` | 卡片内边距、输入框内边距 |
| 16dp | `shared_dp_16` | 页面标准边距、列表项间距 |
| 24dp | `shared_dp_24` | 大间距、区块间距 |
| 32dp | `shared_dp_32` | 区块间大间距 |
| 48dp | `shared_dp_48` | 最小触摸区域、BottomNav 默认高度 |
| 56dp | `shared_dp_56` | AppBar 默认高度、BottomNav 高度 |
| 64dp | `shared_dp_64` | BottomNav 高度（4-5项带文字） |
| 72dp | `shared_dp_72` | 列表项高度（带头像/图标） |

### 通用尺寸（dp）

| 尺寸 | 资源名 | 用途 |
|------|--------|------|
| 200dp | `shared_dp_image_size` | 正方形图片尺寸 |
| 300dp | `shared_dp_response_size` | 响应展示区尺寸 |
| 80dp | `shared_dp_avatar_size` | 头像、大图标尺寸 |
| 96dp | `shared_dp_float_window_size` | 悬浮窗图标尺寸 |
| 120dp | `shared_dp_loading_size` | 加载弹窗边长 |
| 312dp | `shared_dp_dialog_width` | 对话框宽度（= 360dp 基准屏 − 2×24dp 边距） |
| 200dp | `shared_dp_dialog_height` | 对话框高度 |
| 328dp | `shared_dp_button_width` | 标准按钮宽度 |
| 48dp | `shared_dp_button_height` | 标准按钮高度 |
| 56dp | `shared_dp_item_height` | 标准列表项高度 |
| 1dp | `shared_dp_divider` | 分割线 |
| 1080dp | `shared_dp_fragment_height` | Fragment 内容高度 |

### 文字大小（sp）

| 尺寸 | 资源名 | 用途 |
|------|--------|------|
| 12sp | `shared_sp_12` | Caption、Tab 文字、辅助文字 |
| 14sp | `shared_sp_14` | Body2、列表次要文字 |
| 16sp | `shared_sp_16` | Body1、页面正文、对话框标题 |
| 24sp | `shared_sp_24` | H6、页面标题 |

### 图标大小（dp）

图标尺寸在各 drawable XML 中定义，不在 dimens.xml 中统一管理。

| 尺寸 | 用途 |
|------|------|
| 18dp | 小图标（辅助操作） |
| 24dp | 标准图标（BottomNav、Toolbar、列表图标） |
| 32dp | 中等图标（空状态图标） |
| 40dp | 大图标（头像占位） |
| 48dp | 最小触摸区域 |

### 圆角（dp）

| 尺寸 | 资源名 | 用途 |
|------|--------|------|
| 4dp | `shared_dp_corner_4` | 小按钮、Chip、标签 |
| 8dp | `shared_dp_corner_8` | 卡片、对话框、加载弹窗背景、输入框 |
| 12dp | `shared_dp_corner_12` | 大卡片、底部弹窗 |
| 16dp | `shared_dp_corner_16` | 浮动按钮、大圆角容器 |
| 28dp | `shared_dp_corner_28` | 完全圆角（FAB、胶囊按钮） |

## 使用规范

1. **间距**：优先使用 `shared_dp_*` 资源，避免硬编码 dp 值
2. **文字**：优先使用 `shared_sp_*` 资源，确保用户可调整字体大小
3. **圆角**：优先使用 `shared_dp_corner_*` 资源，保持圆角一致性
4. **图标**：在 drawable XML 中定义尺寸，不使用 dimens 资源
5. **自定义尺寸**：如需非标准尺寸，在 dimens.xml 中添加并遵循 `shared_dp_*` 或 `shared_sp_*` 命名

## 参考

- [Material Design - Layout](https://m3.material.io/foundations/layout/applying-layout)
- [Material Design - Typography](https://m3.material.io/styles/typography)
- [Material Design - Shapes](https://m3.material.io/styles/shape)
