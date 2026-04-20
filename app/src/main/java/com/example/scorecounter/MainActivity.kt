package com.example.scorecounter

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorecounter.bluetooth.BluetoothScoreController
import com.example.scorecounter.bluetooth.BluetoothScoreEvent
import com.example.scorecounter.bluetooth.BtConnectionState
import com.example.scorecounter.ui.theme.ScoreCounterTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

// ── Fonts ─────────────────────────────────────────────────────────────────────

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private fun googleFont(name: String, weight: FontWeight) =
    androidx.compose.ui.text.googlefonts.Font(
        googleFont = GoogleFont(name),
        fontProvider = fontProvider,
        weight = weight
    )

val Baloo2 = FontFamily(
    googleFont("Baloo 2", FontWeight.Medium),
    googleFont("Baloo 2", FontWeight.SemiBold),
    googleFont("Baloo 2", FontWeight.Bold),
    googleFont("Baloo 2", FontWeight.ExtraBold),
)

val SpaceGrotesk = FontFamily(
    googleFont("Space Grotesk", FontWeight.Normal),
    googleFont("Space Grotesk", FontWeight.Medium),
    googleFont("Space Grotesk", FontWeight.SemiBold),
    googleFont("Space Grotesk", FontWeight.Bold),
)

val JetBrainsMono = FontFamily(
    googleFont("JetBrains Mono", FontWeight.Normal),
    googleFont("JetBrains Mono", FontWeight.Medium),
)

// ── Theme system ───────────────────────────────────────────────────────────────

data class AppTheme(
    val bgTop: Color,
    val bgBottom: Color,
    val fg: Color,
    val mutedFg: Color,
    val surface: Color,
    val surfaceBorder: Color,
    val chip: Color,
    val isDark: Boolean,
)

val MidnightTheme = AppTheme(
    bgTop = Color(0xFF0B0F1A), bgBottom = Color(0xFF12192A),
    fg = Color(0xFFF4F6FB), mutedFg = Color(0xFF8892A6),
    surface = Color(0xFF161C2B), surfaceBorder = Color(0xFF222A3E),
    chip = Color(0x10FFFFFF), isDark = true
)

val BeachTheme = AppTheme(
    bgTop = Color(0xFFFEF6E7), bgBottom = Color(0xFFFDE3BF),
    fg = Color(0xFF2A1F17), mutedFg = Color(0xFF7A6651),
    surface = Color(0xFFFFFAF0), surfaceBorder = Color(0xFFE9D8B8),
    chip = Color(0x0F2A1F17), isDark = false
)

val CourtTheme = AppTheme(
    bgTop = Color(0xFFF6F4EF), bgBottom = Color(0xFFEBE7DC),
    fg = Color(0xFF15181D), mutedFg = Color(0xFF6B7280),
    surface = Color(0xFFFFFFFF), surfaceBorder = Color(0xFFE3DFD4),
    chip = Color(0x0D15181D), isDark = false
)

val SunsetTheme = AppTheme(
    bgTop = Color(0xFFFFD9B8), bgBottom = Color(0xFFE98AB3),
    fg = Color(0xFF2A1420), mutedFg = Color(0xFF7A4A5A),
    surface = Color(0xB8FFFFFF), surfaceBorder = Color(0xE6FFFFFF),
    chip = Color(0x142A1420), isDark = false
)

// ── Team color palettes ────────────────────────────────────────────────────────

data class TeamPalette(val base: Color, val soft: Color, val deep: Color)

val OceanPalette    = TeamPalette(Color(0xFF2A70D9), Color(0xFFCFE0FA), Color(0xFF123672))
val SunsetPalette   = TeamPalette(Color(0xFFEF7A3D), Color(0xFFFFD7BF), Color(0xFF7A3210))
val PalmPalette     = TeamPalette(Color(0xFF1F9D7A), Color(0xFFBFE9DB), Color(0xFF0B4D3A))
val CoralPalette    = TeamPalette(Color(0xFFE84B6A), Color(0xFFFFD1DC), Color(0xFF6F1428))
val SandPalette     = TeamPalette(Color(0xFFC69553), Color(0xFFF2DCB5), Color(0xFF5A3A12))
val VioletPalette   = TeamPalette(Color(0xFF7C5CD9), Color(0xFFDCCFF5), Color(0xFF3A2380))
val LimePalette     = TeamPalette(Color(0xFFA9C93A), Color(0xFFE3ECB3), Color(0xFF4A5A12))
val GraphitePalette = TeamPalette(Color(0xFF2F3742), Color(0xFFD5D9E0), Color(0xFF0A0D12))

