-- =============================================================================
-- OMA ACADEMY DATABASE SCHEMA & SEED DATA
-- =============================================================================

-- 1. Users
CREATE TABLE IF NOT EXISTS users (
  id            BIGSERIAL PRIMARY KEY,
  name          VARCHAR(100)  NOT NULL,
  email         VARCHAR(254)  NOT NULL,
  password_hash VARCHAR(100)  NOT NULL,
  audience      VARCHAR(10)   NOT NULL CHECK (audience IN ('teen','student','pro','team')),
  created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS users_email_lower_uq ON users (lower(email));

-- 2. Projects (B2C мастер-классы, Студенческие курсы, B2B повышение квалификации)
CREATE TABLE IF NOT EXISTS projects (
  id            BIGSERIAL PRIMARY KEY,
  title         VARCHAR(200)  NOT NULL,
  description   TEXT,
  audience      VARCHAR(10)   NOT NULL CHECK (audience IN ('teen','student','pro','team')),
  category      VARCHAR(50),   -- 'masterclass', 'course', 'b2b'
  difficulty    VARCHAR(20),   -- 'easy', 'medium', 'hard'
  duration_min  INTEGER,
  points_reward INTEGER       NOT NULL DEFAULT 0,
  price         NUMERIC(10,2) NOT NULL DEFAULT 0,
  image_url     TEXT,
  created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- 3. Steps (Project -> Step)
CREATE TABLE IF NOT EXISTS steps (
  id          BIGSERIAL PRIMARY KEY,
  project_id  BIGINT        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  step_number INTEGER       NOT NULL,
  title       VARCHAR(200)  NOT NULL,
  description TEXT,
  video_url   TEXT,
  sort_order  INTEGER       NOT NULL DEFAULT 0
);

-- 4. Materials (Step -> Material with retail & wholesale prices)
CREATE TABLE IF NOT EXISTS materials (
  id              BIGSERIAL PRIMARY KEY,
  step_id         BIGINT        NOT NULL REFERENCES steps(id) ON DELETE CASCADE,
  name            VARCHAR(200)  NOT NULL,
  sku             VARCHAR(50),
  quantity        NUMERIC(10,3) NOT NULL DEFAULT 1,
  unit            VARCHAR(20),
  price_retail    NUMERIC(10,2),
  price_wholesale NUMERIC(10,2),
  image_url       TEXT,
  oma_product_url TEXT
);

-- 5. Tools (Step -> Tool)
CREATE TABLE IF NOT EXISTS tools (
  id              BIGSERIAL PRIMARY KEY,
  step_id         BIGINT        NOT NULL REFERENCES steps(id) ON DELETE CASCADE,
  name            VARCHAR(200)  NOT NULL,
  sku             VARCHAR(50),
  image_url       TEXT,
  oma_product_url TEXT
);

-- 6. Enrollments (LMS Progress)
CREATE TABLE IF NOT EXISTS enrollments (
  id           BIGSERIAL PRIMARY KEY,
  user_id      BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  project_id   BIGINT        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  status       VARCHAR(20)   NOT NULL DEFAULT 'enrolled' CHECK (status IN ('enrolled','in_progress','completed')),
  enrolled_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
  completed_at TIMESTAMPTZ,
  UNIQUE(user_id, project_id)
);

-- 7. Step Completions
CREATE TABLE IF NOT EXISTS step_completions (
  id           BIGSERIAL PRIMARY KEY,
  user_id      BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  step_id      BIGINT        NOT NULL REFERENCES steps(id) ON DELETE CASCADE,
  completed_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
  UNIQUE(user_id, step_id)
);

-- 8. Certificates
CREATE TABLE IF NOT EXISTS certificates (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  project_id  BIGINT        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  issued_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
  cert_number VARCHAR(50)   NOT NULL UNIQUE,
  UNIQUE(user_id, project_id)
);

-- 9. Loyalty Points & OMA Bonus Card
CREATE TABLE IF NOT EXISTS user_points (
  id             BIGSERIAL PRIMARY KEY,
  user_id        BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE UNIQUE,
  total_points   INTEGER       NOT NULL DEFAULT 0,
  oma_bonus_card VARCHAR(50),
  updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- 10. Points Transactions (Earn / Spend)
CREATE TABLE IF NOT EXISTS points_transactions (
  id         BIGSERIAL PRIMARY KEY,
  user_id    BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  amount     INTEGER       NOT NULL,
  reason     VARCHAR(100)  NOT NULL,
  ref_type   VARCHAR(50),
  ref_id     BIGINT,
  created_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- 11. Achievements
CREATE TABLE IF NOT EXISTS achievements (
  id          BIGSERIAL PRIMARY KEY,
  code        VARCHAR(50)   NOT NULL UNIQUE,
  name        VARCHAR(100)  NOT NULL,
  description TEXT,
  icon_url    TEXT
);

-- 12. User Achievements
CREATE TABLE IF NOT EXISTS user_achievements (
  id             BIGSERIAL PRIMARY KEY,
  user_id        BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  achievement_id BIGINT      NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
  earned_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(user_id, achievement_id)
);

-- 13. Onboarding Answers
CREATE TABLE IF NOT EXISTS onboarding_answers (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE UNIQUE,
  interests   TEXT,       -- JSON array of strings
  experience  VARCHAR(50),
  goals       TEXT,       -- JSON array of strings
  answered_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 14. UGC Project Gallery (Before / After)
CREATE TABLE IF NOT EXISTS gallery_posts (
  id           BIGSERIAL PRIMARY KEY,
  user_id      BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  project_id   BIGINT        REFERENCES projects(id) ON DELETE SET NULL,
  title        VARCHAR(200),
  before_url   TEXT,
  after_url    TEXT,
  description  TEXT,
  published_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- 15. B2B Companies & Brigades
CREATE TABLE IF NOT EXISTS companies (
  id         BIGSERIAL PRIMARY KEY,
  name       VARCHAR(200) NOT NULL,
  owner_id   BIGINT       NOT NULL REFERENCES users(id),
  created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS company_members (
  id         BIGSERIAL PRIMARY KEY,
  company_id BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
  user_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role       VARCHAR(20) NOT NULL DEFAULT 'member' CHECK (role IN ('owner','manager','member')),
  UNIQUE(company_id, user_id)
);

-- 16. B2B Course Assignments
CREATE TABLE IF NOT EXISTS course_assignments (
  id          BIGSERIAL PRIMARY KEY,
  company_id  BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
  user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  project_id  BIGINT      NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  assigned_by BIGINT      REFERENCES users(id),
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deadline    TIMESTAMPTZ,
  UNIQUE(company_id, user_id, project_id)
);

-- Seed Achievements
INSERT INTO achievements (code, name, description, icon_url) VALUES
  ('first_project', 'Первый шаг', 'Успешно завершен первый проект', '🏆'),
  ('five_projects', 'Мастер на все руки', 'Завершено 5 обучающих проектов', '⭐'),
  ('certified', 'Сертифицированный специалист', 'Получен официальный сертификат ОМА', '🎓'),
  ('b2b_pro', 'B2B Эксперт', 'Пройден специализированный курс повышения квалификации', '💼'),
  ('social_star', 'Вдохновитель сообщества', 'Опубликован проект в галерее До/После', '📸')
ON CONFLICT (code) DO NOTHING;

-- Seed Projects for each level:
-- 1. B2C (Мастер-классы для всех)
INSERT INTO projects (id, title, description, audience, category, difficulty, duration_min, points_reward, price, image_url)
VALUES
  (1, 'Стильная настенная полка в стиле Лофт', 'Пошаговый мастер-класс по сборке и покраске деревянной полки на металлическом каркасе своими руками.', 'teen', 'masterclass', 'easy', 45, 100, 49.90, 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80'),
  (2, 'Кашпо для растений из бетона и дерева', 'Увлекательный DIY-проект для дома и семейного досуга.', 'teen', 'masterclass', 'easy', 60, 120, 35.00, 'https://images.unsplash.com/photo-1485955900006-10f4d324d411?auto=format&fit=crop&w=800&q=80')
ON CONFLICT (id) DO NOTHING;

-- 2. Студенты (Специальное образование: Дизайн, Электрика, Ремонт)
INSERT INTO projects (id, title, description, audience, category, difficulty, duration_min, points_reward, price, image_url)
VALUES
  (3, 'Базовый электромонтаж квартиры: от щитка до розеток', 'Профессиональный базовый курс для начинающих электриков на базе кабельной продукции и автоматики ОМА.', 'student', 'course', 'medium', 180, 300, 150.00, 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&w=800&q=80'),
  (4, 'Современный дизайн интерьера: раскладка плитки и зонирование', 'Практический курс по укладке керамогранита и сочетанию отделочных материалов из ассортимента ОМА.', 'student', 'course', 'medium', 240, 400, 199.00, 'img/tile-design.jpg')
ON CONFLICT (id) DO NOTHING;

-- 3. B2B / Профессионалы (Повышение квалификации, работа со сложными материалами)
INSERT INTO projects (id, title, description, audience, category, difficulty, duration_min, points_reward, price, image_url)
VALUES
  (5, 'Монтаж крупноформатного керамогранита с брендами Ceresit & Litokol', 'Сертификационный курс для профессиональных плиточников и прорабов. Особенности полимерных клеевых смесей и СВП.', 'pro', 'b2b', 'hard', 360, 600, 350.00, 'https://images.unsplash.com/photo-1581094794329-c8112a89af12?auto=format&fit=crop&w=800&q=80'),
  (6, 'Технологии энергоэффективного утепления фасадов фасадов с Knauf & Ceresit', 'Профессиональная сертификация монтажных бригад. Работа со сложными системами теплоизоляции.', 'team', 'b2b', 'hard', 480, 800, 500.00, 'https://images.unsplash.com/photo-1504307651254-35680f356dfd?auto=format&fit=crop&w=800&q=80')
ON CONFLICT (id) DO NOTHING;

-- Seed Steps, Materials & Tools for Project 1 (Полка Лофт)
INSERT INTO steps (id, project_id, step_number, title, description, video_url, sort_order)
VALUES
  (1, 1, 1, 'Подготовка и шлифовка древесины', 'Шлифуем деревянные щиты наждачной бумагой P120 и P240 вдоль волокон для получения гладкой поверхности.', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 1),
  (2, 1, 2, 'Тонирование и защита маслом', 'Наносим защитное декоративное масло для дерева в два слоя с промежуточной сушкой.', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 2),
  (3, 1, 3, 'Сборка металлического каркаса и монтаж', 'Соединяем металлические кронштейны, фиксируем полку и крепим к стене на дюбели.', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 3)
ON CONFLICT (id) DO NOTHING;

INSERT INTO materials (id, step_id, name, sku, quantity, unit, price_retail, price_wholesale, image_url, oma_product_url)
VALUES
  (1, 1, 'Мебельный щит сосна 800x200x18мм', 'OMA-108234', 2, 'шт', 18.50, 14.80, 'https://oma.by/images/goods/108234.jpg', 'https://oma.by/product/108234'),
  (2, 1, 'Шлифовальная бумага рулон P180 5м', 'OMA-339481', 1, 'шт', 6.20, 4.90, 'https://oma.by/images/goods/339481.jpg', 'https://oma.by/product/339481'),
  (3, 2, 'Масло для дерева V33 Белый Дуб 0.5л', 'OMA-449102', 1, 'шт', 25.00, 20.00, 'https://oma.by/images/goods/449102.jpg', 'https://oma.by/product/449102'),
  (4, 3, 'Кронштейн лофт металлический черный 200мм', 'OMA-778192', 2, 'шт', 12.00, 9.60, 'https://oma.by/images/goods/778192.jpg', 'https://oma.by/product/778192'),
  (5, 3, 'Комплект саморезов и дюбелей Fischer 6x40', 'OMA-551029', 1, 'уп', 7.50, 6.00, 'https://oma.by/images/goods/551029.jpg', 'https://oma.by/product/551029')
ON CONFLICT (id) DO NOTHING;

INSERT INTO tools (id, step_id, name, sku, image_url, oma_product_url)
VALUES
  (1, 1, 'Шлифмашина эксцентриковая Bosch PEX 220', 'OMA-TOOL-001', 'https://oma.by/images/goods/tool-001.jpg', 'https://oma.by/product/tool-001'),
  (2, 2, 'Кисть плоская натуральная щетина 50мм', 'OMA-TOOL-002', 'https://oma.by/images/goods/tool-002.jpg', 'https://oma.by/product/tool-002'),
  (3, 3, 'Дрель-шуруповерт аккумуляторный Makita 12V', 'OMA-TOOL-003', 'https://oma.by/images/goods/tool-003.jpg', 'https://oma.by/product/tool-003')
ON CONFLICT (id) DO NOTHING;

-- Seed Steps for Project 5 (Керамогранит Ceresit B2B)
INSERT INTO steps (id, project_id, step_number, title, description, video_url, sort_order)
VALUES
  (4, 5, 1, 'Подготовка основания и грунтовка Ceresit CT 17', 'Очистка стяжки, проверка на остаточную влажность и обеспыливание грунтовкой глубокого проникновения.', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 1),
  (5, 5, 2, 'Приготовление и нанесение клея Ceresit CM 17', 'Двухстороннее нанесение высокоэластичного клея для плит формата до 120х120см гребенчатым шпателем 10мм.', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 2),
  (6, 5, 3, 'Монтаж СВП и укладка плитки', 'Применение системы выравнивания плитки (СВП 1.5мм) и виброприсоски для устранения пустот.', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 3)
ON CONFLICT (id) DO NOTHING;

INSERT INTO materials (id, step_id, name, sku, quantity, unit, price_retail, price_wholesale, image_url, oma_product_url)
VALUES
  (6, 4, 'Грунтовка глубокого проникновения Ceresit CT 17 10л', 'OMA-CER-01', 2, 'шт', 32.00, 24.00, 'https://oma.by/images/goods/cer-01.jpg', 'https://oma.by/product/cer-01'),
  (7, 5, 'Клей для плитки высокоэластичный Ceresit CM 17 Super Flexible 25кг', 'OMA-CER-02', 6, 'меш', 45.00, 33.50, 'https://oma.by/images/goods/cer-02.jpg', 'https://oma.by/product/cer-02'),
  (8, 6, 'Система выравнивания плитки DLS зажим 1.5мм (500 шт)', 'OMA-SVP-01', 1, 'уп', 38.00, 29.00, 'https://oma.by/images/goods/svp-01.jpg', 'https://oma.by/product/svp-01')
ON CONFLICT (id) DO NOTHING;
