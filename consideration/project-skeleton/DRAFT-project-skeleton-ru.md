# Скелет проекта mission-review

## Контекст

`mission-review` - локальный инструмент ревью Git-изменений перед коммитом.

Приложение должно поддерживать три режима:

- standalone desktop-ревью с передачей комментариев агенту через clipboard;
- agent-driven ревью через MCP и локальную web-ссылку;
- ревью внутри IntelliJ IDEA / JetBrains IDE во вкладке `Local review`.

Во всех режимах пользователь смотрит локальные Git-изменения, оставляет замечания, а ИИ-агент исправляет код после ревью.

## Цель

Собрать Gradle multi-module skeleton, который:

- разделяет desktop-приложение и IDEA plugin;
- переиспользует core-логику между target'ами;
- держит публичные контракты отдельно от реализаций через `api` / `impl`;
- изолирует Git, MCP, network, storage и code rendering в core-модулях;
- разделяет platform-neutral presentation contracts и UI adapter'ы;
- оставляет target-specific код в `app:*` и UI adapter'ах.

## Пользовательские сценарии

### Desktop + clipboard

1. Пользователь запускает desktop-приложение.
2. Выбирает локальный проект.
3. Приложение показывает Git-файлы в статусах `staged` и `untracked`.
4. Пользователь смотрит diff'ы и содержимое новых файлов.
5. Пользователь оставляет комментарии и обязательные исправления.
6. Кнопка `Copy commentaries to clipboard` копирует детерминированный текст ревью.
7. Пользователь вставляет текст в чат или CLI ИИ-агента.

Ограничения первого варианта:

- приложение не запускает ИИ-агента само;
- MCP не требуется;
- review scope: `staged` и `untracked`.

### MCP + web-ссылка

1. ИИ-агент создает review-сессию через MCP.
2. `mission-review` возвращает локальную ссылку на web-ревью.
3. Агент отправляет ссылку пользователю.
4. Пользователь открывает ссылку, проводит ревью и нажимает `Done` / `Готово`.
5. Пользователь пишет агенту `Готово`.
6. Агент через MCP забирает комментарии завершенной сессии.

Ключевые требования:

- MCP хранит состояние review-сессии и результат ревью;
- web UI открывается сразу в контексте проекта, переданного MCP-запросом;
- сообщение пользователя `Готово` в чате только триггерит получение результата через MCP.

### IDEA Local review

1. Пользователь или агент открывает проект в JetBrains IDE.
2. Plugin показывает вкладку `Local review`.
3. Вкладка отображает `staged` и `untracked` файлы текущего IDE project.
4. Пользователь ревьюит diff'ы, оставляет комментарии и переходит из review UI к коду.
5. Завершение доступно двумя способами:
   - `Save to clipboard`;
   - `Save through MCP`.

Ключевое отличие режима - навигация из замечаний и diff-фрагментов к файлам и позициям в IDE editor.

## Архитектура

Базовая форма проекта - Gradle multi-module project.

App target'ы:

- `app:desktop` - standalone desktop-приложение на Compose Multiplatform;
- `app:intellij-plugin` - плагин для IntelliJ IDEA / JetBrains IDE.

Общие правила:

- бизнес-логика живет в `core:*` и `feature:*`;
- UI переиспользуется через состояния, события, render-модели и design tokens, а не через общие toolkit-компоненты;
- `ui:compose` используется desktop target'ом;
- `ui:intellij` используется IDEA plugin target'ом;
- `core:navigation` задает общий Decompose component tree;
- Compose внутри IDEA plugin допускается только после отдельного compatibility spike / ADR.

## Предлагаемая структура модулей

```text
app/
  desktop/
  intellij-plugin/

core/
  code-render/
  di/
  git/
  mcp/
  navigation/
  network/
  storage/
  theme/
  ui-contracts/

ui/
  compose/
  intellij/

feature/
  entrypoint/
    api/
    impl/
  review/
    api/
    impl/
```

## Назначение модулей

### `app:desktop`

Desktop entrypoint. Запускает Compose UI, создает root `ComponentContext`, инициализирует DI, дает выбрать локальный проект и открывает основной review-flow.

### `app:intellij-plugin`

IDE plugin target. Регистрирует plugin entrypoint'ы, вкладку `Local review`, доступ к текущему IDE project, bridge между IntelliJ lifecycle / `Disposable` и Decompose lifecycle, а также навигацию к файлам и позициям в editor.

### `core:navigation`

Общая Decompose-навигация. Содержит root component, child component'ы, route-модели, navigation commands и фабрики component'ов.

Не зависит от Compose, Swing, IntelliJ Platform UI, JCEF и lifecycle API конкретного target'а.

### `core:di`

Общая Koin-конфигурация: core-зависимости, feature wiring, target hooks и binding root component factory.

Не создает UI-объекты и не управляет platform lifecycle.

### `core:git`

Git-интеграция: статус репозитория, список `staged` / `untracked` файлов, diff'ы, содержимое новых файлов, рабочая директория проекта.

### `core:code-render`

Модели rendered code, diff-фрагменты, mapping Git diff -> UI model, позиционные модели для перехода к файлу и строке.

### `core:mcp`

MCP-интеграция: создание review-сессии, генерация ссылки, получение результата, сохранение комментариев, проверка статуса сессии.

### `core:network`

Общие HTTP/web-контракты и настройки Ktor для локальных endpoints и review links.

### `core:storage`

Локальное хранение review-сессий, комментариев, настроек и временных данных MCP-flow.

### `core:ui-contracts`

