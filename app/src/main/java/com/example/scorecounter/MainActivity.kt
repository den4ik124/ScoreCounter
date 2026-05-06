package com.example.scorecounter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.scorecounter.ui.ScoreboardApp
import com.example.scorecounter.ui.theme.ScoreCounterTheme
import com.example.scorecounter.viewmodel.ScoreboardViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var tts: TextToSpeech
    private val viewModel: ScoreboardViewModel by viewModels()

    private val btPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants -> if (grants.values.all { it }) viewModel.startBluetooth() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initTts()
        initBluetooth()
        setContent {
            ScoreCounterTheme {
                ScoreboardApp(
                    viewModel = viewModel,
                    speak = { text -> tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null) }
                )
            }
        }
    }

    private fun initTts() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US)
                tts.voices
                    ?.filter { it.locale == Locale.US && !it.isNetworkConnectionRequired }
                    ?.firstOrNull { it.name.contains("female", ignoreCase = true) }
                    ?.let { tts.voice = it }
            }
        }
    }

    private fun initBluetooth() {
        val perms = arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        if (perms.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            viewModel.startBluetooth()
        } else {
            btPermissionLauncher.launch(perms)
        }
    }

    /**
     * Intercepts VOLUME_UP / VOLUME_DOWN from a paired Bluetooth HID remote before the
     * system can change the ringer volume. All volume key events are consumed so that the
     * volume slider never appears; ACTION_DOWN with repeatCount == 0 is forwarded to the
     * ViewModel for double-click detection and score routing.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                viewModel.onVolumeKeyDown(event.keyCode)
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        tts.stop(); tts.shutdown()
        super.onDestroy()
    }
}
