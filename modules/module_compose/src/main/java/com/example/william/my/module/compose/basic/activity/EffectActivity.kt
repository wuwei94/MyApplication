package com.example.william.my.module.compose.basic.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

/**
 * Effect — Compose 副作用体系实战
 *
 * 聚合演示 Compose 四类标准副作用 API 的生命周期边界与适用语义。
 *
 * 核心机制与避坑点：
 * 1. LaunchedEffect：在 Composable 生命周期内运行挂起代码块（网络请求、计时器等）；key 变化时取消旧协程并重启新任务；
 * 2. SideEffect：重组成功后同步到非 Compose 外部状态，不依赖 key，适合日志、系统设置等无清理副作用；
 * 3. DisposableEffect：注册广播/传感器/连接等需清理资源，离开组合或 key 变化时触发 [DisposableEffectScope.onDispose] 释放；
 * 4. rememberUpdatedState：长生命周期副作用中引用最新 State/Lambda，避免闭包捕获导致 Stale State Capture。
 *
 * https://developer.android.google.cn/develop/ui/compose/side-effects
 */
@Route(path = RouterPath.Compose.Effect)
class EffectActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EffectScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun EffectScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. LaunchedEffect
        LaunchedEffectCard()

        // 2. SideEffect
        SideEffectCard()

        // 3. DisposableEffect
        DisposableEffectCard()

        // 4. rememberUpdatedState
        RememberUpdatedStateCard()
    }
}

/**
 * LaunchedEffect 演示卡片
 */
@Composable
private fun LaunchedEffectCard() {
    var timerCount by remember { mutableIntStateOf(0) }
    var restartTrigger by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(restartTrigger, isRunning) {
        if (isRunning) {
            while (true) {
                delay(1000)
                timerCount++
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "1. LaunchedEffect（挂起协程与 Key 重启）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "在 Composable 生命周期内安全启动挂起协程。当 restartTrigger 或 isRunning 变化时，自动取消上一个协程并重启新任务。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "计数器: $timerCount 秒 (状态: ${if (isRunning) "运行中" else "已暂停"})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { isRunning = !isRunning }) {
                    Text(if (isRunning) "暂停协程" else "恢复协程")
                }
                OutlinedButton(onClick = {
                    timerCount = 0
                    restartTrigger++
                }) {
                    Text("重置计数 (变更 Key)")
                }
            }
        }
    }
}

/**
 * SideEffect 演示卡片
 */
@Composable
private fun SideEffectCard() {
    var count by remember { mutableIntStateOf(0) }
    var totalRecompositions by remember { mutableIntStateOf(0) }

    SideEffect {
        // 每次成功的重组后执行，确保重组成功与非 Compose 外部状态同步
        totalRecompositions++
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "2. SideEffect（每次重组后同步外部状态）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "用于将 Compose 状态安全发布到非 Compose 管理的对象中。每次组合成功都会无挂起执行，不接受 key 参数。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "当前数值: $count  |  已完成重组次数: $totalRecompositions",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Button(onClick = { count++ }) {
                Text("触发重组 (+1)")
            }
        }
    }
}

/**
 * DisposableEffect 演示卡片
 */
@Composable
private fun DisposableEffectCard() {
    var isAttached by remember { mutableStateOf(false) }
    val logs = remember { mutableStateListOf<String>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "3. DisposableEffect（生命周期感知与资源注销）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "用于具有注销或资源清理需求的副作用（如广播接收器、传感器监听）。在离开组合时必须回调 onDispose { ... }。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "挂载模拟监听器: ${if (isAttached) "已挂载" else "已卸载"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = isAttached,
                    onCheckedChange = { isAttached = it },
                )
            }

            if (isAttached) {
                DisposableListenerComponent(onLog = { logs.add(0, it) })
            }

            Text(
                text = "生命周期日志（最新在前）:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (logs.isEmpty()) {
                    Text("暂无日志，切换上方开关观察挂载与 onDispose 释放", style = MaterialTheme.typography.bodySmall)
                } else {
                    logs.take(4).forEach { log ->
                        Text("• $log", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisposableListenerComponent(onLog: (String) -> Unit) {
    DisposableEffect(Unit) {
        onLog("DisposableEffect 启动: 模拟外部资源注册成功")
        onDispose {
            onLog("DisposableEffect.onDispose: 离开组合，模拟外部资源安全释放！")
        }
    }
}

/**
 * rememberUpdatedState 演示卡片
 */
@Composable
private fun RememberUpdatedStateCard() {
    var currentText by remember { mutableStateOf("选项 A") }
    var capturedResult by remember { mutableStateOf("尚未触发延时任务") }
    var isWaiting by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "4. rememberUpdatedState（防止长效闭包捕获陈旧值）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "在长生命周期的 LaunchedEffect 中引用状态时，若不重启协程直接读取状态，可能捕获初值；通过 rememberUpdatedState 可以确保协程在不重启的前提下读取到最新值。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "当前最新选择: $currentText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { currentText = "选项 A" }) { Text("选 A") }
                Button(onClick = { currentText = "选项 B" }) { Text("选 B") }
                Button(onClick = { currentText = "选项 C" }) { Text("选 C") }
            }

            DelayedActionComponent(
                targetText = currentText,
                isWaiting = isWaiting,
                onStart = { isWaiting = true },
                onComplete = { result ->
                    capturedResult = result
                    isWaiting = false
                },
            )

            Text(
                text = "执行结果: $capturedResult",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DelayedActionComponent(
    targetText: String,
    isWaiting: Boolean,
    onStart: () -> Unit,
    onComplete: (String) -> Unit,
) {
    // 关键点：使用 rememberUpdatedState 包裹最新入参
    val updatedTargetText by rememberUpdatedState(targetText)
    var trigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            onStart()
            // 延时 2.5 秒，在此期间用户可以快速切换上方选项
            delay(2500)
            onComplete("延时结束！成功捕获最新状态: $updatedTargetText")
        }
    }

    OutlinedButton(
        onClick = { trigger++ },
        enabled = !isWaiting,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(if (isWaiting) "正在延时 2.5 秒中（请在上方快速切换选项）..." else "启动延时读取任务 (2.5 秒)")
    }
}
