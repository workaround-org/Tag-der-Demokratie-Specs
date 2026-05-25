-- Dev/test seed data

-- Admin user: username=admin, password=admin123 (bcrypt cost 10)
INSERT INTO admin_users (id, username, password_hash, roles, display_name, created_at)
VALUES (gen_random_uuid(), 'admin', '$2a$10$6u1ybSDSJBwHFiUiSn3MeORUMnsX1fMyvLOKa.LvHOzjLuHT7qbR6', 'admin', 'Administrator', now());
