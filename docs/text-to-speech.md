# Text-to-Speech (TTS)

The app reads the score aloud automatically after every point and on demand.

## Automatic announcements

Every score change triggers a two-part read-out. The **serving team is spoken first**, then after the configured delay (default 1 s) the second team is spoken.

Example: Team B is serving at 11–14 → TTS says *"Eagles 14"* · 1 s pause · *"Sharks 11"*.

The delay is the pause **between** the two parts, not before the announcement starts.

## Winner announcement

When a team wins, TTS immediately says *"[Team name] wins!"*. The two-part score read-out is suppressed on the winning point so the win call is never overlapped by a score announcement.

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