// ── Domain models ──────────────────────────────────────────────────────────────

enum class Screen { SETUP, MODE, GAME }

data class ScoreSnapshot(val scoreA: Int, val scoreB: Int, val servingTeamA: Boolean)

data class GameState(
    val teamAName: String = "Team A",
    val teamBName: String = "Team B",
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val targetScore: Int = 25,
    val screen: Screen = Screen.SETUP,
    val winner: String? = null,
    val servingTeamA: Boolean = true,
    val undoHistory: List<ScoreSnapshot> = emptyList(),
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ScoreboardViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothController = BluetoothScoreController(application)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    val btConnectionState: StateFlow<BtConnectionState> = bluetoothController.connectionState

    init {
        viewModelScope.launch {
            bluetoothController.events.collect { event ->
                when (event) {
                    BluetoothScoreEvent.TeamASingleClick -> addPoint(isTeamA = true)
                    BluetoothScoreEvent.TeamADoubleClick -> undoPoint()
                    BluetoothScoreEvent.TeamBSingleClick -> addPoint(isTeamA = false)
                    BluetoothScoreEvent.TeamBDoubleClick -> undoPoint()
                }
            }
        }
    }

    fun startBluetooth() = bluetoothController.startScanning()

    fun setTeamNames(teamA: String, teamB: String) {
        _state.update {
            it.copy(
                teamAName = teamA.ifBlank { "Team A" },
                teamBName = teamB.ifBlank { "Team B" }
            )
        }
    }

    fun navigateToMode() = _state.update { it.copy(screen = Screen.MODE) }
    fun goBack() = _state.update { if (it.screen == Screen.MODE) it.copy(screen = Screen.SETUP) else it }

    fun startGame(target: Int) {
        _state.update {
            it.copy(
                targetScore = target,
                screen = Screen.GAME,
                scoreA = 0, scoreB = 0,
                winner = null,
                servingTeamA = true,
                undoHistory = emptyList()
            )
        }
    }

    fun addPoint(isTeamA: Boolean) {
        _state.update { current ->
            if (current.winner != null) return@update current
            val snap = ScoreSnapshot(current.scoreA, current.scoreB, current.servingTeamA)
            val newA = if (isTeamA) current.scoreA + 1 else current.scoreA
            val newB = if (!isTeamA) current.scoreB + 1 else current.scoreB
            current.copy(
                scoreA = newA, scoreB = newB,
                servingTeamA = isTeamA,
                winner = resolveWinner(newA, newB, current.targetScore, current.teamAName, current.teamBName),
                undoHistory = current.undoHistory + snap
            )
        }
    }

    fun undoPoint() {
        _state.update { current ->
            val history = current.undoHistory
            if (history.isEmpty()) return@update current
            val last = history.last()
            current.copy(
                scoreA = last.scoreA, scoreB = last.scoreB,
                servingTeamA = last.servingTeamA,
                winner = null,
                undoHistory = history.dropLast(1)
            )
        }
    }

    fun swapServe() = _state.update { it.copy(servingTeamA = !it.servingTeamA) }

    fun resetGame() {
        _state.update {
            it.copy(
                scoreA = 0, scoreB = 0,
                winner = null,
                servingTeamA = true,
                undoHistory = emptyList()
            )
        }
    }

    fun restart() = _state.update { GameState() }

    override fun onCleared() {
        bluetoothController.close()
        super.onCleared()
    }

    private fun resolveWinner(a: Int, b: Int, target: Int, nameA: String, nameB: String): String? = when {
        a >= target && abs(a - b) >= 2 -> nameA
        b >= target && abs(a - b) >= 2 -> nameB
        else -> null
    }
}

// ── Activity ──────────────────────────────────────────────────────────────────

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

    override fun onDestroy() {
        tts.stop(); tts.shutdown()
        super.onDestroy()
    }
}

// ── Root composable ───────────────────────────────────────────────────────────

