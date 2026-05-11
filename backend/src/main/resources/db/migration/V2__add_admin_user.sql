-- Добавляем единственного администратора
INSERT INTO users (username, password, role, address, phone)
VALUES (
   'admin',
   -- bcrypt‑hash для пароля "admin123"
   '$2a$10$7CwV/2R2bG5b3Dm5X6v3Ue8nZ/F/6n7WcYl1eFHEhVwoeBOUvlRHG',
   'ADMIN',
   NULL,
   NULL
);