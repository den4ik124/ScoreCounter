package com.example.scorecounter.ui.theme

import androidx.compose.ui.graphics.Color

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
