package com.example.william.my.module.compose.activity.smartrefresh

import androidx.annotation.DrawableRes

/**
 * 话题模型
 *
 * 刷新列表中的话题数据模型。
 */
data class TopicModel(val title: String, @DrawableRes val icon: Int)
