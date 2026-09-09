package dev.charter.catalog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.charter.catalog.registry.CatalogEntryCard
import dev.charter.catalog.registry.catalogEntries
import dev.charter.core.designsystem.tokens.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { TopAppBar(title = { Text("Charter Catalog") }) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.lg),
        ) {
            items(catalogEntries, key = { it.name }) { entry ->
                CatalogEntryCard(entry)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = LocalSpacing.current.md),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}
