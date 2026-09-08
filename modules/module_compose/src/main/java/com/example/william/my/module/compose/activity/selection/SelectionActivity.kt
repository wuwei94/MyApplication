package com.example.william.my.module.compose.activity.selection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme
import kotlin.math.roundToInt

/**
 * 封装三态复选框联动逻辑的领域模型
 */
class TriStateSelectionState(initialItemsCount: Int = 3) {
    val items = mutableStateListOf<Boolean>().apply {
        repeat(initialItemsCount) { add(false) }
    }

    val parentState: ToggleableState
        get() {
            val allChecked = items.all { it }
            val noneChecked = items.none { it }
            return when {
                allChecked -> ToggleableState.On
                noneChecked -> ToggleableState.Off
                else -> ToggleableState.Indeterminate
            }
        }

    fun toggleParent() {
        val target = parentState != ToggleableState.On
        for (i in items.indices) {
            items[i] = target
        }
    }

    fun toggleChild(index: Int) {
        if (index in items.indices) {
            items[index] = !items[index]
        }
    }
}

/**
 * Selection — 选择器与开关控件实战
 *
 * 全景展示 Compose Material 3 选择组件的用法、状态绑定与无障碍语义联动：
 * 1. 复选框体系：单选 [Checkbox] 与全选/反选三态联动 [TriStateCheckbox]；
 * 2. 状态开关：[Switch] 带状态图标（[thumbContent]）与整行无障碍点击联动；
 * 3. 单选按钮：[RadioButton] 单选列表与互斥选择状态联动；
 * 4. 滑动选择器：连续滑动条 [Slider]、离散刻度分段滑动条（[steps]）与双向区间滑块 [RangeSlider]。
 *
 * 官方文档：
 * https://developer.android.google.cn/develop/ui/compose/components/switch
 */
@Route(path = RouterPath.Compose.Selection)
class SelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                SelectionGalleryScreen()
            }
        }
    }

    @Composable
    private fun SelectionGalleryScreen() {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Checkbox 与 TriStateCheckbox 三态全选联动
            TriStateCheckboxSection()

            // 2. Switch 带图标开关
            SwitchSection()

            // 3. RadioButton 单选按钮组
            RadioButtonSection()

            // 4. Slider 连续与离散刻度滑动条
            SliderSection()

            // 5. RangeSlider 双向区间滑动条
            RangeSliderSection()
        }
    }

    @Composable
    private fun TriStateCheckboxSection() {
        val selectionState = remember { TriStateSelectionState(3) }

        SectionCard(title = "1. Checkbox 与 TriStateCheckbox 三态联动") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // 父级全选开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = selectionState.parentState == ToggleableState.On,
                            onValueChange = { selectionState.toggleParent() },
                            role = Role.Checkbox,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TriStateCheckbox(
                        state = selectionState.parentState,
                        onClick = { selectionState.toggleParent() },
                    )
                    Text(
                        text = "全选所有权限项目（父级三态控制）",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }

                // 子项目列表
                val childLabels = listOf("相机拍摄权限", "读取存储权限", "精准定位权限")
                selectionState.items.forEachIndexed { index, checked ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectionState.toggleChild(index) }
                            .padding(start = 28.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { selectionState.toggleChild(index) },
                        )
                        Text(
                            text = childLabels.getOrElse(index) { "权限项目 #$index" },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SwitchSection() {
        var isWifiEnabled by remember { mutableStateOf(true) }
        var isAutoSyncEnabled by remember { mutableStateOf(false) }

        SectionCard(title = "2. Material 3 Switch 状态开关") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 带图标的 Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = isWifiEnabled,
                            onValueChange = { isWifiEnabled = it },
                            role = Role.Switch,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Wi-Fi 高速连接",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (isWifiEnabled) "已连接到办公室无线网络" else "已关闭 Wi-Fi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Switch(
                        checked = isWifiEnabled,
                        onCheckedChange = { isWifiEnabled = it },
                        thumbContent = {
                            Icon(
                                imageVector = if (isWifiEnabled) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                }

                // 普通无图标 Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = isAutoSyncEnabled,
                            onValueChange = { isAutoSyncEnabled = it },
                            role = Role.Switch,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "后台数据自动同步",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = isAutoSyncEnabled,
                        onCheckedChange = { isAutoSyncEnabled = it },
                    )
                }
            }
        }
    }

    @Composable
    private fun RadioButtonSection() {
        val options = listOf("按热度智能推荐", "按发布时间最新", "按评论互动最多")
        var selectedOption by remember { mutableStateOf(options.first()) }

        SectionCard(title = "3. RadioButton 单选按钮组") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = text }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = { selectedOption = text },
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SliderSection() {
        var continuousValue by remember { mutableFloatStateOf(45f) }
        var discreteValue by remember { mutableFloatStateOf(2f) }

        SectionCard(title = "4. Slider 连续与刻度离散滑动条") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 连续滑动
                Text(
                    text = "连续音量调节: ${continuousValue.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Slider(
                    value = continuousValue,
                    onValueChange = { continuousValue = it },
                    valueRange = 0f..100f,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 离散分段滑动条
                Text(
                    text = "离散步进字号调节 (档位 1~5): 第 ${discreteValue.roundToInt()} 档",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Slider(
                    value = discreteValue,
                    onValueChange = { discreteValue = it },
                    valueRange = 1f..5f,
                    steps = 3, // 内部 3 个分段点，分为 4 区间 5 档
                )
            }
        }
    }

    @Composable
    private fun RangeSliderSection() {
        var sliderPosition by remember { mutableStateOf(200f..800f) }

        SectionCard(title = "5. RangeSlider 双向区间选择器") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "预算价格区间: ¥${sliderPosition.start.roundToInt()} ~ ¥${sliderPosition.endInclusive.roundToInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                RangeSlider(
                    value = sliderPosition,
                    onValueChange = { range -> sliderPosition = range },
                    valueRange = 0f..1000f,
                    steps = 9,
                )
            }
        }
    }

    @Composable
    private fun SectionCard(
        title: String,
        content: @Composable () -> Unit,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                content()
            }
        }
    }
}
