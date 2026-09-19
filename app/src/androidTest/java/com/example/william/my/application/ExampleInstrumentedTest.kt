package com.example.william.my.application

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 仪器化测试，在 Android 设备上运行。
 *
 * 参见[测试文档](http://d.android.com/tools/testing)。
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun appContext_packageName_usesApplicationIdPrefix() {
        // demo/prod 与 debug/release 会叠加 applicationIdSuffix，故只校验 applicationId 前缀
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(
            "packageName=${appContext.packageName} 应以 applicationId 前缀开头",
            appContext.packageName.startsWith("com.example.william.my.application"),
        )
    }
}