@Composable
fun ScoreboardApp(
    viewModel: ScoreboardViewModel = viewModel(),
    speak: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val btState by viewModel.btConnectionState.collectAsStateWithLifecycle()

    val theme = MidnightTheme

    Surface(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        color = Color.Transparent
    ) {
        AnimatedContent(
            targetState = state.screen,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                if (forward) slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                else slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            },
            label = "screen"
        ) { screen ->
            ThemedBackground(theme) {
                when (screen) {
                    Screen.SETUP -> SetupScreen(
                        theme = theme,
                        onNext = { a, b ->
                            viewModel.setTeamNames(a, b)
                            viewModel.navigateToMode()
                        }
                    )
                    Screen.MODE -> ModeSelectionScreen(
                        theme = theme,
                        teamAName = state.teamAName,
                        teamBName = state.teamBName,
                        onBack = viewModel::goBack,
                        onPick = viewModel::startGame
                    )
                    Screen.GAME -> GameScreen(
                        state = state,
                        theme = theme,
                        btState = btState,
                        onAddPoint = viewModel::addPoint,
                        onUndo = viewModel::undoPoint,
                        onSwapServe = viewModel::swapServe,
                        onReset = viewModel::resetGame,
                        onQuit = viewModel::restart,
                        speak = speak
                    )
                }
            }
        }
    }
}

// ── Themed background ─────────────────────────────────────────────────────────

@Composable
fun ThemedBackground(theme: AppTheme, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(theme.bgTop, theme.bgBottom)))
    ) {
        if (theme.isDark) DotsPattern()
        content()
    }
}

@Composable
private fun DotsPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = 24.dp.toPx()
        val radius = 1.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            var y = 0f
            while (y <= size.height) {
                drawCircle(Color.White.copy(alpha = 0.045f), radius, Offset(x, y))
                y += spacing
            }
            x += spacing
        }
    }
}

// ── Setup screen ──────────────────────────────────────────────────────────────

