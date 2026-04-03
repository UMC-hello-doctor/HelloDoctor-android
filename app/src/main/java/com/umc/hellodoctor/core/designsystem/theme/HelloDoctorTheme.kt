package com.umc.hellodoctor.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.umc.hellodoctor.core.designsystem.token.HelloDoctorColors

/**
 * Light Color Scheme — HelloDoctor Light Theme
 * Material3 기반 라이트 테마 정의
 */
val LightColorScheme =
    lightColorScheme(
        primary = HelloDoctorColors.Primary, // #1852FF
        onPrimary = HelloDoctorColors.OnPrimary, // #FFFFFF
        primaryContainer = HelloDoctorColors.PrimaryContainer, // #E8F0FF
        onPrimaryContainer = HelloDoctorColors.OnPrimaryContainer, // #0D3292
        secondary = HelloDoctorColors.Secondary, // #1244CC (개선됨)
        onSecondary = HelloDoctorColors.OnSecondary, // #FFFFFF
        secondaryContainer = HelloDoctorColors.SecondaryContainer, // #DFE3FF
        onSecondaryContainer = HelloDoctorColors.OnSecondaryContainer, // #0A2A8A
        tertiary = HelloDoctorColors.Tertiary, // #0EA5E9 (정보색, 분리됨)
        onTertiary = HelloDoctorColors.OnTertiary, // #FFFFFF
        tertiaryContainer = HelloDoctorColors.TertiaryContainer, // #B3E5FC
        onTertiaryContainer = HelloDoctorColors.OnTertiaryContainer, // #00546C
        error = HelloDoctorColors.Error, // #DC2626
        onError = HelloDoctorColors.OnError, // #FFFFFF
        errorContainer = HelloDoctorColors.ErrorContainer, // #FEEEEE
        onErrorContainer = HelloDoctorColors.OnErrorContainer, // #6B0C0C
        background = HelloDoctorColors.Background, // #F4F4F4
        onBackground = HelloDoctorColors.TextMain, // #2A2A2A
        surface = HelloDoctorColors.Surface, // #FFFFFF
        onSurface = HelloDoctorColors.TextMain, // #2A2A2A
        surfaceVariant = HelloDoctorColors.SurfaceVariant, // #EEEEEE
        onSurfaceVariant = HelloDoctorColors.TextSub, // #555555
        outline = HelloDoctorColors.Outline, // #BDBDBD
        outlineVariant = HelloDoctorColors.OutlineVariant, // #EEEEEE
        scrim = Color(0xFF000000), // #000000
    )

/**
 * Dark Color Scheme — HelloDoctor Dark Theme (향후 확장)
 * 다크 테마는 추후 구현 예정
 */
val DarkColorScheme =
    lightColorScheme()

/**
 * Material3 Shapes — HelloDoctor 모서리 반경 정의
 *
 * Material3의 Shape 시스템을 HelloDoctor Dimensions와 연동
 */
val HelloDoctorShapes = Shapes() // 기본값 사용 (향후 커스텀 가능)

/**
 * HelloDoctorTheme — Material3 기반 통합 테마
 *
 * 사용:
 * ```kotlin
 * HelloDoctorTheme {
 *     // Compose 콘텐츠
 *     MyComposeUI()
 * }
 * ```
 *
 * ComposeView에서 사용:
 * ```kotlin
 * binding.composeView.setContent {
 *     HelloDoctorTheme {
 *         MyComposeUI()
 *     }
 * }
 * ```
 *
 * @param isDarkTheme 다크 테마 사용 여부 (기본: 시스템 설정 따름)
 * @param content Composable 콘텐츠
 */
@Composable
fun HelloDoctorTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = HelloDoctorShapes,
        typography = HelloDoctorTypography,
        content = content,
    )
}
