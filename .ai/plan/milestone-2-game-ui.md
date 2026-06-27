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

### Шаг 1 — Тема и MainActivity ✦ DONE
- [x] Создать `ui/theme/Color.kt` — константы цветов команд и карточек classic-скина ✅
- [x] Создать `ui/theme/Theme.kt` — Material3 LightColorScheme + `CodeWordTheme` composable ✅
- [x] `MainActivity`: `ComponentActivity` + `enableEdgeToEdge` + `setContent { CodeWordTheme { Surface } }` ✅
- [x] `AndroidManifest`: добавлен `windowSoftInputMode="adjustResize"` для диалога ввода подсказки ✅

### Шаг 2 — WordPackProvider + слова ✦ DONE
- [x] Создать `data/WordPackProvider.kt` — `getWords(locale, packId)`, список из 60 русских слов ✅
- [x] Добавить строки в `strings.xml`: команды, роли, статус-бар, диалог подсказки, экран итогов ✅

### Шаг 3 — GameUiState + GameViewModel ✦ DONE
- [x] `WinReason` enum (ALL_CARDS, ASSASSIN) в `core/model/` ✅
- [x] `Team.opposite()` вынесен из приватного GameEngine в публичный метод `Team` ✅
- [x] `GameUiState` с полями board, currentTeam, phase, clue, guessesLeft, score, winner, winReason, myRole, myTeam; computed: isMyTurn, isActiveSpymaster, isActiveOperative ✅
- [x] `GameViewModel`: init, onClueSubmit, onCardTap (авто-переход по count и ошибке), resetGame, StateFlow ✅

### Шаг 4 — Экран игры (GameScreen) ✦ DONE
- [x] `CardView`: flip-анимация (`animateFloatAsState` + `graphicsLayer rotationY`), два режима — spymaster видит light-цвета закрытых, operative видит бежевый ✅
- [x] `BoardGrid`: `LazyVerticalGrid 5×5`, `key = card.id`, тапы блокируются если `!isActiveOperative || card.revealed` ✅
- [x] `StatusBar`: команда + фаза + статус-текст + счёт + подсказка с `guessesLeft` (∞ для count=0) ✅
- [x] `ClueInputDialog`: AlertDialog, два поля с валидацией, dismiss заблокирован (спаймастер обязан дать подсказку) ✅
- [x] `GameScreen`: собирает всё, `LaunchedEffect` на winner → `onGameEnd()`, диалог только для `isActiveSpymaster` ✅

### Шаг 5 — Навигация ✦ DONE
- [x] `StartScreen`: 4 кнопки по 2 на команду (Filled = Спаймастер, Outlined = Оперативник) ✅
- [x] `NavGraph.kt`: маршруты `start` → `game/{myRole}/{myTeam}` → `result/{winner}/{winReason}/{myRole}/{myTeam}` ✅
- [x] "Play Again" из ResultScreen: `navigate(game) { popUpTo(start) }` — новая игра с той же ролью ✅
- [x] `ResultScreen` заглушка (полная реализация — шаг 6) ✅
- [x] `MainActivity`: подключён `CodeWordNavGraph()` ✅

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
