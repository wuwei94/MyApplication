package com.example.william.my.basic.basic_repo.sync

import com.example.william.my.basic.basic_repo.sync.model.ChangeListVersions

/**
 * 单元测试专用同步器调度替身（Test Double）
 */
class TestSynchronizer : Synchronizer {

    private var versions = ChangeListVersions()

    override suspend fun getChangeListVersions(): ChangeListVersions = versions

    override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
        versions = versions.update()
    }
}
