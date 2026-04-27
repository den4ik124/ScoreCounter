# Bluetooth Remote

Allows scoring via a physical two-button BLE device, so a referee or scorekeeper does not need to touch the phone screen.

## Permissions

On first launch the app requests `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`. Scanning starts automatically once permissions are granted.

## Button mapping

| Button action       | App action              |
|---------------------|-------------------------|
| Button A single click | +1 point for Team A   |
| Button A double click | Undo last point        |
| Button B single click | +1 point for Team B   |
| Button B double click | Undo last point        |

## Connection states

| State        | Meaning                              |
|--------------|--------------------------------------|
| `IDLE`       | Scanning not started or stopped      |
| `SCANNING`   | Actively searching for the device    |
| `CONNECTED`  | Device found and commands are live   |
| `ERROR`      | Connection lost; auto-reconnect will retry |

## Auto-reconnect

The controller automatically restarts scanning whenever the connection drops, so the device reconnects without any manual action.

## Implementation

`BluetoothScoreController` lives in the `bluetooth` package and exposes a `events: SharedFlow<BluetoothScoreEvent>` that `ScoreboardViewModel` collects to translate hardware events into the same `addPoint` / `undoPoint` calls used by the UI.

## See also

- [Score Tracking](score-tracking.md) — BLE button events call the same `addPoint` / `undoPoint` actions as any other input
- [Swipe Gestures](swipe-gestures.md) — the on-screen alternative; both inputs are independent and can be used simultaneously
