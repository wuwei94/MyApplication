package com.example.william.my.module.database.room.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.database.room.data.OAuth
import com.example.william.my.module.database.room.data.OAuthDao
import com.example.william.my.module.database.room.data.OAuthDataBase
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Room — Jetpack 在 SQLite 之上的类型安全持久化框架
 *
 * 核心机制与避坑点：
 * 1. 编译期校验：@Entity / @Dao / @Database 在编译时校验 SQL 与实体映射；schema 变更必须递增 version 并提供 Migration
 * 2. 主线程限制：默认禁止主线程查询，需在 Dispatchers.IO / Rx Schedulers.io 上执行；allowMainThreadQueries 仅用于调试
 * 3. 响应式查询：DAO 返回 Flow 时表变更自动重发；RxJava 返回类型（Single/Maybe/Flowable）需自行管理 Disposable
 * 4. 事务：insertAll 等批量写入在 DAO 事务内完成，失败时整体回滚
 *
 * 官方参考：
 * https://developer.android.com/training/data-storage/room
 */
@Route(path = RouterPath.Database.Room)
class RoomActivity : BasicResponseActivity() {

    private val oauthDao: OAuthDao by lazy {
        OAuthDataBase.getInstance(applicationContext).getOAuthDao()
    }

    private val disposables = CompositeDisposable()
    private var lastInsertedId: Long = 0L

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "Room 示例（与 ObjectBox 平行轴：插入 / 批量 / 更新 / 查询 / 观察 / 清理）",
        )
        observeOAuthFlow()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 插入单条数据 (Insert Single)",
        "2. 批量插入数据 (Insert Batch)",
        "3. 更新最近一条数据 (Update)",
        "4. 查询数据 (Query by ID / 全表)",
        "5. Flow 响应式观察 (表变更自动上屏)",
        "6. RxJava Single 异步查询 (Rx Query，库特有)",
        "7. 清空数据库 (Delete All)",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> addSingleOAuth()
            1 -> addBatchOAuth()
            2 -> updateLatestOAuth()
            3 -> queryOAuthById()
            4 -> readFlowOnce()
            5 -> queryOAuthByRxSingle()
            6 -> clearOAuth()
        }
    }

    /**
     * 响应式 Flow 查询：表变更自动重发并上屏
     */
    private fun observeOAuthFlow() {
        lifecycleScope.launch {
            oauthDao.getAllOAuthFlow().collect { list: List<OAuth> ->
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

    private fun readFlowOnce() {
        appendLog("→ 读取 Room Flow 当前快照...")
        lifecycleScope.launch(Dispatchers.IO) {
            val list = oauthDao.getAllOAuthFlow().first()
            withContext(Dispatchers.Main) {
                appendLog("✓ [Flow first] 当前共 ${list.size} 条记录")
            }
        }
    }

    private fun addSingleOAuth() {
        appendLog("→ 插入单条 OAuth...")
        lifecycleScope.launch(Dispatchers.IO) {
            val oAuth = OAuth(
                refreshToken = "Token_${System.currentTimeMillis() % 10000}",
                expires = 3600,
            )
            val newId = oauthDao.insertOAuth(oAuth)
            lastInsertedId = newId
            withContext(Dispatchers.Main) {
                appendLog("✓ 插入单条数据成功，生成的 ID: $newId")
            }
        }
    }

    private fun addBatchOAuth() {
        appendLog("→ 批量插入 3 条 OAuth...")
        lifecycleScope.launch(Dispatchers.IO) {
            val list = Array(3) { index ->
                OAuth(
                    refreshToken = "Batch_Token_${index}_${System.currentTimeMillis() % 1000}",
                    expires = 7200,
                )
            }
            oauthDao.insertAll(*list)
            withContext(Dispatchers.Main) {
                appendLog("✓ 批量插入 3 条数据完成")
            }
        }
    }

    private fun updateLatestOAuth() {
        appendLog("→ 更新最近一条 OAuth...")
        lifecycleScope.launch(Dispatchers.IO) {
            if (lastInsertedId == 0L) {
                withContext(Dispatchers.Main) {
                    appendLog("✗ 尚未插入数据，请先插入一条数据")
                }
                return@launch
            }
            val current = oauthDao.getUserById(lastInsertedId)
            if (current != null) {
                val updated = current.copy(
                    refreshToken = "Updated_${System.currentTimeMillis() % 10000}",
                    expires = 9999,
                )
                oauthDao.updateOAuth(updated)
                withContext(Dispatchers.Main) {
                    appendLog("✓ 已更新 ID=$lastInsertedId 的数据为: ${updated.refreshToken}")
                }
            }
        }
    }

    private fun queryOAuthById() {
        appendLog("→ 根据 ID 查询 OAuth...")
        lifecycleScope.launch(Dispatchers.IO) {
            if (lastInsertedId == 0L) {
                withContext(Dispatchers.Main) {
                    appendLog("✗ 尚未记录有效 ID，请先插入数据")
                }
                return@launch
            }
            val result = oauthDao.getUserById(lastInsertedId)
            withContext(Dispatchers.Main) {
                appendLog("✓ 协程根据 ID=$lastInsertedId 查询结果: ${Gson().toJson(result)}")
            }
        }
    }

    private fun queryOAuthByRxSingle() {
        if (lastInsertedId == 0L) {
            appendLog("✗ 尚未记录有效 ID，请先插入数据")
            return
        }
        appendLog("→ RxJava Single 查询 OAuth...")
        val d = oauthDao.getUserSingle(lastInsertedId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { oauth ->
                    appendLog("✓ RxJava Single 查询成功: ID=${oauth.id}, Token=${oauth.refreshToken}")
                },
                { error ->
                    appendLog("✗ RxJava Single 查询失败: ${error.localizedMessage}")
                },
            )
        disposables.add(d)
    }

    private fun clearOAuth() {
        appendLog("→ 清空 Room 数据表...")
        lifecycleScope.launch(Dispatchers.IO) {
            oauthDao.deleteAllOAuth()
            lastInsertedId = 0L
            withContext(Dispatchers.Main) {
                appendLog("✓ 已清空 Room 数据表")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
