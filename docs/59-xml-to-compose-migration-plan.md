# XML → Compose 마이그레이션 계획

**이슈**: #59 [Design] XML → Compose 마이그레이션
**작성일**: 2026-04-03
**상태**: 계획 수립

---

## 개요

HelloDoctor-android 앱을 점진적으로 XML 기반 UI에서 Jetpack Compose 기반 UI로 마이그레이션하는 계획입니다.

---

## 목표

### 주요 목표
1. **UI 일관성 확보**: 통일된 디자인 시스템 적용
2. **유지보수성 향상**: 코드 복잡도 감소, 상태 관리 단순화
3. **개발 생산성 증대**: 재사용 가능한 Compose 컴포넌트 라이브러리 구축
4. **테스트 용이성**: Compose Preview 및 자동화 테스트 강화

### 부가 목표
- Kotlin 언어 특성 극대화
- 다크모드 지원 강화
- 접근성 개선

---

## 마이그레이션 로드맵

### Phase 1: 기반 준비 (진행 중)

#### 1.1 매직넘버 제거 (#60) - ✅ MERGED
- 모든 dimension 값 dimens.xml로 통합
- **Status**: Completed

#### 1.2 디자인 시스템 구조 정의 (#61) - 🔄 IN PROGRESS
- **목표**: XML 기반 통일된 디자인 시스템 구축
- **포함 사항**:
  - ✅ WCAG 색상 팔레트 개선 (PR #72)
  - Spacing 시스템 (8dp scale)
  - Typography 시스템
  - Style 정리 및 정규화
  - Compose Theme 연동 준비
- **타이밍**: 2026-04 (Phase 1 전반)

### Phase 2: Compose 기반 구축 (계획)

#### 2.1 ComposeView 도입 (#62) - ⏳ TODO
- **목표**: 기존 XML UI와 Compose UI를 공존하도록 설정
- **포함 사항**:
  - Material3 Theme 정의
  - 기본 Compose 컴포넌트 라이브러리
  - State 관리 패턴 정의
- **타이밍**: 2026-05

#### 2.2 Home 화면 전환 (#63) - ⏳ TODO
- **목표**: 첫 번째 화면을 Compose로 전환
- **포함 사항**:
  - Home Fragment → Compose 변환
  - Navigation 통합
  - 상태 관리 체계화
- **타이밍**: 2026-05 (2.1과 병렬)

### Phase 3: 점진적 전환 (계획)

#### 3.1 추가 화면 전환 - ⏳ TODO
- Drug 기능 화면
- Chat 기능 화면
- UserInfo 화면
- 기타 화면

#### 3.2 레거시 코드 정리 - ⏳ TODO
- XML 리소스 정리 (#64)
- 불필요한 Adapter 제거
- Fragment 정리

---

## 기술 전략

### 점진적 전환 (Recommended)

**장점**:
- ✅ 안정성 높음 (기존 코드 영향 최소화)
- ✅ 단계적 검증 가능
- ✅ 릴리스 위험 낮음

**단점**:
- ⚠️ 전환 기간 길어짐
- ⚠️ XML/Compose 혼용 복잡도 증가
- ⚠️ 유지보수 비용 일시적 증가

### 구현 패턴

#### 1. ComposeView를 활용한 점진적 전환
```
Fragment (XML) 
  └─ ComposeView 
      └─ Compose UI
```

#### 2. 상태 관리 통일
```
ViewModel (Shared)
  ├─ Fragment + XML
  └─ Compose UI (ComposeView)
```

#### 3. 색상/크기 공유
```
colors.xml (XML)
  ↓
Material3 Theme (Compose)
  ↓
@Composable 컴포넌트
```

---

## 디자인 시스템 (Phase 1 #61)

### 색상 시스템
- **Semantic colors**: success, error, warning, info
- **WCAG AA 준수**: 모든 색상 조합 4.5:1 이상 대비
- **Material3 호환**: Compose 연동 용이

### Spacing 시스템 (향후)
- **8dp 기반 scale**: 4, 8, 16, 24, 32, 40, 48, 56dp
- **컴포넌트 크기**: button, textinput, card 등
- **Corner radius**: 체계적 scale 정의

### Typography 시스템 (향후)
- **텍스트 크기**: 6-8개 레벨
- **폰트 가중치**: Regular, Medium, Bold
- **줄 높이**: 1.5x 기본 적용

---

## 위험 요소 및 완화 전략

### 위험 1: XML/Compose 혼용 상태 관리 복잡도

**위험도**: 높음

**완화 전략**:
- MVVM 패턴 엄격히 준수
- ViewModel을 공유하여 단일 상태 관리
- StateFlow/LiveData로 일관성 유지

### 위험 2: 디자인 시스템 미정의로 인한 중복 구현

**위험도**: 높음

**완화 전략**:
- Phase 1에서 완전한 디자인 시스템 정의
- Compose 전환 전 XML 기반 검증
- Code review에서 디자인 시스템 준수 확인

### 위험 3: 성능 저하

**위험도**: 중간

**완화 전략**:
- ComposeView 도입 시 성능 벤치마크
- 프로파일링으로 병목 지점 파악
- 필요시 부분 최적화

### 위험 4: 테스트 커버리지 저하

**위험도**: 중간

**완화 전략**:
- Compose Preview로 UI 테스트
- 단위 테스트 우선 작성
- 통합 테스트 자동화

---

## 의존 관계

```
#60 매직넘버 제거 ✅
    ↓
#61 디자인 시스템 구조 정의 🔄
    ├─ 색상 팔레트 개선 (PR #72)
    ├─ Spacing 시스템
    ├─ Typography 시스템
    └─ Compose Theme 연동 준비
    ↓
#62 ComposeView 도입 ⏳
    ↓
#63 Home 화면 전환 ⏳
    ↓
#64 XML 제거 및 정리 ⏳
```

---

## 성공 지표

### Phase 1 (#61) 완료 기준
- [ ] WCAG AA 색상 조합 100% 준수
- [ ] Spacing scale 8dp 기반 적용
- [ ] Typography 6개 레벨 정의
- [ ] Material3 Theme 호환 확인

### Phase 2 (#62-63) 완료 기준
- [ ] ComposeView 통합 테스트 성공
- [ ] Home 화면 Compose 변환 완료
- [ ] 성능 저하 없음 (< 10ms 프레임 저하)
- [ ] 자동화 테스트 커버리지 > 70%

### 전체 마이그레이션 완료 기준
- [ ] 모든 화면 Compose 변환
- [ ] XML 레거시 코드 0
- [ ] 성능 개선 (평균 프레임 60fps 이상)
- [ ] 테스트 커버리지 > 80%

---

## 참고 자료

### 프레임워크 문서
- [Jetpack Compose 공식 문서](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Compose in Existing App](https://developer.android.com/jetpack/compose/migrate)

### HelloDoctor 관련
- `.claude/design-system-improvements.md` — 디자인 시스템 상세 분석
- `docs/color-palette-improvements.md` — 색상 팔레트 개선안

---

**최종 업데이트**: 2026-04-03
**담당**: UMC Hello Doctor Dev Team
