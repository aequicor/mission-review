# Выбор проекта и первый экран локального review

## Контекст

Standalone desktop target уже запускает общий Decompose root и показывает placeholder `ProjectSelection`.
Предыдущая задача по навигации специально оставила выбор проекта и review-экран без реальной логики.

Пользовательский сценарий для desktop flow:

- пользователь открывает приложение;
- на первом экране видит список ранее открытых проектов;
- нажимает `Choose project`;
- приложение открывает системный выбор директории;
- после выбора папки приложение открывает экран проекта;
- слева отображаются структура папки и измененные Git-файлы;
- основная область показывает diff выбранного измененного файла;
- выбранный проект сохраняется локально и доступен после повторного запуска приложения.

IntelliJ target продолжает работать с текущим IDE project и не получает возможность выбрать произвольную директорию.

## Цель изменения

Заменить desktop placeholder выбора проекта на первый рабочий local-project flow:

- добавить модель recently opened projects;
- сохранить список recent projects в локальном файле пользователя;
- добавить target-specific выбор директории в desktop app;
- после выбора или открытия recent project переходить на экран local review выбранного проекта;
- на экране local review показать дерево файлов проекта, список измененных файлов и diff/content preview;
- переверстать desktop UI с опорой на raster mockups, созданные через ImageGen;
- использовать лазуритовый тон проекта как основной визуальный акцент;
- добавить недостающий app/logo mark без зависимости от сторонних бренд-ассетов;
- явно отмечать новые файлы в списке changed files;
- сохранить общую Decompose-навигацию как platform-neutral слой.

## Предполагаемая архитектура

### ImageGen и design reference

ImageGen используется только для создания визуальных raster mockups:

- desktop start screen;
- desktop project review screen;
- app/logo mark, если готового бренд-ассета нет.

Изображения сохраняются в `docs/design/project-selection-ui` как дизайн-референсы.
Runtime UI реализуется кодом в Compose; generated raster mockups не используются как background или layout asset.

### `core:storage`

Модуль содержит common API для recent projects:

- `RecentProject`;
- `RecentProjectsStore`.

JVM-реализация `FileRecentProjectsStore` хранит список в user-local application data:

- Windows: `%APPDATA%/mission-review/recent-projects.txt`;
- macOS: `~/Library/Application Support/mission-review/recent-projects.txt`;
- Linux/other JVM: `~/.local/share/mission-review/recent-projects.txt`.

Формат хранения остается внутренней деталью `core:storage`.

### `core:git`

Модуль содержит common API для чтения локального проекта:

- `ProjectInspector`;
- `ProjectInspection`;
- `ProjectFileNode`;
- `ProjectChange`.

JVM-реализация `JvmGitProjectInspector`:

- читает обозримое дерево файлов через `java.nio.file`;
- пропускает служебные и тяжелые директории вроде `.git`, `.gradle`, `build`, `out`, `node_modules`;
- читает измененные файлы через `git status --porcelain=v1 --untracked-files=all`, чтобы новые файлы не схлопывались до директории;
- показывает diff через `git diff` и `git diff --cached`;
- для untracked-файлов без diff показывает текстовый preview, если файл читается как обычный файл;
- возвращает человекочитаемое сообщение, если выбранная папка не является Git repository.

### `core:navigation`

`DefaultMissionReviewRootComponent` получает зависимости как interfaces:

- `ProjectDirectoryPicker` для target-specific выбора папки;
- `RecentProjectsStore` для списка recent projects;
- `ProjectInspector` для построения модели local review.

`ProjectSelectionComponent`:

- отображает список recent projects;
- открывает picker по команде пользователя;
- запоминает выбранный проект;
- навигирует в `LocalReviewChild`.

`LocalReviewComponent`:

- хранит выбранный project path;
- показывает файловое дерево, измененные файлы и diff выбранного файла;
- поддерживает выбор измененного файла;
- поддерживает refresh: повторно читает дерево проекта, Git-состояние, список измененных/новых файлов и diff текущего выбранного файла;
- поддерживает back только в desktop flow.

### `ui:compose`

Compose Desktop adapter отображает:

