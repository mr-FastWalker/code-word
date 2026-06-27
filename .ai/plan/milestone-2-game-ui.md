# Plan: Milestone 2 — Game UI on local data

**Status:** in progress
**Created:** 2026-06-27
**Session:** 1

## Goal
Полностью работающий экран игры (сетка 5×5, карточки, анимация переворота, полный поток хода) на локальных данных — без Firebase. Все 4 роли проверяются вручную на одном устройстве.

## Коллизии с исходным описанием (исправлено)
- ~~«игра на двоих»~~ → **4 игрока, 2 на 2**: RED-спаймастер, RED-оперативник, BLUE-спаймастер, BLUE-оперативник.
- ~~«кнопка Закончить ход»~~ → **ход завершается автоматически**: по исчерпанию `count` тапов или по первой ошибке (нейтрал / чужая / убийца).
- ~~«бонусный +1 тап за угаданные `count`»~~ → **бонуса нет**: после `count` верных тапов ход немедленно переходит сопернику.
- Карточки оперативника: закрытые карты **выглядят одинаково** — цвет проявляется только на открытых.

## Предусловие
- [x] `app/build.gradle.kts` исправлен: Compose, Navigation, Firebase, Coroutines подключены ✅
- [ ] **Действие для тебя:** `File → Sync Project with Gradle Files` в Android Studio, убедиться что нет ошибок
- [ ] **Действие для тебя:** `./gradlew test` или Run Tests в Android Studio — убедиться что 20 тестов GameEngine проходят

## Steps

### Шаг 1 — Тема и MainActivity ✦ старт отсюда
- [ ] Создать `ui/theme/Theme.kt` — Material3, цвета classic-скина (red, blue, grey, black)
- [ ] Создать `ui/theme/Color.kt` — константы цветов команд
- [ ] `MainActivity`: заменить `Activity` → `ComponentActivity`, добавить `setContent { CodeWordTheme { NavHost } }`

### Шаг 2 — WordPackProvider + слова
- [ ] Создать `data/WordPackProvider.kt` — объект с одним методом `getWords(locale, packId): List<String>`
- [ ] Добавить список ~60 русских слов как константу внутри (для MVP без Firestore)
- [ ] Добавить строки в `strings.xml`: названия экранов, подсказки UI, сообщения об ошибках

### Шаг 3 — GameUiState + GameViewModel
- [ ] `GameUiState` — data class:
  ```
  board: List<Card>
  currentTeam: Team
  phase: GamePhase          // CLUE | GUESS
  clue: Clue?
  guessesLeft: Int          // оставшиеся тапы из count; 0 = авто-переход хода
  score: Score
  winner: Team?
  winReason: WinReason?     // ALL_CARDS | ASSASSIN
  myRole: Role              // SPYMASTER | OPERATIVE
  myTeam: Team              // RED | BLUE
  ```
- [ ] `WinReason` — enum (ALL_CARDS, ASSASSIN) — добавить в `core/model/`
- [ ] `GameViewModel`:
  - При старте: `WordPackProvider.getWords(...)`, случайный `startingTeam`, `GameEngine.generateBoard`
  - `onClueSubmit(word, count)` → `GameEngine.submitClue`, устанавливает `guessesLeft = count`
  - `onCardTap(card)` → `GameEngine.applyGuess`; по результату:
    - `Correct` + `guessesLeft > 1` → декремент `guessesLeft`, продолжить
    - `Correct` + `guessesLeft == 1` → `guessesLeft = 0`, **авто-переход хода**
    - `WrongTeam` / `Assassin` → **немедленный переход хода** (или конец игры)
    - `Win` → установить `winner` + `winReason`
  - `resetGame()` — новая партия с теми же параметрами
  - Exposed через `StateFlow<GameUiState>`

### Шаг 4 — Экран игры (GameScreen)

#### 4a — CardView
- [ ] Два режима рендера:
  - **Spymaster**: все 25 карт с цветным фоном (цвет команды / нейтральный / убийца)
  - **Operative**: закрытые карты — одинаковый нейтральный фон; цвет появляется только при `revealed = true`
- [ ] Анимация переворота при `revealed` (`animateFloatAsState` + `graphicsLayer rotationY`)
- [ ] На открытой карте: цветной фон + слово; у спаймастера ещё и текст цвета поверх закрытых

#### 4b — BoardGrid
- [ ] `LazyVerticalGrid(columns = Fixed(5))` из `CardView`
- [ ] Тапы по карточкам блокируются (`enabled = false`) если: не твоя фаза / не твоя очередь / карта уже открыта

#### 4c — StatusBar (верхняя полоска)
- [ ] Чей ход + фаза (`RED • Подсказка` / `BLUE • Угадывает`)
- [ ] Счёт: `redLeft` / `blueLeft`
- [ ] Текущая подсказка: `СЛОВО × N` + `осталось: guessesLeft` (видно всем в фазе GUESS)

#### 4d — ClueInputDialog
- [ ] Показывается только активному спаймастеру в фазе CLUE
- [ ] Поле ввода слова + поле числа (1–9, или 0 = «∞»)
- [ ] Валидация: слово не пустое, число в диапазоне; кнопка «Дать подсказку»

#### 4e — GameScreen (сборка)
- [ ] Принимает `myRole: Role` + `myTeam: Team` (от навигации)
- [ ] Собирает StatusBar + BoardGrid + ClueInputDialog
- [ ] Если `winner != null` — навигация на ResultScreen

### Шаг 5 — Навигация
- [ ] `NavGraph.kt` с маршрутами: `start` → `game/{myRole}/{myTeam}`
- [ ] `StartScreen`: 4 кнопки (RED Спаймастер / RED Оперативник / BLUE Спаймастер / BLUE Оперативник) — для тестирования всех 4 ролей локально

### Шаг 6 — ResultScreen
- [ ] Текст победителя + причина (все карты открыты / убийца)
- [ ] Кнопка «Играть снова» → `viewModel.resetGame()`, назад на GameScreen
- [ ] Кнопка «Выйти» → на StartScreen

### Шаг 7 — Ручная проверка
- [ ] Прогнать полную партию от spymaster RED: подсказка → operative RED угадывает верно × count → ход переходит автоматически
- [ ] Проверить: нейтральная карта → ход переходит мгновенно
- [ ] Проверить: тап убийцы → мгновенный проигрыш
- [ ] Проверить: operative видит закрытые как одинаковые; spymaster видит все цвета

## Notes
- Firebase не трогаем — данные только в памяти ViewModel.
- `Room` model не используем напрямую на этом этапе.
- Правило «оперативник не видит цвета закрытых» реализуется через `CardView`: просто не читаем `card.color` если `!card.revealed && myRole == OPERATIVE`.
- На Milestone 4 эта защита продублируется на уровне Firestore (секретная подколлекция `secret/key`).
- Счётчик `guessesLeft` живёт только в ViewModel (не в Firestore на этом этапе).
- Строки через `strings.xml`; ноль хардкода в Composables.
