package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Game
import com.example.data.RatingMode
import com.example.localization.LocalAppStrings
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.GameDarkSurfaceHighlight
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale

fun getRatingBadgeColor(rating: String): Pair<Color, Color> {
    return when (rating) {
        "Überragend", "Outstanding", "Masterpiece" -> Pair(Color(0xFFFFD700), Color(0xFFFFD700).copy(alpha = 0.18f))
        "Sehr Gut", "Very Good" -> Pair(NeonGreen, NeonGreen.copy(alpha = 0.18f))
        "Gut", "Good" -> Pair(NeonCyan, NeonCyan.copy(alpha = 0.18f))
        "Befriedigend", "Satisfactory", "Decent" -> Pair(Color(0xFF9E86FF), Color(0xFF9E86FF).copy(alpha = 0.18f))
        "Spielbar", "Playable" -> Pair(Color(0xFFFFB300), Color(0xFFFFB300).copy(alpha = 0.18f))
        "Unspielbar", "Unplayable" -> Pair(NeonRed, NeonRed.copy(alpha = 0.18f))
        else -> Pair(TextSecondary, Color.White.copy(alpha = 0.08f))
    }
}

/**
 * Active game card with name, formatted playtime, rating badge (Word or Star), pin toggle, and drag handle.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActiveGameCard(
    game: Game,
    isRunning: Boolean,
    elapsedSessionSeconds: Long,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    ratingMode: RatingMode = RatingMode.WORDS,
    displayRatingWord: String = game.rating,
    indicatorColor: Color? = null,
    onTogglePin: (() -> Unit)? = null,
    showPlaytimeUi: Boolean = true,
    isBeingDragged: Boolean = false,
    dragHandleModifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val displayPlaytime = if (isRunning) {
        game.formattedPlaytime(elapsedSessionSeconds)
    } else {
        game.formattedPlaytime(0L)
    }

    val borderColor = when {
        isBeingDragged -> NeonCyan
        isRunning -> NeonGreen
        else -> (indicatorColor ?: Color(0xFF334155)).copy(alpha = 0.35f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isBeingDragged || isRunning) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("game_card_${game.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isBeingDragged -> Color(0xFF16253B)
                isRunning -> GameDarkSurfaceHighlight
                else -> GameDarkSurface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBeingDragged) 6.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Color indicator bar & Title & Badges
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .size(width = 5.dp, height = 40.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(indicatorColor ?: Color(0xFF475569))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        ),
                        color = TextPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val badgeColor = indicatorColor ?: NeonCyan
                        if (ratingMode == RatingMode.WORDS) {
                            if (displayRatingWord.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = badgeColor.copy(alpha = 0.18f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = displayRatingWord,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        } else {
                            if (game.starRating > 0.0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = badgeColor.copy(alpha = 0.18f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = badgeColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = String.format(Locale.US, "%.1f", game.starRating),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (isRunning) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PulsingTimerDot()
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.timerRunning,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else if (game.notes.isNotBlank()) {
                            Text(
                                text = game.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right: Pin button, Playtime badge & Drag Handle
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onTogglePin != null) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (game.isPinnedToPlaytime) strings.pinnedToPlayTimeDesc else strings.pinToPlayTimeDesc,
                            tint = if (game.isPinnedToPlaytime) NeonCyan else TextTertiary.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (showPlaytimeUi) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isRunning) NeonGreen.copy(alpha = 0.15f) else GameDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isRunning) NeonGreen.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Text(
                            text = displayPlaytime,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isRunning) NeonGreen else Color(game.colorHex),
                            modifier = Modifier
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                .testTag("playtime_text_${game.id}")
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )

                // Drag Handle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .then(dragHandleModifier)
                        .testTag("drag_handle_${game.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = strings.reorderPositionDesc,
                        tint = if (isBeingDragged) NeonCyan else TextTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PulsingTimerDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(NeonGreen)
            .alpha(pulseAlpha)
    )
}
