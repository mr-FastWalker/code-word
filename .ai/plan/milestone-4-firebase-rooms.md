# Plan: Milestone 4 — Firebase rooms + sync

**Status:** in progress
**Created:** 2026-06-28
**Session:** 1

## Goal
Создавать и находить комнату по коду; реальный мультиплеер через realtime Firestore listener.

## Firestore структура

```
/rooms/{code}            ← code = ID документа (6 символов)
  schemaVersion: 1
  hostUid: string
  status: "waiting" | "playing" | "finished"
  createdAt, updatedAt: Timestamp
  config: { locale, wordPackId, skinId }
  cards: [{ id, word, color, revealed }]   ← все цвета; безопасность правилами — Milestone 8
  startingTeam: "red" | "blue"
  turn: { currentTeam, phase, clue: {word,count}|null, guessesMade }
  players: { uid: { name, team, role, connected, ready } }
  score: { redLeft, blueLeft }
  winner: "red"|"blue"|null
  winReason: "all_cards"|"assassin"|null
```

## Steps

### Шаг 1 — Обновить domain models
- [x] `Room.kt` — добавить `hostUid`, `winReason` ✅
- [x] `Player.kt` — добавить `ready` ✅
- [x] `TurnState.kt` — добавить `guessesMade` ✅

### Шаг 2 — RoomRepository interface + exceptions
- [x] `data/RoomRepository.kt` ✅

### Шаг 3 — Firestore маппер
- [x] `data/RoomMapper.kt` — Room ↔ Firestore Map ✅

### Шаг 4 — RoomRepositoryImpl
- [x] `createRoom` — 6-символьный код, транзакция проверки коллизии ✅
- [x] `joinRoom` — проверка существования и статуса ✅
- [x] `observeRoom` — callbackFlow + SnapshotListener → Flow<Room?> ✅
- [x] `claimSlot`, `setReady`, `startGame`, `submitClue`, `revealCard`, `endGame` ✅

### Шаг 5 — Проверка
- [x] Sync + Build — убедиться, что нет compile errors
- [ ] Создать комнату с одного устройства, зайти по коду с другого (или эмулятора)
- [ ] Проверить realtime синхронизацию в Firestore Console

## Notes
- Цвета карт пишутся в публичный документ (упрощение MVP); secret/key — Milestone 8.
- `generateRoomCode()` — 6 символов из A-Z0-9; при коллизии — до 5 попыток.
- Кастомные имена комнат — Milestone 5 (в лобби).
- Ошибки — sealed class `RoomError` в `RoomRepository.kt`.
