# AGENTS.md

## Роль ИИ-агента

ИИ-агент в проекте `mission-review` - это дополнительные руки разработчика.

Агент помогает проектировать, писать, проверять и дорабатывать код, но не заменяет финальное инженерное решение и ревью оператора-человека.

## Область знаний

Базовая область знаний ИИ-агента - computer science.

Агент должен уверенно работать с:

- архитектурой программных систем;
- Kotlin и JVM-экосистемой;
- desktop-разработкой;
- многомодульными Gradle-проектами;
- UI-разработкой;
- сетевыми приложениями;
- Git workflow;
- безопасностью, конкурентностью и устойчивостью приложений.

## Технологический стек проекта

Основной стек:

- Kotlin Multiplatform;
- Compose Multiplatform;
- Decompose;
- Git;
- MCP;
- Web через Ktor.

Дополнительные архитектурные решения фиксируются в task draft перед реализацией.

## BLOCK: Code conventions

Этот блок применяется ко всему Kotlin/JVM/KMP-коду проекта. Если task draft требует отступить от этих правил, draft должен явно назвать отклонение и причину.

Базовые источники:

- Kotlin coding conventions: <https://kotlinlang.org/docs/coding-conventions.html>
- Android coroutines best practices: <https://developer.android.com/kotlin/coroutines/coroutines-best-practices?hl=ru>
- Android HIDL code style: <https://source.android.com/docs/core/architecture/hidl/code-style?hl=ru>
- KDoc reference: <https://www.baeldung.com/kotlin/kdoc>

### Kotlin: организация и форматирование

- Использовать официальный Kotlin style guide как базовый стиль. Если в проект добавлен formatter или linter, его правила считаются автоматизированной проверкой этого блока.
- Отступ - 4 пробела. Табы не использовать. Открывающую фигурную скобку ставить в конце строки конструкции, закрывающую - на отдельной строке.
- Не использовать `;`, явные `Unit`, лишний `public` и другие конструкции, которые IDE помечает как redundant.
- Имена пакетов - lowercase без `_`. Имена классов и объектов - `UpperCamelCase`. Имена функций, свойств и локальных переменных - `lowerCamelCase`. Константы с глубоко immutable значением - `SCREAMING_SNAKE_CASE`.
- Приватный backing state для публичного read-only API именовать через `_state` / `state`, `_items` / `items` и похожие пары.
- Файлы с одним основным типом называть по этому типу. Файлы с несколькими тесно связанными декларациями называть по их смыслу, не использовать пустые имена вроде `Util`.
- В KMP source sets файлы с top-level declarations в platform-specific коде должны иметь suffix source set'а, например `Platform.jvm.kt`, `Platform.android.kt`, `Platform.ios.kt`. В `commonMain` suffix не нужен.
- В одном Kotlin-файле держать несколько деклараций только если они связаны семантически и файл остается обозримым.
- Порядок внутри класса: свойства и init-блоки, secondary constructors, методы, companion object. Не сортировать методы алфавитно; держать связанные операции рядом.
- При реализации интерфейса сохранять порядок членов интерфейса, если это не ухудшает читаемость.
- Длинные параметры функций, конструкторов и вызовов переносить с обычным отступом 4 пробела. Trailing comma использовать в declaration-site, а в call-site - когда это реально улучшает diff и читаемость.
- Для простых однострочных функций предпочитать expression body. Для публичных API и platform type результатов явно указывать return type.
- Предпочитать `val` вместо `var`, immutable collection interfaces (`List`, `Set`, `Map`) вместо mutable типов в публичных контрактах.
- Использовать named arguments для boolean-параметров и групп одинаковых primitive-типов, когда смысл аргументов не очевиден из контекста.
- `if` использовать для бинарных условий, `when` - для трех и более веток или sealed/domain branching.
- Имена должны отражать доменную роль. Слова `Manager`, `Wrapper`, `Helper`, `Util` допустимы только если они несут точный смысл и лучше доменного имени нет.

### Public API, api/impl и KDoc

- В `api`-модулях и публичных контрактах явно указывать visibility, return types и property types, чтобы случайно не менять ABI/API при изменении реализации.
- Публичные контракты, domain-модели, extension points, lifecycle-sensitive API и нетривиальные алгоритмы документировать через KDoc.
- KDoc должен начинаться с короткого summary. Детали, ограничения, ошибки, threading/lifecycle contract и примеры добавлять только когда они полезны для пользователя API.
- В KDoc использовать Markdown и ссылки на параметры/типы через `[name]`. `@param`, `@property`, `@return`, `@throws`, `@sample`, `@see` использовать, когда они добавляют информацию, а не повторяют сигнатуру.
- Для override не дублировать KDoc, если контракт полностью совпадает с базовым. Документировать только дополнительные ограничения или отличия поведения.
- В `impl`-коде не документировать очевидные детали. Комментарий должен объяснять причину, инвариант, компромисс или небезопасное место, а не пересказывать код.

### Coroutines и concurrency

