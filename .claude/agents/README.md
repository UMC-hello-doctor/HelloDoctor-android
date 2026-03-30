# 에이전트 정의 폴더

이 폴더에는 Claude Code의 **커스텀 서브에이전트** 정의 파일이 있습니다.

## 에이전트란?

에이전트 파일(`.md`)은 특정 역할에 특화된 서브에이전트의 시스템 프롬프트와 도구 제한을 정의합니다.
`Agent` 도구의 `subagent_type` 파라미터로 호출되며, 주로 스킬에서 체인 형태로 실행됩니다.

## 에이전트 목록

| 파일 | 이름 | 역할 | 사용 도구 |
|------|------|------|-----------|
| `feature-planner.md` | `feature-planner` | 기능 요청 분석 → 구현 계획 수립 | Read, Glob, Grep |
| `feature-coder.md` | `feature-coder` | 계획 기반 Kotlin 코드 구현 | Read, Glob, Grep, Write, Edit, Bash |
| `feature-refactorer.md` | `feature-refactorer` | 코드 품질 개선 (중복 제거, Kotlin 관용구) | Read, Glob, Grep, Edit |
| `feature-reviewer.md` | `feature-reviewer` | 버그·보안·아키텍처 최종 리뷰 | Read, Glob, Grep |
| `bug-analyzer.md` | `bug-analyzer` | 버그 원인 분석 → 수정 계획 수립 | Read, Glob, Grep |

## 에이전트 체인 구조

```
/feature  →  feature-planner → feature-coder → feature-refactorer → feature-reviewer
/bug      →  bug-analyzer    → feature-coder → feature-reviewer
```

## 에이전트 추가 방법

새 에이전트를 만들려면 이 폴더에 `.md` 파일을 생성하고 아래 형식을 따르세요.

```markdown
---
name: 에이전트-이름
description: 에이전트 역할 한 줄 설명 (호출 시 어떤 용도인지 명확히)
tools: Read, Glob, Grep, Write, Edit, Bash  # 필요한 도구만 나열
---

에이전트 시스템 프롬프트 내용...
```

> **팁**: `tools` 필드를 최소화할수록 에이전트가 범위를 벗어나는 행동을 방지할 수 있습니다.
