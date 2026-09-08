package com.example.william.my.module.compose.activity.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.R

/**
 * Image — 图片显示与变换特性
 *
 * 演示 Compose 中 Image 组件的加载与多维视觉处理：
 * 1. **ContentScale 缩放策略**：Crop、Fit、FillBounds、Inside 对比；
 * 2. **形状裁剪与边框**：CircleShape 圆形头像、RoundedCornerShape 圆角与 Border 描边；
 * 3. **色彩滤镜与透明度**：ColorFilter.tint 矢量着色与 Alpha 透明度调节。
 *
 * https://developer.android.google.cn/jetpack/compose/graphics/images/loading
 */
@Route(path = RouterPath.Compose.Image)
class ImageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    private fun ImageContent(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. ContentScale 对比
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. ContentScale 缩放裁剪模式",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "在固定尺寸容器（宽 76dp × 高 52dp）下的呈现表现",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        ScaleItem(name = "Crop", scale = ContentScale.Crop)
                        ScaleItem(name = "Fit", scale = ContentScale.Fit)
                        ScaleItem(name = "FillBounds", scale = ContentScale.FillBounds)
                        ScaleItem(name = "Inside", scale = ContentScale.Inside)
                    }
                }
            }

            // 2. 形状裁剪与边框描边
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. 形状裁剪与边框装饰",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "基于 clip 与 border 实现圆形头像、圆角卡片与彩色描边",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 原图
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(R.drawable.shared_ic_launcher),
                                contentDescription = "原图",
                                modifier = Modifier.size(64.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "默认方形", style = MaterialTheme.typography.labelMedium)
                        }

                        // 圆角裁剪 + 描边
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(R.drawable.shared_ic_launcher),
                                contentDescription = "圆角矩形",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "圆角+描边", style = MaterialTheme.typography.labelMedium)
                        }

                        // 圆形裁剪头像 + 描边
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(R.drawable.shared_ic_launcher),
                                contentDescription = "圆形头像",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "圆形头像", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // 3. 滤镜着色与透明度
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. 滤镜着色与透明度调节",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "通过 ColorFilter.tint 进行单色着色，及 alpha 透明度渲染",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 原始色彩
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(R.drawable.shared_ic_launcher),
                                contentDescription = "原始色彩",
                                modifier = Modifier.size(64.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "原始色彩", style = MaterialTheme.typography.labelMedium)
                        }

                        // ColorFilter.tint
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(R.drawable.shared_ic_launcher),
                                contentDescription = "Tint 着色",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(64.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "主题色 Tint", style = MaterialTheme.typography.labelMedium)
                        }

                        // 半透明 Alpha
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(R.drawable.shared_ic_launcher),
                                contentDescription = "半透明",
                                alpha = 0.35f,
                                modifier = Modifier.size(64.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Alpha 35%", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ScaleItem(name: String, scale: ContentScale) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(width = 76.dp, height = 52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.shared_ic_launcher),
                    contentDescription = name,
                    contentScale = scale,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = name, style = MaterialTheme.typography.labelSmall)
        }
    }
}
