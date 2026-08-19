package com.example.william.my.module.kotlin.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.google.gson.Gson

/**
 * Kotlin 内联函数（Inline Functions）与作用域函数演示
 *
 * 1. 作用域函数（Scope Functions）：
 *    - `with(T)`：非扩展，接收者为 `this`，返回 Lambda 结果。
 *    - `T.let`：扩展函数，接收者为参数 `it`，返回 Lambda 结果，常用空安全调用。
 *    - `T.run`：扩展函数，接收者为 `this`，返回 Lambda 结果。
 *    - `T.also`：扩展函数，接收者为参数 `it`，返回对象本身 `this`，常用于附加链式操作。
 *    - `T.apply`：扩展函数，接收者为 `this`，返回对象本身 `this`，常用于对象初始化配置。
 * 2. 泛型实化（reified）：在 `inline` 函数中使用 `reified T` 绕过 JVM 泛型擦除，直接获取 `T::class.java`。
 * 3. 自定义内联扩展函数：演示高阶函数内联消除 Lambda 对象分配开销。
 */
@Route(path = RouterPath.Kotlin.Inline)
class MyInlineActivity : BasicResponseActivity() {

    data class UserData(var name: String = "", var score: Int = 0) {
        fun toJson(): String = Gson().toJson(this)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Kotlin 内联函数、作用域函数与泛型实化 (reified)")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "作用域函数对比（with / let / run / also / apply）",
            "泛型实化类型获取（inline + reified）",
            "自定义内联扩展函数（mAlso / mApply / mStandard）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> testScopeFunctions()
            1 -> testReifiedGenerics()
            2 -> testCustomInlineExtensions()
        }
    }

    // ─────────────────────────────────────────────
    // 1. 作用域函数对比
    // ─────────────────────────────────────────────
    private fun testScopeFunctions() {
        val user = UserData("InitUser", 60)

        // with
        val withResult = with(user) {
            name = "WithUser"
            "with 返回: name=$name"
        }
        appendLog("【with】$withResult")

        // let
        val letResult = user.let {
            it.score = 70
            "let 返回: score=${it.score}"
        }
        appendLog("【let】$letResult")

        // run
        val runResult = user.run {
            score = 80
            "run 返回: ${toJson()}"
        }
        appendLog("【run】$runResult")

        // also
        val alsoResult = user.also {
            it.name = "AlsoUser"
        }
        appendLog("【also】返回对象本身: ${alsoResult.toJson()}")

        // apply
        val applyResult = user.apply {
            name = "ApplyUser"
            score = 100
        }
        appendLog("【apply】返回对象本身: ${applyResult.toJson()}")
    }

    // ─────────────────────────────────────────────
    // 2. 泛型实化 (reified)
    // ─────────────────────────────────────────────
    private inline fun <reified T> getTypeName(): String {
        return "类型名称: ${T::class.java.simpleName}, 全限定名: ${T::class.java.name}"
    }

    private inline fun <reified T> parseJson(json: String): T {
        return Gson().fromJson(json, T::class.java)
    }

    private fun testReifiedGenerics() {
        appendLog("【reified】${getTypeName<UserData>()}")
        appendLog("【reified】${getTypeName<String>()}")

        val json = """{"name":"ReifiedUser","score":95}"""
        val parsedUser: UserData = parseJson(json)
        appendLog("【reified 解析】name=${parsedUser.name}, score=${parsedUser.score}")
    }

    // ─────────────────────────────────────────────
    // 3. 自定义内联扩展函数
    // ─────────────────────────────────────────────
    private inline fun <T> T.mAlso(block: (T) -> Unit): T {
        block(this)
        return this
    }

    private inline fun <T> T.mApply(block: T.() -> Unit): T {
        block()
        return this
    }

    private inline fun <T, R> T.mRun(block: T.() -> R): R {
        return block()
    }

    private fun testCustomInlineExtensions() {
        val user = UserData("CustomUser", 50)
            .mApply {
                score = 88
            }.mAlso {
                it.name = "CustomUserUpdated"
            }

        val summary = user.mRun {
            "【自定义内联】结果: ${toJson()}"
        }
        appendLog(summary)
    }
}