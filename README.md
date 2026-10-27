# OMA ACADEMY — Образовательная e-commerce платформа для ритейлера ОМА

Платформа объединяет обучение и процесс покупок в единую экосистему с тремя уровнями аудитории:
1. **Мастер-классы для всех (B2C)**: короткие практические уроки с покупкой набора материалов в 1 клик.
2. **Специальное образование (Студенты)**: курсы по дизайну интерьера, электрике и ремонту на базе ассортимента ОМА.
3. **Повышение квалификации (B2B / Профессионалы)**: курсы со сложными материалами от брендов-партнеров (Ceresit, Knauf, Litokol), сертификация мастеров, оптовые сметы и личный кабинет бригадира.

---

## 🚀 Быстрый запуск

### 1. База данных (PostgreSQL)
```bash
docker compose up -d
```
СУБД поднимется на порту `5432`, автоматически применив схему и сиды из [schema.sql](file:///d:/OMA_HACKATON/db/schema.sql).

### 2. Запуск бэкенда (Java Spring Boot 3 + Java 21)
```bash
cd backend
mvn spring-boot:run
```
Бэкенд запустится на порту `8080`.

Параметры окружения (настроены дефолты):
- `DB_URL` (default: `jdbc:postgresql://localhost:5432/oma_academy`)
- `DB_USER` (default: `oma`)
- `DB_PASSWORD` (default: `oma_dev_password`)
- `JWT_SECRET` (default: `dev-only-secret-change-me-please-32chars`)
- `CORS_ORIGINS` (default: `http://localhost:5500,http://localhost:8000,http://localhost:3000,http://localhost:5173`)

### 3. Фронтенд
В [config.js](file:///d:/OMA_HACKATON/config.js) укажите:
```javascript
window.OMA_CONFIG = {
  API_URL: 'http://localhost:8080'
};
```
Запустите любой локальный HTTP-сервер:
```bash
python -m http.server 8000
```

---

## 📚 REST API Спецификация

### 1. Авторизация и SSO ( oma.by )
- `POST /api/auth/register` — Регистрация нового пользователя `{name, email, password, audience}`
- `POST /api/auth/login` — Вход по email и паролю `{email, password}`
- `POST /api/auth/sso` — Единый бесшовный вход через oma.by `{email, name, omaBonusCard, omaSsoToken, audience}`
- `GET /api/auth/me` — Текущий профиль пользователя (`Authorization: Bearer <token>`)

### 2. Каталог проектов и курсов
- `GET /api/projects?audience={teen|student|pro|team}&category={masterclass|course|b2b}` — Список проектов с фильтрами
- `GET /api/projects/{id}` — Детальная страница проекта: шаги, материалы с ценами, инструменты, статус прохождения

### 3. Обучение и LMS (Enrollments & Steps)
- `POST /api/enrollments` — Запись на курс `{projectId}`
- `GET /api/enrollments` — Мои активные и завершенные курсы
- `GET /api/enrollments/{projectId}` — Прогресс по конкретному курсу
- `POST /api/enrollments/{projectId}/steps/{stepId}/complete` — Отметка шага как завершенного. При завершении всех шагов начисляются баллы и проверяются достижения.

### 4. Сертификация (генерация PDF на лету)
- `POST /api/certificates` — Выпуск официального сертификата после прохождения курса `{projectId}`
- `GET /api/certificates` — Список моих сертификатов
- `GET /api/certificates/{id}` — Информация о сертификате
- `GET /api/certificates/{id}/pdf` — Скачивание сгенерированного PDF-сертификата (Content-Type: `application/pdf`)

### 5. E-commerce: BOM-адаптер и Корзина ОМА
- `GET /api/bom?projectId={id}&wholesale={true|false}` — Расчет Bill of Materials (смета) на все материалы в 1 клик. Возвращает список материалов, сумму, экономию и прямую ссылку бесшовного добавления в корзину: `https://oma.by/cart?items=SKU:QTY,...`
- `GET /api/pricing?projectId={id}` — Динамический пересчет стоимости (розница vs B2B опт для профи и бригад)

### 6. Геймификация и программа лояльности
- `GET /api/points` — Баланс баллов лояльности, привязанная бонусная карта ОМА и история транзакций
- `POST /api/points/spend` — Списание баллов на скидку в магазине `{points, reason}`
- `POST /api/points/sync-card` — Привязка дисконтной карты ОМА покупателя `{omaBonusCard}` (+50 приветственных бонусов)
- `GET /api/achievements` — Цифровые награды (ачивки): статус выполнения и даты получения

### 7. Онбординг (Персонализация)
- `GET /api/onboarding` — Статус и ответы опроса при первом входе
- `POST /api/onboarding` — Сохранение ответов `{interests: [...], experience: "...", goals: [...]}`

### 8. Социальное доказательство (UGC Галерея До/После)
- `GET /api/gallery` — Публичная галерея завершенных работ До/После
- `POST /api/gallery` — Публикация работы `{projectId, title, beforeUrl, afterUrl, description}`

### 9. B2B Кабинет (Бригадир / Компания)
- `POST /api/b2b/companies` — Создание компании / бригады `{name}`
- `GET /api/b2b/companies` — Мои компании
- `GET /api/b2b/companies/{id}` — Карточка компании
- `POST /api/b2b/companies/{id}/members` — Добавление сотрудника/мастера в бригаду `{email, role}`
- `GET /api/b2b/companies/{id}/members` — Список сотрудников
- `POST /api/b2b/companies/{id}/assignments` — Назначение курса сотруднику `{userId, projectId, deadline}`
- `GET /api/b2b/companies/{id}/assignments` — Контроль прохождения курсов и сертификации сотрудниками

---

## 🗄 Модель данных (PostgreSQL)

```
Project (Проект / Курс / Мастер-класс)
  └── Step (Шаг обучения)
        ├── Material (Материал с артикулом OMA, розничной и оптовой ценой)
        └── Tool (Инструмент с артикулом OMA)
```
- `users`: пользователи с аудиторией (`teen`, `student`, `pro`, `team`)
- `enrollments` & `step_completions`: прогресс обучения в LMS
- `certificates`: уникальные номера `OMA-YYYY-XXXXXX` и даты выдачи
- `user_points` & `points_transactions`: баллы лояльности и синхронизация с картой ОМА
- `achievements` & `user_achievements`: геймификация
- `companies`, `company_members`, `course_assignments`: B2B функционал
- `gallery_posts`: UGC галерея До/После
- `onboarding_answers`: персонализация на старте
