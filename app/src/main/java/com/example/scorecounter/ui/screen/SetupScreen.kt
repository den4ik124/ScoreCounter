package com.example.scorecounter.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scorecounter.ui.ThemedBackground
import com.example.scorecounter.ui.component.ChipButton
import com.example.scorecounter.ui.component.VolleyballGlyph
import com.example.scorecounter.ui.component.clickableNoRipple
import com.example.scorecounter.ui.theme.AppTheme
import com.example.scorecounter.ui.theme.Baloo2
import com.example.scorecounter.ui.theme.MidnightTheme
import com.example.scorecounter.ui.theme.OceanPalette
import com.example.scorecounter.ui.theme.ScoreCounterTheme
import com.example.scorecounter.ui.theme.SpaceGrotesk
import com.example.scorecounter.ui.theme.SunsetPalette

@Composable
fun SetupScreen(theme: AppTheme, onNext: (String, String) -> Unit, onSettings: () -> Unit) {
    var teamA by remember { mutableStateOf("") }
    var teamB by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            ChipButton(theme = theme, onClick = onSettings) {
                Text("⚙", fontFamily = SpaceGrotesk, fontSize = 16.sp, color = theme.fg)
            }
        }
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(OceanPalette.base, SunsetPalette.base))),
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
            placeholder = { Text(placeholder, fontFamily = SpaceGrotesk, color = theme.mutedFg) },
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
            textStyle = TextStyle(
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SetupScreenPreview() {
    ScoreCounterTheme {
        ThemedBackground(MidnightTheme) {
            SetupScreen(theme = MidnightTheme, onNext = { _, _ -> }, onSettings = {})
        }
    }
}
