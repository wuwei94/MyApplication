package com.example.william.my.module.database.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.database.room.OAuth
import com.example.william.my.module.database.room.OAuthDao
import com.example.william.my.module.database.room.OAuthDataBase
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Room — 数据库持久化框架
 *
 * Room 是 Android Jetpack 提供的数据库持久化框架，在 SQLite 之上提供类型安全与响应式抽象层。
 *
 * 核心特性：
 * 1. 编译时验证：在编译时验证 SQL 语法与实体映射，减少运行时错误
 * 2. 注解驱动：使用注解定义数据库结构，简化代码
 * 3. 协程 & Flow 支持：DAO 挂起函数与 Flow 响应式查询数据驱动 UI
 * 4. RxJava 支持：原生支持 Single、Maybe、Flowable 响应式流
 * 5. 事务支持：RoomDatabase.withTransaction 支持多表原子事务
 *
 * 核心组件：
 * 1. @Entity：定义数据库表结构
 * 2. @Dao：定义数据访问对象，包含增删改查方法
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
 *     suspend fun insertAll(vararg users: User)
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
 * - 复杂数据查询与响应式数据流
 *
 * https://developer.android.google.cn/jetpack/androidx/releases/room
 */
@Route(path = RouterPath.Database.Room)
class RoomActivity : BasicResponseActivity() {

    private val mOAuthDao: OAuthDao by lazy {
        OAuthDataBase.getInstance(applicationContext).getOAuthDao()
    }

    private val disposables = CompositeDisposable()
    private var lastInsertedId: Long = 0L

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项演示 Room 数据库 CRUD 与 Flow/RxJava 响应式查询")
        observeOAuthFlow()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "插入单条数据 (Insert Single)",
        "批量插入数据 (Insert Batch)",
        "更新最近一条数据 (Update)",
        "根据 ID 查询数据 (Query by ID)",
        "RxJava Single 异步查询 (Rx Query)",
        "清空数据库 (Delete All)",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> addSingleOAuth()
            1 -> addBatchOAuth()
            2 -> updateLatestOAuth()
            3 -> queryOAuthById()
            4 -> queryOAuthByRxSingle()
            5 -> clearOAuth()
        }
    }

    /**
     * 1. 响应式 Flow 查询：监听数据表全量变化并实时输出
     */
    private fun observeOAuthFlow() {
        lifecycleScope.launch {
            mOAuthDao.getAllOAuthFlow().collect { list: List<OAuth> ->
                if (list.isEmpty()) {
                    appendLog("[Room Flow 监听] 当前数据库无数据")
                } else {
                    appendLog("[Room Flow 监听] 数据库记录更新（共 ${list.size} 条）：")
                    list.take(3).forEach { oauth ->
                        appendLog("  -> ID=${oauth.id}, Token=${oauth.refreshToken}, Expires=${oauth.expires}s")
                    }
                    if (list.size > 3) {
                        appendLog("  -> ... 更多省略")
                    }
                }
            }
        }
    }

    /**
     * 2. 插入单条记录
     */
    private fun addSingleOAuth() {
        lifecycleScope.launch(Dispatchers.IO) {
            val oAuth = OAuth(refreshToken = "Token_${System.currentTimeMillis() % 10000}", expires = 3600)
            val newId = mOAuthDao.insertOAuth(oAuth)
            lastInsertedId = newId
            withContext(Dispatchers.Main) {
                appendLog("插入单条数据成功，生成的 ID: $newId")
            }
        }
    }

    /**
     * 3. 批量插入多条记录
     */
    private fun addBatchOAuth() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = Array(3) { index ->
                OAuth(refreshToken = "Batch_Token_${index}_${System.currentTimeMillis() % 1000}", expires = 7200)
            }
            mOAuthDao.insertAll(*list)
            withContext(Dispatchers.Main) {
                appendLog("批量插入 3 条数据完成")
            }
        }
    }

    /**
     * 4. 更新最新一条数据
     */
    private fun updateLatestOAuth() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (lastInsertedId == 0L) {
                withContext(Dispatchers.Main) {
                    appendLog("尚未插入数据，请先插入一条数据")
                }
                return@launch
            }
            val current = mOAuthDao.getUserById(lastInsertedId)
            if (current != null) {
                val updated = current.copy(refreshToken = "Updated_${System.currentTimeMillis() % 10000}", expires = 9999)
                mOAuthDao.updateOAuth(updated)
                withContext(Dispatchers.Main) {
                    appendLog("已更新 ID=$lastInsertedId 的数据为: ${updated.refreshToken}")
                }
            }
        }
    }

    /**
     * 5. 协程查询单条数据
     */
    private fun queryOAuthById() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (lastInsertedId == 0L) {
                withContext(Dispatchers.Main) {
                    appendLog("尚未记录有效 ID，请先插入数据")
                }
                return@launch
            }
            val result = mOAuthDao.getUserById(lastInsertedId)
            withContext(Dispatchers.Main) {
                appendLog("协程根据 ID=$lastInsertedId 查询结果: ${Gson().toJson(result)}")
            }
        }
    }

    /**
     * 6. RxJava Single 响应式查询
     */
    private fun queryOAuthByRxSingle() {
        if (lastInsertedId == 0L) {
            appendLog("尚未记录有效 ID，请先插入数据")
            return
        }
        val d = mOAuthDao.getUserSingle(lastInsertedId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ oauth ->
                appendLog("RxJava Single 查询成功: ID=${oauth.id}, Token=${oauth.refreshToken}")
            }, { error ->
                appendLog("RxJava Single 查询失败: ${error.localizedMessage}")
            })
        disposables.add(d)
    }

    /**
     * 7. 清空全部数据
     */
    private fun clearOAuth() {
        lifecycleScope.launch(Dispatchers.IO) {
            mOAuthDao.deleteAllOAuth()
            lastInsertedId = 0L
            withContext(Dispatchers.Main) {
                appendLog("已清空 Room 数据表")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
