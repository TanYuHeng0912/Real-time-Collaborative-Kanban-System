-- Create Admin User Script for Render.com Production Database
-- Run this in your Render PostgreSQL database after running schema.sql
-- 
-- IMPORTANT: Change the password_hash before using in production!
-- To generate a BCrypt hash, you can use:
-- 1. Online tool (temporary): https://bcrypt-generator.com/ (rounds: 10)
-- 2. Or run this Java code in your local project:
--    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
--    String hash = encoder.encode("your-desired-password");
--    System.out.println(hash);
--
-- Default credentials (CHANGE THESE IN PRODUCTION):
-- Username: admin
-- Password: admin123
-- Email: admin@kanban.com

-- Insert admin user
INSERT INTO users (username, email, password_hash, full_name, role, is_deleted, created_at, updated_at)
VALUES (
    'admin',
    'admin@kanban.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- BCrypt hash for 'admin123' - CHANGE THIS!
    'System Administrator',
    'ADMIN',
    FALSE,
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING; -- Prevents error if admin already exists

-- Verify the admin user was created
SELECT id, username, email, role, created_at 
FROM users 
WHERE username = 'admin' AND role = 'ADMIN';


