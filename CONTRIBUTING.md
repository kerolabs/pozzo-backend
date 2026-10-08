# Contributing to Pozzo Backend

Thank you for your interest in contributing to the Pozzo Backend project! This document outlines our branch management workflow, commit standards, testing procedures, and coding guidelines to maintain high software quality and collaborative efficiency.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Git Branching Strategy](#git-branching-strategy)
- [Commit Message Conventions](#commit-message-conventions)
- [Pull Request Standards](#pull-request-standards)
- [Testing & Quality Assurance](#testing--quality-assurance)
- [Coding Style & Architecture Standards](#coding-style--architecture-standards)

---

## Code of Conduct

We are committed to providing a friendly, safe, and welcoming environment for all contributors. Please treat all collaborators with respect and professionalism.

---

## Git Branching Strategy

Our development workflow uses a structured Git Flow model with two core branches and topic branches.

```
feature/* or docs/* â”€â”€â”€â–º develop (Integration) â”€â”€â”€â–º main (Production)
```

### Branch Roles

1. **`main`**:
   - Represents the production-ready, stable codebase.
   - Direct commits and direct pushes are forbidden.
   - Updated exclusively via Pull Requests merged from `develop` upon release.
   - Every merge to `main` should be tagged with a semantic version (e.g., `v1.0.0`).

2. **`develop`**:
   - The primary integration branch where ongoing features, documentation, and bug fixes converge.
   - All feature and documentation work branches out from and merges back into `develop`.
   - Must always remain in a buildable and passing test state.

3. **Topic Branches** (`feature/*`, `docs/*`, `fix/*`, `refactor/*`):
   - Created off the latest state of `develop`.
   - Dedicated to a single concern or user story.
   - Examples:
     - `feature/turn-swapping-support`
     - `docs/code-comments-and-guidelines`
     - `fix/fcm-token-expiration`
     - `refactor/compliance-scoring-algorithm`

### Workflow Step-by-Step

```bash
# 1. Ensure develop is up to date
git checkout develop
git pull origin develop

# 2. Create a dedicated branch for your work
git checkout -b feature/my-new-feature

# 3. Work, test, and commit your changes atomically
git add .
git commit -m "feat(savingsgroups): add support for manual turn swapping"

# 4. Push your branch to the remote repository
git push origin feature/my-new-feature

# 5. Open a Pull Request targeting 'develop'
```

---

## Commit Message Conventions

We follow the **Conventional Commits** specification (v1.0.0). Consistent commit history allows automated release notes generation and clear change tracking.

### Commit Format

```
<type>(<optional scope>): <description>

[optional body]

[optional footer(s)]
```

### Allowed Types

| Type | Purpose | Example |
| :--- | :--- | :--- |
| `feat` | Introduces a new feature or endpoint | `feat(iam): add multi-device FCM push registration` |
| `fix` | Patches a bug or resolves an unexpected issue | `fix(contributions): resolve rounding discrepancy in period payout` |
| `docs` | Documentation-only changes (README, Javadoc, guides)| `docs: add CONTRIBUTING.md with branch and pull request standards` |
| `style` | Code formatting or whitespace without logic changes | `style: format imports and remove unused spaces` |
| `refactor` | Code restructuring without altering behavior | `refactor(compliance): simplify score calculation pipeline` |
| `perf` | Performance optimizations | `perf(persistence): add index to membership foreign keys` |
| `test` | Adding or updating unit/integration tests | `test(savingsgroups): add test cases for seeded turn assignment` |
| `build` | Changes to build tools, dependencies, or Maven plugins | `build(pom): bump spring-boot parent version` |
| `ci` | Changes to CI/CD pipelines, workflows, or scripts | `ci: add GitHub Actions workflow for automated testing` |
| `chore` | Routine tasks, maintenance, or configuration adjustments | `chore: update .gitignore with local IDE paths` |

### Rules

- Use the **imperative mood** in the description (e.g., `add`, `fix`, `document` instead of `added`, `fixes`, `documented`).
- Do not capitalize the first letter of the description.
- Do not place a period (`.`) at the end of the commit summary.
- Keep the subject line under 72 characters.
- Ensure commits are **atomic** â€” each commit should represent one logical unit of work that compiles cleanly.

---

## Pull Request Standards

Before submitting a Pull Request (PR), verify that your branch fulfills the following criteria:

### PR Checklist

- [ ] Branch targets `develop` (not `main`).
- [ ] All commits follow the Conventional Commits format.
- [ ] Code compiles without warnings (`./mvnw clean compile`).
- [ ] All unit and integration tests pass (`./mvnw test`).
- [ ] Public API methods and REST controllers are documented with descriptive Javadoc.
- [ ] No credentials, secret keys, or `.env` files are tracked in git.
- [ ] PR title accurately summarizes the changes.
- [ ] The PR description describes:
  - Motivation and problem statement.
  - Summary of technical changes.
  - Verification steps performed.

### Code Review Process

- Every Pull Request requires at least one peer code review before merging.
- Address all reviewer comments and resolve discussions prior to squash-and-merge or rebase merge into `develop`.

---

## Testing & Quality Assurance

We maintain high confidence in our codebase through automated unit and integration tests.

### Running Tests Locally

```bash
# Run all tests
./mvnw test

# Run tests on Windows
.\mvnw.cmd test

# Run a specific test class
./mvnw test -Dtest=TurnAssignmentServiceTest

# Compile test sources without running execution
./mvnw test-compile
```

### Testing Guidelines

- **Domain Logic**: Unit test all aggregates, entities, and domain services in isolation without Spring context.
- **Application Services**: Mock outbound ports and test command/query handlers.
- **REST Interfaces**: Verify controller contracts using `@WebMvcTest` or mock requests.
- **No Flaky Tests**: Tests must be deterministic and independent of execution order or external live networks.

---

## Coding Style & Architecture Standards

We adhere to the project's `.editorconfig` settings and Domain-Driven Design principles:

### Formatting Rules

- **Indentation**: 4 spaces (no tabs) for Java source files.
- **Line Length**: Limit lines to a maximum of 120 characters where possible.
- **Imports**: Explicit imports only; do **not** use wildcard imports (`import java.util.*;`).
- **File Endings**: Ensure files terminate with a single newline character (`LF`).

### Architecture Rules

1. **Domain Isolation**:
   - The domain layer (`domain/model`, `domain/services`) must remain free of framework dependencies (e.g., no Spring Web or JPA annotations in pure domain aggregates).
   - Invariants and business validations belong inside aggregate roots and value objects.

2. **Command / Query Responsibility Segregation (CQRS)**:
   - Modifications flow through application `commandservices` using explicit Command records.
   - Reads flow through application `queryservices` using Query records.

3. **Result and Error Handling**:
   - Application services return `Result<T>` instead of throwing unchecked business exceptions across context boundaries.
   - Controllers transform `Result.failure()` into standardized HTTP errors via `ResponseEntityAssembler` and `ErrorResponseAssembler`.

4. **Javadoc**:
   - Provide clear, concise Javadoc comments on REST controllers, public domain services, outbound ports, and configuration beans.
