
# 📋 Task Planning System

## Trigger
If the task text contains the word plan, create a plan file in .ai/plan/ before execution.

## Plan Creation
1. Create a file .ai/plan/<short-task-name>.md.
2. File structure:
```md
# Plan: <task name>

**Status:** in progress | done
**Created:** YYYY-MM-DD
**Session:** 1

## Goal
One sentence — what we aim to achieve.

## Steps
- [ ] Step 1 — description
- [ ] Step 2 — description
- [x] Step 3 — description ✅ result/note

## Notes
Any nuances, blockers, or decisions made along the way.
```

## During Execution
- After completing each step, update the checkbox: [ ] → [x] and add a short result.
- If a step produces an unexpected blocker — record it in ## Notes.
- When all steps are completed, change Status: done.

## Complex Tasks & Session Changes
- Handle complex tasks step by step: break them into independent blocks, each completable within one session.
- If the session context is close to overflowing or the task is paused — record current progress in the plan and stop.
- At the start of a new session, first read the plan file (Read .ai/plan/<name>.md) to restore context without reanalyzing the codebase.
- Increment the session number in the Session: field each time work resumes.

## File Naming
Use kebab-case, keep it short and meaningful: auth-roles.md, cart-redux.md, danea-import.md.
