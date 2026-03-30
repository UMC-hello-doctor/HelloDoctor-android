---
name: issue
description: Creates a GitHub issue using the project's ISSUE_TEMPLATE for the given type. Reads the matching template and fills it in. Usage: /issue <type> <title> (type: feature | bug | refactor | chore | design)
---

You will create a GitHub issue using the project's issue templates.

## Step 1 — Parse Arguments

The user's input after `/issue` follows this format:
```
/issue <type> <title>
```

- `<type>`: one of `feature`, `bug`, `refactor`, `chore`, `design`
- `<title>`: brief description of the issue (everything after the type)

If no type is given, ask the user: "어떤 타입의 이슈인가요? (feature / bug / refactor / chore / design)"
If no title is given, ask the user: "이슈 제목을 입력해주세요."

## Step 2 — Read the Template

Based on `<type>`, read the corresponding template file:

| type     | file                                      |
|----------|-------------------------------------------|
| feature  | `.github/ISSUE_TEMPLATE/feature.md`       |
| bug      | `.github/ISSUE_TEMPLATE/bug.md`           |
| refactor | `.github/ISSUE_TEMPLATE/refactor.md`      |
| chore    | `.github/ISSUE_TEMPLATE/chore.md`         |
| design   | `.github/ISSUE_TEMPLATE/design.md`        |

Strip the YAML frontmatter (everything between the first `---` and the second `---`) from the template body before using it.

## Step 3 — Fill the Template

Use the template body as the issue body structure. Fill in what you can from the title and context. Leave placeholder lines (e.g., `>`, empty checkboxes) as-is for the user to complete later.

- For `feature`: fill the 개요 section from the title; leave Tasks and DoD as empty checkboxes
- For `bug`: fill the 문제 상황 section from the title; leave 재현 방법, 환경 as placeholders
- For `refactor`: fill 개선 내용 from the title; leave 영향 범위 as placeholders
- For `chore`: fill 작업 내용 from the title; leave 작업 리스트 as empty checkboxes
- For `design`: fill 주제 from the title; leave 고려 사항 and 결론 as placeholders

## Step 4 — Determine Label and Title Format

From the template frontmatter, extract the `labels` field to use as the issue label.
Title format: `[Type] <title>` — capitalize the type (e.g., `[Feature]`, `[Bug]`).

## Step 5 — Create the Issue

Use `gh issue create` with:
- `--title "[Type] <title>"`
- `--label "<label from template>"`
- `--body` containing the filled template body (use HEREDOC to preserve formatting)

## Final Output

Print the issue URL after creation.
