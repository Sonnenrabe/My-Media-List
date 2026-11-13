package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppColorSettingsManager
import com.example.data.RatingMode
import com.example.localization.LocalAppStrings
import com.example.notification.TimerTarget
import com.example.notification.TimerNotificationManager
import com.example.ui.components.BookEditDialog
import com.example.ui.components.EditGameDialog
import com.example.ui.components.EditNotesDialog
import com.example.ui.components.GameDetailDialog
import com.example.ui.components.MangaEditDialog
import com.example.ui.components.ManualAdjustTimeDialog
import com.example.ui.components.SeriesAnimeEditDialog
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.GameDarkSurfaceHighlight
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppDestination
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.PlayableEntry
import java.util.Locale

/**
 * Dedicated PlayTime Tracker Tab (Accessible via Hamburger Menu).
 * Displays pinned entries from all categories marked as Game Category for quick one-tap tracking,
 * live session tracking, notification bar sync, and 30-day activity chart.
 */
@Composable
fun PlayTimeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val pinnedEntries by viewModel.pinnedPlayableEntries.collectAsStateWithLifecycle()
    val runningEntry by viewModel.runningPlayableEntry.collectAsStateWithLifecycle()
    val runningTarget by viewModel.runningTarget.collectAsStateWithLifecycle()
    val elapsedSessionSeconds by viewModel.elapsedSessionSeconds.collectAsStateWithLifecycle()
    val dailyPlaytimeByGame by viewModel.dailyPlaytimeByGame.collectAsStateWithLifecycle()
    val dailyPlaytimeByMedia by viewModel.dailyPlaytimeByMedia.collectAsStateWithLifecycle()

    val allActiveGamesAllCategories by viewModel.allActiveGamesAllCategories.collectAsStateWithLifecycle()
    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()
    val allAnime by viewModel.allAnime.collectAsStateWithLifecycle()
    val allManga by viewModel.allManga.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()

    val categoryColors by viewModel.categoryColors.collectAsStateWithLifecycle()
    val gameRatingColors by viewModel.gameRatingColors.collectAsStateWithLifecycle()

    var selectedTargetForDetails by remember { mutableStateOf<TimerTarget?>(null) }
    var targetToAdjust by remember { mutableStateOf<PlayableEntry?>(null) }
    var targetToEditNotes by remember { mutableStateOf<PlayableEntry?>(null) }
    var targetToEditDetails by remember { mutableStateOf<PlayableEntry?>(null) }
    var pendingEntryToPlay by remember { mutableStateOf<PlayableEntry?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        pendingEntryToPlay?.let { entry ->
            viewModel.playTarget(
                categoryKey = entry.categoryKey,
                itemId = entry.id,
                title = entry.title,
                notes = entry.notes,
                colorHex = entry.colorHex,
                totalPlaytimeSeconds = entry.totalPlaytimeSeconds,
                context = context
            )
            pendingEntryToPlay = null
        }
    }

    fun startPlayingWithNotificationCheck(entry: PlayableEntry) {
        val areNotificationsEnabled = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!areNotificationsEnabled) {
            Toast.makeText(
                context,
                strings.enableNotificationsToast,
                Toast.LENGTH_LONG
            ).show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                pendingEntryToPlay = entry
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        viewModel.playTarget(
            categoryKey = entry.categoryKey,
            itemId = entry.id,
            title = entry.title,
            notes = entry.notes,
            colorHex = entry.colorHex,
            totalPlaytimeSeconds = entry.totalPlaytimeSeconds,
            context = context
        )
    }

    fun getDailyRecordsForEntry(entry: PlayableEntry) =
        when (entry.categoryKey) {
            AppColorSettingsManager.KEY_SERIEN,
            AppColorSettingsManager.KEY_ANIME,
            AppColorSettingsManager.KEY_MANGA,
            AppColorSettingsManager.KEY_BUECHER -> dailyPlaytimeByMedia[entry.categoryKey to entry.id] ?: emptyList()
            else -> dailyPlaytimeByGame[entry.id] ?: emptyList()
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameDarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val areNotifsAllowed = remember(runningTarget) {
                TimerNotificationManager.areNotificationsAllowed(context)
            }
            if (!areNotifsAllowed) {
                Surface(
                    color = Color(0xFF38151E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { TimerNotificationManager.openNotificationSettings(context) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = NeonRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.notificationsDisabledBanner,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            runningEntry?.let { activeEntry ->
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
                    label = "pulseAlpha"
                )

                Surface(
                    color = GameDarkSurfaceHighlight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            selectedTargetForDetails = TimerTarget(activeEntry.categoryKey, activeEntry.id)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                                    .alpha(pulseAlpha)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.currentlyRunning(activeEntry.title),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = activeEntry.asGameSnapshot.formattedPlaytime(elapsedSessionSeconds),
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = NeonGreen
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.pauseGame(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = Color(0xFF10131E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = strings.pauseUpper, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.pauseUpper, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Surface(
                color = Color(0xFF002026),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PushPin, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.playTimeInfoBanner,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (pinnedEntries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GameDarkSurfaceVariant,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = strings.noPinnedGamesTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.noPinnedGamesSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.navigateTo(AppDestination.GAMES) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF003844),
                            contentColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.goToGames, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pinnedEntries, key = { "${it.categoryKey}:${it.id}" }) { entry ->
                        val isRunning = runningTarget?.categoryKey == entry.categoryKey && runningTarget?.itemId == entry.id
                        val displayPlaytime = if (isRunning) {
                            entry.asGameSnapshot.formattedPlaytime(elapsedSessionSeconds)
                        } else {
                            entry.asGameSnapshot.formattedPlaytime(0L)
                        }

                        val indicatorColor = viewModel.getIndicatorColorForRating(
                            ratingMode = entry.ratingMode,
                            wordRating = entry.ratingWord,
                            starRating = entry.starRating,
                            fallback = Color(0xFF475569)
                        )
                        val catAccent = Color(entry.categoryAccentHex)

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isRunning) GameDarkSurfaceHighlight else GameDarkSurface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isRunning) 2.dp else 1.dp,
                                color = if (isRunning) NeonGreen else indicatorColor.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedTargetForDetails = TimerTarget(entry.categoryKey, entry.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .size(width = 5.dp, height = 40.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(indicatorColor)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                lineHeight = 18.sp
                                            ),
                                            color = TextPrimary,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (entry.categoryKey != AppColorSettingsManager.KEY_SPIELE) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = catAccent.copy(alpha = 0.16f)
                                                ) {
                                                    Text(
                                                        text = entry.categoryDisplayName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = catAccent,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (entry.ratingMode == RatingMode.WORDS) {
                                                val displayRating = viewModel.resolveDisplayRatingWord(entry.ratingWord)
                                                if (displayRating.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = indicatorColor.copy(alpha = 0.18f),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, indicatorColor.copy(alpha = 0.35f))
                                                    ) {
                                                        Text(
                                                            text = displayRating,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = indicatorColor,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            } else {
                                                if (entry.starRating > 0.0) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = indicatorColor.copy(alpha = 0.18f),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, indicatorColor.copy(alpha = 0.35f))
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = indicatorColor,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = String.format(Locale.US, "%.1f", entry.starRating),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = indicatorColor,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isRunning) NeonGreen.copy(alpha = 0.15f) else GameDarkSurfaceVariant
                                            ) {
                                                Text(
                                                    text = displayPlaytime,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = if (isRunning) NeonGreen else NeonCyan,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        if (isRunning) {
                                            viewModel.pauseGame(context)
                                        } else {
                                            startPlayingWithNotificationCheck(entry)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRunning) NeonAmber else Color(entry.colorHex),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isRunning) strings.pauseUpper else strings.playUpper,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isRunning) strings.pauseUpper else strings.trackUpper,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { viewModel.toggleTargetPinned(entry.categoryKey, entry.id, false) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = strings.unpinFromPlayTimeDesc,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedTargetForDetails?.let { target ->
        val currentEntry = pinnedEntries.find { it.categoryKey == target.categoryKey && it.id == target.itemId }
            ?: runningEntry?.takeIf { it.categoryKey == target.categoryKey && it.id == target.itemId }
        if (currentEntry == null) {
            selectedTargetForDetails = null
        } else {
            val isRunning = runningTarget?.categoryKey == currentEntry.categoryKey && runningTarget?.itemId == currentEntry.id
            GameDetailDialog(
                game = currentEntry.asGameSnapshot,
                isRunning = isRunning,
                elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
                dailyRecords = getDailyRecordsForEntry(currentEntry),
                onTogglePlayPause = {
                    if (isRunning) {
                        viewModel.pauseGame(context)
                    } else {
                        startPlayingWithNotificationCheck(currentEntry)
                    }
                },
                onOpenAdjustTime = { targetToAdjust = currentEntry },
                onOpenEditNotes = { targetToEditNotes = currentEntry },
                onOpenEditDetails = { targetToEditDetails = currentEntry },
                onDismiss = { selectedTargetForDetails = null },
                indicatorColor = viewModel.getIndicatorColorForRating(
                    ratingMode = currentEntry.ratingMode,
                    wordRating = currentEntry.ratingWord,
                    starRating = currentEntry.starRating,
                    fallback = Color(currentEntry.colorHex)
                )
            )
        }
    }

    targetToEditDetails?.let { entry ->
        val accent = Color(categoryColors[entry.categoryKey]?.accentColorHex ?: entry.categoryAccentHex)
        when (entry.categoryKey) {
            AppColorSettingsManager.KEY_SERIEN -> {
                allSeries.firstOrNull { it.id == entry.id }?.let { item ->
                    SeriesAnimeEditDialog(
                        initialName = item.name,
                        initialSeason = item.season,
                        initialRating = item.rating,
                        initialRatingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
                        initialNotes = item.notes,
                        ratingMode = entry.ratingMode,
                        ratingWords = gameRatingColors,
                        accentColor = accent,
                        isAnime = false,
                        isEdit = true,
                        onSave = { name, season, rating, ratingWord, notes ->
                            viewModel.updateSeries(item.copy(name = name, season = season, rating = rating, ratingWord = ratingWord, notes = notes))
                            targetToEditDetails = null
                        },
                        onDelete = {
                            viewModel.deleteTargetItem(entry.categoryKey, entry.id)
                            targetToEditDetails = null
                            selectedTargetForDetails = null
                        },
                        onDismiss = { targetToEditDetails = null }
                    )
                }
            }
            AppColorSettingsManager.KEY_ANIME -> {
                allAnime.firstOrNull { it.id == entry.id }?.let { item ->
                    SeriesAnimeEditDialog(
                        initialName = item.name,
                        initialSeason = item.season,
                        initialRating = item.rating,
                        initialRatingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
                        initialNotes = item.notes,
                        ratingMode = entry.ratingMode,
                        ratingWords = gameRatingColors,
                        accentColor = accent,
                        isAnime = true,
                        isEdit = true,
                        onSave = { name, season, rating, ratingWord, notes ->
                            viewModel.updateAnime(item.copy(name = name, season = season, rating = rating, ratingWord = ratingWord, notes = notes))
                            targetToEditDetails = null
                        },
                        onDelete = {
                            viewModel.deleteTargetItem(entry.categoryKey, entry.id)
                            targetToEditDetails = null
                            selectedTargetForDetails = null
                        },
                        onDismiss = { targetToEditDetails = null }
                    )
                }
            }
            AppColorSettingsManager.KEY_MANGA -> {
                allManga.firstOrNull { it.id == entry.id }?.let { item ->
                    MangaEditDialog(
                        initialTitle = item.title,
                        initialChapter = item.chapter,
                        initialRating = item.rating,
                        initialRatingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
                        initialNotes = item.notes,
                        ratingMode = entry.ratingMode,
                        ratingWords = gameRatingColors,
                        accentColor = accent,
                        isEdit = true,
                        onSave = { title, chapter, rating, ratingWord, notes ->
                            viewModel.updateManga(item.copy(title = title, chapter = chapter, rating = rating, ratingWord = ratingWord, notes = notes))
                            targetToEditDetails = null
                        },
                        onDelete = {
                            viewModel.deleteTargetItem(entry.categoryKey, entry.id)
                            targetToEditDetails = null
                            selectedTargetForDetails = null
                        },
                        onDismiss = { targetToEditDetails = null }
                    )
                }
            }
            AppColorSettingsManager.KEY_BUECHER -> {
                allBooks.firstOrNull { it.id == entry.id }?.let { item ->
                    BookEditDialog(
                        initialTitle = item.title,
                        initialAuthor = item.author,
                        initialRating = item.rating,
                        initialRatingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
                        initialNotes = item.notes,
                        ratingMode = entry.ratingMode,
                        ratingWords = gameRatingColors,
                        accentColor = accent,
                        isEdit = true,
                        onSave = { title, author, rating, ratingWord, notes ->
                            viewModel.updateBook(item.copy(title = title, author = author, rating = rating, ratingWord = ratingWord, notes = notes))
                            targetToEditDetails = null
                        },
                        onDelete = {
                            viewModel.deleteTargetItem(entry.categoryKey, entry.id)
                            targetToEditDetails = null
                            selectedTargetForDetails = null
                        },
                        onDismiss = { targetToEditDetails = null }
                    )
                }
            }
            else -> {
                val liveGame = allActiveGamesAllCategories.firstOrNull { it.id == entry.id } ?: entry.asGameSnapshot
                EditGameDialog(
                    game = liveGame.copy(rating = viewModel.resolveDisplayRatingWord(liveGame.rating)),
                    ratingMode = entry.ratingMode,
                    ratingWords = gameRatingColors,
                    onSave = { newTitle, newRating, newStarRating, newColorHex, isPinned ->
                        viewModel.updateGameDetails(
                            gameId = liveGame.id,
                            newTitle = newTitle,
                            newRating = newRating,
                            newColorHex = newColorHex,
                            isPinned = isPinned,
                            context = context,
                            newStarRating = newStarRating
                        )
                        targetToEditDetails = null
                        Toast.makeText(context, strings.updatedToast(newTitle), Toast.LENGTH_SHORT).show()
                    },
                    onDelete = {
                        viewModel.deleteTargetItem(entry.categoryKey, entry.id)
                        targetToEditDetails = null
                        selectedTargetForDetails = null
                    },
                    onDismiss = { targetToEditDetails = null }
                )
            }
        }
    }

    targetToAdjust?.let { entry ->
        ManualAdjustTimeDialog(
            game = entry.asGameSnapshot,
            onConfirm = { newSeconds ->
                viewModel.adjustTargetPlaytime(entry.categoryKey, entry.id, newSeconds)
                targetToAdjust = null
            },
            onDismiss = { targetToAdjust = null }
        )
    }

    targetToEditNotes?.let { entry ->
        EditNotesDialog(
            game = entry.asGameSnapshot,
            onSaveNotes = { newNotes ->
                viewModel.updateTargetNotes(entry.categoryKey, entry.id, newNotes)
                targetToEditNotes = null
                Toast.makeText(context, strings.savedToast, Toast.LENGTH_SHORT).show()
            },
            onDismiss = { targetToEditNotes = null }
        )
    }
}
