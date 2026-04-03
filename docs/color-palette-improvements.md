# 색상 팔레트 개선안

> 헬스케어 · 신뢰감 · 10~20대 타겟 기준  
> 역할(Role) 기반으로 분석 및 개선한 색상 토큰 가이드

---

## 변경 요약

| 구분 | 항목 수 |
|------|--------|
| 유지 | 14개 |
| 교체 | 4개 |
| 추가 | 2개 |
| 삭제 | 2개 |
| 보류 | 1개 |

---

## 브랜드 색상

### `color_primary` — 유지

| 항목 | 값 |
|------|-----|
| HEX | `#1852FF` |
| On color | `#FFFFFF` |
| 역할 | 브랜드 메인 — 버튼, 링크, 주요 액션 |

헬스케어에서 가장 보편적으로 쓰이는 신뢰·전문성 색상. 채도가 높아 10~20대 앱 UI에서도 선명하게 작동.

---

### `color_primary_container` — 추가 🆕

| 항목 | 값 |
|------|-----|
| HEX | `#E8F0FF` |
| On color | `#1244CC` |
| 역할 | Primary 연한 배경 — 선택 상태, 활성 배경 |

선택된 탭, 활성 리스트 아이템 등 Primary가 배경으로 쓰여야 할 때 사용.

---

### `color_secondary` — 교체 ⚠️

| 항목 | 기존 | 개선 |
|------|------|------|
| HEX | `#CCC2DC` | `#1244CC` |
| On color | `#FFFFFF` | `#FFFFFF` |
| 역할 | 브랜드 보조 | 브랜드 보조 — Primary 딥버전, 강조 텍스트 |

**교체 이유:** 기존 연보라(`#CCC2DC`)는 Primary 파란색과 색상 계통이 달라 브랜드 통일성을 해침. 흰색 텍스트와의 대비율도 1.9:1로 WCAG AA 기준(4.5:1) 미충족. Primary의 명도를 낮춘 딥블루로 교체해 계통을 통일.

---

### `color_on_primary` / `color_on_secondary` — 유지

| 항목 | 값 |
|------|-----|
| HEX | `#FFFFFF` |
| 역할 | 브랜드 색상 위 텍스트 |

---

## 시맨틱 색상

### Success — 유지

| 토큰 | HEX | 역할 |
|------|-----|------|
| `color_success` | `#10B981` | 건강 달성, 복약 완료, 정상 수치 |
| `color_success_light` | `#D1FAE5` | 성공 상태 배경 |

초록은 헬스케어에서 건강·회복을 상징. 역할과 색상이 완벽히 일치.

---

### Error — 유지

| 토큰 | HEX | 역할 |
|------|-----|------|
| `color_error` | `#EF4444` | 이상 수치, 긴급 알림, 입력 오류 |
| `color_error_light` | `#FEE2E2` | 에러 상태 배경 |

> **주의:** 헬스케어 앱에서 에러 색상이 과도하게 노출되면 사용자 불안감을 유발할 수 있음. 노출 빈도 조절 권장.

---

### Warning — 유지

| 토큰 | HEX | 역할 |
|------|-----|------|
| `color_warning` | `#F59E0B` | 경계 수치, 복약 지연, 주의 상태 |
| `color_warning_light` | `#FEF3C7` | 경고 상태 배경 |

---

### Info — 교체 ⚠️

| 토큰 | 기존 | 개선 |
|------|------|------|
| `color_info` | `#3B82F6` | `#0EA5E9` |
| `color_info_light` | `#DBEAFE` | `#E0F2FE` |
| 역할 | 정보 | 복약 안내, 건강 팁 — Primary와 명확히 구분 |

**교체 이유:** 기존 `#3B82F6`은 `color_primary(#1852FF)`와 모두 파란색 계열이라 사용자가 "브랜드 버튼"과 "정보 안내"를 구별하기 어려움. 헬스케어에서는 복약 안내, 건강 팁 등 정보성 메시지가 많아 이 혼란이 특히 위험. 청록 계열로 분리.

---

## 텍스트 색상

| 토큰 | HEX | 대비율 | 역할 | 변경 |
|------|-----|--------|------|------|
| `color_text_main` | `#2A2A2A` | 17.1:1 | 기본 텍스트 — 제목, 본문, 의료 수치 | 유지 |
| `color_text_sub` | `#555555` | 7.4:1 | 보조 텍스트 — 라벨, 설명 | **교체** (`#696969` → `#555555`) |
| `color_text_hint` | `#A0A0A0` | 2.8:1 | Placeholder, 부가 설명 | 유지 |
| `color_text_disabled` | `#CCCCCC` | — | 비활성 텍스트 (WCAG 예외) | 유지 |

**`color_text_sub` 교체 이유:** 기존 `#696969`는 흰 배경에서 대비율 4.5:1로 WCAG AA를 간신히 충족하나, 14px 미만 소폰트에서는 위험. `#555555`로 강화해 여유 있는 가독성 확보.

