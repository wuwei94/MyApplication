package com.example.william.my.module.compose.activity.smartrefresh

/**
 * 刷新状态数据
 *
 * 封装下拉刷新与加载更多的状态。
 */
data class SmartRefreshStateData(
    val data: MutableList<TopicModel> = arrayListOf(),
    val isLoadMore: Boolean = false,
    val flag: Boolean = true,
)