@Composable
fun SetupScreen(theme: AppTheme, onNext: (String, String) -> Unit) {
    var teamA by remember { mutableStateOf("") }
    var teamB by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // Gradient avatar with volleyball glyph
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(OceanPalette.base, SunsetPalette.base))
                ),
            contentAlignment = Alignment.Center
        ) {
            VolleyballGlyph(size = 52.dp, color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Scoreboard",
            fontFamily = Baloo2,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 34.sp,
            color = theme.fg,
            letterSpacing = (-0.02).sp
        )
        Text(
            text = "Tap to play · Swipe to score",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = theme.mutedFg,
            letterSpacing = 0.04.sp
        )

        Spacer(Modifier.height(40.dp))

        TeamInputField(
            label = "TEAM A",
            dotColor = OceanPalette.base,
            value = teamA,
            placeholder = "e.g. Sharks",
            theme = theme,
            imeAction = ImeAction.Next,
            onValueChange = { teamA = it },
            onIme = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Spacer(Modifier.height(16.dp))

        TeamInputField(
            label = "TEAM B",
            dotColor = SunsetPalette.base,
            value = teamB,
            placeholder = "e.g. Eagles",
            theme = theme,
            imeAction = ImeAction.Done,
            onValueChange = { teamB = it },
            onIme = { focusManager.clearFocus(); onNext(teamA, teamB) }
        )

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(OceanPalette.base, SunsetPalette.base)))
                .clickableNoRipple { onNext(teamA, teamB) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Continue →",
                fontFamily = Baloo2,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Leave blank to use \"Team A\" / \"Team B\"",
            fontFamily = SpaceGrotesk,
            fontSize = 12.sp,
            color = theme.mutedFg,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TeamInputField(
    label: String,
    dotColor: Color,
    value: String,
    placeholder: String,
    theme: AppTheme,
    imeAction: ImeAction,
    onValueChange: (String) -> Unit,
    onIme: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = theme.mutedFg,
                letterSpacing = 0.1.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(placeholder, fontFamily = SpaceGrotesk, color = theme.mutedFg)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onNext = { onIme() },
                onDone = { onIme() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = theme.fg,
                unfocusedTextColor = theme.fg,
                focusedBorderColor = dotColor,
                unfocusedBorderColor = theme.surfaceBorder,
                focusedContainerColor = theme.surface,
                unfocusedContainerColor = theme.surface,
                cursorColor = dotColor
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = theme.fg
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Volleyball glyph ─────────────────────────────────────────────────────────

@Composable
fun VolleyballGlyph(size: Dp, color: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val r = minOf(this.size.width, this.size.height) / 2f
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val strokeW = r * 0.09f

        drawIntoCanvas { canvas ->
            val fPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = strokeW
                this.color = color.copy(alpha = 0.85f).toArgb()
                strokeCap = android.graphics.Paint.Cap.ROUND
            }
            // Vertical curve
            val path1 = android.graphics.Path().apply {
                moveTo(cx, cy - r * 0.87f)
                quadTo(cx - r * 0.6f, cy, cx, cy + r * 0.87f)
            }
            // Top horizontal curve
            val path2 = android.graphics.Path().apply {
                moveTo(cx - r * 0.87f, cy - r * 0.44f)
                quadTo(cx, cy - r * 0.1f, cx + r * 0.87f, cy - r * 0.44f)
            }
            // Bottom horizontal curve
            val path3 = android.graphics.Path().apply {
                moveTo(cx - r * 0.87f, cy + r * 0.44f)
                quadTo(cx, cy + r * 0.1f, cx + r * 0.87f, cy + r * 0.44f)
            }
            canvas.nativeCanvas.drawPath(path1, fPaint)
            canvas.nativeCanvas.drawPath(path2, fPaint)
            canvas.nativeCanvas.drawPath(path3, fPaint)
        }
    }
}

// ── Mode selection screen ─────────────────────────────────────────────────────

private data class ModeOption(
    val id: String, val label: String, val desc: String,
    val target: Int, val accentColor: Color, val icon: String
)

private val MODE_OPTIONS = listOf(
    ModeOption("short", "Short",    "Quick match · first to 15", 15,  Color(0xFFf4a33a), "⚡"),
    ModeOption("beach", "Beach",    "Beach volleyball · first to 21", 21, Color(0xFFe8824a), "🏖"),
    ModeOption("full",  "Full Set", "Official set · first to 25", 25,  Color(0xFF2a70d9), "🏐"),
    ModeOption("custom","Custom",   "Set your own target",        11,  Color(0xFF7c5cd9), "⚙"),
)

@Composable
fun ModeSelectionScreen(
    theme: AppTheme,
    teamAName: String,
    teamBName: String,
    onBack: () -> Unit,
    onPick: (Int) -> Unit
) {
    var customTarget by remember { mutableIntStateOf(11) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Back button
        ChipButton(theme = theme, onClick = onBack) {
            Text("← Back", fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = theme.fg)
        }

        Spacer(Modifier.height(20.dp))

        // Matchup chip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, theme.surfaceBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(OceanPalette.base))
            Spacer(Modifier.width(10.dp))
            Text(
                text = teamAName,
                fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = theme.fg, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "VS",
                fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                color = theme.mutedFg, modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = teamBName,
                fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = theme.fg, maxLines = 1, overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End, modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(10.dp).clip(CircleShape).background(SunsetPalette.base))
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Game mode",
            fontFamily = Baloo2, fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp, color = theme.fg, letterSpacing = (-0.02).sp
        )
        Text(
            text = "How long should the set be?",
            fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, color = theme.mutedFg
        )

        Spacer(Modifier.height(18.dp))

        MODE_OPTIONS.forEach { mode ->
            ModeCard(
                mode = mode,
                theme = theme,
                customTarget = customTarget,
                onCustomChange = { customTarget = it },
                onClick = {
                    onPick(if (mode.id == "custom") customTarget else mode.target)
                }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ModeCard(
    mode: ModeOption,
    theme: AppTheme,
    customTarget: Int,
    onCustomChange: (Int) -> Unit,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surface, RoundedCornerShape(18.dp))
            .border(1.dp, theme.surfaceBorder, RoundedCornerShape(18.dp))
            .clickableNoRipple(enabled = mode.id != "custom") {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(mode.accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(mode.icon, fontSize = 26.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.label,
                fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = theme.fg, lineHeight = 20.sp
            )
            Text(
                text = if (mode.id == "custom") "First to $customTarget" else mode.desc,
                fontFamily = SpaceGrotesk, fontSize = 13.sp, color = theme.mutedFg
            )
        }
        if (mode.id == "custom") {
            Row(
                modifier = Modifier
                    .background(theme.chip, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepperButton(theme, "−") { onCustomChange((customTarget - 1).coerceAtLeast(3)) }
                Text(
                    text = customTarget.toString(),
                    fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    color = theme.fg, modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )
                StepperButton(theme, "+") { onCustomChange((customTarget + 1).coerceAtMost(99)) }
            }
            Spacer(Modifier.width(8.dp))
            ChipButton(theme = theme, onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() }) {
                Text("→", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = theme.fg)
            }
        } else {
            Text("→", fontFamily = SpaceGrotesk, fontSize = 20.sp, color = theme.mutedFg)
        }
    }
}

@Composable
private fun StepperButton(theme: AppTheme, label: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickableNoRipple { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontFamily = SpaceGrotesk, fontSize = 16.sp, color = theme.fg, fontWeight = FontWeight.SemiBold)
    }
}

// ── Game / Scoreboard screen ──────────────────────────────────────────────────

@Composable
fun GameScreen(
    state: GameState,
    theme: AppTheme,
    btState: BtConnectionState,
    onAddPoint: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onSwapServe: () -> Unit,
    onReset: () -> Unit,
    onQuit: () -> Unit,
    speak: (String) -> Unit
) {
    val view = LocalView.current
    DisposableEffect(Unit) { view.keepScreenOn = true; onDispose { view.keepScreenOn = false } }

    // Timer
    var startTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) { delay(1000); nowMs = System.currentTimeMillis() }
    }
    LaunchedEffect(state.scoreA, state.scoreB) {
        if (state.scoreA == 0 && state.scoreB == 0) startTimeMs = System.currentTimeMillis()
    }
    val elapsed = ((nowMs - startTimeMs) / 1000).toInt()
    val timerText = "%02d:%02d".format(elapsed / 60, elapsed % 60)

    // TTS
    var prevScores by remember { mutableStateOf(Pair(state.scoreA, state.scoreB)) }
    LaunchedEffect(state.scoreA, state.scoreB) {
        if (prevScores != Pair(state.scoreA, state.scoreB)) {
            speak("${state.teamAName} ${state.scoreA}, ${state.teamBName} ${state.scoreB}")
            prevScores = Pair(state.scoreA, state.scoreB)
        }
    }
    LaunchedEffect(state.winner) { state.winner?.let { speak("$it wins!") } }

    val showButtons = btState != BtConnectionState.CONNECTED

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChipButton(theme = theme, onClick = onQuit) {
                    Text("×", fontFamily = SpaceGrotesk, fontSize = 16.sp, color = theme.fg, fontWeight = FontWeight.Bold)
                }
                // Timer + target chip
                Row(
                    modifier = Modifier
                        .background(theme.chip, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = timerText,
                        fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium,
                        fontSize = 13.sp, color = theme.fg
                    )
                    Box(Modifier.size(3.dp).clip(CircleShape).background(theme.mutedFg))
                    Text(
                        text = "FIRST TO ${state.targetScore}",
                        fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp, color = theme.mutedFg, letterSpacing = 0.04.sp
                    )
                }
                ChipButton(
                    theme = theme,
                    enabled = state.undoHistory.isNotEmpty(),
                    onClick = onUndo
                ) {
                    Text(
                        "↺", fontFamily = SpaceGrotesk, fontSize = 16.sp,
                        color = if (state.undoHistory.isNotEmpty()) theme.fg else theme.mutedFg,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Team panels
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TeamPanel(
                        team = state.teamAName,
                        palette = OceanPalette,
                        score = state.scoreA,
                        target = state.targetScore,
                        isServing = state.servingTeamA && state.winner == null,
                        isLeader = state.scoreA > state.scoreB && state.winner == null,
                        isWinner = state.winner == state.teamAName,
                        showButtons = showButtons,
                        buttonsEnabled = state.winner == null,
                        isDark = theme.isDark,
                        onUp = { onAddPoint(true) },
                        onDown = { onUndo() },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    TeamPanel(
                        team = state.teamBName,
                        palette = SunsetPalette,
                        score = state.scoreB,
                        target = state.targetScore,
                        isServing = !state.servingTeamA && state.winner == null,
                        isLeader = state.scoreB > state.scoreA && state.winner == null,
                        isWinner = state.winner == state.teamBName,
                        showButtons = showButtons,
                        buttonsEnabled = state.winner == null,
                        isDark = theme.isDark,
                        onUp = { onAddPoint(false) },
                        onDown = { onUndo() },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                // VS pill
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(if (theme.isDark) Color(0xFF0B0F1A) else Color.White)
                        .border(1.5.dp, theme.surfaceBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "VS", fontFamily = Baloo2, fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp, color = theme.fg, letterSpacing = 0.06.sp
                    )
                }
            }

            // Bottom actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(theme = theme, label = "Reset", modifier = Modifier.weight(1f), onClick = onReset)
                ActionButton(theme = theme, label = "Swap serve", modifier = Modifier.weight(1f), onClick = onSwapServe)
            }
        }

        // Winner overlay
        if (state.winner != null) {
            WinnerOverlay(
                winner = state.winner,
                winnerPalette = if (state.winner == state.teamAName) OceanPalette else SunsetPalette,
                scoreA = state.scoreA,
                scoreB = state.scoreB,
                theme = theme,
                onRematch = onReset
            )
        }
    }
}

