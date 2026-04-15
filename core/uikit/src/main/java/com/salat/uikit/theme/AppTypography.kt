package com.salat.uikit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
@ConsistentCopyVisibility
data class AppTypography internal constructor(

    val headline1: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 43.sp,
    ),
    val headline2: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 35.sp,
    ),
    val toolbar: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 21.sp
    ),
    val stubTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    val screenTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 19.sp
    ),
    val dialogListTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 19.sp
    ),
    val confirmDialogTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 19.sp,
        lineHeight = 22.sp
    ),
    val dialogSubtitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    val idTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 13.sp
    ),
    val buttonTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 19.sp
    ),
    val cardTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 18.sp
    ),
    val cardFormatTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 17.sp
    ),
    val radioTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 17.sp
    ),
    val addingSectionTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 48.sp,
    ),
    val dialogTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 19.sp,
        lineHeight = 22.sp
    ),
    val dialogButton: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 18.sp
    ),
    val settingsTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 18.sp
    ),
    val toggleChip: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 15.sp
    ),
    val statusTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 23.sp
    ),
    val aboutText: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    val alertDialogButton: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 18.sp
    ),
    val sourceType: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
)

// val MaterialTypography = Typography()
internal val LocalAppTypography = staticCompositionLocalOf { AppTypography() }
