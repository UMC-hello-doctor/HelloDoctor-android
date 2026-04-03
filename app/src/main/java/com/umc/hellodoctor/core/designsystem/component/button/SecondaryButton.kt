package com.umc.hellodoctor.core.designsystem.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.umc.hellodoctor.core.designsystem.theme.HelloDoctorTheme
import com.umc.hellodoctor.core.designsystem.token.HelloDoctorColors
import com.umc.hellodoctor.core.designsystem.token.HelloDoctorDimensions

/**
 * HelloDoctor Secondary Button
 *
 * 보조 액션 버튼으로, 아웃라인 스타일의 버튼입니다.
 * 취소, 건너뛰기, 돌아가기 등의 보조 작업에 사용됩니다.
 *
 * @param text 버튼 텍스트
 * @param onClick 클릭 콜백
 * @param modifier 커스텀 modifier (기본값: fullWidth 50dp)
 * @param enabled 활성 여부 (기본값: true)
 *
 * 사용 예:
 * ```kotlin
 * SecondaryButton(
 *     text = "취소",
 *     onClick = { navController.popBackStack() }
 * )
 * ```
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier =
        Modifier
            .fillMaxWidth()
            .height(HelloDoctorDimensions.ButtonHeight)
            .padding(
                horizontal = HelloDoctorDimensions.Spacing20,
                vertical = HelloDoctorDimensions.Spacing8,
            ),
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = HelloDoctorColors.ButtonSecondaryBg,
                contentColor = HelloDoctorColors.ButtonSecondaryText,
                disabledContainerColor = HelloDoctorColors.ButtonSecondaryBgDisabled,
                disabledContentColor = HelloDoctorColors.ButtonSecondaryTextDisabled,
            ),
        shape = RoundedCornerShape(HelloDoctorDimensions.CornerLarge),
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = HelloDoctorDimensions.TextSizeBody,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    HelloDoctorTheme {
        SecondaryButton(
            text = "취소",
            onClick = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonDisabledPreview() {
    HelloDoctorTheme {
        SecondaryButton(
            text = "취소",
            onClick = { },
            enabled = false,
        )
    }
}
