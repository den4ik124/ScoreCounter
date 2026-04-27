# Game Timer

Displays elapsed time since the start of the current set, shown in the centre of the top bar.

## Display

Format: `MM:SS` (e.g. `12:34`), rendered in JetBrains Mono for a clean monospaced look. Sits next to the "FIRST TO N" target label inside a pill-shaped chip.

## Behaviour

- The timer starts when the game screen appears
- It resets automatically whenever both scores return to 0 (Reset or Rematch)
- It keeps running after a winner is declared, so the final match duration is visible on the winner overlay

## Implementation

A `LaunchedEffect` coroutine increments a `nowMs` timestamp every second. The display value is derived from `nowMs - startTimeMs`, so no mutable counter drifts over time. The screen is kept on (`keepScreenOn = true`) for the duration of the game to prevent the timer from pausing mid-match.

## See also

- [Score Tracking](score-tracking.md) — the timer reset is triggered when both scores return to 0 (via Reset or Rematch)
- [Winner Overlay](winner-overlay.md) — the timer keeps running after a win, so the final match duration is visible on the overlay
