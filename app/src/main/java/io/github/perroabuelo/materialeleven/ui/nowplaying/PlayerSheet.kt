package io.github.perroabuelo.materialeleven.ui.nowplaying

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import io.github.perroabuelo.materialeleven.ui.player.PlayerState
import io.github.perroabuelo.materialeleven.ui.theme.ElevenColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class SheetValue { Collapsed, Expanded }

/**
 * The player docked under the lists. Tapping or dragging the mini player up opens Now Playing;
 * dragging down or the system Back closes it. [expandRequests] opens it each time it grows, as the
 * notification asks. [onExpandedChange] reports where the sheet is heading.
 */
@Composable
fun PlayerSheet(
    state: PlayerState,
    actions: PlayerActions,
    expandRequests: Int,
    onExpandedChange: (Boolean) -> Unit = {},
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val sheet = remember { AnchoredDraggableState(SheetValue.Collapsed) }
    val navigationBarPx = WindowInsets.navigationBars.getBottom(density)

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val fullHeight = constraints.maxHeight.toFloat()
        val collapsedOffset = fullHeight - with(density) { MiniPlayerHeight.toPx() } - navigationBarPx
        SideEffect {
            sheet.updateAnchors(
                DraggableAnchors {
                    SheetValue.Collapsed at collapsedOffset
                    SheetValue.Expanded at 0f
                },
            )
        }

        LaunchedEffect(expandRequests) {
            if (expandRequests > 0) sheet.animateTo(SheetValue.Expanded)
        }
        LaunchedEffect(sheet.targetValue) { onExpandedChange(sheet.targetValue == SheetValue.Expanded) }
        BackHandler(enabled = sheet.targetValue == SheetValue.Expanded) {
            scope.launch { sheet.animateTo(SheetValue.Collapsed) }
        }

        val offset = sheet.offset.takeUnless { it.isNaN() } ?: collapsedOffset
        val progress = if (collapsedOffset > 0f) (1f - offset / collapsedOffset).coerceIn(0f, 1f) else 0f
        val drag = Modifier.anchoredDraggable(sheet, Orientation.Vertical)

        Box(
            Modifier
                .offset { IntOffset(0, offset.roundToInt()) }
                .fillMaxSize()
                .background(lerp(ElevenColors.Surface, ElevenColors.Background, progress)),
        ) {
            if (progress > 0.01f) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .alpha(progress)
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                ) {
                    NowPlayingScreen(
                        state = state,
                        actions = actions,
                        onCollapse = { scope.launch { sheet.animateTo(SheetValue.Collapsed) } },
                        dragHandle = drag,
                    )
                }
            }
            if (progress < 0.99f) {
                MiniPlayer(
                    state = state,
                    onTogglePlay = actions.togglePlay,
                    onNext = actions.next,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .alpha((1f - progress * 3f).coerceIn(0f, 1f))
                        .then(drag)
                        .clickable { scope.launch { sheet.animateTo(SheetValue.Expanded) } },
                )
            }
        }
    }
}
