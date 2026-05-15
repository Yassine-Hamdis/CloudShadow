-- ─── Companies ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL
);

-- ─── Users ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- ─── Servers ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS servers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    last_seen DATETIME,
    CONSTRAINT fk_server_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- ─── Metrics ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    server_id BIGINT NOT NULL,
    cpu FLOAT NOT NULL,
    memory FLOAT NOT NULL,
    disk FLOAT NOT NULL,
    network_in FLOAT,
    network_out FLOAT,
    timestamp DATETIME NOT NULL,
    CONSTRAINT fk_metric_server FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE
);

CREATE INDEX idx_metrics_server_timestamp ON metrics(server_id, timestamp);

-- ─── Alerts ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    server_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    timestamp DATETIME NOT NULL,
    is_ai_generated BOOLEAN DEFAULT FALSE,
    confidence_score FLOAT,
    prediction_window VARCHAR(100),
    recommended_action TEXT,
    CONSTRAINT fk_alert_server FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE
);

CREATE INDEX idx_alerts_company_severity ON alerts(server_id, severity);
CREATE INDEX idx_alerts_ai_generated ON alerts(is_ai_generated);