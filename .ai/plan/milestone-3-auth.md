# Plan: Milestone 3 — Auth layer

**Status:** in progress
**Created:** 2026-06-27
**Session:** 1

## Goal
Каждый запуск приложения гарантирует наличие uid (анонимный или постоянный).
`ensureSignedIn()` вызывается один раз при старте — до показа UI.

## Steps

- [x] Добавить `kotlinx-coroutines-play-services` в зависимости (нужен для `.await()` на Firebase Tasks) ✅
- [x] `data/auth/AuthRepository.kt` — интерфейс: `currentUser`, `ensureSignedIn()`, `signOut()` ✅
- [x] `data/auth/AuthRepositoryImpl.kt` — реализация через `FirebaseAuth` ✅
- [x] `MainActivity` — вызов `ensureSignedIn()` в `lifecycleScope`, сплэш пока загружается ✅

## Notes
- Для `.await()` на `Task<AuthResult>` нужен отдельный артефакт `kotlinx-coroutines-play-services`.
- Ошибка входа не блокирует приложение — логируем и идём дальше (офлайн-режим).
- `AuthRepositoryImpl` создаётся в `MainActivity`; DI (Hilt/Koin) — за рамками MVP.
- Google Sign-In (linkWithGoogle) — Milestone 7.
