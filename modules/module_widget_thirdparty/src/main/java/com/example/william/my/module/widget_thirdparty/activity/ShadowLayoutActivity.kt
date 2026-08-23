package com.example.william.my.module.widget_thirdparty.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityShadowLayoutBinding

/**
 * ShadowLayout — 阴影布局控件
 *
 * ShadowLayout 是一个轻量级的阴影布局库，支持自定义阴影效果。
 *
 * 核心特性：
 * 1. 简单易用：一行代码添加阴影效果
 * 2. 丰富的自定义：支持自定义阴影颜色、半径、偏移量
 * 3. 性能优秀：使用 Canvas 绘制，性能优于 CardView
 * 4. 兼容性好：支持 Android 4.0+
 *
 * 基本用法：
 * ```kotlin
 * // XML 中使用
 * <com.lihang.ShadowLayout
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:sl_shadowRadius="10dp"
 *     app:sl_shadowColor="#33000000"
 *     app:sl_dx="0dp"
 *     app:sl_dy="5dp">
 *     <!-- 子布局 -->
 * </com.lihang.ShadowLayout>
 * ```
 *
 * 适用场景：
 * - 卡片式布局、列表项
 * - 按钮、输入框阴影
 * - 需要阴影效果的任何场景
 *
 * https://github.com/lihangleo2/ShadowLayout
 */
@Route(path = RouterPath.WidgetThirdparty.ShadowLayout)
class ShadowLayoutActivity : BaseVBActivity<WidgetThirdpartyActivityShadowLayoutBinding>() {

    override fun getViewBinding(): WidgetThirdpartyActivityShadowLayoutBinding {
        return WidgetThirdpartyActivityShadowLayoutBinding.inflate(layoutInflater)
    }
}
