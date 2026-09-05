package com.example.william.my.module.kotlin.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlin.math.hypot

/**
 * Kotlin 现代核心语法与类型安全 DSL
 *
 * 演示特性：
 * 1. 操作符重载（Operator Overloading）：+、*、[]、in、invoke 等预定义运算符重载
 * 2. 中缀表达式（Infix Functions）：省略点号和括号的自然语言风格方法调用
 * 3. 解构声明（Destructuring Declarations）：Data class 与自定义 componentN() 解构
 * 4. 密封接口与模式匹配（Sealed Interface）：类型安全层次与 when 表达式穷举
 * 5. 类型安全 DSL 构建器（Type-Safe Builders）：带接收者的 Lambda（T.() -> Unit）
 *
 * https://kotlinlang.org/docs/operator-overloading.html
 * https://kotlinlang.org/docs/type-safe-builders.html
 */
@Route(path = RouterPath.Kotlin.Syntax)
class MySyntaxActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Kotlin 现代语法：操作符重载、中缀函数、解构声明、密封接口与 DSL 构建器")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 操作符重载（+ / * / [] / in / invoke）",
        "2. 中缀函数（infix fun）",
        "3. 解构声明（Data Class & componentN）",
        "4. 密封接口与模式匹配（Sealed Interface & when）",
        "5. 类型安全 DSL 构建器（Type-Safe Builder）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> testOperatorOverloading()
            1 -> testInfixFunctions()
            2 -> testDestructuring()
            3 -> testSealedInterface()
            4 -> testDslBuilder()
        }
    }

    // ─────────────────────────────────────────────
    // 1. 操作符重载 (Operator Overloading)
    // ─────────────────────────────────────────────
    data class Point(val x: Int, val y: Int) {
        // 重载 + 运算符 (p1 + p2)
        operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)

        // 重载 * 运算符 (p * scale)
        operator fun times(scale: Int): Point = Point(x * scale, y * scale)

        // 重载 [] 索引访问 (p[0], p[1])
        operator fun get(index: Int): Int = when (index) {
            0 -> x
            1 -> y
            else -> throw IndexOutOfBoundsException("Invalid index $index for Point")
        }

        // 重载 in 包含判断 (value in point)
        operator fun contains(value: Int): Boolean = (x == value || y == value)

        // 重载 () 函数调用 (point("Prefix"))
        operator fun invoke(prefix: String): String = "$prefix: Point($x, $y)"
    }

    private fun testOperatorOverloading() {
        val p1 = Point(3, 4)
        val p2 = Point(5, 6)

        val plusResult = p1 + p2
        appendLog("【操作符 +】Point(3,4) + Point(5,6) = $plusResult")

        val timesResult = p1 * 3
        appendLog("【操作符 *】Point(3,4) * 3 = $timesResult")

        appendLog("【操作符 []】p1[0]=${p1[0]}, p1[1]=${p1[1]}")
        appendLog("【操作符 in】3 in p1: ${3 in p1}, 99 in p1: ${99 in p1}")
        appendLog("【操作符 ()】调用: ${p1("当前坐标")}")
    }

    // ─────────────────────────────────────────────
    // 2. 中缀函数 (Infix Functions)
    // ─────────────────────────────────────────────
    private infix fun Point.distanceTo(other: Point): Double = hypot((this.x - other.x).toDouble(), (this.y - other.y).toDouble())

    private infix fun String.combineWith(other: String): String = "$this ⇄ $other"

    private fun testInfixFunctions() {
        val p1 = Point(0, 0)
        val p2 = Point(3, 4)

        // 中缀调用：省略点号和圆括号
        val distance = p1 distanceTo p2
        appendLog("【中缀函数】p1 distanceTo p2 = $distance")

        val message = "Kotlin" combineWith "Android Architecture"
        appendLog("【中缀函数】'Kotlin' combineWith 'Android Architecture' = $message")
    }

    // ─────────────────────────────────────────────
    // 3. 解构声明 (Destructuring Declarations)
    // ─────────────────────────────────────────────
    data class Developer(val name: String, val age: Int, val role: String)

    // 非 data class 通过实现 operator fun componentN 支持解构
    class Coordinates(val latitude: Double, val longitude: Double) {
        operator fun component1(): Double = latitude
        operator fun component2(): Double = longitude
    }

    private fun testDestructuring() {
        // Data Class 解构
        val dev = Developer("Alice", 28, "Android Architect")
        val (name, age, role) = dev
        appendLog("【DataClass 解构】name=$name, age=$age, role=$role")

        // 自定义 componentN 解构
        val coord = Coordinates(39.9042, 116.4074)
        val (lat, lng) = coord
        appendLog("【自定义类解构】纬度=$lat, 经度=$lng")

        // Map 遍历解构
        val map = mapOf("Kotlin" to 2.0, "Coroutines" to 1.8)
        for ((lang, ver) in map) {
            appendLog("【Map 遍历解构】$lang -> v$ver")
        }
    }

    // ─────────────────────────────────────────────
    // 4. 密封接口与模式匹配 (Sealed Interface & when)
    // ─────────────────────────────────────────────
    sealed interface UiResult<out T> {
        object Idle : UiResult<Nothing>
        object Loading : UiResult<Nothing>
        data class Success<T>(val data: T) : UiResult<T>
        data class Error(val message: String) : UiResult<Nothing>
    }

    private fun renderResult(result: UiResult<String>): String {
        // 密封接口让 when 具有编译器穷举检查能力，无需 else 分支
        return when (result) {
            is UiResult.Idle -> "状态: 空闲 (Idle)"
            is UiResult.Loading -> "状态: 加载中 (Loading...)"
            is UiResult.Success -> "状态: 成功 (Success) -> 数据: ${result.data}"
            is UiResult.Error -> "状态: 失败 (Error) -> 原因: ${result.message}"
        }
    }

    private fun testSealedInterface() {
        val states = listOf(
            UiResult.Idle,
            UiResult.Loading,
            UiResult.Success("用户信息同步成功"),
            UiResult.Error("HTTP 500 服务器错误"),
        )
        states.forEach { state ->
            appendLog("【Sealed Interface】${renderResult(state)}")
        }
    }

    // ─────────────────────────────────────────────
    // 5. 类型安全 DSL 构建器 (Type-Safe Builder)
    // ─────────────────────────────────────────────
    @DslMarker
    annotation class LayoutDsl

    @LayoutDsl
    class MenuItemBuilder {
        var name: String = ""
        var route: String = ""

        fun build(): String = "  - [条目] $name (路由: $route)"
    }

    @LayoutDsl
    class MenuBuilder {
        var title: String = ""
        private val items = mutableListOf<String>()

        fun item(block: MenuItemBuilder.() -> Unit) {
            val itemBuilder = MenuItemBuilder().apply(block)
            items.add(itemBuilder.build())
        }

        fun build(): String = buildString {
            appendLine("【DSL 菜单】$title")
            items.forEach { appendLine(it) }
        }.trimEnd()
    }

    // DSL 入口函数，接收带有 MenuBuilder 接收者的 Lambda
    private fun menu(init: MenuBuilder.() -> Unit): MenuBuilder = MenuBuilder().apply(init)

    private fun testDslBuilder() {
        val menuConfig = menu {
            title = "Kotlin 特性导航菜单"
            item {
                name = "协程并发"
                route = "/Kotlin/Coroutines"
            }
            item {
                name = "Flow 数据流"
                route = "/Kotlin/Flow"
            }
            item {
                name = "现代语法与 DSL"
                route = "/Kotlin/Syntax"
            }
        }.build()

        appendLog(menuConfig)
    }
}
