-- Dev/test seed data

-- Admin user: username=admin, password=admin123 (bcrypt cost 10)
INSERT INTO AdminUser (id, username, passwordhash, roles, displayname, createdat)
VALUES (gen_random_uuid(), 'admin', '$2a$10$6u1ybSDSJBwHFiUiSn3MeORUMnsX1fMyvLOKa.LvHOzjLuHT7qbR6', 'admin', 'Administrator', now());
