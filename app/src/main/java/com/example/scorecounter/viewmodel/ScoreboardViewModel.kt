package com.example.scorecounter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorecounter.bluetooth.BluetoothScoreController
import com.example.scorecounter.bluetooth.BluetoothScoreEvent
import com.example.scorecounter.bluetooth.BtConnectionState
import com.example.scorecounter.model.GameState
import com.example.scorecounter.model.Screen
import com.example.scorecounter.model.ScoreSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class ScoreboardViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothController = BluetoothScoreController(application)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    val btConnectionState: StateFlow<BtConnectionState> = bluetoothController.connectionState

    init {
        viewModelScope.launch {
            bluetoothController.events.collect { event ->
                when (event) {
                    BluetoothScoreEvent.TeamASingleClick -> addPoint(isTeamA = true)
                    BluetoothScoreEvent.TeamADoubleClick -> undoPoint()
                    BluetoothScoreEvent.TeamBSingleClick -> addPoint(isTeamA = false)
                    BluetoothScoreEvent.TeamBDoubleClick -> undoPoint()
                }
            }
        }
    }

    fun startBluetooth() = bluetoothController.startScanning()

    fun setTeamNames(teamA: String, teamB: String) {
        _state.update {
            it.copy(
                teamAName = teamA.ifBlank { "Team A" },
                teamBName = teamB.ifBlank { "Team B" }
            )
        }
    }

    fun navigateToMode() = _state.update { it.copy(screen = Screen.MODE) }
    fun goBack() = _state.update { if (it.screen == Screen.MODE) it.copy(screen = Screen.SETUP) else it }

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

    override fun onCleared() {
        bluetoothController.close()
        super.onCleared()
    }

    private fun resolveWinner(a: Int, b: Int, target: Int, nameA: String, nameB: String): String? = when {
        a >= target && abs(a - b) >= 2 -> nameA
        b >= target && abs(a - b) >= 2 -> nameB
        else -> null
    }
}
