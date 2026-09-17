# 现代 Android 标准代码模板库

> 本文档汇集经实践验证的代码模板骨架。
> 新建页面、重构架构、编写单元测试或扩展新模块时优先采用本库结构。
> 规则真相源：示例页与平行实现见 [showcase.md](showcase.md)；通用风格见 [style.md](style.md)。模板与规则冲突时以规则文件为准。
> 包路径示意：示例页常见于 `module_<域>/.../activity/...`；模块入口 Activity 多在模块包根（如 `BluetoothMainActivity`）。新建页面以兄弟页既有分包为准，不强行套模板路径。

---

## 1. 控制台交互示例页模板（Showcase Activity）

适用于展示第三方库、系统 API 或架构组件能力的示例 Activity。采用 `buildList()` 声明操作项列表，配合 `onRecyclerClick(position, string)` 响应列表点击；依托 `BasicResponseActivity` 内置的暗色控制台，提供格式化、带时间戳的运行日志。

```kotlin
package com.example.william.my.module.<模块名>.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * <组件/技术名> — <一句话核心定位>
 *
 * 核心机制与避坑点：
 * 1. <机制要点 1>：<简要说明，聚焦关键参数或时序>
 * 2. <避坑要点 2>：<生命周期/资源释放/线程切换等非显而易见的注意事项>
 *
 * 官方参考：
 * <官方权威文档 URL；UI/动画演示页可省，见 showcase.md §2.2>
 */
@Route(path = RouterPath.<模块名>.<页面名>)
class SampleActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 <功能名> 的核心 API 调用与状态联动")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 触发基础操作",
        "2. 触发异步流程",
        "3. 重置 / 清理资源",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> executeBasicOperation()
            1 -> executeAsyncOperation()
            2 -> clearResources()
        }
    }

    private fun executeBasicOperation() {
        appendLog("→ 开始执行基础操作...")
        // 业务调用...
        appendLog("✓ 基础操作执行成功")
    }

    private fun executeAsyncOperation() {
        appendLog("→ 启动异步任务...")
        // 离散事件：成功用 ✓，失败用 ✗，高频进度用 updateLog("progress", "...")
    }

    private fun clearResources() {
        appendLog("✓ 资源已释放")
    }
}
```

---

## 2. 现代单向数据流 UDF / MVI 架构模板

严格遵循 Unidirectional Data Flow（UDF）原则，解耦为：
- **`UiState`**：单一可信源的页面状态快照（不可变 Data Class）；
- **`UiIntent`**：用户交互发起的意图（Sealed Interface）；
- **`UiEffect`**：一次性副作用，如弹 Toast、页面导航（通过 Channel 发送，保证不因重组/重绘重复消费）；
- **`ViewModel`**：状态协调器，负责协程异常捕获与状态向外暴露。

### 2.1 状态、意图与副作用契约

```kotlin
// UiState: 不可变数据快照
data class ArticleUiState(
    val isLoading: Boolean = false,
    val articles: List<ArticleItem> = emptyList(),
    val errorMessage: String? = null,
)

// UiIntent: 用户驱动的离散动作
sealed interface ArticleUiIntent {
    data object Refresh : ArticleUiIntent
    data class ToggleFavorite(val articleId: Long) : ArticleUiIntent
    data class LoadDetail(val articleId: Long) : ArticleUiIntent
}

// UiEffect: 单次消费的副作用事件
sealed interface ArticleUiEffect {
    data class ShowToast(val message: String) : ArticleUiEffect
    data class NavigateToDetail(val articleId: Long) : ArticleUiEffect
}
```

### 2.2 ViewModel 状态驱动器

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArticleMviViewModel(
    private val repository: ArticleRepository,
) : ViewModel() {

    val intent = Channel<ArticleUiIntent>(Channel.UNLIMITED)

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ArticleUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            intent.consumeAsFlow().collect { action ->
                when (action) {
                    is ArticleUiIntent.Refresh -> loadArticles()
                    is ArticleUiIntent.ToggleFavorite -> toggleFavorite(action.articleId)
                    is ArticleUiIntent.LoadDetail -> {
                        _uiEffect.send(ArticleUiEffect.NavigateToDetail(action.articleId))
                    }
                }
            }
        }
    }

    private fun loadArticles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.getArticles()
            }.onSuccess { list ->
                _uiState.update { it.copy(isLoading = false, articles = list) }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false, errorMessage = throwable.message) }
                _uiEffect.send(ArticleUiEffect.ShowToast("网络请求失败: ${throwable.message}"))
            }
        }
    }

    private fun toggleFavorite(articleId: Long) {
        // 更新逻辑...
    }
}
```

---

## 3. Jetpack Compose 现代页面模板

遵循 **Stateful Container**（容器层：ViewModel 注入、状态收集、副作用监听）与 **Stateless Screen**（展示层：纯 UI 渲染、状态提升、无架构绑定）严格分离的准则，使 UI 组件天然支持 `@Preview` 与单元化测试。

```kotlin
package com.example.william.my.module.compose.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

/**
 * 容器层 Composable (Stateful)
 *
 * 职责：连接架构层（ViewModel）、安全收集 Flow 状态、分发副作用与生命周期事件。
 */
@Composable
fun ArticleRoute(
    viewModel: ArticleMviViewModel,
    onNavigateDetail: (Long) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ArticleUiEffect.ShowToast -> onShowToast(effect.message)
                is ArticleUiEffect.NavigateToDetail -> onNavigateDetail(effect.articleId)
            }
        }
    }

    ArticleScreen(
        uiState = uiState,
        onRefresh = {
            scope.launch {
                viewModel.intent.send(ArticleUiIntent.Refresh)
            }
        },
        onArticleClick = { id ->
            scope.launch {
                viewModel.intent.send(ArticleUiIntent.LoadDetail(id))
            }
        },
        modifier = modifier,
    )
}

