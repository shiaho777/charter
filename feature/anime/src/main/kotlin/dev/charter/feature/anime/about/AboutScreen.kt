package dev.charter.feature.anime.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.feature.anime.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.md, vertical = spacing.lg),
        ) {
            Text(
                text = "Charter",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = "版本 0.1.0 · agent-native Android 参考框架的参考应用",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.lg))

            SectionHeader(title = "产品原型")
            Spacer(Modifier.height(spacing.sm))
            Text(
                text =
                    "本应用的产品原型借鉴自开源项目 Kazumi（Predidit/Kazumi，GPL-3.0）：" +
                        "四 Tab 外壳、星期时间表、详情页多源检索 Sheet、五态收藏、行级观看历史等" +
                        "屏幕结构与交互模式。Charter 以 Apache-2.0 发布，GPL-3.0 与 Apache-2.0 不兼容，" +
                        "因此本项目仅借鉴产品设计，未复制 Kazumi 的任何源代码、美术资源或图标，" +
                        "所有界面均基于 Charter 设计系统以 Jetpack Compose 重新实现。" +
                        "决策记录见仓库 docs/adr/0008-kazumi-prototype.md。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.lg))

            SectionHeader(title = "数据与播放")
            Spacer(Modifier.height(spacing.sm))
            Text(
                text =
                    "番剧元数据（推荐、时间表、搜索、详情、剧集）来自 Bangumi 开放 API（api.bgm.tv）。" +
                        "播放源为本地演示数据：应用不进行任何第三方站点抓取，也不播放真实视频流；" +
                        "播放页为占位界面，用于演示选集、线路切换与观看进度的交互。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.lg))

            SectionHeader(title = "框架")
            Spacer(Modifier.height(spacing.sm))
            Text(
                text =
                    "Charter 是「规范可机器化」的 Android 框架示例：AGENTS.md 是可执行契约，" +
                        "动效只来自命名 token，模块边界由 Konsist 把守，设计不变量由 designAudit 审计。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.xxl))
        }
    }
}
