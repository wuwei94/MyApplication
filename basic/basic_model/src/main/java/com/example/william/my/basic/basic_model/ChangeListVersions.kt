package com.example.william.my.basic.basic_model

/**
 * 增量同步版本游标（ChangeList Versions）
 *
 * 记录各业务领域最后一次增量同步拉取的版本号或时间戳游标。
 * 遵循 Google Now in Android 设计，增量拉取时仅拉取服务端版本号高于此游标的数据变更，
 * 极大节省网络流量与后台电量。
 *
 * @param articleVersion 文章模块当前已同步的最高版本游标（默认 0，表示尚未进行增量同步）
 */
data class ChangeListVersions(
    val articleVersion: Int = 0,
)
