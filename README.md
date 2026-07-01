# mission-review

`mission-review` is a local AI-assisted code review application for checking changes before commit.

The app supports three review entrypoints: a standalone desktop flow, an MCP-driven web review flow, and an IntelliJ IDEA / JetBrains IDE `Local review` tab. In all modes, the user reviews local Git changes, leaves comments, and passes those comments to an AI coding agent for fixes before the code is committed.

## Purpose

The project helps keep AI-generated and human-written code under review before it reaches the repository history.

Main goals:

- review local Git changes before commit;
- keep review context on the developer machine;
- turn review comments into deterministic instructions for an AI agent;
- support clipboard handoff for manual agent workflows;
- support an MCP workflow where an AI agent can request a review and later fetch review results;
- support IDE-native review with navigation from review findings to code.

## Workflows

### Desktop clipboard review

1. Open `mission-review`.
2. Select a local project.
3. Review Git files in `staged` and `untracked` states.
4. Leave comments and required fixes.
5. Click **Copy commentaries to clipboard**.
6. Open an AI coding agent, press `Ctrl+V`, then `Enter`.
7. The agent applies fixes based on the review comments.
8. Re-run review if needed, then commit.

### MCP web review

The MCP flow is intended for agent-driven development:

1. The AI agent writes or changes code.
2. The agent sends a request to the `mission-review` MCP server.
3. `mission-review` generates a review link.
4. The agent sends this link to the user in chat.
5. The user opens the link and reviews the changes.
6. The user clicks **Done** when the review is complete.
7. The user returns to the chat and writes `Done` or `Готово`.
8. The agent uses MCP to download review comments.
9. The agent fixes the reported issues.

### IDEA Local review

The IDE flow is intended for users who want to review and navigate code inside IntelliJ IDEA / JetBrains IDE:

1. Open the project in IDEA.
2. Open the `Local review` tab.
3. Review Git files in `staged` and `untracked` states.
4. Navigate from review findings and diff fragments to the corresponding source code.
5. Leave comments and required fixes.
6. Click **Save to clipboard** to paste comments into an AI agent manually, or click **Save through MCP** to store comments for an active MCP review session.
7. Return to the AI agent and either paste the comments or write `Done` / `Готово` so the agent can fetch them through MCP.

## Technology Stack

- Kotlin Multiplatform
- Compose Multiplatform
- Gradle multi-module project
- Feature modules split into `api` and `impl`
- Koin for dependency injection
- Decompose for navigation and component lifecycle
- Git integration for reading project changes
- MCP for AI-agent integration
- Ktor for web endpoints and review links

## Architecture

The project is a modular Kotlin application with explicit feature boundaries.

Module style:

- `feature-name:api` contains public contracts, models, and interfaces;
- `feature-name:impl` contains implementation details, UI, services, and integration code;
- shared infrastructure modules provide Git, MCP, web, navigation, and common UI functionality.

This structure keeps feature implementations replaceable while allowing other modules to depend only on stable APIs.

Current Gradle modules:

- `app:desktop` - Compose Desktop entrypoint.
- `app:intellij-plugin` - IntelliJ plugin entrypoint skeleton and lifecycle bridge.
- `core:git` - Git change discovery for `staged` and `untracked` files.
- `core:code-render` - diff/content render models.
- `core:mcp` - local MCP review-session contracts.
- `core:network` - local review-link contracts.
- `core:storage` - review-session storage contracts and in-memory implementation.
- `core:navigation` - Decompose root/component tree and navigation commands.
- `core:di` - Koin module and root factory wiring.
- `core:theme` - semantic design tokens.
- `core:ui-contracts` - platform-neutral UI contracts.
- `ui:compose` - Compose Desktop UI adapter.
- `ui:intellij` - IntelliJ UI adapter boundary.
- `feature:entrypoint:api` / `feature:entrypoint:impl` - project-selection flow.
- `feature:review:api` / `feature:review:impl` - review flow and deterministic commentary export.

Architecture notes:

- [Navigation architecture](docs/navigation-ru.md) - Decompose root/component tree, desktop start mode, IntelliJ current-project-only mode, lifecycle ownership, and target adapter boundaries.

## Core Features

- local project selection;
- Git diff and `staged` / `untracked` file discovery;
- desktop review UI;
- IDEA `Local review` UI with code navigation;
- comment collection for review findings;
- deterministic comment export for AI-agent fixes;
- clipboard export;
- MCP endpoint for creating review sessions;
- review link generation through Ktor;
- MCP endpoint for saving and fetching completed review comments.

## Current Status

The repository contains the first Gradle multi-module skeleton. Core contracts, feature contracts, target entrypoints, and adapter boundaries are present; full product UI, persistent storage, MCP server transport, and packaged IntelliJ SDK integration are still future work.

## Build

Use the Gradle wrapper:

```shell
./gradlew check
```

On Windows:

```powershell
.\gradlew.bat check
```

## Development Notes

When adding implementation:

- keep feature contracts in `api` modules and concrete code in `impl` modules;
- avoid leaking implementation dependencies through public APIs;
- keep Git, MCP, and Ktor integrations behind explicit interfaces;
- make exported comments deterministic and easy for AI agents to follow;
- treat review comments as structured data, not only free-form text.
