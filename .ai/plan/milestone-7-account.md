# Plan: Milestone 7 — Account linking + Profile screen

**Status:** pending
**Created:** 2026-06-28
**Session:** —

## Goal
Привязать анонимный аккаунт к Google, слить локальную статистику в Firestore, показать экран профиля.

## Steps

- [ ] `AuthRepository.linkWithGoogle(activity)` — `linkWithCredential(GoogleAuthProvider)` на текущем анонимном uid; uid не меняется
- [ ] При успешном линковании: создать `/users/{uid}` в Firestore если не существует, записать `isAnonymous=false`, `displayName`
- [ ] `StatsRepositoryImpl.mergeAndMigrate()` — прочитать LocalStatsStore, сложить с Remote (если есть), обнулить локальный стор
- [ ] `feature/profile/ProfileScreen.kt` — отображает: имя, uid (последние 6 символов), кнопку "Войти через Google" (для гостей) / "Выйти" (для авторизованных), статистику
- [ ] `feature/profile/ProfileViewModel.kt`
- [ ] Навигация: иконка профиля на HomeScreen → ProfileScreen (или отдельный route)
- [ ] Проверка: гость → линкует Google → статистика сохранилась

## Notes
- `linkWithCredential` сохраняет uid — все записи в комнатах остаются валидными.
- Google Sign-In требует SHA-1 fingerprint в Firebase Console и `play-services-auth` (уже в зависимостях).
- Для получения `GoogleSignInClient` нужен `Activity` context — передавать через `rememberLauncherForActivityResult`.
- Если аккаунт Google уже привязан к другому uid — Firebase выбросит `FirebaseAuthUserCollisionException`; обработать отдельно.
