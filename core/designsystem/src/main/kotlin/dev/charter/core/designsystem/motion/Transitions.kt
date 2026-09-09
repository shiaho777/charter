package dev.charter.core.designsystem.motion

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment

/**
 * Named transition patterns — the choreography vocabulary. Use these instead
 * of hand-rolling enter/exit pairs; if a screen needs a pattern that isn't
 * here, add it here first (with a reason), then use it.
 *
 * Slide fractions are "share of full size" travel distances; scale dips are
 * subtle depth cues (0.97–0.98 — felt, not seen).
 */
object Transitions {
    private const val SCALE_DIP = 0.98f
    private const val LIST_SCALE_DIP = 0.97f
    private const val Y_ENTER_SLIDE_FRACTION = 14
    private const val Y_EXIT_SLIDE_FRACTION = 18
    private const val X_ENTER_SLIDE_FRACTION = 16
    private const val X_EXIT_SLIDE_FRACTION = 20
    private const val TEXT_SWAP_FRACTION = 10

    /** Default screen change: cross-fade through a subtle scale dip. */
    fun fadeThrough() =
        (
            fadeIn(animationSpec = Motion.enterTween) +
                scaleIn(initialScale = SCALE_DIP, animationSpec = Motion.softFloat)
        ) togetherWith (
            fadeOut(animationSpec = Motion.exitTween) +
                scaleOut(targetScale = SCALE_DIP, animationSpec = Motion.softFloat)
        )

    /** Hierarchical navigation along the vertical axis. */
    fun sharedAxisY(forward: Boolean = true) =
        (
            fadeIn(animationSpec = Motion.enterTween) +
                slideInVertically(animationSpec = Motion.enterOffset) {
                    if (forward) it / Y_ENTER_SLIDE_FRACTION else -it / Y_ENTER_SLIDE_FRACTION
                }
        ) togetherWith (
            fadeOut(animationSpec = Motion.exitTween) +
                slideOutVertically(animationSpec = Motion.exitOffset) {
                    if (forward) -it / Y_EXIT_SLIDE_FRACTION else it / Y_EXIT_SLIDE_FRACTION
                }
        )

    /** Peer-level navigation along the horizontal axis (tabs, pages). */
    fun sharedAxisX(forward: Boolean = true) =
        (
            fadeIn(animationSpec = Motion.enterTween) +
                slideInHorizontally(animationSpec = Motion.enterOffset) {
                    if (forward) it / X_ENTER_SLIDE_FRACTION else -it / X_ENTER_SLIDE_FRACTION
                }
        ) togetherWith (
            fadeOut(animationSpec = Motion.exitTween) +
                slideOutHorizontally(animationSpec = Motion.exitOffset) {
                    if (forward) -it / X_EXIT_SLIDE_FRACTION else it / X_EXIT_SLIDE_FRACTION
                }
        )

    /** List items appearing (insert, filter change, section reveal). */
    fun listEnter() =
        fadeIn(animationSpec = Motion.enterTween) +
            expandVertically(
                animationSpec = Motion.expandSize,
                expandFrom = Alignment.Top,
                clip = false,
            ) +
            scaleIn(initialScale = LIST_SCALE_DIP, animationSpec = Motion.softFloat)

    /** List items leaving (delete, filter-out). */
    fun listExit() =
        fadeOut(animationSpec = Motion.exitTween) +
            shrinkVertically(
                animationSpec = Motion.expandSize,
                shrinkTowards = Alignment.Top,
                clip = false,
            ) +
            scaleOut(targetScale = LIST_SCALE_DIP, animationSpec = Motion.softFloat)

    /** In-place value swap (counters, labels that change). */
    fun textSwap() =
        (
            fadeIn(animationSpec = Motion.enterTween) +
                slideInVertically(animationSpec = Motion.enterOffset) { it / TEXT_SWAP_FRACTION }
        ) togetherWith (
            fadeOut(animationSpec = Motion.exitTween) +
                slideOutVertically(animationSpec = Motion.exitOffset) { -it / TEXT_SWAP_FRACTION }
        )
}
