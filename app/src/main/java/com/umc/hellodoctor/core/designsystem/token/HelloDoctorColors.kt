package com.umc.hellodoctor.core.designsystem.token

import androidx.compose.ui.graphics.Color

/**
 * HelloDoctor Color Palette
 * Based on colors.xml + Phase 1 improvements (Issue #61)
 *
 * 색상 체계:
 * - Primary: 브랜드 메인 색 (파란색)
 * - Secondary: 보조 강조 색 (개선: 대비율 강화)
 * - Tertiary: 정보 안내 색 (청록, Primary와 분리)
 * - Gray: 8단계 회색 스케일
 * - Semantic: 성공/경고/에러
 */
object HelloDoctorColors {
    // ==================== PRIMARY ====================
    /** 브랜드 메인 색 — 로고, 주요 액션 버튼 */
    val Primary = Color(0xFF1852FF)

    /** Primary 배경 위의 텍스트/아이콘 */
    val OnPrimary = Color(0xFFFFFFFF)

    /** Primary 선택 상태 배경 (구성 요소의 활성 상태) */
    val PrimaryContainer = Color(0xFFE8F0FF)

    /** Primary Container 위의 텍스트 */
    val OnPrimaryContainer = Color(0xFF0D3292)

    // ==================== SECONDARY ====================
    /** 보조 강조 색 — Issue #61 Phase 1: #CCC2DC → #1244CC (대비율 개선) */
    val Secondary = Color(0xFF1244CC)

    /** Secondary 배경 위의 텍스트/아이콘 */
    val OnSecondary = Color(0xFFFFFFFF)

    /** Secondary 선택 상태 배경 */
    val SecondaryContainer = Color(0xFFDFE3FF)

    val OnSecondaryContainer = Color(0xFF0A2A8A)

    // ==================== TERTIARY ====================
    /** 정보 안내 색 (정보/진행 상태) — Issue #61: #3B82F6 → #0EA5E9 (분리) */
    val Tertiary = Color(0xFF0EA5E9)

    /** Tertiary 배경 위의 텍스트/아이콘 */
    val OnTertiary = Color(0xFFFFFFFF)

    /** Tertiary 선택 상태 배경 */
    val TertiaryContainer = Color(0xFFB3E5FC)

    val OnTertiaryContainer = Color(0xFF00546C)

    // ==================== ERROR ====================
    /** 에러/위험 알림 */
    val Error = Color(0xFFDC2626)

    /** Error 배경 위의 텍스트/아이콘 */
    val OnError = Color(0xFFFFFFFF)

    /** Error 선택 상태 배경 */
    val ErrorContainer = Color(0xFFFEEAEA)

    val OnErrorContainer = Color(0xFF6B0C0C)

    // ==================== TEXT ====================
    /** 본문 텍스트 — 기본 가독성 */
    val TextMain = Color(0xFF2A2A2A)

    /** 서브/보조 설명 텍스트 — Issue #61: #696969 → #555555 (가독성 강화) */
    val TextSub = Color(0xFF555555)

    // ==================== BACKGROUND ====================
    /** 화면 기본 배경 색 */
    val Background = Color(0xFFF4F4F4)

    /** 기본 표면 색 (카드, 버튼 등) */
    val Surface = Color(0xFFFFFFFF)

    /** 높여진 표면 색 — 모달, 드롭다운, 플로팅 요소 */
    val SurfaceRaised = Color(0xFFFAFCFF)

    /** Surface 위의 텍스트 */
    val OnSurface = Color(0xFF2A2A2A)

    /** Surface의 변형 (약간의 대비) */
    val SurfaceVariant = Color(0xFFEEEEEE)

    /** SurfaceVariant 위의 텍스트 */
    val OnSurfaceVariant = Color(0xFF555555)

    // ==================== OUTLINE ====================
    /** 테두리/분리선 기본 색 */
    val Outline = Color(0xFFBDBDBD)

    /** 테두리/분리선 약한 색 */
    val OutlineVariant = Color(0xFFEEEEEE)

    // ==================== SCRIM ====================
    /** 모달 배경 반투명 검정 */
    val Scrim = Color(0xFF000000)

    // ==================== GRAY SCALE (8-step + 확장) ====================
    val Gray50 = Color(0xFFFAFAFA) // 극연한 회색
    val Gray100 = Color(0xFFF5F5F5) // 매우 연한 회색
    val Gray200 = Color(0xFFEEEEEE) // 연한 회색
    val Gray300 = Color(0xFFE0E0E0) // 밝은 회색 (배경용)
    val Gray400 = Color(0xFFBDBDBD) // 중간 회색 (분리선)
    val Gray500 = Color(0xFF9E9E9E) // 중간 회색
    val Gray600 = Color(0xFF757575) // Issue #61: #666666 → 스케일 정정
    val Gray700 = Color(0xFF616161) // Issue #61: #777777 → 스케일 정정
    val Gray800 = Color(0xFF424242) // 어두운 회색
    val Gray900 = Color(0xFF212121) // 매우 어두운 회색

    // ==================== BUTTON STATES ====================
    val ButtonPrimaryBg = Color(0xFF1852FF) // Primary 버튼 배경
    val ButtonPrimaryText = Color(0xFFFFFFFF) // Primary 버튼 텍스트
    val ButtonPrimaryBgDisabled = Color(0xFFCCCCCC) // Primary 버튼 비활성
    val ButtonPrimaryTextDisabled = Color(0xFFFFFFFF)

    val ButtonSecondaryBg = Color(0xFFFFFFFF) // Secondary 버튼 배경
    val ButtonSecondaryText = Color(0xFF555555) // Issue #61: #696969 → 가독성 강화
    val ButtonSecondaryBgDisabled = Color(0xFFF0F0F0)
    val ButtonSecondaryTextDisabled = Color(0xFFB0B0B0)

    // ==================== SEMANTIC COLORS ====================
    val Success = Color(0xFF10B981) // 성공/확인 (초록)
    val Warning = Color(0xFFF59E0B) // 경고 (주황)
    val Info = Color(0xFF0EA5E9) // 정보 (청록)
}
