-- Insert a sample admin user. Replace the password hash with a generated BCrypt hash in production.
INSERT INTO users (email, password, full_name, role)
VALUES ('admin@medicalflow.local', '$2a$10$CwTycUXWue0Thq9StjUM0uJ8z0b/1pQy7Gq9sD4sYb9Kq8hZ0G/6.', 'Administrator', 'ADMIN');

-- Note: The password hash above is a placeholder. Run the application register endpoint to create secure users,
-- or replace the hash using a bcrypt generator before deploying to production.
