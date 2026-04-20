package com.example.scorecounter.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

private const val DOUBLE_CLICK_WINDOW_MS = 400L
private const val RECONNECT_DELAY_MS = 3_000L

/** UUIDs for the custom scoring device firmware. */
val SCORE_SERVICE_UUID: UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
val BUTTON_CHAR_UUID: UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

sealed interface BluetoothScoreEvent {
    data object TeamASingleClick : BluetoothScoreEvent
    data object TeamADoubleClick : BluetoothScoreEvent
    data object TeamBSingleClick : BluetoothScoreEvent
    data object TeamBDoubleClick : BluetoothScoreEvent
}

enum class BtConnectionState { IDLE, SCANNING, CONNECTING, CONNECTED }

/**
 * Manages a BLE connection to a two-button scoring device.
 *
 * Expected BLE protocol:
 *   Service:        0000fff0-0000-1000-8000-00805f9b34fb
 *   Characteristic: 0000fff1-0000-1000-8000-00805f9b34fb  (notify)
 *   Value byte:     0x01 = Team A button | 0x02 = Team B button
 *
 * Single press  → increment score.
 * Double press within [DOUBLE_CLICK_WINDOW_MS] → decrement score.
 * Auto-reconnects after disconnection.
 */
class BluetoothScoreController(context: Context) {

    private val appContext = context.applicationContext
    private val bluetoothAdapter =
        appContext.getSystemService(BluetoothManager::class.java)?.adapter

    /**
     * All mutable state is accessed exclusively on the main thread via [scope].
     * GATT callbacks arrive on a binder thread and dispatch here with scope.launch{}.
     */
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _connectionState = MutableStateFlow(BtConnectionState.IDLE)
    val connectionState: StateFlow<BtConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<BluetoothScoreEvent>()
    val events: SharedFlow<BluetoothScoreEvent> = _events.asSharedFlow()

    val isBluetoothAvailable: Boolean get() = bluetoothAdapter != null

    private var gatt: BluetoothGatt? = null
    private var pendingClickJobA: Job? = null
    private var pendingClickJobB: Job? = null
    private var reconnectJob: Job? = null

    // ---- Public API ----

    fun startScanning() {
        if (bluetoothAdapter?.isEnabled != true) return
        if (_connectionState.value == BtConnectionState.SCANNING ||
            _connectionState.value == BtConnectionState.CONNECTED) return

        _connectionState.value = BtConnectionState.SCANNING
        val scanner = bluetoothAdapter.bluetoothLeScanner ?: run {
            _connectionState.value = BtConnectionState.IDLE
            return
        }
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SCORE_SERVICE_UUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(listOf(filter), settings, scanCallback)
    }

    fun close() {
        scope.cancel()      // cancels reconnect, pending clicks, and ignores further callbacks
        stopScanning()
        closeGatt()
    }

    // ---- BLE scan ----

    private fun stopScanning() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            stopScanning()
            _connectionState.value = BtConnectionState.CONNECTING
            result.device.connectGatt(
                appContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE
            )
        }

        override fun onScanFailed(errorCode: Int) {
            _connectionState.value = BtConnectionState.IDLE
        }
    }

    // ---- GATT ----

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            scope.launch {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        this@BluetoothScoreController.gatt = gatt
                        _connectionState.value = BtConnectionState.CONNECTED
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        closeGatt()
                        _connectionState.value = BtConnectionState.IDLE
                        scheduleReconnect()
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            scope.launch {
                if (status != BluetoothGatt.GATT_SUCCESS) return@launch
                val char = gatt.getService(SCORE_SERVICE_UUID)
                    ?.getCharacteristic(BUTTON_CHAR_UUID) ?: return@launch
                gatt.setCharacteristicNotification(char, true)
                val descriptor = char.getDescriptor(CCCD_UUID) ?: return@launch
                // API 33+ write API — minSdk is 33 so no deprecated fallback needed
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            }
        }

        // API 33+ callback — minSdk is 33
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            scope.launch { handleButtonValue(value.firstOrNull() ?: return@launch) }
        }
    }

    // ---- Button event logic ----

    private fun handleButtonValue(value: Byte) {
        when (value) {
            0x01.toByte() -> handleClick(isTeamA = true)
            0x02.toByte() -> handleClick(isTeamA = false)
        }
    }

    /**
     * Detects single vs double press using a pending-job pattern.
     * First press starts a delayed job; a second press within the window cancels it and
     * emits DoubleClick instead of SingleClick.
     */
    private fun handleClick(isTeamA: Boolean) {
        if (isTeamA) {
            if (pendingClickJobA?.isActive == true) {
                pendingClickJobA?.cancel()
                pendingClickJobA = null
                scope.launch { _events.emit(BluetoothScoreEvent.TeamADoubleClick) }
            } else {
                pendingClickJobA = scope.launch {
                    delay(DOUBLE_CLICK_WINDOW_MS)
                    _events.emit(BluetoothScoreEvent.TeamASingleClick)
                }
            }
        } else {
            if (pendingClickJobB?.isActive == true) {
                pendingClickJobB?.cancel()
                pendingClickJobB = null
                scope.launch { _events.emit(BluetoothScoreEvent.TeamBDoubleClick) }
            } else {
                pendingClickJobB = scope.launch {
                    delay(DOUBLE_CLICK_WINDOW_MS)
                    _events.emit(BluetoothScoreEvent.TeamBSingleClick)
                }
            }
        }
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(RECONNECT_DELAY_MS)
            startScanning()
        }
    }

    private fun closeGatt() {
        gatt?.close()   // close() releases resources and ends the connection
        gatt = null
    }
}
