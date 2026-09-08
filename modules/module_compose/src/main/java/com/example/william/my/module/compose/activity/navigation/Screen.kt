package com.example.william.my.module.compose.activity.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * 强类型底部导航 Tab 契约 (Type-Safe TopLevel Destination)
 *
 * 遵循 Android 官方 Material 3 底部导航规范：
 * 采用强类型 `@Serializable` 对象表示各个一级 Tab，支持独立 Multi-BackStack 返回栈状态保存与恢复。
 */
sealed interface TopLevelDestination {

    val label: String
    val icon: ImageVector

    @Serializable
    data object Home : TopLevelDestination {
        override val label: String = "首页"
        override val icon: ImageVector = Icons.Filled.Home
    }

    @Serializable
    data object Discover : TopLevelDestination {
        override val label: String = "发现"
        override val icon: ImageVector = Icons.Filled.Explore
    }

    @Serializable
    data object Profile : TopLevelDestination {
        override val label: String = "我的"
        override val icon: ImageVector = Icons.Filled.Person
    }
}

/**
 * 各 Tab 内部的强类型二级详情页路由
 *
 * 用于演示在 Tab 内部入栈后，切换到底部其他 Tab 再切回，原栈状态与二级页面依然保留的 Multi-BackStack 特性。
 */
@Serializable
data class SubDetailRoute(
    val fromTab: String,
    val itemId: Int,
    val itemTitle: String,
)
