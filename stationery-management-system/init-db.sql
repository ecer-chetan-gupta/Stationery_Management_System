-- ============================================================
-- init-db.sql: Pre-create all databases on MySQL container startup
-- Runs automatically when MySQL container starts for the first time
-- ============================================================

CREATE DATABASE IF NOT EXISTS auth_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS inventory_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS request_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Grant full permissions to root from any host (required for Docker networking)
GRANT ALL PRIVILEGES ON auth_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON request_db.* TO 'root'@'%';
FLUSH PRIVILEGES;