---

## 배경 / 표면

| 토큰 | HEX | 레이어 | 역할 | 변경 |
|------|-----|--------|------|------|
| `color_background` | `#F4F4F4` | 0 (최하단) | 앱 기본 배경 | 유지 |
| `color_surface` | `#FFFFFF` | 1 (중간) | 카드, 리스트 아이템 | 유지 |
| `color_surface_raised` | `#FAFCFF` | 2 (최상단) | 모달, 드롭다운 | **추가** 🆕 |

**`color_surface_raised` 추가 이유:** 기존 2단계 배경 구조로는 모달·드롭다운 등 높은 elevation의 컴포넌트를 표현할 방법이 없었음.

---

## 버튼 색상

### Primary 버튼 — 유지

| 토큰 | HEX |
|------|-----|
| `color_button_primary_bg` | `#1852FF` |
| `color_button_primary_text` | `#FFFFFF` |
| `color_button_primary_bg_disabled` | `#CCCCCC` |
| `color_button_primary_text_disabled` | `#FFFFFF` |

### Secondary 버튼 — text 교체

| 토큰 | 기존 | 개선 |
|------|------|------|
| `color_button_secondary_bg` | `#FFFFFF` | `#FFFFFF` |
| `color_button_secondary_text` | `#696969` | `#555555` |
| `color_button_secondary_bg_disabled` | `#F0F0F0` | `#F0F0F0` |
| `color_button_secondary_text_disabled` | `#B0B0B0` | `#B0B0B0` |
| `color_button_secondary_stroke` | `#E0E0E0` | `#E0E0E0` |

---

## Gray 스케일 — 순서 정정

| 토큰 | 기존 | 개선 | 변경 |
|------|------|------|------|
| `gray_50` | `#FAFAFA` | `#FAFAFA` | 유지 |
| `gray_100` | `#F5F5F5` | `#F5F5F5` | 유지 |
| `gray_200` | `#EEEEEE` | `#EEEEEE` | 유지 |
| `gray_300` | `#E0E0E0` | `#E0E0E0` | 유지 |
| `gray_400` | `#BDBDBD` | `#BDBDBD` | 유지 |
| `gray_500` | `#9E9E9E` | `#9E9E9E` | 유지 |
| `gray_600` | `#666666` | `#757575` | **교체** |
| `gray_700` | `#777777` | `#616161` | **교체** |
| `gray_800` | `#424242` | `#424242` | 유지 |
| `gray_900` | `#212121` | `#212121` | 유지 |

**정정 이유:** 기존 `gray_600(#666666)`이 `gray_700(#777777)`보다 어두워 숫자가 올라갈수록 어두워져야 하는 스케일 원칙을 위반. 다크모드 및 컴포넌트 작업 시 예기치 않은 버그 유발 가능.

---

## 제거 및 보류

### 삭제 권장

| 토큰 | HEX | 이유 |
|------|-----|------|
| `blue_primary` | `#2563EB` | `color_primary`와 역할 중복. 링크 색상 전용으로 명확히 정의하지 않을 경우 삭제. |
| `alias: primary` | `#1852FF` | `color_primary`와 완전히 동일한 중복 alias. `color_primary`만 사용. |

### 보류 — 역할 정의 필요

| 토큰 | HEX | 상태 |
|------|-----|------|
| `orange_accent` | `#FF6B35` | 역할 미정의. CTA 강조·배지·이벤트 배너 등 구체적 용도 확정 후 이름 변경 또는 삭제. |

---

## 개선 XML

```xml
<!-- ============= 교체 항목 ============= -->

<!-- Secondary: 연보라 → 브랜드 딥블루 -->
<color name="color_secondary">#1244CC</color>
<color name="color_on_secondary">#FFFFFF</color>

<!-- Info: 파란색 → 청록 (Primary와 분리) -->
<color name="color_info">#0EA5E9</color>
<color name="color_info_light">#E0F2FE</color>

<!-- 텍스트 보조: 대비 강화 -->
<color name="color_text_sub">#555555</color>

<!-- Secondary 버튼 텍스트: 대비 강화 -->
<color name="color_button_secondary_text">#555555</color>

<!-- Gray 스케일: 역전 오류 수정 -->
<color name="gray_600">#757575</color>
<color name="gray_700">#616161</color>

<!-- ============= 추가 항목 ============= -->

<!-- Primary 연한 배경 (선택 상태, 활성 배경) -->
<color name="color_primary_container">#E8F0FF</color>

<!-- 표면 elevation 3단계 (모달, 드롭다운) -->
<color name="color_surface_raised">#FAFCFF</color>

<!-- ============= 삭제 권장 ============= -->
<!-- blue_primary: color_primary와 역할 충돌 → 삭제 -->
<!-- alias "primary": color_primary 중복 → 삭제 -->
```

---

*최종 업데이트: 2026-04-03*
