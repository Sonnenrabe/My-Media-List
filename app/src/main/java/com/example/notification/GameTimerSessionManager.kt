package com.example.notification

import android.content.Context
import android.os.SystemClock
import com.example.data.AppColorSettingsManager
import com.example.data.Game
import com.example.data.GameDatabase
import com.example.data.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TimerTarget(
    val categoryKey: String,
    val itemId: Long
)

object GameTimerSessionManager {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _runningTarget = MutableStateFlow<TimerTarget?>(null)
    val runningTarget: StateFlow<TimerTarget?> = _runningTarget.asStateFlow()

    private val _runningGameId = MutableStateFlow<Long?>(null)
    val runningGameId: StateFlow<Long?> = _runningGameId.asStateFlow()

    private val _runningGame = MutableStateFlow<Game?>(null)
    val runningGame: StateFlow<Game?> = _runningGame.asStateFlow()

    private val _elapsedSessionSeconds = MutableStateFlow(0L)
    val elapsedSessionSeconds: StateFlow<Long> = _elapsedSessionSeconds.asStateFlow()

    private var sessionStartRealtime: Long = 0L
    private var tickerJob: Job? = null

    var onPauseListener: (() -> Unit)? = null

    fun isRunningTarget(categoryKey: String, itemId: Long): Boolean {
        val current = _runningTarget.value ?: return false
        return current.categoryKey == categoryKey && current.itemId == itemId
    }

    fun startItem(
        context: Context,
        categoryKey: String,
        itemId: Long,
        title: String,
        notes: String = "",
        colorHex: Long = 0xFF818CF8L,
        totalPlaytimeSeconds: Long = 0L,
        repository: GameRepository
    ) {
        val newTarget = TimerTarget(categoryKey, itemId)
        val currentTarget = _runningTarget.value
        if (currentTarget != null && currentTarget != newTarget) {
            pauseGame(context, repository)
        }

        val gameSnapshot = Game(
            id = itemId,
            title = title,
            totalPlaytimeSeconds = totalPlaytimeSeconds,
            colorHex = colorHex,
            notes = notes,
            categoryKey = categoryKey
        )

        _runningTarget.value = newTarget
        _runningGameId.value = if (categoryKey == AppColorSettingsManager.KEY_SPIELE || !AppColorSettingsManager.BUILT_IN_CATEGORY_KEYS.contains(categoryKey)) itemId else null
        _runningGame.value = gameSnapshot
        sessionStartRealtime = SystemClock.elapsedRealtime()
        _elapsedSessionSeconds.value = 0L

        TimerNotificationManager.showTimerNotification(context, gameSnapshot, sessionStartRealtime)

        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(500)
                val elapsedMillis = SystemClock.elapsedRealtime() - sessionStartRealtime
                _elapsedSessionSeconds.value = elapsedMillis / 1000
            }
        }
    }

    fun startGame(context: Context, game: Game, repository: GameRepository) {
        startItem(
            context = context,
            categoryKey = game.categoryKey,
            itemId = game.id,
            title = game.title,
            notes = game.notes,
            colorHex = game.colorHex,
            totalPlaytimeSeconds = game.totalPlaytimeSeconds,
            repository = repository
        )
    }

    fun updateRunningGame(context: Context, updatedGame: Game) {
        val current = _runningTarget.value ?: return
        if (current.categoryKey == updatedGame.categoryKey && current.itemId == updatedGame.id) {
            _runningGame.value = updatedGame
            TimerNotificationManager.showTimerNotification(context, updatedGame, sessionStartRealtime)
        }
    }

    fun pauseGame(context: Context, repository: GameRepository) {
        val target = _runningTarget.value ?: return
        tickerJob?.cancel()
        tickerJob = null

        val elapsedMillis = SystemClock.elapsedRealtime() - sessionStartRealtime
        val sessionSeconds = (elapsedMillis / 1000).coerceAtLeast(0L)

        TimerNotificationManager.cancelTimerNotification(context)

        scope.launch(Dispatchers.IO) {
            if (sessionSeconds > 0) {
                repository.addSessionPlaytimeToTarget(target.categoryKey, target.itemId, sessionSeconds)
            }
        }

        _runningTarget.value = null
        _runningGameId.value = null
        _runningGame.value = null
        _elapsedSessionSeconds.value = 0L

        onPauseListener?.invoke()
    }

    fun pauseFromBroadcast(context: Context) {
        val target = _runningTarget.value ?: return
        tickerJob?.cancel()
        tickerJob = null

        val elapsedMillis = SystemClock.elapsedRealtime() - sessionStartRealtime
        val sessionSeconds = (elapsedMillis / 1000).coerceAtLeast(0L)

        TimerNotificationManager.cancelTimerNotification(context)

        scope.launch(Dispatchers.IO) {
            val db = GameDatabase.getInstance(context, scope)
            val repository = GameRepository(db.gameDao(), db.mediaDao())
            if (sessionSeconds > 0) {
                repository.addSessionPlaytimeToTarget(target.categoryKey, target.itemId, sessionSeconds)
            }
        }

        _runningTarget.value = null
        _runningGameId.value = null
        _runningGame.value = null
        _elapsedSessionSeconds.value = 0L

        onPauseListener?.invoke()
    }
}
