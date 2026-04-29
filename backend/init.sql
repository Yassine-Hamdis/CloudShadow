-- ─── Create database ──────────────────────────────────────────────────────
CREATE DATABASE IF NOT EXISTS cloudShadowdb;

-- ─── Create user and grant privileges ────────────────────────────────────
CREATE USER IF NOT EXISTS 'cloudshadow_user'@'%'
    IDENTIFIED BY 'cloudshadow123';

GRANT ALL PRIVILEGES ON cloudShadowdb.*
    TO 'cloudshadow_user'@'%';

FLUSH PRIVILEGES;