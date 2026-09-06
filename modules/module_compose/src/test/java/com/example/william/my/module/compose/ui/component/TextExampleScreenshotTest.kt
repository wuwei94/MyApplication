package com.example.william.my.module.compose.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * [TextExample] 的 Roborazzi 截图测试。
 *
 * 工作流：
 * - 录制基准：`./gradlew :modules:module_compose:recordRoborazziDemoDebug`（首次或 UI 有意变更时）
 * - 回归校验：`./gradlew :modules:module_compose:verifyRoborazziDemoDebug`（差异超阈值即失败）
 *
 * 基准图入库于 `src/roborazzi/screenshots/`，PR 中可直接 review 像素级差异。
 * qualifiers 固定为一档常见手机密度（420dpi 全高清），保证截图尺寸可复现；
 * 关闭动态取色：dynamicColor 走系统资源读取，在 Robolectric 下不稳定。
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class TextExampleScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun textExample_lightTheme_matchesSnapshot() {
        composeRule.setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                TextExample(str = "Hello Text")
            }
        }
        composeRule.onNodeWithText("Hello Text").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun textExample_darkTheme_matchesSnapshot() {
        composeRule.setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                TextExample(str = "Hello Text")
            }
        }
        composeRule.onNodeWithText("Hello Text").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }
}
