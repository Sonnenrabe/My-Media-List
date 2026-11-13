package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppColorSettingsManager
import com.example.data.AutoBackupManager
import com.example.localization.AppStringsProvider
import com.example.localization.LocalAppStrings
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.AppDestination
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val categoryColors by viewModel.categoryColors.collectAsStateWithLifecycle()
    val strings = remember(appLanguage) { AppStringsProvider.get(appLanguage) }

    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val selectedCustomCategoryKey by viewModel.selectedCustomCategoryKey.collectAsStateWithLifecycle()
    val orderedCategoryKeys by viewModel.orderedCategoryKeys.collectAsStateWithLifecycle()
    val activeCountsByCategory by viewModel.activeCountsByCategory.collectAsStateWithLifecycle()
    val runningTarget by viewModel.runningTarget.collectAsStateWithLifecycle()
    val pinnedEntries by viewModel.pinnedPlayableEntries.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val allActiveGames by viewModel.allActiveGames.collectAsStateWithLifecycle()
    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()
    val allAnime by viewModel.allAnime.collectAsStateWithLifecycle()
    val allManga by viewModel.allManga.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()

    var showSearchBar by remember { mutableStateOf(false) }

    // Automatic backup executed once when opening the app for the first time each day
    LaunchedEffect(Unit) {
        if (AutoBackupManager.shouldRunDailyBackup(context)) {
            val csv = viewModel.exportDataAsExcelCsv()
            val result = AutoBackupManager.runDailyBackup(context, csv, force = false)
            result.onSuccess {
                Toast.makeText(
                    context,
                    strings.dailyAutoBackupToast(AutoBackupManager.BACKUP_FILE_NAME),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Handle back button: close drawer first, or navigate to GAMES before exiting
    BackHandler(enabled = drawerState.isOpen || currentDestination != AppDestination.GAMES) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            viewModel.navigateTo(AppDestination.GAMES)
        }
    }

    CompositionLocalProvider(LocalAppStrings provides strings) {
        val gamesLabel = viewModel.getCategoryDisplayName(AppColorSettingsManager.KEY_SPIELE, strings)
        val seriesLabel = viewModel.getCategoryDisplayName(AppColorSettingsManager.KEY_SERIEN, strings)
        val animeLabel = viewModel.getCategoryDisplayName(AppColorSettingsManager.KEY_ANIME, strings)
        val mangaLabel = viewModel.getCategoryDisplayName(AppColorSettingsManager.KEY_MANGA, strings)
        val booksLabel = viewModel.getCategoryDisplayName(AppColorSettingsManager.KEY_BUECHER, strings)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = GameDarkSurface,
                    modifier = Modifier
                        .width(310.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Drawer Header
                    Surface(
                        color = GameDarkSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = PrimaryViolet,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "MY MEDIA LIST",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        letterSpacing = 1.5.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = strings.appSubtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (runningTarget != null) {
                                Spacer(modifier = Modifier.height(14.dp))
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val pulseAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.35f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
                                    label = "pulseAlpha"
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonGreen.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.navigateTo(AppDestination.PLAYTIME)
                                            coroutineScope.launch { drawerState.close() }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(NeonGreen)
                                                .alpha(pulseAlpha)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = strings.timerActiveDrawer,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Navigation Items (Built-in + Custom Categories)
                    orderedCategoryKeys.forEach { catKey ->
                        val catConfig = categoryColors[catKey]
                        val catColor = Color(catConfig?.accentColorHex ?: 0xFF818CF8L)
                        val catLabel = viewModel.getCategoryDisplayName(catKey, strings)
                        val icon: ImageVector
                        val badgeCount: Int
                        val isSelected: Boolean
                        val onItemClick: () -> Unit

                        when (catKey) {
                            AppColorSettingsManager.KEY_SPIELE -> {
                                icon = Icons.Default.SportsEsports
                                badgeCount = activeCountsByCategory[catKey] ?: allActiveGames.size
                                isSelected = (currentDestination == AppDestination.GAMES)
                                onItemClick = { viewModel.navigateTo(AppDestination.GAMES) }
                            }
                            AppColorSettingsManager.KEY_SERIEN -> {
                                icon = Icons.Default.Tv
                                badgeCount = allSeries.size
                                isSelected = (currentDestination == AppDestination.SERIES)
                                onItemClick = { viewModel.navigateTo(AppDestination.SERIES) }
                            }
                            AppColorSettingsManager.KEY_ANIME -> {
                                icon = Icons.Default.Tv
                                badgeCount = allAnime.size
                                isSelected = (currentDestination == AppDestination.ANIME)
                                onItemClick = { viewModel.navigateTo(AppDestination.ANIME) }
                            }
                            AppColorSettingsManager.KEY_MANGA -> {
                                icon = Icons.Default.AutoStories
                                badgeCount = allManga.size
                                isSelected = (currentDestination == AppDestination.MANGA)
                                onItemClick = { viewModel.navigateTo(AppDestination.MANGA) }
                            }
                            AppColorSettingsManager.KEY_BUECHER -> {
                                icon = Icons.Default.Book
                                badgeCount = allBooks.size
                                isSelected = (currentDestination == AppDestination.BOOKS)
                                onItemClick = { viewModel.navigateTo(AppDestination.BOOKS) }
                            }
                            else -> {
                                icon = if (catConfig?.isGameCategory == true) Icons.Default.SportsEsports else Icons.Default.Category
                                badgeCount = activeCountsByCategory[catKey] ?: 0
                                isSelected = (currentDestination == AppDestination.CUSTOM_CATEGORY && selectedCustomCategoryKey == catKey)
                                onItemClick = { viewModel.navigateToCustomCategory(catKey) }
                            }
                        }

                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) catColor else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = catLabel,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            badge = {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) catColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f)
                                ) {
                                    Text(
                                        text = "$badgeCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) catColor else TextTertiary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            },
                            selected = isSelected,
                            onClick = {
                                onItemClick()
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = catColor.copy(alpha = 0.16f),
                                unselectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }

                    HorizontalDivider(
                        color = GameDarkSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )

                    // PlayTime item
                    val isPlayTimeSelected = (currentDestination == AppDestination.PLAYTIME)
                    val playTimeColor = if (runningTarget != null) NeonGreen else NeonCyan
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isPlayTimeSelected) playTimeColor else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = strings.navPlayTime,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = if (isPlayTimeSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = if (isPlayTimeSelected) TextPrimary else TextSecondary
                            )
                        },
                        badge = {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPlayTimeSelected) playTimeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f)
                            ) {
                                Text(
                                    text = if (runningTarget != null) strings.liveBadge else strings.pinnedCount(pinnedEntries.size),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPlayTimeSelected) playTimeColor else TextTertiary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        },
                        selected = isPlayTimeSelected,
                        onClick = {
                            viewModel.navigateTo(AppDestination.PLAYTIME)
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = playTimeColor.copy(alpha = 0.16f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // Backup & Export item
                    val isBackupSelected = (currentDestination == AppDestination.BACKUP)
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = if (isBackupSelected) PrimaryViolet else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = strings.navBackup,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = if (isBackupSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = if (isBackupSelected) TextPrimary else TextSecondary
                            )
                        },
                        selected = isBackupSelected,
                        onClick = {
                            viewModel.navigateTo(AppDestination.BACKUP)
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PrimaryViolet.copy(alpha = 0.16f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // Settings item
                    val isSettingsSelected = (currentDestination == AppDestination.SETTINGS)
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = if (isSettingsSelected) NeonPurple else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = strings.navSettings,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = if (isSettingsSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = if (isSettingsSelected) TextPrimary else TextSecondary
                            )
                        },
                        selected = isSettingsSelected,
                        onClick = {
                            viewModel.navigateTo(AppDestination.SETTINGS)
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = NeonPurple.copy(alpha = 0.16f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        ) {
            val headerBg = viewModel.getCategoryHeaderSurface(currentDestination)
            val headerAccent = viewModel.getCategoryAccent(currentDestination)
            val headerText = viewModel.getCategoryText(currentDestination)

            val topBarTitle = when (currentDestination) {
                AppDestination.GAMES -> gamesLabel
                AppDestination.SERIES -> seriesLabel
                AppDestination.ANIME -> animeLabel
                AppDestination.MANGA -> mangaLabel
                AppDestination.BOOKS -> booksLabel
                AppDestination.CUSTOM_CATEGORY -> viewModel.getCategoryDisplayName(
                    selectedCustomCategoryKey ?: AppColorSettingsManager.KEY_SPIELE,
                    strings
                )
                AppDestination.PLAYTIME -> strings.navPlayTimeTitle
                AppDestination.BACKUP -> strings.navBackup
                AppDestination.SETTINGS -> strings.navSettings
            }

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                topBar = {
                    Surface(
                        color = headerBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                        ) {
                            CenterAlignedTopAppBar(
                                navigationIcon = {
                                    IconButton(
                                        onClick = { coroutineScope.launch { drawerState.open() } },
                                        modifier = Modifier.testTag("hamburger_menu_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = strings.openMenuDesc,
                                            tint = headerText
                                        )
                                    }
                                },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val titleIcon = when (currentDestination) {
                                            AppDestination.GAMES -> Icons.Default.SportsEsports
                                            AppDestination.SERIES -> Icons.Default.Tv
                                            AppDestination.ANIME -> Icons.Default.Tv
                                            AppDestination.MANGA -> Icons.Default.AutoStories
                                            AppDestination.BOOKS -> Icons.Default.Book
                                            AppDestination.CUSTOM_CATEGORY -> {
                                                val key = selectedCustomCategoryKey ?: AppColorSettingsManager.KEY_SPIELE
                                                if (categoryColors[key]?.isGameCategory == true) Icons.Default.SportsEsports else Icons.Default.Category
                                            }
                                            AppDestination.PLAYTIME -> Icons.Default.Schedule
                                            AppDestination.BACKUP -> Icons.Default.Save
                                            AppDestination.SETTINGS -> Icons.Default.Palette
                                        }
                                        Icon(
                                            imageVector = titleIcon,
                                            contentDescription = null,
                                            tint = headerAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = topBarTitle.uppercase(),
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            letterSpacing = 1.5.sp,
                                            color = headerText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                actions = {
                                    // Search Toggle on list screens
                                    if (currentDestination != AppDestination.BACKUP && currentDestination != AppDestination.PLAYTIME && currentDestination != AppDestination.SETTINGS) {
                                        IconButton(
                                            onClick = {
                                                showSearchBar = !showSearchBar
                                                if (!showSearchBar) viewModel.setSearchQuery("")
                                            },
                                            modifier = Modifier.testTag("search_toggle_button")
                                        ) {
                                            Icon(
                                                imageVector = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                                                contentDescription = if (showSearchBar) strings.closeDesc else strings.searchDesc,
                                                tint = if (showSearchBar) headerAccent else headerText.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    // Quick shortcut to PlayTime / running timer indicator
                                    if (runningTarget != null && currentDestination != AppDestination.PLAYTIME) {
                                        IconButton(
                                            onClick = { viewModel.navigateTo(AppDestination.PLAYTIME) },
                                            modifier = Modifier.testTag("quick_playtime_shortcut")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = strings.toActiveTimerDesc,
                                                tint = NeonGreen
                                            )
                                        }
                                    }
                                },
                                windowInsets = WindowInsets(0, 0, 0, 0),
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                            )

                            // Expandable search bar
                            AnimatedVisibility(visible = showSearchBar) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { viewModel.setSearchQuery(it) },
                                        placeholder = { Text(strings.searchPlaceholder) },
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                                    Icon(Icons.Default.Close, contentDescription = strings.clearDesc, tint = TextSecondary)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("search_text_field"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = headerAccent,
                                            unfocusedBorderColor = headerAccent.copy(alpha = 0.3f),
                                            focusedContainerColor = GameDarkSurface,
                                            unfocusedContainerColor = GameDarkSurface
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                containerColor = GameDarkBackground
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentDestination) {
                        AppDestination.GAMES -> GamesScreen(viewModel = viewModel)
                        AppDestination.SERIES -> SeriesScreen(viewModel = viewModel)
                        AppDestination.ANIME -> AnimeScreen(viewModel = viewModel)
                        AppDestination.MANGA -> MangaScreen(viewModel = viewModel)
                        AppDestination.BOOKS -> BooksScreen(viewModel = viewModel)
                        AppDestination.CUSTOM_CATEGORY -> GamesScreen(
                            viewModel = viewModel,
                            categoryKey = selectedCustomCategoryKey ?: AppColorSettingsManager.KEY_SPIELE
                        )
                        AppDestination.PLAYTIME -> PlayTimeScreen(viewModel = viewModel)
                        AppDestination.BACKUP -> BackupScreen(viewModel = viewModel)
                        AppDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
