# ОМА Академия: фронтенд + вход через Java и PostgreSQL

## Быстрый старт (демо, без сервера)
Откройте через любой статический сервер, например `python3 -m http.server 8000` в этой папке, и зайдите на http://localhost:8000.
По умолчанию `config.js` пустой: регистрация и вход работают в демо-режиме (данные в localStorage браузера).

## Подключение бэкенда
1. База: `docker compose up -d` (PostgreSQL 16, таблица создаётся из `db/schema.sql`).
2. Сервер: `cd backend && mvn spring-boot:run` (Java 21). Порт 8080.
   Настройки через переменные окружения: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (от 32 символов), `CORS_ORIGINS`.
3. Фронтенд: в `config.js` укажите `API_URL: 'http://localhost:8080'`.

## Контракт API (единственное, что должен знать фронтенд)
| Метод | URL | Тело | Ответ |
|---|---|---|---|
| POST | /api/auth/register | `{name,email,password,audience}` | 201 `{token,user}` |
| POST | /api/auth/login | `{email,password}` | 200 `{token,user}` |
| GET | /api/auth/me | заголовок `Authorization: Bearer <token>` | 200 `user` |

`user` = `{id,name,email,audience}`, где `audience`: teen, student, pro, team. Ошибки: `{"message":"..."}` со статусом 400, 401 или 409.

## Что нужно доделать до продакшена
HTTPS; ограничение числа попыток входа; подтверждение email; проверка роли «сотрудник ОМА»; хранение токена в httpOnly-cookie вместо localStorage; миграции (Flyway) вместо `schema.sql`.
