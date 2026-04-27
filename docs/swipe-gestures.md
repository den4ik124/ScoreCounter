# Swipe Gestures

The primary way to interact with the scoreboard during a game. No need to aim at small buttons.

## Gestures

| Gesture        | Action                        |
|----------------|-------------------------------|
| Swipe up       | +1 point for that team        |
| Swipe down     | Undo the last point (either panel) |

## How it works

Each team panel occupies half the screen. A vertical drag of more than **80 px** in either direction triggers the action. The gesture fires once per swipe — a single continuous drag cannot score multiple points.

The threshold is intentionally high enough to prevent accidental triggers from incidental touches, but low enough to feel responsive during fast play.

## When gestures are disabled

Swipe-up is blocked once a winner has been declared. Swipe-down (undo) remains active so players can correct a mis-tap before dismissing the winner overlay.

## See also

- [Score Tracking](score-tracking.md) — what the gestures actually modify (points and undo history)
- [Winner Overlay](winner-overlay.md) — the overlay that disables swipe-up; swipe-down still works through it
- [Bluetooth Remote](bluetooth-remote.md) — hardware alternative to on-screen gestures; maps to the same add/undo actions