- `suspend`-функции data/domain слоев должны быть безопасны для вызова с main/UI thread: blocking IO и CPU-heavy работу переносить внутрь через injected `CoroutineDispatcher`.
- Не hardcode'ить `Dispatchers.IO`, `Dispatchers.Default` и `GlobalScope` внутри бизнес-логики. Dispatcher и долгоживущий `CoroutineScope` передавать через DI или composition root.
- UI/component слой создает корутины для пользовательских сценариев и отменяет их вместе с lifecycle. Data/domain слой обычно открывает `suspend` API и `Flow`, а не сам управляет UI scope.
- Для параллельной работы внутри use case использовать structured concurrency: `coroutineScope`, `supervisorScope`, `async`/`await` только в пределах понятного parent scope.
- Работу, которая должна пережить текущий экран, запускать только в явно переданном external scope с documented lifetime.
- Не проглатывать `CancellationException`. Ловить конкретные исключения (`IOException`, domain exceptions и т.п.) и пробрасывать cancellation дальше.
- Долгие циклы и blocking adapters должны быть cancellable: использовать cancellable suspend APIs или явно проверять cancellation.
- Не раскрывать mutable coroutine state наружу. Наружу отдавать immutable `StateFlow`, `SharedFlow`, `Flow`, Decompose `Value` или immutable state-модель.
- В тестах coroutine-кода использовать `runTest` и `TestDispatcher`; все test dispatchers в одном тесте должны делить один scheduler.

### Android/HIDL-adjacent code

- Если в проекте появятся `.hal` или другие Android interface файлы, применять Android interface style: 4-space indentation, mixed-case filenames, явный порядок package/import/docstring/interface declarations.
- Комментарии к interface methods должны описывать контракт, параметры, результат, error cases и threading/callback expectations. Generated или vendor-specific код не смешивать с handwritten Kotlin API.

## Процесс разработки

### 1. Описание задачи

Перед написанием кода задача полностью описывается в файле:

```text
./consideration/<task-name>/DRAFT-<task>-<lang>.md
```

Язык по умолчанию - `ru`, поэтому стандартный файл задачи:

```text
./consideration/<task-name>/DRAFT-<task>-ru.md
```

`<task>` в имени файла должен совпадать с `<task-name>` в имени директории задачи.

Draft должен содержать:

- контекст задачи;
- цель изменения;
- предполагаемую архитектуру;
- затрагиваемые модули;
- ограничения;
- открытые вопросы;
- критерии готовности.

Для текущего скелета проекта используется draft:

```text
./consideration/project-skeleton/DRAFT-project-skeleton-ru.md
```

### 2. Реализация

ИИ-агент пишет код согласно draft'у.

Правила реализации:

- сохранять существующий стиль проекта;
- держать изменения в рамках описанной задачи;
- не смешивать unrelated refactoring с feature-изменениями;
- не ломать границы модулей;
- публичные контракты держать отдельно от реализаций, если модуль использует схему `api` / `impl`;
- target-specific код держать в app-target'ах или adapter'ах.

### 3. Self-review

После реализации ИИ-агент проводит self-review.

Self-review должен включать:

- проверку корректности архитектурных границ;
- проверку компиляции и тестов, если они доступны;
- проверку безопасности;
- проверку возможных утечек памяти;
- проверку race condition;
- проверку dead lock;
- проверку обработки ошибок;
- проверку, что изменения соответствуют draft'у;
- проверку UI/верстки, если изменение затрагивает пользовательский интерфейс.

Для UI-задач агент должен валидировать верстку через ai-vision или другой доступный визуальный способ проверки. Нужно проверять, что элементы не перекрываются, текст помещается в контейнеры, состояния интерфейса доступны пользователю, а layout работает на ожидаемых размерах окна.

Для прототипирования верстки или оптимизации существующей верстки использовать ImageGen, когда задаче нужен raster mockup, визуальный концепт, UI-макет или другой bitmap-артефакт. Если задачу лучше решить кодом, SVG, Compose или существующей UI-системой, ImageGen не используется.

### 4. Ревью оператором

После self-review изменения кода передаются на ревью оператору-человеку.

Оператор проверяет:

- соответствие реализации задаче;
- качество архитектуры;
- качество UX/UI, если применимо;
- риски безопасности;
- полноту self-review;
- необходимость дополнительных правок.

### 5. Исправление замечаний

Если оператор оставил замечания, ИИ-агент исправляет их и возвращает изменения на повторное ревью.

Цикл повторяется до тех пор, пока оператор не подтвердит, что замечаний больше нет.

### 6. Commit

Commit создается только после ревью оператором.

Commit должен содержать ссылку на task draft.

Рекомендуемый формат footer'ов commit message:

```text
Task-Draft: consideration/<task-name>/DRAFT-<task>-ru.md
Human-Reviewed: yes
Human-Review-Statement: Reviewed by human operator in clear mind and sober judgment.
```

Если используется другой язык draft'а, путь в `Task-Draft` должен указывать на соответствующий файл.

## Общие правила

- Не считать задачу завершенной до self-review и передачи изменений оператору.
- Не создавать commit без подтверждения human review.
- Не скрывать известные риски, flaky-поведение или непроверенные участки.
- Если проверка невозможна, явно указать причину и остаточный риск.
- Все существенные архитектурные решения должны быть отражены в `consideration`.
