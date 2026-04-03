package com.umc.hellodoctor.core.designsystem.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.umc.hellodoctor.core.designsystem.theme.HelloDoctorTheme
import com.umc.hellodoctor.core.designsystem.token.HelloDoctorColors
import com.umc.hellodoctor.core.designsystem.token.HelloDoctorDimensions

/**
 * HelloDoctor Primary Button
 *
 * 주요 액션 버튼으로, 강조된 색상의 버튼입니다.
 * 확인, 다음, 전송, 로그인 등의 주요 작업에 사용됩니다.
 *
 * @param text 버튼 텍스트
 * @param onClick 클릭 콜백
 * @param modifier 커스텀 modifier (기본값: fullWidth 50dp)
 * @param enabled 활성 여부 (기본값: true)
 *
 * 사용 예:
 * ```kotlin
 * PrimaryButton(
 *     text = "로그인",
 *     onClick = { viewModel.login() },
 *     enabled = isFormValid
 * )
 * ```
 */
@Composable
fun PrimaryButton(
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
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = HelloDoctorColors.Primary,
                contentColor = HelloDoctorColors.OnPrimary,
                disabledContainerColor = HelloDoctorColors.ButtonPrimaryBgDisabled,
                disabledContentColor = HelloDoctorColors.ButtonPrimaryTextDisabled,
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
private fun PrimaryButtonPreview() {
    HelloDoctorTheme {
        PrimaryButton(
            text = "로그인",
            onClick = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonDisabledPreview() {
    HelloDoctorTheme {
        PrimaryButton(
            text = "로그인",
            onClick = { },
            enabled = false,
        )
    }
}
