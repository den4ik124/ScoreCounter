package com.example.scorecounter.model

enum class Screen { SETUP, MODE, GAME }

data class ScoreSnapshot(val scoreA: Int, val scoreB: Int, val servingTeamA: Boolean)

data class GameState(
    val teamAName: String = "Team A",
    val teamBName: String = "Team B",
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val targetScore: Int = 25,
    val screen: Screen = Screen.SETUP,
    val winner: String? = null,
    val servingTeamA: Boolean = true,
    val undoHistory: List<ScoreSnapshot> = emptyList(),
)
