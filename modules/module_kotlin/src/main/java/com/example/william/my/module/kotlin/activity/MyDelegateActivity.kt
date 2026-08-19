package com.example.william.my.module.kotlin.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Kotlin 委托机制
 *
 * 委托是 Kotlin 的重要特性，通过 by 关键字实现代码复用和解耦。
 *
 * 类委托（Class Delegation）：
 * 通过 by 关键字将接口实现委托给内部实例，装饰模式语法糖。
 *
 * 属性委托（Property Delegation）：
 * 通过 ReadOnlyProperty 或 ReadWriteProperty 拦截属性的 getValue / setValue。
 *
 * 标准库委托：
 * 1. by lazy：延迟初始化（线程安全默认使用 LazyThreadSafetyMode.SYNCHRONIZED）
 * 2. Delegates.observable：监听属性变更并接收新旧值
 * 3. Delegates.vetoable：条件拦截，通过布尔返回值决定是否接受新值
 * 4. Map 映射委托：通过 val property: Type by map 将属性名作为 Key 直接从 Map 读取
 *
 * 基本用法：
 * ```kotlin
 * // 类委托
 * interface Printer { fun print(): String }
 * class RealPrinter : Printer { override fun print() = "Hello" }
 * class DelegatedPrinter(printer: Printer) : Printer by printer
 *
 * // 属性委托
 * val lazyValue: String by lazy { "Computed" }
 * var observedValue: String by Delegates.observable("Initial") { _, old, new ->
 *     println("$old -> $new")
 * }
 * ```
 *
 * 适用场景：
 * - 装饰模式、代理模式
 * - 延迟初始化、属性监听
 * - 配置管理、数据映射
 */
@Route(path = RouterPath.Kotlin.Delegate)
class MyDelegateActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Kotlin 委托机制：类委托与属性委托")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "类委托（Class Delegation: by base）",
            "自定义属性委托（ReadWriteProperty）",
            "延迟属性委托（by lazy）",
            "可观察属性（Delegates.observable）",
            "可否决属性（Delegates.vetoable）",
            "Map 映射属性委托（by map）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> testClassDelegate()
            1 -> testAttrDelegate()
            2 -> testLazyDelegate()
            3 -> testObservableDelegate()
            4 -> testVetoableDelegate()
            5 -> testMapDelegate()
        }
    }

    // ─────────────────────────────────────────────
    // 1. 类委托
    // ─────────────────────────────────────────────
    interface Printer {
        fun printMessage(): String
    }

    class RealPrinter : Printer {
        override fun printMessage(): String = "RealPrinter.printMessage() 执行"
    }

    class DelegatedPrinter(printer: Printer) : Printer by printer

    private fun testClassDelegate() {
        val printer = DelegatedPrinter(RealPrinter())
        appendLog("【类委托】调用结果: ${printer.printMessage()}")
    }

    // ─────────────────────────────────────────────
    // 2. 自定义属性委托
    // ─────────────────────────────────────────────
    class StringDelegate : ReadWriteProperty<Any?, String> {
        private var innerValue = "DefaultValue"

        override fun getValue(thisRef: Any?, property: KProperty<*>): String {
            return innerValue
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            innerValue = value
        }
    }

    class CustomDelegateHolder {
        var text: String by StringDelegate()
    }

    private fun testAttrDelegate() {
        val holder = CustomDelegateHolder()
        appendLog("【属性委托】读取初始值: ${holder.text}")
        holder.text = "NewDelegateValue"
        appendLog("【属性委托】赋值后读取: ${holder.text}")
    }

    // ─────────────────────────────────────────────
    // 3. 延迟属性委托 by lazy
    // ─────────────────────────────────────────────
    private var lazyInitCount = 0
    private val lazyValue: String by lazy {
        lazyInitCount++
        "LazyComputedResult(count=$lazyInitCount)"
    }

    private fun testLazyDelegate() {
        appendLog("【Lazy委托】第 1 次访问: $lazyValue")
        appendLog("【Lazy委托】第 2 次访问: $lazyValue（未重复初始化，计算次数: $lazyInitCount）")
    }

    // ─────────────────────────────────────────────
    // 4. 可观察属性 Delegates.observable
    // ─────────────────────────────────────────────
    class ObservableUser(private val logCallback: (String) -> Unit) {
        var name: String by Delegates.observable("InitName") { prop, old, new ->
            logCallback("【Observable】属性 ${prop.name} 变更: $old -> $new")
        }
    }

    private fun testObservableDelegate() {
        val user = ObservableUser { appendLog(it) }
        user.name = "Alice"
        user.name = "Bob"
    }

    // ─────────────────────────────────────────────
    // 5. 可否决属性 Delegates.vetoable
    // ─────────────────────────────────────────────
    class VetoableUser(private val logCallback: (String) -> Unit) {
        var age: Int by Delegates.vetoable(18) { prop, old, new ->
            val allow = new in 0..150
            logCallback("【Vetoable】尝试将 ${prop.name} 从 $old 修改为 $new: ${if (allow) "允许" else "否决"}")
            allow
        }
    }

    private fun testVetoableDelegate() {
        val user = VetoableUser { appendLog(it) }
        user.age = 25
        appendLog("【Vetoable】当前 age: ${user.age}")
        user.age = -5
        appendLog("【Vetoable】当前 age: ${user.age}（非法值未生效）")
    }

    // ─────────────────────────────────────────────
    // 6. Map 映射属性委托
    // ─────────────────────────────────────────────
    class MapConfig(val map: Map<String, Any?>) {
        val title: String by map
        val version: Int by map
    }

    private fun testMapDelegate() {
        val map = mapOf("title" to "Antigravity App", "version" to 2)
        val config = MapConfig(map)
        appendLog("【Map委托】title: ${config.title}, version: ${config.version}")
    }
}