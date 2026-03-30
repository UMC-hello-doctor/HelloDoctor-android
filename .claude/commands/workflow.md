---
name: workflow
description: Check CI workflow status, view logs, and rerun failed steps. Usage: /workflow [status|check|rerun <id>|list]
---

You will manage GitHub Actions workflows for HelloDoctor-android.

Default action: `status` (if no subcommand provided)

## Subcommand: status

Show the 10 most recent workflow runs on the current branch.

```bash
git branch --show-current | xargs -I {} gh run list --branch {} --limit 10
```

Format output as a readable table showing:
- Workflow name
- Status (completed, in_progress, queued, requested)
- Conclusion (success, failure, cancelled, skipped)
- Time created (relative time, e.g., "2 hours ago")
- Run ID

Example output:
```
Name                  Status      Conclusion   Created        ID
code-style-check      completed   success      2 hours ago    12345
test-coverage         completed   failure      2 hours ago    12344
pr-labeler            completed   success      1 hour ago     12343
```

## Subcommand: check

Show HEAD commit's workflow status and failed step logs.

Steps:
1. Get all runs for current commit:
   ```bash
   git rev-parse HEAD | xargs -I {} gh run list --commit {} --limit 1 --json databaseId,status,conclusion | jq -r '.[0] | "\(.databaseId) \(.status) \(.conclusion)"'
   ```

2. View full run details including failed logs:
   ```bash
   gh run view <run_id> --log
   ```

3. If there are failures, extract and highlight failed step names and their log excerpts.

Print format:
```
Run #12344 for HEAD commit: status=completed, conclusion=failure

❌ Failed steps:
- test-unit-tests (exit code 1)
  > Error: Database connection timeout

- detekt-check (exit code 1)
  > Detekt analysis found issues

View full log: gh run view 12344
```

If all steps passed:
```
✅ All steps passed for HEAD commit
```

## Subcommand: rerun <id_or_name>

Rerun only failed steps for a workflow run.

Steps:
1. If input is non-numeric, treat as workflow name and resolve to most recent run ID:
   ```bash
   gh run list --workflow "<name>" --limit 1 --json databaseId | jq -r '.[0].databaseId'
   ```

2. Rerun failed steps only:
   ```bash
   gh run rerun <resolved_id> --failed-only
   ```

3. Print confirmation:
   ```
   ✅ Rerunning failed steps in run #<id>
   ```

**Known workflow names** (for user reference, use for name resolution):
- code-style-check
- test-coverage
- issue-labeler
- labels-sync
- pr-labeler
- pr-line-limit
- pr-merge-status
- pr-title-labeler

## Subcommand: list

List all workflows in the repository.

```bash
gh workflow list
```

Format as a table with:
- Workflow name
- State (active, disabled, deleted)
- Filename (.github/workflows/*.yml)

Example:
```
NAME              STATE     PATH
code-style-check  active    .github/workflows/code-style-check.yml
test-coverage     active    .github/workflows/test-coverage.yml
issue-labeler     active    .github/workflows/issue-labeler.yml
```

## Error Handling

- **Invalid subcommand**: "❌ Unknown subcommand. Use: `status`, `check`, `rerun <id|name>`, or `list`"
- **Run ID not found**: "❌ No workflow run found for that ID or branch. Try `/workflow status` to list recent runs."
- **Network error**: "❌ GitHub CLI error. Check your internet connection and GitHub credentials."
- **Workflow name not found**: "❌ Workflow '{name}' not found. Use `/workflow list` to see available workflows."
