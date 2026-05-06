# Text-to-Speech (TTS)

The app reads the score aloud automatically after every point and on demand.

## Automatic announcements

Every time the score changes, the app speaks both scores after the configured **announcement delay** (default 1 s). The **serving team is always announced first**, so the "home" score comes first regardless of which team is A or B.

Example: if Team B is serving at 11–14, TTS says *"Eagles 14, Sharks 11"*.

## Winner announcement

When a team wins, TTS says *"[Team name] wins!"* after the same announcement delay. When the winning point is scored, the score read-out is suppressed so only the winner announcement is spoken — the two never overlap.

## Manual read-out

The **🔊** button in the bottom action bar speaks the current score on demand, using the same serving-first order. Useful after a disputed point or when returning to the court.

## Voice selection

On startup the app searches for a US English female voice that does not require a network connection. If none is found, the system default voice is used.

## Implementation note

TTS is initialised in `MainActivity` and passed as a `speak: (String) -> Unit` lambda down to `GameScreen`. This keeps the `@Composable` tree free of Android lifecycle objects.

## See also

- [Settings](settings.md) — configure the announcement delay (Off to 5 s in 0.5 s steps)
- [Score Tracking](score-tracking.md) — every point change triggers an automatic announcement
- [Winner Overlay](winner-overlay.md) — reaching the win condition triggers the winner announcement
- [Team Setup](team-setup.md) — team names entered here are what TTS reads aloud
