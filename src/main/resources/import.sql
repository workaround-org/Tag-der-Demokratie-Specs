-- Dev/test seed data

-- Admin user: username=admin, password=admin123 (bcrypt cost 10)
INSERT INTO AdminUser (id, username, passwordhash, roles, displayname, createdat)
VALUES (gen_random_uuid(), 'admin', '$2a$10$WntIbRd5nSwh8/0pAQuRlO1La/5ym7n1AmJbf27g79ZT6zpYXAlCi', 'admin', 'Administrator', now());
