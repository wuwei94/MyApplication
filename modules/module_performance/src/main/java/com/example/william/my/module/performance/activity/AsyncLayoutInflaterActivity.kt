package com.example.william.my.module.performance.activity

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.performance.R
import com.example.william.my.module.performance.manager.ViewPreloadManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.william.my.basic.basic_shared.R as SharedR

/**
 * AsyncLayoutInflater 异步布局加载与 ViewPreloadManager 预加载示例
 *
 * 本示例演示 AsyncLayoutInflater 的原生异步机制与 ViewPreloadManager 工程预加载架构：
 * 1. 核心机制：在后台工作线程异步解析 XML、反射实例化 View 对象，完成后在主线程回调中挂载至展示容器；
 * 2. ViewPreloadManager 预加载模式（Pre-Inflation）：
 *    - 在前置页面（如列表页）或系统空闲期，提前在后台批量异步解析详情页/弹窗/复杂卡片布局并缓存入池；
 *    - 进入详情页或点击弹窗时，直接调用 [ViewPreloadManager.getView] 瞬间取出挂载，实现 0ms 秒开；
 *    - 若池为空，自动降级（Fallback）走常规同步 inflate 加载，保证高可用。
 */
@Route(path = RouterPath.Performance.AsyncLayoutInflater)
class AsyncLayoutInflaterActivity : BasicLayoutActivity() {

    private lateinit var asyncInflater: AsyncLayoutInflater

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        asyncInflater = AsyncLayoutInflater(this)

        // 配置详情页布局的最大预加载容量
        ViewPreloadManager.setMaxCount(R.layout.performance_layout_async_detail, 3)
        ViewPreloadManager.setMaxCount(R.layout.performance_layout_async_card, 5)

