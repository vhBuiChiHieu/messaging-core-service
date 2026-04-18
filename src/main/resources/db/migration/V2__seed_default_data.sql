-- Seed dữ liệu admin mặc định để smoke test Flyway resource.
INSERT INTO app_user (id, username, created_at)
VALUES (1, 'admin', CURRENT_TIMESTAMP);
