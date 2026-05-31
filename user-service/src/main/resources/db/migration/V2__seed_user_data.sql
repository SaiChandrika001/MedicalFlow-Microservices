INSERT INTO users (email, password, full_name, role, created_at) VALUES
('admin@medicalflow.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoO6Dj/Eq7QPy3zT5xOU4qFqcGof1u0zZC', 'System Administrator', 'ADMIN', CURRENT_TIMESTAMP),
('doctor@medicalflow.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoO6Dj/Eq7QPy3zT5xOU4qFqcGof1u0zZC', 'Primary Care Doctor', 'DOCTOR', CURRENT_TIMESTAMP),
('patient@medicalflow.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoO6Dj/Eq7QPy3zT5xOU4qFqcGof1u0zZC', 'Patient One', 'PATIENT', CURRENT_TIMESTAMP);