        showHintView("AsyncLayoutInflater 与 ViewPreloadManager 视图预加载示例\n点击下方列表演示后台预加载、0ms 瞬间挂载与弹窗秒开")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 异步加载卡片并挂载（原生 AsyncLayoutInflater 异步解析）",
        "2. ViewPreloadManager 预加载 2 个详情页布局入池（后台 Pre-Inflation）",
        "3. ViewPreloadManager.getView 取出并挂载详情页（0ms 瞬间上屏 / Fallback）",
        "4. 预加载 BottomSheet 复杂弹窗并瞬间弹出（0ms 弹窗体验）",
        "5. 查看 ViewPreloadManager 运行状态与池容量",
        "6. 清空展示容器与 ViewPreloadManager 全部缓存池",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> testAsyncInflateCard()
            1 -> testPreloadDetailViews()
            2 -> testConsumeDetailView()
            3 -> testPreloadAndShowBottomSheet()
            4 -> showPreloadManagerStatus()
            5 -> testClearAll()
        }
    }

    /**
     * 示例 1：原生 AsyncLayoutInflater 异步加载单卡片
     */
    private fun testAsyncInflateCard() {
        val startTime = System.currentTimeMillis()

        asyncInflater.inflate(R.layout.performance_layout_async_card, mContainer) { view, _, _ ->
            val cost = System.currentTimeMillis() - startTime
            val titleView = view.findViewById<TextView>(R.id.performance_async_card_title)
            val descView = view.findViewById<TextView>(R.id.performance_async_card_desc)

            titleView.text = "⚡ 异步加载卡片（耗时：${cost}ms）"
            descView.text = "解析线程：后台工作线程 | 挂载线程：${Thread.currentThread().name}\n" +
                "完成时间：${System.currentTimeMillis()}，已动态挂载至上方展示区！"

            setView(view)
        }
    }

    /**
     * 示例 2：使用 ViewPreloadManager 在后台异步预加载 2 个详情页视图
     */
    private fun testPreloadDetailViews() {
        val startTime = System.currentTimeMillis()

        ViewPreloadManager.preload(this, R.layout.performance_layout_async_detail, count = 2) { loadedCount ->
            val cost = System.currentTimeMillis() - startTime
            val currentPoolSize = ViewPreloadManager.getPoolSize(R.layout.performance_layout_async_detail)

            showHintView(
                "✅ [ViewPreloadManager 后台预加载完成]\n" +
                    "成功在后台解析并入池 $loadedCount 个详情页 View（总耗时：${cost}ms）\n" +
                    "当前详情页池大小：$currentPoolSize\n\n" +
                    "👉 请点击【3. ViewPreloadManager.getView 取出并挂载详情页】体验 0ms 瞬间渲染",
            )
        }
    }

    /**
     * 示例 3：通过 ViewPreloadManager.getView 获取并挂载详情页
     * - 池中有 View：0ms 瞬间取出（命中缓存）
     * - 池为空：自动安全降级（Fallback）走同步加载
     */
    private fun testConsumeDetailView() {
        val hasCache = ViewPreloadManager.hasCache(R.layout.performance_layout_async_detail)
        val startTime = System.currentTimeMillis()

        // 核心 API：无论池中有无，直接调用 getView()
        val detailView = ViewPreloadManager.getView(this, R.layout.performance_layout_async_detail)
        val cost = System.currentTimeMillis() - startTime

        val tagView = detailView.findViewById<TextView>(R.id.performance_detail_tag)
        val footerView = detailView.findViewById<TextView>(R.id.performance_detail_footer)

        if (hasCache) {
            tagView.text = "⚡ [0ms 命中预加载池] 详情页 View 瞬间取出并挂载！"
            footerView.text = "状态：命中 ViewPreloadManager 预加载池（获取耗时：${cost}ms，剩余池容量：${ViewPreloadManager.getPoolSize(R.layout.performance_layout_async_detail)}）"
        } else {
            tagView.text = "⚠️ [未命中预加载池] 自动 Fallback 同步加载成功"
            footerView.text = "状态：池为空，触发自动降级同步 inflate（耗时：${cost}ms）"
        }

        setView(detailView)
    }

    /**
     * 示例 4：预加载复杂 BottomSheet 弹窗并瞬间弹出
     */
    private fun testPreloadAndShowBottomSheet() {
        // 预加载一个卡片布局作为弹窗内容
        ViewPreloadManager.preload(this, R.layout.performance_layout_async_card, count = 1) {
            val dialogView = ViewPreloadManager.getView(this, R.layout.performance_layout_async_card)
            val titleView = dialogView.findViewById<TextView>(R.id.performance_async_card_title)
            val descView = dialogView.findViewById<TextView>(R.id.performance_async_card_desc)

            titleView.text = "🎉 预加载 BottomSheet 弹窗"
            descView.text = "本弹窗视图已提前由 ViewPreloadManager 在后台异步解析完成。\n点击弹出时 0ms 无卡顿即时呈现！"

            BottomSheetDialog(this).apply {
                setContentView(dialogView)
                show()
            }
        }
    }

    /**
     * 示例 5：查看 ViewPreloadManager 当前池状态
     */
    private fun showPreloadManagerStatus() {
        val detailPoolSize = ViewPreloadManager.getPoolSize(R.layout.performance_layout_async_detail)
        val cardPoolSize = ViewPreloadManager.getPoolSize(R.layout.performance_layout_async_card)

        showHintView(
            "📊 【ViewPreloadManager 缓存池运行状态】\n\n" +
                "• 详情页布局（performance_layout_async_detail）当前就绪池大小：$detailPoolSize / 上限: 3\n" +
                "• 卡片/弹窗布局（performance_layout_async_card）当前就绪池大小：$cardPoolSize / 上限: 5\n\n" +
                "调用 ViewPreloadManager.getView() 时，有缓存则 0ms 瞬间返回，无缓存自动 Fallback 同步加载。",
        )
    }

    /**
     * 示例 6：清空全部缓存池与展示区
     */
    private fun testClearAll() {
        clearContainer()
        ViewPreloadManager.clearAll()
        showHintView("已清空展示容器与 ViewPreloadManager 全部缓存池！\n点击下方列表重新演示")
    }

    private fun showHintView(message: String) {
        val textView = TextView(this).apply {
            text = message
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, SharedR.color.shared_color_accent))
            textSize = 13f
            setPadding(32, 32, 32, 32)
        }
        setView(textView)
    }

    override fun onDestroy() {
        super.onDestroy()
        ViewPreloadManager.clearAll()
    }
}
