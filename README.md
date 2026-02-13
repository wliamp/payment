# 🔀 Merge Strategy

This repository enforces a **linear history** policy (`Merge Commit` and `Squash & Merge` are **disabled**).

# 🛠 Branch Workflows

This repository follows a **simplified GitFlow-inspired** branching model.

## 🏗 Branch Structure

`main` → Production-ready code. Only receives merges from `dev`. Triggers artifact publishing.

`dev` → Integration branch for new features and bug fixes.

## 🔄 Workflow Summary

### ✨ Feature Development

Create a branch from `dev`.
Used for new functionality.
Merge back into `dev`.

Direct merge into main is **not allowed**.

### 🚀 Promotion to Production

When `dev` is stable and validated:
Create a Pull Request from `dev` → `main`.

After approval and merge:
Artifact publishing pipeline is triggered.

### 📊 Visual

```mermaid
gitGraph
    commit id: "main: init - base build & publishing setup"
    branch dev
    checkout dev
    commit id: "build: setup multi-module structure"
    commit id: "ci: add artifact publish pipeline (dev -> main)"
    branch module/auth-core
    checkout module/auth-core
    commit id: "feat(auth-core): add domain models"
    commit id: "feat(auth-core): expose service APIs"
    commit id: "test(auth-core): add unit tests"
    checkout dev
    merge module/auth-core id: "integrate auth-core module (rebase)"
    branch module/chat-api
    checkout module/chat-api
    commit id: "feat(chat-api): add chat usecases"
    commit id: "feat(chat-api): define repository ports"
    checkout dev
    merge module/chat-api id: "integrate chat-api module (rebase)"
    branch change/versioning-logic
    checkout change/versioning-logic
    commit id: "build: refine version calculation per module"
    checkout dev
    merge change/versioning-logic id: "integrate versioning changes (rebase)"
    checkout main
    merge dev id: "release: publish changed artifacts v0.2.0"
```

---

# 🛡 Branch Protection Rules

### 🔒 Protected

- **Applied** `main`
- **Restrict** `deletion` | `creation` | `updates`

### ⚙️ Workflows

- **Applied** `main` |`dev`
- **Restrict** `creations` | `deletions` | `force pushes`
- **Required**
    - `pull request`
        - required approvals: 1
        - dismiss stale approvals when new commits are pushed
        - conversation resolution before merging
        - allowed merge: *Rebase & Merge*
    - `status checks`
        - up to date before merging