/**
 * 展示层 Composable (Stateless)
 *
 * 职责：纯粹的 UI 布局与绘制，所有数据由入参传入，所有交互通过 lambda 向外冒泡。
 */
@Composable
fun ArticleScreen(
    uiState: ArticleUiState,
    onRefresh: () -> Unit,
    onArticleClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.errorMessage != null -> Text(text = "错误: ${uiState.errorMessage}")
                else -> Text(text = "已加载 ${uiState.articles.size} 篇文章")
            }
        }
    }
}

/**
 * Preview 假数据注入器
 */
class ArticleUiStatePreviewProvider : PreviewParameterProvider<ArticleUiState> {
    override val values: Sequence<ArticleUiState> = sequenceOf(
        ArticleUiState(isLoading = true),
        ArticleUiState(errorMessage = "网络连接超时"),
        ArticleUiState(articles = listOf(ArticleItem(id = 1, title = "Jetpack Compose 实战"))),
    )
}

@Preview(showBackground = true, name = "多状态预览")
@Composable
private fun ArticleScreenPreview(
    @PreviewParameter(ArticleUiStatePreviewProvider::class) state: ArticleUiState,
) {
    ArticleScreen(
        uiState = state,
        onRefresh = {},
        onArticleClick = {},
    )
}
```

---

## 4. Turbine 响应式流单元测试模板

用于对 `ViewModel`、`Repository` 的 `StateFlow` / `SharedFlow` 状态发射序列进行确定性验证。

```kotlin
package com.example.william.my.module.arch.viewmodel

import app.cash.turbine.test
import com.example.william.my.module.arch.fake.FakeArticleRepository
import com.example.william.my.module.arch.mvi.data.ArticleUiIntent
import com.example.william.my.module.arch.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakeArticleRepository()

    @Test
    fun loadArticles_success_emitsLoadingThenSuccessState() = runTest {
        val viewModel = ArticleMviViewModel(fakeRepository)

        viewModel.uiState.test {
            // 初始状态
            val initial = awaitItem()
            assertFalse(initial.isLoading)
            assertTrue(initial.articles.isEmpty())

            // 触发刷新意图
            viewModel.intent.send(ArticleUiIntent.Refresh)

            // 预期状态 1：加载中
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            // 推进协程调度
            testScheduler.advanceUntilIdle()

            // 预期状态 2：加载完成，包含数据
            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals(2, successState.articles.size)
        }
    }
}
```

---

## 5. 手写 Fake 数据源测试替身模板

优先采用**手写 Fake**（内存可控实现）代替重量级 Mockito。手写 Fake 具备自闭合、可预测、维护成本低的优势。

```kotlin
package com.example.william.my.module.arch.fake

import java.io.IOException

class FakeArticleRepository : ArticleRepository {

    private val articles = mutableListOf<ArticleItem>()
    private var shouldReturnError = false

    fun setReturnError(shouldError: Boolean) {
        shouldReturnError = shouldError
    }

    fun emitArticles(newArticles: List<ArticleItem>) {
        articles.clear()
        articles.addAll(newArticles)
    }

    override suspend fun getArticles(): List<ArticleItem> {
        if (shouldReturnError) {
            throw IOException("Fake Network Error")
        }
        return articles.toList()
    }
}
```

---

## 6. Roborazzi 截图基准与视觉回归测试模板

针对 Compose 控件或界面，验证 UI 渲染与基准位图的一致性。

```kotlin
package com.example.william.my.module.compose.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ArticleScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun capture_articleScreen_normalState() {
        composeTestRule.setContent {
            ArticleScreen(
                uiState = ArticleUiState(articles = listOf(ArticleItem(1, "Roborazzi 截图验证"))),
                onRefresh = {},
                onArticleClick = {},
            )
        }

        // 校验或生成基准截图
        composeTestRule.onRoot().captureRoboImage()
    }
}
```

---

## 7. 新模块脚手架与 Convention Plugin 决策模板

在新建模块时，根据职责选用最精确的约定插件，避免过度引入无关依赖：

| 模块形态 | 采用 Convention Plugin | 适用场景 |
|---------|-----------------------|---------|
| **纯领域/无 Android 依赖** | `my.jvm.library` | 如 `basic_model`、算法、契约定义 |
| **基础封装层（无 UI）** | `my.android.library` | 如 `libs/lib_okhttp`、网络拦截器封装 |
| **带 Compose 的纯库层** | `my.android.library.compose` | 如通用 Compose UI 组件库 |
| **标准业务/示例功能模块** | `my.android.feature` | 如 `modules/module_http`（内置 Hilt + ARouter + 基础 UI 依赖） |
| **Compose 功能模块** | `my.android.feature.compose` | 如 `modules/module_compose`（内置 Compose 依赖体系） |

### 快速接入清单：
1. **`settings.gradle.kts`**：使用 `include(":modules:module_xxx")` 注册；
2. **`build.gradle.kts`**：
   ```kotlin
   plugins {
       alias(libs.plugins.my.android.feature)
   }
   android {
       namespace = "com.example.william.my.module.<域名>"
       // 前缀见 structure.md「modules 资源前缀」：新建默认域名_，勿模仿 ui_/demo_ 例外
       resourcePrefix("<域名>_")
   }
   ```
3. **`RouterPath.kt`**：在对应技术分类的对象内注册常量：`const val <页面名> = "/<模块名>/<页面名>"`（模块名与页面名均 PascalCase，见 [structure.md](structure.md)「路由」）；
4. **`Category.kt` / `CategoryActivity.kt`**：将主入口挂载到 10 大技术领域对应分组。
