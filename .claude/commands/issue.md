---
name: issue
description: Creates a GitHub issue using templates and auto-fills content with github-issue-helper agent. Usage: /issue <type> <description>
---

You will create a GitHub issue for HelloDoctor-android following the project's issue templates.

## Step 1 — Parse Issue Type

Extract the first argument as the issue type. Support both English and Korean:

**Type Mapping:**
- `bug` | `버그` → `.github/ISSUE_TEMPLATE/bug.md`
- `feature` | `기능` → `.github/ISSUE_TEMPLATE/feature.md`
- `chore` | `보수` | `유지` → `.github/ISSUE_TEMPLATE/chore.md`
- `refactor` | `리팩` | `리팩터` → `.github/ISSUE_TEMPLATE/refactor.md`
- `design` | `디자인` → `.github/ISSUE_TEMPLATE/design.md`

If the type is invalid or empty, respond with an error message listing valid types.

## Step 2 — Read the Template

Read the matching template from `.github/ISSUE_TEMPLATE/<type>.md`.
- Extract the YAML frontmatter to get `labels` field (e.g., `labels: bug`)
- Extract the body sections (everything after `---`)

## Step 3 — Call github-issue-helper Agent

Pass the template content and user's description to the `github-issue-helper` agent:

```
Invoke: Agent(subagent_type="github-issue-helper", prompt="""
Issue type: {type}
User description: {rest_of_args}
Template body:
{template_body}
""")
```

The agent will auto-fill template sections and return a completed issue body.

## Step 4 — Create the Issue

Run `gh issue create` with:
- `--title "[{TYPE_PREFIX}] {auto_summary_from_agent}"`
- `--body "$(cat <<'EOF'{completed_body}EOF)"` (heredoc format)
- `--label "{emoji_label}"` (e.g., `🐞 bug`)
- Additional label: `📌 status: todo`

**Type Prefix & Emoji Label Mapping:**

| Type | Prefix | Label |
|------|--------|-------|
| bug | Bug | 🐞 bug |
| feature | Feature | ✨ feature |
| chore | Chore | 🧹 chore |
| refactor | Refactor | 🔧 refactor |
| design | Design | 💡 design |

Command template:
```bash
gh issue create \
  --title "[Bug] 앱이 크래시 남" \
  --body "$(cat <<'EOF'
## 🐞 문제 상황
...
EOF
)" \
  --label "🐞 bug" \
  --label "📌 status: todo"
```

## Step 5 — Print Result

Output the created issue URL and number in format:
```
✅ Issue created: #123
🔗 URL: https://github.com/UMC-hello-doctor/HelloDoctor-android/issues/123
```

## Error Handling

- **Invalid type**: "❌ Invalid type. Use: bug, 버그, feature, 기능, chore, 보수, refactor, 리팩, design, 디자인"
- **Template file missing**: "❌ Template not found: .github/ISSUE_TEMPLATE/{type}.md"
- **gh issue create failed**: Print the error and ask user to check GitHub CLI credentials
