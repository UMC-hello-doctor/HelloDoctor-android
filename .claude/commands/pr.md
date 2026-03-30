---
name: pr
description: Creates a GitHub PR for the current branch following the HelloDoctor-android PR template. Reads git diff and commit history to fill in the template automatically. Usage: /pr [issue number]
---

You will create a GitHub Pull Request for the current branch following the project's PR template and conventions.

## Step 1 — Gather Information

Run these commands to understand what changed:
- `git log develop..HEAD --oneline` — commit list
- `git diff develop...HEAD --stat` — changed files summary
- `git diff develop...HEAD` — full diff (for understanding changes)

Also read `.github/pull-request-template.md` to know the required format.

## Step 2 — Determine PR Type

Based on the changes, determine the type label:
- `[Feature]` — new functionality
- `[Bug]` — bug fix
- `[Refactor]` — code improvement without behavior change
- `[Chore]` — build, config, dependency changes
- `[Design]` — architecture or design decisions

## Step 3 — Check Line Count

Run `git diff develop...HEAD --stat` and count total lines changed.
- If over 200 lines: warn the user and suggest splitting (project rule: PR must be ≤ 200 lines)
- If using `stacked-pr` label: the 200-line limit is skipped

## Step 4 — Create the PR

Use `gh pr create` with the following body structure (fill in from the diff):

```
제목 형식: [Type] 작업 내용 한 줄 요약

## 📌 PR 개요
- [한 줄 요약]

---

## ✨ 작업 내용
- [ ] [변경 사항 1]
- [ ] [변경 사항 2]

---

## 🧩 관련 이슈
- Closes #[이슈 번호] (사용자가 제공한 경우)

---

## ⚠️ 체크 사항
- [ ] 빌드 및 실행 정상 동작 확인
- [ ] 기존 기능에 영향 없음
- [ ] 변경 사항에 대한 테스트 작성 또는 기존 테스트 통과 확인
- [ ] PR 변경 줄 수 200줄 이하 확인
- [ ] 불필요한 로그 제거
- [ ] 코드 컨벤션 준수

---

## 💬 기타 참고 사항
- [리뷰어가 알아야 할 사항]
```

Target branch: `develop`

## Final Output

Print the PR URL after creation.
If line count exceeds 200, print a warning **before** creating and ask the user to confirm.
