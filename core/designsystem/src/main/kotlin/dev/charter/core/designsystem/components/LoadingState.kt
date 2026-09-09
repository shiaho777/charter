package dev.charter.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.charter.core.designsystem.tokens.LocalSpacing

/**
 * Loading screen anatomy. `animated` controls the morphing indicator;
 * screenshot goldens and previews pass `animated = false` to pin a static
 * frame — the same component serves both (see CharterLoadingIndicator).
 */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    label: String = "Loading…",
    animated: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(LocalSpacing.current.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.sm, Alignment.CenterVertically),
    ) {
        CharterLoadingIndicator(animated = animated)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
