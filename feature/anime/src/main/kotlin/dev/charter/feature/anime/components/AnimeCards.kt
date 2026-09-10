package dev.charter.feature.anime.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.charter.core.designsystem.motion.pressableClickable
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.model.Bangumi

/** Poster cards follow the 5:7 ratio of Bangumi cover art. */
private const val POSTER_RATIO = 5f / 7f
private val THUMB_WIDTH = 56.dp
private const val THUMB_RATIO = 5f / 7f

@Composable
fun AnimePosterCard(
    bangumi: Bangumi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    // pressableClickable: Card(onClick=) hardcodes ripple (designsystem trap).
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.pressableClickable(onClick = onClick),
    ) {
        Column {
            PosterImage(
                imageUrl = bangumi.imageUrl,
                contentDescription = bangumi.displayName,
                modifier = Modifier.fillMaxWidth().aspectRatio(POSTER_RATIO),
            )
            Column(Modifier.padding(spacing.sm)) {
                Text(
                    text = bangumi.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                metaLine(bangumi)?.let { meta ->
                    Spacer(Modifier.height(spacing.xs))
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Score · rank · year — the one-line summary under a poster. */
fun metaLine(bangumi: Bangumi): String? {
    val score = bangumi.rating?.score?.let { "★ $it" }
    val rank = bangumi.rank?.let { "#$it" }
    val year = bangumi.airDate?.take(4)
    return listOfNotNull(score, rank, year).joinToString(" · ").ifBlank { null }
}

@Composable
private fun PosterImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun AnimePosterGrid(
    items: List<Bangumi>,
    columns: Int,
    onOpenBangumi: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    footer: (LazyGridScope.() -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        items(items = items, key = { it.id }) { bangumi ->
            AnimePosterCard(
                bangumi = bangumi,
                onClick = { onOpenBangumi(bangumi.id) },
                modifier = Modifier.animateItem(),
            )
        }
        footer?.invoke(this)
    }
}

/** Display data for [MediaRowCard] — a parameter object keeps the composable under the parameter ceiling. */
data class MediaRow(
    val imageUrl: String?,
    val title: String,
    val subtitle: String?,
    /** 0f..1f watch progress; renders a thin bar under the subtitle. */
    val progress: Float? = null,
)

/**
 * Horizontal media row: thumbnail + title + subtitle (+ optional watch
 * progress and trailing slot). Shared by the collection and history.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaRowCard(
    row: MediaRow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(
            Modifier.padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            PosterImage(
                imageUrl = row.imageUrl,
                contentDescription = row.title,
                modifier = Modifier.width(THUMB_WIDTH).aspectRatio(THUMB_RATIO).clip(RoundedCornerShape(spacing.sm)),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = row.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (row.subtitle != null) {
                    Spacer(Modifier.height(spacing.xs))
                    Text(
                        text = row.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (row.progress != null) {
                    Spacer(Modifier.height(spacing.sm))
                    LinearProgressIndicator(
                        progress = { row.progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(spacing.xs),
                    )
                }
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}
