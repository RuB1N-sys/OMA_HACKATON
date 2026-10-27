CREATE TABLE IF NOT EXISTS users (
  id            BIGSERIAL PRIMARY KEY,
  name          VARCHAR(100)  NOT NULL,
  email         VARCHAR(254)  NOT NULL,
  password_hash VARCHAR(100)  NOT NULL,           -- BCrypt, пароль в открытом виде не хранится
  audience      VARCHAR(10)   NOT NULL CHECK (audience IN ('teen','student','pro','team')),
  created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS users_email_lower_uq ON users (lower(email));
