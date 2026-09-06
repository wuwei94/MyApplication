package com.example.william.my.module.compose.activity.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 导航路由定义
 *
 * 封装底部导航各页面的路由、标题与图标。
 */
sealed class Screen(val route: String, val resourceId: String, val icon: ImageVector) {
    /**
     * 首页路由
     */
    data object Home : Screen("home", "Home", Icons.Filled.Home)

    /**
     * 个人页路由
     */
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}
