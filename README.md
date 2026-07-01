# mission-review

`mission-review` is a local AI-assisted code review application for checking changes before commit.

The app is designed as a desktop tool: you select a project, review the code locally, add review comments, and then generate a prompt. The generated prompt is copied into an AI coding agent, which uses it to fix all review findings before the code is committed.

## Purpose

The project helps keep AI-generated and human-written code under review before it reaches the repository history.

Main goals:

- review local Git changes before commit;
- keep review context on the developer machine;
- turn review comments into a structured prompt for an AI agent;
- support an MCP workflow where an AI agent can request a review and later fetch review results.

## Workflows

### Desktop review

1. Open `mission-review`.
2. Select a local project.
3. Review the changed code.
4. Leave comments and required fixes.
5. Click **Generate prompt**.
6. Paste the generated prompt into an AI coding agent.
7. The agent applies fixes based on the review comments.
8. Re-run review if needed, then commit.

### MCP review

The MCP flow is intended for agent-driven development:

1. The AI agent writes or changes code.
2. The agent sends a request to the `mission-review` MCP server.
3. `mission-review` generates a review link.
4. The agent sends this link to the user in chat.
5. The user opens the link and reviews the changes.
6. The user returns to the chat and writes that the review is ready.
7. The agent uses MCP to download review comments.
8. The agent fixes the reported issues.

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

The project is planned as a modular Kotlin application with explicit feature boundaries.

Expected module style:

- `feature-name:api` contains public contracts, models, and interfaces;
- `feature-name:impl` contains implementation details, UI, services, and integration code;
- shared infrastructure modules provide Git, MCP, web, navigation, and common UI functionality.

This structure keeps feature implementations replaceable while allowing other modules to depend only on stable APIs.

## Core Features

- local project selection;
- Git diff and changed-file discovery;
- desktop review UI;
- comment collection for review findings;
- prompt generation for AI-agent fixes;
- MCP endpoint for creating review sessions;
- review link generation through Ktor;
- MCP endpoint for fetching completed review comments.

## Current Status

The repository is in an early stage. The product direction, workflows, and target stack are described here; implementation details and build commands should be updated as the Gradle multi-module structure is added.

## Development Notes

When adding implementation:

- keep feature contracts in `api` modules and concrete code in `impl` modules;
- avoid leaking implementation dependencies through public APIs;
- keep Git, MCP, and Ktor integrations behind explicit interfaces;
- make the generated prompt deterministic and easy for AI agents to follow;
- treat review comments as structured data, not only free-form text.

