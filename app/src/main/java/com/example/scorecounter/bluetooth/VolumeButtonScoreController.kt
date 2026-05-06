package com.example.scorecounter.bluetooth

import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val VOLUME_DOUBLE_CLICK_WINDOW_MS = 400L

/**
 * Translates VOLUME_UP / VOLUME_DOWN key events from a paired Bluetooth HID remote
 * (e.g. a media clicker or headset volume buttons) into score events, intercepting
 * the volume change entirely.
 *
 * VOLUME_UP single click  → [BluetoothScoreEvent.TeamASingleClick]  (+1 Team A)
 * VOLUME_UP double click  → [BluetoothScoreEvent.TeamADoubleClick]  (−1 Team A)
 * VOLUME_DOWN single click → [BluetoothScoreEvent.TeamBSingleClick] (+1 Team B)
 * VOLUME_DOWN double click → [BluetoothScoreEvent.TeamBDoubleClick] (−1 Team B)
 *
 * Call [onKeyDown] from [android.app.Activity.dispatchKeyEvent] for ACTION_DOWN events
 * with repeatCount == 0. Returns true when the event is consumed.
 *
 * No cleanup needed — pending jobs are tied to [scope] and are cancelled when it is.
 */
class VolumeButtonScoreController(private val scope: CoroutineScope) {

    private val _events = MutableSharedFlow<BluetoothScoreEvent>()
    val events: SharedFlow<BluetoothScoreEvent> = _events.asSharedFlow()

    private var pendingUpJob: Job? = null
    private var pendingDownJob: Job? = null

    fun onKeyDown(keyCode: Int): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP -> { handleClick(isUp = true); true }
        KeyEvent.KEYCODE_VOLUME_DOWN -> { handleClick(isUp = false); true }
        else -> false
    }

    private fun handleClick(isUp: Boolean) {
        if (isUp) {
            if (pendingUpJob?.isActive == true) {
                pendingUpJob?.cancel()
                pendingUpJob = null
                scope.launch { _events.emit(BluetoothScoreEvent.TeamADoubleClick) }
            } else {
                pendingUpJob = scope.launch {
                    delay(VOLUME_DOUBLE_CLICK_WINDOW_MS)
                    _events.emit(BluetoothScoreEvent.TeamASingleClick)
                }
            }
        } else {
            if (pendingDownJob?.isActive == true) {
                pendingDownJob?.cancel()
                pendingDownJob = null
                scope.launch { _events.emit(BluetoothScoreEvent.TeamBDoubleClick) }
            } else {
                pendingDownJob = scope.launch {
                    delay(VOLUME_DOUBLE_CLICK_WINDOW_MS)
                    _events.emit(BluetoothScoreEvent.TeamBSingleClick)
                }
            }
        }
    }
}
