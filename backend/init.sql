-- ─── Create database if not exists ───────────────────────────────────────
CREATE DATABASE IF NOT EXISTS cloudShadowdb;

-- ─── Grant all privileges to cloudshadow_user ─────────────────────────────
GRANT ALL PRIVILEGES ON cloudShadowdb.* TO 'cloudshadow_user'@'%';
FLUSH PRIVILEGES;