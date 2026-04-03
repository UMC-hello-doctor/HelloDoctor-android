package com.umc.hellodoctor.core.design_system.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.umc.hellodoctor.core.design_system.token.HelloDoctorColors
import com.umc.hellodoctor.core.design_system.token.HelloDoctorDimensions

/**
 * HelloDoctor Typography — Material3 기반 타이포그래피 시스템
 *
 * Material3 표준 스타일:
 * - displayLarge, displayMedium, displaySmall (큰 디스플레이)
 * - headlineLarge, headlineMedium, headlineSmall (헤드라인)
 * - titleLarge, titleMedium, titleSmall (제목)
 * - bodyLarge, bodyMedium, bodySmall (본문)
 * - labelLarge, labelMedium, labelSmall (레이블/버튼)
 */
val HelloDoctorTypography = Typography(
    // ==================== DISPLAY ====================
    displayLarge = TextStyle(
        fontSize = HelloDoctorDimensions.TextSizeDisplay,
        lineHeight = HelloDoctorDimensions.LineHeightXLarge,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        color = HelloDoctorColors.TextMain
    ),
    displayMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        color = HelloDoctorColors.TextMain
    ),
    displaySmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        color = HelloDoctorColors.TextMain
    ),

    // ==================== HEADLINE ====================
    headlineLarge = TextStyle(
        fontSize = HelloDoctorDimensions.TextSizeHeadline,
        lineHeight = HelloDoctorDimensions.LineHeightLarge,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        color = HelloDoctorColors.TextMain
    ),
    headlineMedium = TextStyle(
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        color = HelloDoctorColors.TextMain
    ),
    headlineSmall = TextStyle(
        fontSize = 18.sp,
        lineHeight = 26.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        color = HelloDoctorColors.TextMain
    ),

    // ==================== TITLE ====================
    titleLarge = TextStyle(
        fontSize = HelloDoctorDimensions.TextSizeTitle,
        lineHeight = 28.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        color = HelloDoctorColors.TextMain
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        color = HelloDoctorColors.TextMain
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        color = HelloDoctorColors.TextMain
    ),

    // ==================== BODY ====================
    bodyLarge = TextStyle(
        fontSize = HelloDoctorDimensions.TextSizeBody,
        lineHeight = HelloDoctorDimensions.LineHeightMedium,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        color = HelloDoctorColors.TextMain
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        color = HelloDoctorColors.TextMain
    ),
    bodySmall = TextStyle(
        fontSize = HelloDoctorDimensions.TextSizeCaption,
        lineHeight = HelloDoctorDimensions.LineHeightSmall,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        color = HelloDoctorColors.TextSub
    ),

    // ==================== LABEL ====================
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        color = HelloDoctorColors.TextMain
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        color = HelloDoctorColors.TextSub
    ),
    labelSmall = TextStyle(
        fontSize = HelloDoctorDimensions.TextSizeCaption,
        lineHeight = HelloDoctorDimensions.LineHeightSmall,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        color = HelloDoctorColors.TextSub
    )
)
