# Навигация

Документ описывает текущую navigation-архитектуру `mission-review` для desktop-приложения и IntelliJ IDEA / JetBrains IDE plugin.

## Назначение

Навигация построена на Decompose и находится в модуле `core:navigation`.

Главная задача слоя:

- держать общий component tree для разных app target'ов;
- не зависеть от Compose, Swing, IntelliJ Platform и target lifecycle API;
- описывать только route/component contracts и переходы между placeholder-экранами;
- не протаскивать Git, MCP, storage и review-domain данные до появления соответствующих задач;
- оставлять создание root lifecycle и UI rendering target-specific модулям.

## Модули

`core:navigation`:

- владеет `MissionReviewRootComponent`;
- создает Decompose `ChildStack`;
- содержит child contracts `ProjectSelectionComponent` и `LocalReviewComponent`;
- содержит dumb placeholder-модели `ProjectSelectionModel` и `LocalReviewModel`;
- содержит start-mode contract `MissionReviewStart`;
- не содержит Compose, Swing, IntelliJ Platform, Git, MCP или storage API.

KDoc оформляется по правилам `AGENTS.md`: документируются public contracts, lifecycle-sensitive adapters и non-obvious internal configs/components. `@param` и `@property` используются только там, где они добавляют контрактную информацию, а не повторяют сигнатуру.

`ui:compose`:

- отображает общий `MissionReviewRootComponent` через Compose Desktop;
- использует Decompose Compose extensions `Children`;
- не создает root component и не управляет lifecycle.

`ui:intellij`:

- отображает общий `MissionReviewRootComponent` через Swing panel;
- подписывается на `component.childStack`;
- не создает root component и не управляет lifecycle;
- не показывает форму выбора проекта.

`app:desktop`:

- создает `LifecycleRegistry`;
- создает `DefaultMissionReviewRootComponent`;
- стартует root с `MissionReviewStart.DesktopCompose`;
- связывает lifecycle окна через Decompose `LifecycleController`;
- рендерит `MissionReviewRootContent`.

`app:intellij-plugin`:

- создает `LifecycleRegistry`;
- создает `DefaultMissionReviewRootComponent`;
- стартует root с `MissionReviewStart.IntelliJPlatform`;
- передает только `basePath` текущего IntelliJ `Project`;
- освобождает panel и lifecycle через IntelliJ `Disposer`.

## Component Tree

```text
MissionReviewRootComponent
└── ChildStack
    ├── ProjectSelectionChild
    │   └── ProjectSelectionComponent
    └── LocalReviewChild
        └── LocalReviewComponent
```

`ProjectSelectionComponent` используется desktop target'ом как placeholder будущего выбора проекта.

`LocalReviewComponent` представляет placeholder будущего локального ревью. Сейчас он не содержит project path, project name, Git state, diff state, review comments, MCP session или storage state.

## Start Modes

### Desktop Compose

Desktop target стартует так:

```kotlin
MissionReviewStart.DesktopCompose
```

Начальный child: `ProjectSelectionChild`.

Сейчас экран выбора проекта является тупой болванкой. Нажатие `Open review placeholder` переводит root в `LocalReviewChild`, но не открывает реальный проект и не валидирует путь.

### IntelliJ Platform

IntelliJ target стартует так:

```kotlin
MissionReviewStart.IntelliJPlatform(
    projectPath = project.basePath.orEmpty(),
)
```

Начальный child: всегда `LocalReviewChild`.

Plugin работает только с текущим IDE project. Через plugin нельзя открыть другой локальный проект, выбрать другой путь или перейти в review flow для внешней директории.

Если `project.basePath` пустой, UI показывает placeholder-сообщение:

```text
Open a project in IntelliJ IDEA before using Local review.
```

Когда появится Git integration, отсутствие Git repository должно обрабатываться отдельно от отсутствия открытого проекта. Для проекта без Git нужно показывать действие или сообщение уровня:

```text
Initialize Git repository with git init.
```

## Lifecycle

Root component создается target'ом, потому что lifecycle зависит от runtime.

Desktop:

- `LifecycleRegistry` создается в `app:desktop`;
- `DefaultComponentContext` создается на UI thread;
- Decompose `LifecycleController` связывает lifecycle с Compose window state.

IntelliJ:

- `LifecycleRegistry` создается при создании tool window content;
- lifecycle переводится в `resume`;
- `MissionReviewIntellijPanel.dispose()` снимает подписку на `childStack`;
- `lifecycle.destroy()` вызывается через IntelliJ `Disposer`.

## Threading

Decompose navigation-команды должны выполняться на UI thread target'а.

Текущие команды вызываются из:

- Compose UI event handlers в desktop target;
- Swing event handlers в IntelliJ target.

Swing adapter дополнительно проверяет EDT перед перерисовкой active child.

## Back Navigation

Desktop flow:

- `ProjectSelectionChild -> LocalReviewChild`;
- `LocalReviewComponent.onBackClicked()` возвращает к `ProjectSelectionChild`;
- `LocalReviewModel.canNavigateBack = true`.

IntelliJ flow:

- всегда стартует в `LocalReviewChild`;
- back к выбору проекта недоступен;
- `LocalReviewModel.canNavigateBack = false`.

## State Restoration

Сейчас `childStack` создается с `serializer = null`.

Navigation state не восстанавливается между пересозданиями окна или tool window. Это сознательное ограничение первой реализации: подключение kotlinx serialization и устойчивого сохранения конфигураций должно быть отдельной задачей.

## Extension Points

Следующие изменения ожидаемо будут расширять текущую структуру:

- реальный desktop project selection;
- Git-backed review state в `LocalReviewComponent`;
- route/model для MCP-started review session;
- navigation contract для перехода из review finding к файлу и позиции;
- проверка, что IntelliJ file navigation не выходит за `project.basePath`;
- сохранение navigation state;
- DI factory для root component вместо прямого создания в app target'ах.

При расширении важно сохранять границы:

- contracts и Decompose tree остаются в `core:navigation`;
- Compose rendering остается в `ui:compose`;
- Swing/IntelliJ rendering остается в `ui:intellij`;
- IntelliJ Platform API остается в `app:intellij-plugin` или `ui:intellij`;
- desktop project selection не должен появляться в IntelliJ plugin UI;
- Git/MCP/storage детали не должны появляться в navigation-слое до появления явных contracts.

## Проверка

Основная проверка:

```powershell
.\gradlew.bat check
```

Навигационные сценарии покрыты тестами в:

```text
core/navigation/src/commonTest/kotlin/com/aequicor/missionreview/core/navigation/MissionReviewNavigationTest.kt
```
