package com.example.william.my.module.jetpack.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.jetpack.room.OAuth
import com.example.william.my.module.jetpack.room.OAuthDao
import com.example.william.my.module.jetpack.room.OAuthDataBase
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Room — 数据库持久化框架
 *
 * Room 是 Android Jetpack 提供的数据库持久化框架，在 SQLite 之上提供抽象层。
 *
 * 核心特性：
 * 1. 编译时验证：在编译时验证 SQL 语句，减少运行时错误
 * 2. 注解驱动：使用注解定义数据库结构，简化代码
 * 3. LiveData/Flow 支持：支持响应式查询，数据变化自动通知
 * 4. 迁移支持：支持数据库版本迁移，避免数据丢失
 *
 * 核心组件：
 * 1. @Entity：定义数据库表结构
 * 2. @Dao：定义数据访问对象，包含查询方法
 * 3. @Database：定义数据库，包含版本号和实体列表
 *
 * 基本用法：
 * ```kotlin
 * // 定义实体
 * @Entity
 * data class User(
 *     @PrimaryKey val uid: Int,
 *     @ColumnInfo(name = "first_name") val firstName: String?
 * )
 *
 * // 定义 DAO
 * @Dao
 * interface UserDao {
 *     @Query("SELECT * FROM user")
 *     fun getAll(): List<User>
 *
 *     @Insert
 *     fun insertAll(vararg users: User)
 * }
 *
 * // 定义数据库
 * @Database(entities = [User::class], version = 1)
 * abstract class AppDatabase : RoomDatabase() {
 *     abstract fun userDao(): UserDao
 * }
 * ```
 *
 * 适用场景：
 * - 本地数据持久化
 * - 离线数据缓存
 * - 复杂数据查询
 *
 * https://developer.android.google.cn/jetpack/androidx/releases/room
 */
@Route(path = RouterPath.Jetpack.Room)
class RoomActivity : BasicResponseActivity() {

    private val mOAuthDao: OAuthDao by lazy {
        OAuthDataBase.getInstance(this).getOAuthDao()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项操作 Room 数据库")
        showOAuth()
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("插入 OAuth 数据", "清空 OAuth 数据")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> addOAuth()
            1 -> clearOAuth()
        }
    }

    private fun addOAuth() {
        /*
         * query ,Insert ,Update ,需要在后台线程执行
         */
        Executors.newSingleThreadExecutor().execute {
            val oAuth = OAuth()
            mOAuthDao.insertOAuth(oAuth)
            appendLog("插入 OAuth 数据成功")
        }
    }

    private fun clearOAuth() {
        Executors.newSingleThreadExecutor().execute {
            mOAuthDao.deleteAllOAuth()
            appendLog("清空 OAuth 数据成功")
        }
    }

    private fun showOAuth() {
        lifecycleScope.launch {
            mOAuthDao.getAllOAuthFlow().collect { list: List<OAuth> ->
                if (list.isEmpty()) {
                    appendLog("当前 Room 无数据")
                } else {
                    list.forEach { oauth ->
                        appendLog(Gson().toJson(oauth))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mOAuthDao.deleteAllOAuth()
    }
}