// ── Team panel ────────────────────────────────────────────────────────────────

@Composable
fun TeamPanel(
    team: String,
    palette: TeamPalette,
    score: Int,
    target: Int,
    isServing: Boolean,
    isLeader: Boolean,
    isWinner: Boolean,
    showButtons: Boolean,
    buttonsEnabled: Boolean,
    isDark: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val progressAnim by animateFloatAsState(
        targetValue = (score.toFloat() / target).coerceIn(0f, 1f),
        animationSpec = tween(350),
        label = "progress"
    )
    val borderColor = if (isLeader) palette.base else palette.base.copy(alpha = 0.33f)
    val borderWidth = if (isLeader) 2.dp else 1.5.dp
    val panelBg = if (isDark)
        Brush.verticalGradient(listOf(palette.deep, Color(0xFF0B0F1A)))
    else
        Brush.verticalGradient(listOf(palette.soft, palette.base.copy(alpha = 0.1f)))
    val scoreColor = if (isDark) Color.White else palette.deep
    val textColor = if (isDark) Color.White else palette.deep

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(panelBg)
            .border(borderWidth, borderColor, RoundedCornerShape(28.dp))
            .then(if (isLeader) Modifier.coloredGlow(palette.base) else Modifier)
            .swipeToScore(
                onSwipeUp = {
                    if (buttonsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onUp()
                    }
                },
                onSwipeDown = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDown()
                }
            )
            .padding(horizontal = 16.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Team name row
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(palette.base)
            )
            Text(
                text = team,
                fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.01).sp
            )
        }

        // Serving badge
        if (isServing) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .background(palette.base, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                Text(
                    "SERVING",
                    fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
                    fontSize = 10.sp, color = Color.White, letterSpacing = 0.1.sp
                )
            }
        } else {
            Spacer(Modifier.height(if (isServing) 0.dp else 22.dp))
        }

        // Score
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    (scaleIn(tween(200), initialScale = 1.1f) + fadeIn(tween(200))) togetherWith
                            (scaleOut(tween(150), targetScale = 0.9f) + fadeOut(tween(150)))
                },
                label = "score_$team"
            ) { s ->
                Text(
                    text = s.toString(),
                    fontFamily = Baloo2,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (s > 99) 100.sp else 120.sp,
                    color = scoreColor,
                    lineHeight = 108.sp,
                    letterSpacing = (-0.05).sp
                )
            }
            if (isWinner) {
                Text(
                    "🏆", fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                )
            }
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black.copy(alpha = if (isDark) 0.18f else 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressAnim)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.base)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Buttons or swipe hint
        if (showButtons) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onUp()
                    },
                    enabled = buttonsEnabled,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.base,
                        disabledContainerColor = palette.base.copy(alpha = 0.35f)
                    )
                ) {
                    Text("+", fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                }
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDown()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = scoreColor),
                    border = BorderStroke(1.5.dp, palette.base.copy(alpha = 0.55f))
                ) {
                    Text("−", fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = scoreColor)
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("↑", fontSize = 13.sp, color = scoreColor.copy(alpha = 0.6f))
                Text(
                    " Score  ·  ",
                    fontFamily = SpaceGrotesk, fontSize = 11.sp,
                    color = scoreColor.copy(alpha = 0.6f), fontWeight = FontWeight.Medium
                )
                Text("↓", fontSize = 13.sp, color = scoreColor.copy(alpha = 0.6f))
                Text(
                    " Undo",
                    fontFamily = SpaceGrotesk, fontSize = 11.sp,
                    color = scoreColor.copy(alpha = 0.6f), fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ── Winner overlay ────────────────────────────────────────────────────────────

@Composable
fun WinnerOverlay(
    winner: String,
    winnerPalette: TeamPalette,
    scoreA: Int,
    scoreB: Int,
    theme: AppTheme,
    onRematch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .background(theme.surface, RoundedCornerShape(24.dp))
                .border(1.5.dp, winnerPalette.base, RoundedCornerShape(24.dp))
                .padding(horizontal = 26.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏆", fontSize = 48.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = winner,
                fontFamily = Baloo2, fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp, color = winnerPalette.deep, textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "wins $scoreA–$scoreB",
                fontFamily = SpaceGrotesk, fontSize = 13.sp, color = theme.mutedFg
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onRematch,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = winnerPalette.base)
            ) {
                Text(
                    "Rematch", fontFamily = Baloo2,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White
                )
            }
        }
    }
}

