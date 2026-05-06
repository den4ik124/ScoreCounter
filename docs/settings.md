# Settings

A dedicated screen for configuring app behaviour. Accessible from the Setup screen before starting a match.

## Accessing settings

Tap the **⚙** button in the top-right corner of the Setup screen. Press **← Back** to return to Setup without restarting.

## Options

### Announcement delay

Controls how long the app waits after a score change before speaking the score aloud via Text-to-Speech.

| Value | Meaning |
|-------|---------|
| **Off** (0) | Speaks immediately on every score change |
| **0.5 s – 5 s** | Waits the configured duration before announcing |

- **Default:** 1 s
- **Range:** Off – 5 s
- **Step:** 0.5 s
- The delay applies equally to score announcements and winner announcements.
- When a point wins the set, the score read-out is suppressed and only *"[Team] wins!"* is spoken (after the delay), so the two announcements never overlap.

## Implementation

`AppSettings` is a plain data class held in a `MutableStateFlow` inside `ScoreboardViewModel`. It is independent of `GameState`, so settings survive a Reset or Rematch without being cleared.

`SettingsScreen` reads the current `AppSettings` and exposes two callbacks — `onIncreaseDelay` / `onDecreaseDelay` — that each step the value by 500 ms within the allowed range. The stepper buttons are automatically disabled at the bounds.

`GameScreen` receives `announcementDelayMs: Int` as a parameter and applies it via `delay()` inside the score and winner `LaunchedEffect` blocks.

## See also

- [Text-to-Speech](text-to-speech.md) — the feature that the delay controls
- [Score Tracking](score-tracking.md) — every point triggers a potential announcement
- [Winner Overlay](winner-overlay.md) — win announcements use the same delay
