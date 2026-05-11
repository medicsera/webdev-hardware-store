-- Создаём таблицу пользователей
CREATE TABLE users (
   id          BIGSERIAL PRIMARY KEY,
   username    VARCHAR(255) NOT NULL UNIQUE,
   password    VARCHAR(255) NOT NULL,
   role        VARCHAR(50)  NOT NULL,
   address     VARCHAR(255),
   phone       VARCHAR(50)
);
