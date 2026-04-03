package com.umc.hellodoctor.core.designsystem.token

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * HelloDoctor Dimension System (8dp scale)
 *
 * 디자인 시스템 기반 간격, 크기, 타이포그래피 토큰
 *
 * Spacing: 4dp, 8dp, 12dp, 16dp, 20dp, 24dp, 32dp, 40dp, 48dp (8dp 배수)
 */
object HelloDoctorDimensions {
    // ==================== SPACING ====================
    val Spacing0 = 0.dp
    val Spacing2 = 2.dp // 극소 간격
    val Spacing4 = 4.dp // 아주 작은 간격
    val Spacing8 = 8.dp // 작은 간격
    val Spacing12 = 12.dp // 기본 간격
    val Spacing16 = 16.dp // 중간 간격
    val Spacing20 = 20.dp // 큰 간격 (버튼 좌우 여백)
    val Spacing24 = 24.dp // 더 큰 간격
    val Spacing32 = 32.dp // 매우 큰 간격
    val Spacing40 = 40.dp // 초대형 간격
    val Spacing48 = 48.dp // 최대 간격

    // ==================== CORNER RADIUS ====================
    val CornerSmall = 8.dp // 작은 모서리 (input 필드)
    val CornerMedium = 12.dp // 중간 모서리 (button)
    val CornerLarge = 15.dp // 큰 모서리 (chat_corner)
    val CornerXLarge = 20.dp // 초대형 모서리 (카드)

    // ==================== TYPOGRAPHY SIZES ====================
    val TextSizeCaption = 12.sp // 작은 보조 텍스트 (설명, 캡션)
    val TextSizeBody = 15.sp // 기본 본문 및 버튼 텍스트
    val TextSizeTitle = 20.sp // 화면 타이틀/섹션 헤더
    val TextSizeHeadline = 24.sp // 헤드라인 (새로 추가)
    val TextSizeDisplay = 32.sp // 큰 디스플레이 텍스트 (새로 추가)

    // ==================== COMPONENT HEIGHTS ====================
    val ButtonHeight = 50.dp // 주요 액션 버튼 높이
    val ButtonHeightSmall = 40.dp // 작은 버튼 높이
    val ChoiceButtonHeight = 48.dp // 선택 버튼 높이 (예/아니오, 성별 등)

    // ==================== ICON SIZES ====================
    val IconSizeSmall = 16.dp // 작은 아이콘
    val IconSizeMedium = 24.dp // 중간 아이콘
    val IconSizeLarge = 32.dp // 큰 아이콘

    // ==================== ELEVATION / SHADOW ====================
    val ElevationNone = 0.dp // 그림자 없음
    val ElevationSmall = 2.dp // 작은 그림자
    val ElevationMedium = 4.dp // 중간 그림자
    val ElevationLarge = 8.dp // 큰 그림자 (모달 등)

    // ==================== LINE HEIGHT ====================
    /** 작은 텍스트 줄높이 */
    val LineHeightSmall = 16.sp

    /** 기본 줄높이 */
    val LineHeightMedium = 24.sp

    /** 큰 텍스트 줄높이 */
    val LineHeightLarge = 32.sp

    /** 매우 큰 텍스트 줄높이 */
    val LineHeightXLarge = 40.sp
}