Platform-neutral presentation contracts: состояния экранов, UI events/intents, render-модели, presenter/view-model контракты.

Не зависит от Compose, Swing, IntelliJ Platform UI или JCEF.

### `core:theme`

Semantic design tokens: цвета, типографика, spacing. Без привязки к UI toolkit.

### `ui:compose`

Compose Desktop adapter. Рендерит состояния из `core:ui-contracts` и Decompose component tree для desktop-приложения.

Не является зависимостью `app:intellij-plugin`.

### `ui:intellij`

IntelliJ Platform UI adapter. Рендерит review-flow внутри IDE, связывает UI с lifecycle IDE, tool windows / tabs / actions и навигацией к editor.

Не зависит от `ui:compose`.

### `feature:entrypoint:api`

Публичный контракт стартового flow: состояние выбора проекта, события и переход к review-flow.

### `feature:entrypoint:impl`

Реализация стартового flow. Может использовать `core:navigation`, `core:di`, `core:git` и `feature:review:api`.

Не создает Compose UI, Swing UI, tool windows или dialogs напрямую.

### `feature:review:api`

Публичные контракты ревью: review-сессия, комментарии, состояния, экспорт комментариев, clipboard/MCP завершение.

### `feature:review:impl`

Реализация review-flow. Работает с Git, code rendering, storage, MCP и UI contracts.

Должна поддерживать:

- копирование комментариев в clipboard;
- сохранение результата активной MCP-сессии;
- завершение web review-сессии для последующего чтения через MCP.

## Принципы зависимостей

- `api` не зависит от `impl`.
- `impl` зависит от `api` и нужных `core:*`.
- `app:*` собирает target и знает о конкретных реализациях.
- `core:*` не зависит от `app:*`.
- Target-specific код остается в `app:*`, `ui:compose` или `ui:intellij`.
- Compose допустим только в `ui:compose` и `app:desktop`.
- IntelliJ Platform UI допустим только в `ui:intellij` и `app:intellij-plugin`.
- Decompose допустим в `core:navigation` и component contracts.
- IntelliJ-Decompose lifecycle bridge находится в `app:intellij-plugin` или `ui:intellij`, но не в `core:navigation`.

## Ограничения

- Draft фиксирует архитектурные границы, а не версии библиотек.
- Версии Kotlin, Compose Multiplatform, Decompose, Koin, Ktor, IntelliJ Platform Gradle Plugin и target IDE задаются в Gradle-конфигурации или version catalog.
- Перед использованием Compose внутри IDEA plugin нужен compatibility spike: Kotlin, Compose compiler/runtime, Compose Multiplatform Gradle Plugin, IntelliJ Platform Gradle Plugin, target IDE build и JetBrains Runtime.
- Для первой реализации plugin UI предпочтителен IntelliJ Platform UI adapter.
- Bridge IntelliJ lifecycle -> Decompose должен учитывать plugin/project disposal, EDT/background threading, classloader isolation и конфликты версий.
- Визуальная консистентность desktop и plugin target'ов означает одинаковые сценарии, терминологию, состояния и данные, но не pixel-perfect UI.

## Затрагиваемые модули

- `app:desktop`;
- `app:intellij-plugin`;
- `core:navigation`;
- `core:di`;
- `core:git`;
- `core:mcp`;
- `core:network`;
- `core:storage`;
- `core:code-render`;
- `core:theme`;
- `core:ui-contracts`;
- `ui:compose`;
- `ui:intellij`;
- `feature:entrypoint:api`;
- `feature:entrypoint:impl`;
- `feature:review:api`;
- `feature:review:impl`.

## Открытые вопросы

- Нужно ли включать `unstaged` изменения в первый review scope.
- Как связывать `Save through MCP` в IDEA plugin с активной MCP-сессией, если ревью начато не агентом.
- Чем должна быть вкладка `Local review`: tool window, editor tab, panel внутри Commit UI или отдельная IDE panel.
- Нужен ли Compose-in-IDE spike после базовой реализации через IntelliJ Platform UI.
- Где разместить IntelliJ-Decompose bridge: `app:intellij-plugin` или `ui:intellij`.
- Нужен ли JCEF для code/diff viewer или достаточно editor components / Swing / IntelliJ UI DSL.
- Какое storage-хранилище выбрать на первом этапе: files, embedded database или in-memory.
- Использовать Git CLI или библиотеку.
- Выносить ли экспорт комментариев для ИИ-агента в отдельный модуль.

## Критерии готовности

- Gradle skeleton содержит заявленные модули.
- Core/api слои не зависят от desktop/plugin UI.
- `app:desktop` собирает desktop entrypoint через `ui:compose`.
- `app:intellij-plugin` собирает plugin entrypoint через `ui:intellij`.
- `core:navigation` содержит общий Decompose root/component tree.
- `app:intellij-plugin` создает `ComponentContext` через IntelliJ lifecycle bridge.
- Desktop-flow показывает `staged` / `untracked` файлы и копирует комментарии через `Copy commentaries to clipboard`.
- MCP-flow позволяет создать review-ссылку, завершить web-ревью и получить комментарии через MCP.
- IDEA plugin-flow показывает `staged` / `untracked` файлы, поддерживает переходы к коду и завершение через clipboard или MCP.
- Feature- и core-контракты не содержат Compose, Swing, IntelliJ Platform UI или JCEF типы.
- Версии внешних библиотек находятся в Gradle-конфигурации или version catalog.
- Доступные проверки компиляции и тестов проходят.
- Если UI уже реализован, desktop и plugin layout проверены отдельно.
