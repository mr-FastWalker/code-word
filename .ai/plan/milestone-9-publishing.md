# Plan: Milestone 9 — Publishing

**Status:** pending
**Created:** 2026-06-28
**Session:** —

## Goal
Опубликовать приложение в Google Play через internal testing → production.

## Steps

### Подготовка
- [ ] Выбрать и зафиксировать `applicationId` (`com.codeword.app`) — уже выбран, не менять
- [ ] Создать keystore для подписи релиза (`keytool -genkey ...`), сохранить в безопасном месте, добавить в `.gitignore`
- [ ] Настроить `signingConfigs` в `app/build.gradle.kts` (читать keystore из `local.properties` или env-переменных)
- [ ] Собрать release APK / AAB: `./gradlew bundleRelease`
- [ ] Проверить ProGuard / R8 — убедиться что Firestore модели не обфусцированы (добавить `-keep` правила если нужно)

### Google Play Console
- [ ] Зарегистрировать аккаунт разработчика (25 USD, одноразово)
- [ ] Создать приложение в Play Console
- [ ] Заполнить store listing: название, описание, скриншоты (минимум 2), иконка 512×512
- [ ] Privacy Policy — обязательна (можно разместить на GitHub Pages)
- [ ] Заполнить анкету контентного рейтинга (IARC) → получить рейтинг
- [ ] Загрузить AAB в Internal Testing track
- [ ] Добавить тестеров по email, проверить установку через Play

### Firebase перед релизом
- [ ] Добавить SHA-1 release keystore в Firebase Console (для Google Sign-In в проде)
- [ ] Скачать обновлённый `google-services.json`
- [ ] Убедиться что Firestore security rules задеплоены (M8)
- [ ] Включить Firebase App Check (опционально, защита от abuse)

### Релиз
- [ ] Internal Testing → Closed Testing (если нужно) → Production
- [ ] Поэтапный rollout (10% → 50% → 100%)

## Notes
- AAB (Android App Bundle) предпочтительнее APK для Play Store.
- `google-services.json` и keystore — никогда не коммитить в git.
- Privacy Policy минимально должна описывать: что собираем (uid, имя, статистику), где храним (Firebase), как удалить данные.
- GitHub Pages для privacy policy: создать репозиторий `<username>.github.io` или использовать `/docs` папку в существующем репо.
