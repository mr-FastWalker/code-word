# Plan: Milestone 6 — Stats

**Status:** pending
**Created:** 2026-06-28
**Session:** —

## Goal
Считать и хранить статистику игрока после каждой партии: победы/поражения, роли, винстрик.

## Архитектура

```
data/stats/
  StatsRepository.kt         — интерфейс
  LocalStatsStore.kt         — DataStore (гости)
  RemoteStatsStore.kt        — Firestore /users/{uid}/stats (авторизованные)
  StatsRepositoryImpl.kt     — выбирает источник по isAnonymous; слияние при линковке
```

Статистика считается **клиентски** после окончания игры (в GameViewModel или отдельном use-case).

## Firestore структура

```
/users/{uid}
  isAnonymous: bool
  displayName: string
  createdAt, updatedAt: Timestamp
  stats:
    gamesPlayed, gamesWon, gamesLost: int
    asSpymaster, asOperative: int
    winStreak, bestWinStreak: int
```

## Steps

- [ ] `core/model/PlayerStats.kt` — data class со всеми полями
- [ ] `data/stats/StatsRepository.kt` — интерфейс: `getStats()`, `recordGame(won, role)`
- [ ] `data/stats/LocalStatsStore.kt` — DataStore Preferences, сериализация вручную
- [ ] `data/stats/RemoteStatsStore.kt` — Firestore `/users/{uid}`, Firestore Transaction для атомарного инкремента
- [ ] `data/stats/StatsRepositoryImpl.kt` — делегирует в Local или Remote по `FirebaseAuth.currentUser.isAnonymous`; метод `mergeAndMigrate()` для линковки аккаунта (M7)
- [ ] Вызов `recordGame()` в `GameViewModel` когда `winner != null` (только один раз, guard через флаг)
- [ ] Проверка: сыграть партию, убедиться что DataStore обновляется (гость) и Firestore (авторизованный)

## Notes
- Зрители (`Role.SPECTATOR`) в статистику не попадают.
- `winStreak` сбрасывается при поражении; `bestWinStreak` только растёт.
- Счётчик `asSpymaster` / `asOperative` по роли в конкретной партии.
- Запись происходит только на устройстве победителя/проигравшего (клиентская сторона); anti-cheat через Cloud Functions — за рамками MVP.