- стартовый экран выбора проекта с кнопкой `Choose project` и списком recent projects;
- recent project row с иконкой папки, Git metadata, временем открытия и переходом в проект;
- экран проекта с двухпанельной компоновкой:
  - слева: заголовок проекта, дерево файлов, измененные файлы;
  - справа: diff/content preview выбранного файла.
- changed files сгруппированы по статусам, новые файлы показываются как `New`;
- верхний `Refresh` и маленький icon-button рядом с `Changed files` вызывают одно и то же обновление проекта;
- длинные пути обрезаются через `TextOverflow.Ellipsis`;
- блоки не перекрываются при окне 1100x720;
- changed files остаются видимыми одновременно с project tree.

UI остается рабочим инструментом, без marketing/landing layout.

### `ui:intellij`

Swing adapter сохраняет совместимость с текущим Decompose tree.
Для IntelliJ target выбор проекта не отображается.

### `app:desktop`

Desktop app создает JVM-реализации:

- `SwingProjectDirectoryPicker`;
- `FileRecentProjectsStore`;
- `JvmGitProjectInspector`.

Эти зависимости передаются в root component при bootstrap.

### `app:intellij-plugin`

Plugin app создает `JvmGitProjectInspector` для текущего IDE project.
Recent projects и directory picker не используются в IntelliJ target.

## Затрагиваемые модули

- `consideration/project-selection`;
- `docs/design`;
- `core:storage`;
- `core:git`;
- `core:navigation`;
- `ui:compose`;
- `ui:intellij`;
- `app:desktop`;
- `app:intellij-plugin`.

## Ограничения

- Review comments, AI handoff, MCP session linking and code navigation не реализуются в этой задаче.
- Список recent projects хранится локально синхронно и не синхронизируется между машинами.
- Чтение Git status/diff выполняется через установленный `git` в PATH.
- Первый проход не вводит kotlinx serialization для Decompose stack state.
- Файловое дерево ограничивается по глубине и количеству узлов, чтобы UI не зависал на больших репозиториях.
- Binary diff/rendering не реализуется: если Git или файл не может дать текстовый diff, UI показывает понятное сообщение.
- Directory picker является desktop-target зависимостью; `core:navigation` знает только interface.
- Не добавлять новые UI dependencies без необходимости.
- Не превращать экран в marketing/landing page.
- Текст должен помещаться в кнопки и панели на 1100x720.
- Цветовая схема должна быть спокойной и рабочей, с лазуритовым основным тоном, не однотонной purple/slate/beige темой.

## Открытые вопросы

- Какой формат и модель нужны для review comments и required fixes.
- Нужно ли хранить выбранный project as last active и сразу открывать его при старте.
- Нужна ли фильтрация changed files по staged/unstaged/untracked отдельными группами.
- Нужен ли async loading/cancellation для очень больших репозиториев.
- Должен ли IntelliJ target сохранять текущий IDE project в desktop recent projects.
- Нужна ли в будущем полноценная design system в `core:theme`.
- Нужна ли настройка плотности UI для очень маленьких окон.

## Критерии готовности

- На desktop стартовом экране есть кнопка выбора проекта.
- При выборе папки открывается local review экран выбранного проекта.
- Выбранный проект сохраняется в локальном recent projects store.
- После повторного создания root component recent projects загружаются из store.
- На local review экране слева есть файловое дерево и список измененных файлов.
- Новые файлы отображаются в списке changed files как отдельные элементы со статусом `New`.
- При выборе измененного файла основная область показывает diff или понятный fallback.
- `Refresh` перечитывает проект с диска и Git-состояние, включая дерево файлов, changed/new files и diff.
- Маленькая иконка refresh в сайдбаре является реальным action control или отсутствует; ложного декоративного контрола нет.
- Созданы raster mockups через ImageGen и сохранены в workspace.
- Создан недостающий app icon/logo asset и сохранен в workspace.
- Start screen визуально проверен.
- Project review screen визуально проверен.
- IntelliJ target не показывает выбор произвольного проекта и продолжает открывать текущий IDE project.
- `core:navigation` не зависит от Compose, Swing, IntelliJ Platform или JVM filesystem APIs.
- Доступные Gradle проверки компиляции и тестов проходят либо причина невозможности явно зафиксирована.
- Проведен self-review архитектурных границ, UI, lifecycle, error handling, возможных блокировок UI и соответствия draft.
