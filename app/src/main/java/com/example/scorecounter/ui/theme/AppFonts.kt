package com.example.scorecounter.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.scorecounter.R

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
