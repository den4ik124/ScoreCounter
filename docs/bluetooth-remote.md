# Bluetooth Remote

Allows scoring via a physical button device without touching the phone screen.
Two input types are supported and can be used simultaneously.

---

## Volume HID remote (headset / media clicker)

Any Bluetooth device that sends standard Android volume key events works out of the box — no pairing setup required beyond the OS Bluetooth settings.

### Button mapping

| Button action          | App action                |
|------------------------|---------------------------|
| Volume Up single click | +1 point for Team A       |
| Volume Up double click | −1 point for Team A       |
| Volume Down single click | +1 point for Team B     |
| Volume Down double click | −1 point for Team B     |

Double-click window is **400 ms** — two presses within that window are recognised as a double click.

The volume slider is suppressed entirely while the app is in the foreground.

---

## Custom BLE scoring device

A two-button BLE device running custom firmware communicates over a dedicated GATT service.

**Protocol**

| Field | Value |
|-------|-------|
| Service UUID | `0000fff0-0000-1000-8000-00805f9b34fb` |
| Characteristic UUID | `0000fff1-0000-1000-8000-00805f9b34fb` (notify) |
| Byte `0x01` | Team A button |
| Byte `0x02` | Team B button |

### Button mapping

| Button action       | App action                |
|---------------------|---------------------------|
| Button A single click | +1 point for Team A     |
| Button A double click | −1 point for Team A     |
| Button B single click | +1 point for Team B     |
| Button B double click | −1 point for Team B     |

---

## Permissions

On first launch the app requests `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`. The BLE controller starts scanning automatically once permissions are granted. The volume HID path requires no additional permissions.

## Connection states (BLE only)

| State        | Meaning                                       |
|--------------|-----------------------------------------------|
| `IDLE`       | Scanning not started or stopped               |
| `SCANNING`   | Actively searching for the device             |
| `CONNECTING` | Device found; establishing GATT connection    |
| `CONNECTED`  | Device found and commands are live            |

## Auto-reconnect (BLE only)

The BLE controller automatically restarts scanning whenever the connection drops, so the device reconnects without any manual action.

## Implementation

- **Volume path:** `VolumeButtonScoreController` receives raw key codes from `MainActivity.dispatchKeyEvent`, performs double-click detection, and emits `BluetoothScoreEvent` values that `ScoreboardViewModel` collects.
- **BLE path:** `BluetoothScoreController` scans for the custom service UUID, subscribes to GATT notifications, and emits the same `BluetoothScoreEvent` values.
- Both controllers share the same `handleScoreEvent` router in `ScoreboardViewModel`, which calls `addPoint` (single click) or `removePoint` (double click) for the appropriate team.

## See also

- [Score Tracking](score-tracking.md) — button events call the same `addPoint` / `removePoint` actions as any other input
- [Swipe Gestures](swipe-gestures.md) — the on-screen alternative; all three inputs are independent and can be used simultaneously
