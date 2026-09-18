package com.example.william.my.module.compose.activity.remember

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Remember — 重组记忆与状态持久化对比
 *
 * 对比演示 remember 与 rememberSaveable 在重组、进程重建与依赖项上的差异。
 *
 * 核心机制与避坑点：
 * 1. remember：重组时记住状态，不涉及持久化；依赖项通常是 Composable 内的状态或参数；
 * 2. rememberSaveable：重组时记住状态，并在进程重建/应用重启后恢复；可配合自定义 Saver；
 * 3. 读写形态对比：`val` 读值、`by` 委托读写、解构 `(value, setValue)` 三种用法；
 * 4. derivedStateOf：由高频状态派生低频状态，减少不必要重组。
 *
 * https://developer.android.google.cn/develop/ui/compose/state-saving
 */
@Route(path = RouterPath.Compose.Remember)
class RememberActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RememberExample()
        }
    }

    // listSaver、mapSaver（rememberSaveable 的自定义 Saver）
    @Composable
    fun RememberExample() {
        val state = remember { mutableStateOf("") }
        val state2 by remember { mutableStateOf("") }
        val (state3, setState3) = remember { mutableStateOf("") }

        val stateSaveable = rememberSaveable { mutableStateOf("") }
        val stateSaveable2 by rememberSaveable { mutableStateOf("") }
        val (stateSaveable3, setStateSaveable3) = rememberSaveable { mutableStateOf("") }

        val (text, setText) = rememberSaveable { mutableStateOf("") }

        var clickCount by remember { mutableIntStateOf(0) }
        val clickedALot by remember { derivedStateOf { clickCount >= 3 } }

        Column {
            // 显示当前文本状态的 Text
            Text(text = "Current text: $text")

            // 输入框，用户可以输入文本
            OutlinedTextField(
                value = text,
                onValueChange = setText,
                label = { Text("Enter text here") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    // 当用户点击键盘上的“完成”按钮时调用
                    println("Final text: $text")
                }),
            )

            Button(onClick = { clickCount++ }) {
                Text(text = "Click me")
            }

            if (clickedALot) {
                Text(text = "You clicked a lot")
            }
        }
    }
}
