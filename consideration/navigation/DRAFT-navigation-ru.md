# Навигация для desktop-compose и IntelliJ Platform

## Контекст

Проект `mission-review` должен запускать review-flow из двух target'ов:

- standalone desktop-приложение на Compose Multiplatform;
- вкладка `Local review` внутри IntelliJ IDEA / JetBrains IDE.

Скелет проекта уже содержит границы модулей `core:navigation`, `ui:compose`, `ui:intellij`, `app:desktop` и `app:intellij-plugin`, но не содержит Decompose component tree и target bootstrap.

Актуальная документация Decompose рекомендует использовать `ChildStack` для стековой навигации, `StackNavigation` для команд навигации, `ComponentContext#childStack` для создания child-компонентов и Compose `Children` для отображения стека. Навигацию и создание component tree нужно выполнять на main/UI thread.

## Цель изменения

Разработать базовую navigation-болванку, пригодную для desktop-compose и IntelliJ Platform:

- общий root component и child components в `core:navigation`;
- `ProjectSelection` placeholder для desktop target;
- `LocalReview` placeholder для desktop и IntelliJ target'ов;
- target-specific bootstrap root component в desktop app и IntelliJ plugin;
- Compose Desktop adapter для отображения Decompose stack;
- Swing/IntelliJ adapter для отображения того же component tree в tool window.

## Предполагаемая архитектура

### `core:navigation`

Модуль содержит platform-neutral Decompose component tree:

- `MissionReviewRootComponent`;
- `MissionReviewChild`;
- `ProjectSelectionChild`;
- `LocalReviewChild`;
- `ProjectSelectionComponent`;
- `LocalReviewComponent`;
- `ProjectSelectionModel`;
- `LocalReviewModel`;
- `MissionReviewStart`;
- `DefaultMissionReviewRootComponent`.

Root component управляет `ChildStack`:

- `ProjectSelectionChild` - desktop placeholder будущего выбора проекта;
- `LocalReviewChild` - placeholder будущего локального ревью.

Desktop target стартует с `ProjectSelectionChild`.

IntelliJ target всегда стартует с `LocalReviewChild` для текущего IDE `Project`. Через plugin нельзя открыть другой локальный проект или ввести произвольный путь; выбор проекта остается только в standalone desktop target.

Если у текущего IDE project нет `basePath`, plugin показывает сообщение о необходимости открыть проект в IntelliJ IDEA. Когда появится Git integration, отсутствие Git repository должно обрабатываться отдельно: для проекта без Git нужно показывать сообщение уровня `Initialize Git repository with git init`.

### `ui:compose`

Модуль отображает `MissionReviewRootComponent` через Decompose Compose extensions:

- `Children(stack = component.childStack)`;
- screen composable для desktop project-selection placeholder;
- screen composable для local-review placeholder.

### `ui:intellij`

Модуль содержит Swing panel adapter, который подписывается на Decompose `Value<ChildStack<...>>` и перерисовывает текущий active child.

Адаптер не создает root component и не управляет lifecycle root'а. Это делает `app:intellij-plugin`.

IntelliJ adapter не рендерит форму выбора проекта. Tool window работает только с текущим IDE project, переданным из `app:intellij-plugin`.

### `app:desktop`

Desktop app:

- создает `LifecycleRegistry`;
- создает root component на EDT/UI thread;
- связывает lifecycle окна через `LifecycleController`;
- отображает `MissionReviewRootContent`.

### `app:intellij-plugin`

Plugin tool window:

- создает `LifecycleRegistry`;
- создает root component для текущего IDE project;
- переводит lifecycle в resumed state при создании tool window;
- уничтожает panel и lifecycle через IntelliJ `Disposer`;
- не предоставляет пользователю возможность выбрать или открыть другой проект.

## Затрагиваемые модули

- `consideration/navigation`;
- `docs`;
- `core:navigation`;
- `ui:compose`;
- `ui:intellij`;
- `app:desktop`;
- `app:intellij-plugin`.

## Ограничения

- Git/MCP/storage/domain review logic не реализуется в этой задаче.
- Desktop project selection остается placeholder'ом без реального выбора пути.
- IntelliJ plugin работает только с текущим IDE project; произвольный path input и открытие другого проекта запрещены для этого target'а.
- Навигационные configuration не сохраняются через serializer в первой версии, чтобы не расширять scope задачи на настройку kotlinx serialization plugin.
- `core:navigation` не зависит от Compose, Swing, IntelliJ Platform, Git, MCP, storage или target lifecycle API.
- `ui:compose` не используется IntelliJ plugin'ом.
- IntelliJ adapter использует Swing и не вводит Compose-in-IDE до отдельного compatibility spike / ADR.
- Все Decompose navigation-команды должны выполняться на UI thread target'а.

## Открытые вопросы

- Когда появится Git integration, какие route arguments нужны для review-сессии.
- Нужно ли сохранять navigation state между пересозданиями desktop window / IDE tool window через kotlinx serialization.
- Какой слой будет владеть переходом из review finding к editor position: `ui:intellij` или `app:intellij-plugin`.
- Нужно ли отдельное состояние для MCP-started review session.
- Где будет располагаться проверка наличия Git repository и сообщение про `git init`.

## Критерии готовности

- В `core:navigation` есть общий Decompose root/component tree без target-specific зависимостей.
- Public navigation contracts, lifecycle-sensitive adapters и non-obvious internal configs/components имеют KDoc согласно `AGENTS.md`.
- KDoc использует `@param` / `@property` только там, где это добавляет контрактную информацию, а не повторяет сигнатуру.
- Навигационные classes разбиты по отдельным файлам.
- Desktop app запускает Compose window с root navigation.
- IntelliJ plugin создает tool window content через общий root navigation и Swing adapter для текущего IDE project.
- В IntelliJ UI нет поля выбора другого проекта.
- Compose adapter отображает child stack через Decompose `Children`.
- IntelliJ adapter подписывается на Decompose `Value` и корректно освобождает subscription.
- Документация navigation-слоя находится в `docs/navigation-ru.md`.
- Доступные Gradle проверки компиляции проходят либо явно зафиксирована причина, почему проверка невозможна.
- Проведен self-review по архитектурным границам, lifecycle, thread model, error handling и соответствию draft'у.