// ── Shared small composables ──────────────────────────────────────────────────

@Composable
private fun ChipButton(
    theme: AppTheme,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .background(theme.chip, CircleShape)
            .clickableNoRipple(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ActionButton(theme: AppTheme, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .height(46.dp)
            .background(theme.surface, RoundedCornerShape(14.dp))
            .border(1.dp, theme.surfaceBorder, RoundedCornerShape(14.dp))
            .clickableNoRipple { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = theme.fg)
    }
}

// ── Modifier extensions ───────────────────────────────────────────────────────

@Composable
private fun Modifier.clickableNoRipple(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    this.clickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )

private fun Modifier.swipeToScore(onSwipeUp: () -> Unit, onSwipeDown: () -> Unit): Modifier =
    this.pointerInput(onSwipeUp, onSwipeDown) {
        var totalDy = 0f
        var triggered = false
        detectVerticalDragGestures(
            onDragStart = { totalDy = 0f; triggered = false },
            onDragEnd = { totalDy = 0f; triggered = false },
            onDragCancel = { totalDy = 0f; triggered = false }
        ) { change, dragAmount ->
            change.consume()
            if (!triggered) {
                totalDy += dragAmount
                if (abs(totalDy) > 80f) {
                    triggered = true
                    if (totalDy < 0) onSwipeUp() else onSwipeDown()
                }
            }
        }
    }

private fun Modifier.coloredGlow(color: Color): Modifier =
    this.drawBehind {
        drawRoundRect(
            color = color.copy(alpha = 0.22f),
            topLeft = Offset(-8.dp.toPx(), -8.dp.toPx()),
            size = Size(size.width + 16.dp.toPx(), size.height + 16.dp.toPx()),
            cornerRadius = CornerRadius(30.dp.toPx())
        )
    }

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SetupScreenPreview() {
    ScoreCounterTheme {
        ThemedBackground(MidnightTheme) {
            SetupScreen(theme = MidnightTheme, onNext = { _, _ -> })
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ModeSelectionScreenPreview() {
    ScoreCounterTheme {
        ThemedBackground(MidnightTheme) {
            ModeSelectionScreen(
                theme = MidnightTheme,
                teamAName = "Sharks", teamBName = "Eagles",
                onBack = {}, onPick = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun GameScreenPreview() {
    ScoreCounterTheme {
        ThemedBackground(MidnightTheme) {
            GameScreen(
                state = GameState(
                    teamAName = "Sharks", teamBName = "Eagles",
                    scoreA = 14, scoreB = 11, targetScore = 25,
                    servingTeamA = true, screen = Screen.GAME
                ),
                theme = MidnightTheme,
                btState = BtConnectionState.IDLE,
                onAddPoint = {}, onUndo = {}, onSwapServe = {},
                onReset = {}, onQuit = {}, speak = {}
            )
        }
    }
}
