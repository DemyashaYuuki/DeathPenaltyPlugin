# EdenReligion

Paper-плагин для выбора стороны в лоре EdenWorld.

## Что делает
- При первом заходе открывает GUI с вопросом **«На чьей стороне ты?»**.
- Даёт 4 варианта:
  - **Я за Асуну**
  - **Я за Error'a**
  - **Я против обоих**
  - **Я не участвую в лоре**
- Выбор **нельзя сменить**.
- Если игрок выбрал **«Я не участвую в лоре»**, он может позже **один раз** открыть меню командой `/religion choose`.
- Регистрирует PlaceholderAPI плейсхолдеры:
  - `%edenreligion_symbol%`
  - `%edenreligion_choice%`
  - `%edenreligion_raw%`

## Placeholder значения
- Asuna → `✦`
- Error → `&k✦&r`
- Against both → `⚔`
- None → пробел

## Сборка
```bash
gradle build
```

Готовый jar:
```bash
build/libs/EdenReligion-1.0.0.jar
```
