package dev.charter.core.model

/**
 * The five-state collection taxonomy from the product prototype (Kazumi):
 * a subject is never just "favorited" — the state says where in the watching
 * lifecycle the user is. Codes mirror the prototype's persistence values;
 * they are protocol, not tunables.
 */
@Suppress("MagicNumber")
enum class CollectStatus(
    val code: Int,
    val label: String,
) {
    WATCHING(1, "在看"),
    WANT(2, "想看"),
    ON_HOLD(3, "搁置"),
    WATCHED(4, "看过"),
    DROPPED(5, "抛弃"),
    ;

    companion object {
        fun fromCode(code: Int): CollectStatus? = entries.firstOrNull { it.code == code }
    }
}
