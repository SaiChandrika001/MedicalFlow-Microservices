CREATE TABLE IF NOT EXISTS reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    report_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(255) NOT NULL,
    file_url VARCHAR(1024) NOT NULL,
    uploaded_date TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    storage_file_name VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reports_patient_id ON reports (patient_id);
