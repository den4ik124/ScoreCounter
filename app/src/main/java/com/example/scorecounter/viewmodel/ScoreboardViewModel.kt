package com.example.scorecounter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorecounter.bluetooth.BluetoothScoreController
import com.example.scorecounter.bluetooth.BluetoothScoreEvent
import com.example.scorecounter.bluetooth.BtConnectionState
import com.example.scorecounter.bluetooth.VolumeButtonScoreController
import com.example.scorecounter.model.AppSettings
import com.example.scorecounter.model.GameState
import com.example.scorecounter.model.Screen
import com.example.scorecounter.model.ScoreSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

private const val DELAY_STEP_MS = 500
private const val DELAY_MIN_MS = 0
private const val DELAY_MAX_MS = 5_000

class ScoreboardViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothController = BluetoothScoreController(application)
    private val volumeController = VolumeButtonScoreController(viewModelScope)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    val btConnectionState: StateFlow<BtConnectionState> = bluetoothController.connectionState

    init {
        viewModelScope.launch {
            bluetoothController.events.collect { handleScoreEvent(it) }
        }
        viewModelScope.launch {
            volumeController.events.collect { handleScoreEvent(it) }
        }
    }

    fun startBluetooth() = bluetoothController.startScanning()

    fun onVolumeKeyDown(keyCode: Int): Boolean = volumeController.onKeyDown(keyCode)

    // ---- Navigation ----

    fun setTeamNames(teamA: String, teamB: String) {
        _state.update {
            it.copy(
                teamAName = teamA.ifBlank { "Team A" },
                teamBName = teamB.ifBlank { "Team B" }
            )
        }
    }

    fun navigateToMode() = _state.update { it.copy(screen = Screen.MODE) }

    fun navigateToSettings() = _state.update { it.copy(screen = Screen.SETTINGS) }

    fun goBack() = _state.update {
        when (it.screen) {
            Screen.MODE -> it.copy(screen = Screen.SETUP)
            Screen.SETTINGS -> it.copy(screen = Screen.SETUP)
            else -> it
        }
    }

    // ---- Game actions ----

    fun startGame(target: Int) {
        _state.update {
            it.copy(
                targetScore = target,
                screen = Screen.GAME,
                scoreA = 0, scoreB = 0,
                winner = null,
                servingTeamA = true,
                undoHistory = emptyList()
            )
        }
    }

    fun addPoint(isTeamA: Boolean) {
        _state.update { current ->
            if (current.winner != null) return@update current
            val snap = ScoreSnapshot(current.scoreA, current.scoreB, current.servingTeamA)
            val newA = if (isTeamA) current.scoreA + 1 else current.scoreA
            val newB = if (!isTeamA) current.scoreB + 1 else current.scoreB
            current.copy(
                scoreA = newA, scoreB = newB,
                servingTeamA = isTeamA,
                winner = resolveWinner(newA, newB, current.targetScore, current.teamAName, current.teamBName),
                undoHistory = current.undoHistory + snap
            )
        }
    }

    fun removePoint(isTeamA: Boolean) {
        _state.update { current ->
            val snap = ScoreSnapshot(current.scoreA, current.scoreB, current.servingTeamA)
            val newA = if (isTeamA) max(0, current.scoreA - 1) else current.scoreA
            val newB = if (!isTeamA) max(0, current.scoreB - 1) else current.scoreB
            current.copy(
                scoreA = newA,
                scoreB = newB,
                winner = resolveWinner(newA, newB, current.targetScore, current.teamAName, current.teamBName),
                undoHistory = current.undoHistory + snap
            )
        }
    }

    fun undoPoint() {
        _state.update { current ->
            val history = current.undoHistory
            if (history.isEmpty()) return@update current
            val last = history.last()
            current.copy(
                scoreA = last.scoreA, scoreB = last.scoreB,
                servingTeamA = last.servingTeamA,
                winner = null,
                undoHistory = history.dropLast(1)
            )
        }
    }

    fun swapServe() = _state.update { it.copy(servingTeamA = !it.servingTeamA) }

    fun resetGame() {
        _state.update {
            it.copy(
                scoreA = 0, scoreB = 0,
                winner = null,
                servingTeamA = true,
                undoHistory = emptyList()
            )
        }
    }

    fun restart() = _state.update { GameState() }

    // ---- Settings ----

    fun increaseAnnouncementDelay() {
        _settings.update {
            it.copy(announcementDelayMs = (it.announcementDelayMs + DELAY_STEP_MS).coerceAtMost(DELAY_MAX_MS))
        }
    }

    fun decreaseAnnouncementDelay() {
        _settings.update {
            it.copy(announcementDelayMs = (it.announcementDelayMs - DELAY_STEP_MS).coerceAtLeast(DELAY_MIN_MS))
        }
    }

    override fun onCleared() {
        bluetoothController.close()
        super.onCleared()
    }

    private fun handleScoreEvent(event: BluetoothScoreEvent) {
        when (event) {
            BluetoothScoreEvent.TeamASingleClick -> addPoint(isTeamA = true)
            BluetoothScoreEvent.TeamADoubleClick -> removePoint(isTeamA = true)
            BluetoothScoreEvent.TeamBSingleClick -> addPoint(isTeamA = false)
            BluetoothScoreEvent.TeamBDoubleClick -> removePoint(isTeamA = false)
        }
    }

    private fun resolveWinner(a: Int, b: Int, target: Int, nameA: String, nameB: String): String? = when {
        a >= target && abs(a - b) >= 2 -> nameA
        b >= target && abs(a - b) >= 2 -> nameB
        else -> null
    }
}
