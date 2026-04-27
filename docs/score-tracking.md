# Score Tracking

Core mechanic of the game screen. Tracks points for both teams with full undo support.

## Adding a point

- **Swipe up** on a team's panel to award that team a point
- The serving indicator automatically moves to the team that just scored
- Haptic feedback fires on every score change

## Undoing a point

- **Swipe down** on either panel to undo the last scoring action
- The ↺ button in the top-right corner does the same thing (it is dimmed when there is nothing to undo)
- Undo restores the exact previous score **and** the previous serving team
- Multiple consecutive undos are supported — the full match history is stored

## Undo history

Every score change saves a snapshot of `(scoreA, scoreB, servingTeam)` before the change is applied. Undo pops the most recent snapshot. The history is cleared on Reset or when starting a new game.

## Winner detection

Once a team reaches the target score with a 2-point lead, the winner is locked in. Further scoring attempts are ignored until Reset or Rematch is used.

## See also

- [Swipe Gestures](swipe-gestures.md) — primary on-screen input for adding and undoing points
- [Bluetooth Remote](bluetooth-remote.md) — hardware input that triggers the same add/undo actions
- [Text-to-Speech](text-to-speech.md) — every score change fires a TTS announcement
- [Game Timer](game-timer.md) — timer resets automatically when both scores return to 0
- [Winner Overlay](winner-overlay.md) — shown when the win condition is first met; Rematch clears the score and history
- [Game Modes](game-modes.md) — the target score that determines the win threshold is set on this screen
