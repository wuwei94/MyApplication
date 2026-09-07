package com.example.william.my.basic.basic_repo.sync

import com.example.william.my.basic.basic_repo.sync.model.ChangeListVersions

/**
 * 高阶同步调度契约接口（对齐 Google Now in Android 同步架构）
 *
 * 负责为 [Syncable] 提供增量游标查询与更新，并协调数据仓库执行安全的后台同步。
 */
interface Synchronizer {

    /**
     * 获取当前持久化的各模块版本游标
     */
    suspend fun getChangeListVersions(): ChangeListVersions

    /**
     * 更新持久化的版本游标
     */
    suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions)

    /**
     * 针对 [Syncable] 数据源发起同步调度的扩展方法
     *
     * @return true 表示同步成功或当前版本已是最新无需同步（幂等成功）；false 表示同步遇到异常
     */
    suspend fun Syncable.sync(): Boolean = syncWith(this@Synchronizer)
}

/**
 * 可同步数据仓库接口（Syncable）
 *
 * 任何支持离线优先后台增量同步的 Repository 均应实现此接口。
 */
interface Syncable {

    /**
     * 配合给定的 [synchronizer] 调度器执行具体的版本拉取与本地 Room 写入
     *
     * @param synchronizer 注入的同步调度器
     * @return true 表示同步成功（幂等），false 表示同步失败
     */
    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}
