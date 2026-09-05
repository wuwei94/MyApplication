package com.example.william.my.module.performance.activity

import android.os.Bundle
import androidx.profileinstaller.ProfileInstaller
import androidx.profileinstaller.ProfileInstaller.DiagnosticsCallback
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.util.concurrent.Executors

/**
 * Jetpack Baseline Profiles — 基线配置文件与 AOT 预编译优化
 *
 * 核心设计与优化价值：
 * 1. 【AOT 提前编译】：在应用安装或后台空闲时，指导 ART 将关键代码路径（CUJ）直接预编译为机器码，消除首次执行的 JIT 编译抖动与解释执行耗时；
 * 2. 【冷启动提速】：相比纯 JIT 模式，通常可减少 20%~40% 的冷启动时间；
 * 3. 【流畅度提升】：显著降低首次进入复杂页面、滚动列表时的丢帧率（Jank Rate）；
 * 4. 【ProfileInstaller】：无缝集成在 APK 内，由 Play Store 分发 Cloud Profile，或通过 profileinstaller 库在本地触发 baseline profile 安装。
 */
@Route(path = RouterPath.Performance.BaselineProfiles)
class BaselineProfilesActivity : BasicResponseActivity() {

    private val executor = Executors.newSingleThreadExecutor()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            """
            Jetpack Baseline Profiles 核心架构：
            • 运行原理：ART 基于基线配置文件预编译关键路径（CUJ）为机器码；
            • ProfileInstaller：客户端运行时基线配置文件解析与写入触发器；
            • 性能收益：冷启动速度提升 20%~40%，消除列表初次滑动与转场掉帧。
            """.trimIndent(),
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 触发 ProfileInstaller 本地写入与诊断 (writeProfile)",
        "2. 深度解析：JIT vs AOT vs Baseline Profiles 编译演进",
        "3. 规则剖析：Baseline Profile 生成语法与 CUJ 规则",
        "4. 实战基准：Macrobenchmark 性能测试与 ADB 调试指令",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> triggerProfileInstaller()
            1 -> showCompilationArchitecture()
            2 -> showProfileRuleSyntax()
            3 -> showAdbAndBenchmarkGuide()
        }
    }

    /**
     * 1. 触发 ProfileInstaller 诊断与基线配置写入
     */
    private fun triggerProfileInstaller() {
        appendLog("── 开始调用 ProfileInstaller.writeProfile ──")

        val callback = object : DiagnosticsCallback {
            override fun onDiagnosticReceived(eventCode: Int, data: Any?) {
                runOnUiThread {
                    appendLog("【ProfileInstaller 诊断事件】code=$eventCode, data=$data")
                }
            }

            override fun onResultReceived(resultCode: Int, data: Any?) {
                runOnUiThread {
                    val resultDesc = when (resultCode) {
                        ProfileInstaller.RESULT_INSTALL_SUCCESS -> "RESULT_INSTALL_SUCCESS (基线配置写入成功)"
                        ProfileInstaller.RESULT_ALREADY_INSTALLED -> "RESULT_ALREADY_INSTALLED (已存在安装配置，无需重复安装)"
                        ProfileInstaller.RESULT_UNSUPPORTED_ART_VERSION -> "RESULT_UNSUPPORTED_ART_VERSION (当前 Android 版本不支持)"
                        ProfileInstaller.RESULT_NOT_WRITABLE -> "RESULT_NOT_WRITABLE (目标目录不可写)"
                        ProfileInstaller.RESULT_DESIRED_FORMAT_UNSUPPORTED -> "RESULT_DESIRED_FORMAT_UNSUPPORTED (不支持的文件格式)"
                        ProfileInstaller.RESULT_BASELINE_PROFILE_NOT_FOUND -> "RESULT_BASELINE_PROFILE_NOT_FOUND (APK assets/dexopt 中未内置 baseline.prof，常见于 Debug 包)"
                        ProfileInstaller.RESULT_IO_EXCEPTION -> "RESULT_IO_EXCEPTION (I/O 读写异常)"
                        ProfileInstaller.RESULT_PARSE_EXCEPTION -> "RESULT_PARSE_EXCEPTION (配置文件解析异常)"
                        else -> "Result Code: $resultCode, Data: $data"
                    }
                    appendLog("【ProfileInstaller 结果】$resultDesc")
                }
            }
        }

        // 异步执行写入与诊断
        ProfileInstaller.writeProfile(this, executor, callback)
    }

    /**
     * 2. Android ART 编译演进深度解析
     */
    private fun showCompilationArchitecture() {
        appendLog(
            """
            ── Android 编译执行演进与 Baseline Profiles ──
            1. Android 4.4 之前 (Dalvik)：
               • 纯 JIT 解释执行，运行时将字节码翻译为机器码，性能低且耗电。
            2. Android 5.0 ~ 6.0 (全量 AOT)：
               • 安装应用时将所有 DEX 全量编译为 ELF 机器码。
               • 痛点：安装极慢、系统更新后全量优化耗时十几分钟、ROM 空间占用翻倍。
            3. Android 7.0+ (混合编译 JIT + AOT + 配置文件)：
               • 平时运行时 JIT，并在设备空闲充电时（Profile-guided optimization）将热点代码 AOT 编译。
               • 痛点：新安装或刚更新的应用在第一次启动时依然走 JIT，前几次启动体验依然较差。
            4. Jetpack Baseline Profiles (现代化终极方案)：
               • 开发者在编译期通过 Macrobenchmark 采集核心旅程（CUJ），生成 baseline.prof 随 APK 发布；
               • 应用首次安装时，ART 直接拿到 Profile 进行局部 AOT 预编译，让用户「首次启动即享受全速 AOT 体验」。
            """.trimIndent(),
        )
    }

    /**
     * 3. Baseline Profile 语法规则与 CUJ
     */
    private fun showProfileRuleSyntax() {
        appendLog(
            """
            ── Baseline Profile 规则文件剖析 (baseline-prof.txt) ──
            规则由方法与类签名组成，前缀标记表示方法在 AOT 阶段的编译级别：
            • H (Hot)：热点方法，优先 AOT 编译；
            • S (Startup)：启动期（Cold Start）必须加载执行的方法；
            • P (Post-startup)：启动后主流程执行的方法；
            • L (Literal)：包含类加载与初始化信息。

            示例标记规则：
            HSPLcom/example/william/my/basic/basic_shared/category/DirectoryActivity;->onCreate(Landroid/os/Bundle;)V
            HSPLandroidx/recyclerview/widget/RecyclerView;->onLayout(ZIIII)V
            HSPLcom/alibaba/android/arouter/launcher/ARouter;->getInstance()Lcom/alibaba/android/arouter/launcher/ARouter;
            """.trimIndent(),
        )
    }

    /**
     * 4. ADB 调试与基准测试指令
     */
    private fun showAdbAndBenchmarkGuide() {
        appendLog(
            """
            ── Baseline Profiles 真机调试常用 ADB 指令 ──
            1. 清除当前应用的已编译 profile（重置为未优化纯 JIT 状态）：
               adb shell cmd package compile --reset com.example.william.my

            2. 强制 ART 立即使用内置 Baseline Profile 进行 AOT 编译：
               adb shell cmd package compile -m speed-profile -f com.example.william.my

            3. 验证应用当前的编译模式（如 filter=speed-profile 或 filter=verify）：
               adb shell dumpsys package com.example.william.my | grep -A 1 "dexopt"

            4. Macrobenchmark 自动化基准测试：
               在 benchmark 模块中编写 BaselineProfileGenerator，运行 ./gradlew :benchmark:pixel2Api31Benchmark 自动生成最新规则。
            """.trimIndent(),
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}
