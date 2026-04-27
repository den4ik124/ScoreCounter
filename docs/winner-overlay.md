# Winner Overlay

Displayed as a modal overlay on the game screen when the win condition is met.

## Content

- Trophy emoji (🏆)
- Winner's team name in the team's accent colour
- Final score in `A–B` format
- **Rematch** button

## Win condition

A team wins when it reaches the target score **and** its lead over the opponent is at least 2 points. This is the standard volleyball deuce rule.

## Actions

| Action       | Result                                      |
|--------------|---------------------------------------------|
| Rematch      | Resets both scores to 0, clears history, keeps team names and target |
| Swipe down   | Undo is still possible while the overlay is showing, to correct a mis-scored final point |

## Dismissal

The overlay disappears automatically if an undo removes the winning score. There is no explicit close button — the intended flow is Rematch or quit (× in top-left) back to the setup screen.

## See also

- [Score Tracking](score-tracking.md) — the overlay is triggered the moment the win condition is met; Rematch resets score and history
- [Text-to-Speech](text-to-speech.md) — a win announcement is spoken the instant the overlay appears
- [Game Timer](game-timer.md) — the timer is still running and visible while the overlay is shown
- [Swipe Gestures](swipe-gestures.md) — swipe-up (scoring) is disabled while the overlay is active; swipe-down (undo) remains enabled
- [Game Modes](game-modes.md) — the target score that defines the win condition is chosen on this screen
