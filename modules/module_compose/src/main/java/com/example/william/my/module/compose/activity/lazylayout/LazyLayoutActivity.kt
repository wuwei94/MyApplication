package com.example.william.my.module.compose.activity.lazylayout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme
import kotlin.random.Random

/**
 * 列表项数据模型
 */
data class ContactItem(
    val id: Int,
    val name: String,
    val group: String,
    val phone: String,
)

/**
 * LazyLayout — 列表高级特性实战
 *
 * 演示 Jetpack Compose Lazy 列表核心进阶能力：
 * 1. 吸顶悬浮分组：[stickyHeader] 字母/分类头部贴顶吸附；
 * 2. 列表项动效：[Modifier.animateItem] 实现列表元素增删与重排平滑过渡动画；
 * 3. 动态数据驱动：支持动态随机插入、局部删除与乱序（Shuffle）重排；
 * 4. 触底加载状态：模拟分页加载进度与状态卡片。
 *
 * 官方文档：
 * https://developer.android.google.cn/develop/ui/compose/lists#item-animations
 */
@Route(path = RouterPath.Compose.LazyLayout)
class LazyLayoutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                LazyLayoutScreen()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun LazyLayoutScreen() {
        var nextId by remember { mutableStateOf(100) }
        val contactList = remember {
            mutableStateListOf(
                ContactItem(1, "Alice Cooper", "A", "138-0000-0001"),
                ContactItem(2, "Alan Turing", "A", "138-0000-0002"),
                ContactItem(3, "Bob Dylan", "B", "138-0000-0003"),
                ContactItem(4, "Bruce Wayne", "B", "138-0000-0004"),
                ContactItem(5, "Charlie Chaplin", "C", "138-0000-0005"),
                ContactItem(6, "Clark Kent", "C", "138-0000-0006"),
                ContactItem(7, "David Bowie", "D", "138-0000-0007"),
                ContactItem(8, "Diana Prince", "D", "138-0000-0008"),
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "LazyColumn 进阶（吸顶 + animateItem 动效）",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击下方操作按钮观察：列表项插入、删除及乱序移动时的平滑物理动效。",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = {
                                    val groups = listOf("A", "B", "C", "D")
                                    val randomGroup = groups.random()
                                    val newItem = ContactItem(
                                        id = nextId++,
                                        name = "New Contact #$nextId",
                                        group = randomGroup,
                                        phone = "139-${Random.nextInt(1000, 9999)}-${Random.nextInt(1000, 9999)}",
                                    )
                                    val insertIndex = contactList.indexOfLast { it.group == randomGroup }
                                    if (insertIndex != -1) {
                                        contactList.add(insertIndex + 1, newItem)
                                    } else {
                                        contactList.add(newItem)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("插入", modifier = Modifier.padding(start = 4.dp))
                            }

                            Button(
                                onClick = {
                                    if (contactList.isNotEmpty()) {
                                        contactList.removeAt(Random.nextInt(contactList.size))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("随机删", modifier = Modifier.padding(start = 4.dp))
                            }

                            Button(
                                onClick = {
                                    contactList.shuffle()
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("乱序", modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            val groupedItems = contactList.groupBy { it.group }.toSortedMap()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                groupedItems.forEach { (group, items) ->
                    // 1. stickyHeader 吸顶头部
                    stickyHeader(key = "header_$group") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "分组 $group (${items.size} 位联系人)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    // 2. 列表项（带 Modifier.animateItem 动效）
                    items(items, key = { it.id }) { contact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = contact.name.take(1),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp),
                                ) {
                                    Text(
                                        text = contact.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = contact.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                IconButton(
                                    onClick = { contactList.remove(contact) },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. 列表触底加载状态
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(
                            text = "已加载全部数据",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
