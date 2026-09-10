package dev.charter.feature.anime.my

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.feature.anime.components.SectionHeader

/**
 * Hub screen — static by design: every row is a doorway, the content lives
 * in the destinations. Disabled rows stay visible (with reason) instead of
 * disappearing: the prototype's shape, honestly labeled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen(
    onOpenHistory: () -> Unit,
    onOpenCollection: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("我的") }) },
    ) { innerPadding ->
        Column(
            Modifier.padding(innerPadding).fillMaxSize(),
        ) {
            Column(Modifier.padding(horizontal = spacing.md, vertical = spacing.lg)) {
                Text(
                    text = "Charter",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(spacing.xs))
                Text(
                    text = "番剧浏览参考应用 · 产品原型借鉴 Kazumi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SectionHeader(
                title = "内容库",
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            )
            ListItem(
                headlineContent = { Text("历史记录") },
                supportingContent = { Text("观看进度与续播") },
                leadingContent = { Icon(Icons.Filled.List, contentDescription = null) },
                trailingContent = { NavChevron() },
                modifier = Modifier.clickable(onClick = onOpenHistory),
            )
            ListItem(
                headlineContent = { Text("追番收藏") },
                supportingContent = { Text("在看 / 想看 / 搁置 / 看过 / 抛弃") },
                leadingContent = { Icon(Icons.Filled.FavoriteBorder, contentDescription = null) },
                trailingContent = { NavChevron() },
                modifier = Modifier.clickable(onClick = onOpenCollection),
            )
            Spacer(Modifier.height(spacing.md))
            SectionHeader(
                title = "更多",
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            )
            DisabledRow(title = "离线下载", reason = "演示原型未包含")
            DisabledRow(title = "设置", reason = "演示原型未包含")
            ListItem(
                headlineContent = { Text("关于") },
                supportingContent = { Text("原型归属与数据来源") },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                trailingContent = { NavChevron() },
                modifier = Modifier.clickable(onClick = onOpenAbout),
            )
        }
    }
}

@Composable
private fun NavChevron() {
    Icon(
        imageVector = Icons.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DisabledRow(
    title: String,
    reason: String,
) {
    val disabled = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(reason) },
        colors =
            ListItemDefaults.colors(
                headlineColor = disabled,
                supportingColor = disabled,
            ),
    )
}

private const val DISABLED_ALPHA = 0.38f
