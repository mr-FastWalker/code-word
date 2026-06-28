# Plan: Milestone 8 — Polish

**Status:** pending
**Created:** 2026-06-28
**Session:** —

## Goal
Привести приложение в публикабельный вид: стабильность, UI, безопасность Firestore.

## Steps

### UX / UI
- [ ] Иконка приложения (адаптивная, `.xml` или `.webp` в mipmap)
- [ ] Splash screen (androidx.core.splashscreen) — пока идёт `ensureSignedIn()`
- [ ] Экран результата: показывать счёт партии, роль победившей команды, личную статистику
- [ ] Анимация при перевороте карточки (уже есть) — добавить haptic feedback при тапе
- [ ] Обработка дисконнекта: `players.{uid}.connected = false` при уходе из приложения (Firestore `onDisconnect` через Realtime Database или периодический heartbeat)
- [ ] Сброс `ready`-флагов и `isPrivate` при "Играть снова" (хост нажимает старт — флаги уже перезаписываются, но ready остаются)
- [ ] Лобби: кнопка "Покинуть комнату" (удаляет `players.{uid}` из Firestore)
- [ ] Валидация слотов перед стартом: хотя бы по одному спаймастеру и оперативнику в каждой команде (или настраиваемое правило)

### Безопасность
- [ ] Firestore security rules — написать и задеплоить правила:
  - Читать комнату может любой авторизованный
  - Писать в `players.{uid}` может только сам пользователь
  - `status`, `cards`, `turn`, `winner` — только хост (или через Cloud Function)
  - `/users/{uid}` — только сам пользователь
- [ ] Проверить правила через Firebase Rules Playground

### i18n
- [ ] Убедиться что все строки в `strings.xml` (нет хардкода в Composable)
- [ ] Подготовить `values-en/strings.xml` (английский) если планируется глобальный релиз

### Технические долги
- [ ] GameScreen: обработать `players[uid] == null` (зритель зашёл во время игры) — показывать борд в режиме зрителя
- [ ] `StartScreen.kt` — удалить (больше не используется)
- [ ] Убрать `score` поле из Firestore записи при `startGame` (оно не читается, score считается из карт)

## Notes
- `onDisconnect` в Firestore не существует — нужно либо Realtime Database для presence, либо просто показывать `connected=false` визуально, без автоматики.
- Firestore rules деплоятся через Firebase Console → Firestore → Rules или через `firebase deploy --only firestore:rules`.
