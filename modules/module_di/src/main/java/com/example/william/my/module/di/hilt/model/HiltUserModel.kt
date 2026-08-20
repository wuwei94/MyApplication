package com.example.william.my.module.di.hilt.model

import javax.inject.Inject

/**
 * 模拟用户数据库访问层
 */
class HiltUserDatabase @Inject constructor() {
    fun queryUser(id: Long): String = "User(id=$id, name='Developer_Hilt')"
}

/**
 * 模拟用户仓库层，依赖 HiltUserDatabase
 */
class HiltUserRepository @Inject constructor(
    private val database: HiltUserDatabase
) {
    fun getUserInfo(id: Long): String = "UserRepository 查询结果 -> ${database.queryUser(id)}"
}
