# Settings

A dedicated screen for configuring app behaviour. Accessible from the Setup screen before starting a match.

## Accessing settings

Tap the **⚙** button in the top-right corner of the Setup screen. Press **← Back** to return to Setup without restarting.

## Options

### Announcement delay

Controls the pause **between** the two team announcements after every score change.

Each point triggers a two-part read-out: the serving team is spoken first, the app waits the configured duration, then the second team is spoken.

| Value | Behaviour |
|-------|-----------|
| **Off** (0) | Both teams announced back-to-back with no pause |
| **0.5 s – 5 s** | Configured silence between the first and second team |

- **Default:** 1 s
- **Range:** Off – 5 s
- **Step:** 0.5 s
- When a point wins the set, the two-part score is suppressed and only *"[Team] wins!"* is spoken, so the win call is never buried under a score read-out.

## Implementation

`AppSettings` is a plain data class held in a `MutableStateFlow` inside `ScoreboardViewModel`. It is independent of `GameState`, so settings survive a Reset or Rematch without being cleared.

`SettingsScreen` reads the current `AppSettings` and exposes two callbacks — `onIncreaseDelay` / `onDecreaseDelay` — that each step the value by 500 ms within the allowed range. The stepper buttons are automatically disabled at the bounds.

`GameScreen` receives `announcementDelayMs: Int` and a `speakAppend: (String) -> Unit` lambda (backed by `TextToSpeech.QUEUE_ADD`). The score `LaunchedEffect` calls `speak()` for the first team, suspends for `announcementDelayMs`, then calls `speakAppend()` for the second — `QUEUE_ADD` ensures the second part is queued rather than interrupting the first. The on-demand 🔊 button uses the same pattern via `rememberCoroutineScope`.

## See also

- [Text-to-Speech](text-to-speech.md) — the feature that the delay controls
- [Score Tracking](score-tracking.md) — every point triggers a potential announcement
- [Winner Overlay](winner-overlay.md) — win announcements use the same delay
