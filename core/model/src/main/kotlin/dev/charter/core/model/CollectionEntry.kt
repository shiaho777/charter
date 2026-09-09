package dev.charter.core.model

/** A collected (followed) subject with denormalized display fields. */
data class CollectionEntry(
    val bangumiId: Long,
    val name: String,
    val nameCn: String?,
    val imageUrl: String?,
    val ratingScore: Float?,
    val status: CollectStatus,
    val updatedAt: Long,
) {
    val displayName: String
        get() = nameCn?.takeIf { it.isNotBlank() } ?: name
}
