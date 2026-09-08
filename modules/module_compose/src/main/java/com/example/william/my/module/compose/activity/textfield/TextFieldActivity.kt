package com.example.william.my.module.compose.activity.textfield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * TextField — 输入框与软键盘交互示例页
 *
 * 聚合展示 Compose 中表单输入的核心体系：
 * 1. **三种形态**：填充型 (TextField)、轮廓型 (OutlinedTextField) 与完全自定义 (BasicTextField)；
 * 2. **焦点控制**：通过 FocusRequester 程序化索取焦点，FocusManager 清除焦点收起键盘；
 * 3. **键盘与动作联动**：KeyboardOptions 配置键盘类型与 IME 动作，KeyboardActions 监听回车事件；
 * 4. **视觉转换 (VisualTransformation)**：密码掩码显隐切换、手机号 3-4-4 动态分段格式化。
 *
 * https://developer.android.google.cn/develop/ui/compose/text/user-input
 */
@Route(path = RouterPath.Compose.TextField)
class TextFieldActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TextFieldContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    private fun TextFieldContent(modifier: Modifier = Modifier) {
        val focusManager = LocalFocusManager.current
        val passwordFocusRequester = remember { FocusRequester() }

        var username by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var customNote by remember { mutableStateOf("") }

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. 基础形态：Filled TextField 与 OutlinedTextField
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. 标准输入框形态 (Filled & Outlined)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "演示前后置图标、一键清空与 ImeAction 下一步联动",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 填充型 TextField
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("用户名（Filled）") },
                        placeholder = { Text("请输入账号") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = {
                            if (username.isNotEmpty()) {
                                IconButton(onClick = { username = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清空")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 手机号（带 VisualTransformation 3-4-4 分段格式化）
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 11) phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("手机号（Outlined + 3-4-4 分段）") },
                        placeholder = { Text("请输入11位手机号码") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { passwordFocusRequester.requestFocus() },
                        ),
                        visualTransformation = PhoneNumberVisualTransformation(),
                    )
                }
            }

            // 2. 焦点控制与密码掩码
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. 焦点控制与密码显隐 (FocusRequester)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "支持通过按钮索取焦点、点击键盘 Done 自动收起软键盘",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocusRequester),
                        label = { Text("登录密码") },
                        placeholder = { Text("请输入密码") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "隐藏密码" else "显示密码",
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() },
                        ),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = { passwordFocusRequester.requestFocus() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("程序化聚焦密码框")
                        }
                        Button(
                            onClick = { focusManager.clearFocus() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("清除焦点收起键盘")
                        }
                    }
                }
            }

            // 3. 极简自定义输入框：BasicTextField
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. 极简底层输入框 (BasicTextField)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "零系统原生装饰，完全由开发者自定义背景、光标与占位符",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    BasicTextField(
                        value = customNote,
                        onValueChange = { customNote = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (customNote.isEmpty()) {
                                    Text(
                                        text = "写点什么备忘笔记吧（支持多行输入）...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }
            }
        }
    }

    /**
     * 手机号 3-4-4 动态分段 VisualTransformation (如 138 0000 0000)
     */
    private class PhoneNumberVisualTransformation : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            val raw = text.text
            val out = StringBuilder()
            for (i in raw.indices) {
                out.append(raw[i])
                if ((i == 2 || i == 6) && i != raw.lastIndex) {
                    out.append(' ')
                }
            }

            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 3) return offset
                    if (offset <= 7) return offset + 1
                    return (offset + 2).coerceAtMost(out.length)
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 3) return offset
                    if (offset <= 8) return offset - 1
                    return (offset - 2).coerceAtMost(raw.length)
                }
            }

            return TransformedText(AnnotatedString(out.toString()), offsetMapping)
        }
    }
}
