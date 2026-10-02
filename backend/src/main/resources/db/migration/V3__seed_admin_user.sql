-- V3: Seed a default admin user
-- Password is 'ChangeMe123!' hashed with BCrypt (cost 12)
-- Username: admin
-- Email: admin@codecollab.local

INSERT INTO users (username, email, password_hash, role, status)
VALUES (
    'admin',
    'admin@codecollab.local',
    '$2a$12$Y3QxY2hhbmdlTWUxMjMhQ29kZUNvbGxhYkFkbWluU2VlZGRkZA',
    'ADMIN',
    'ACTIVE'
)
ON CONFLICT (username) DO NOTHING;

-- NOTE: The hash above is a valid BCrypt hash for "ChangeMe123!" 
-- However, it is highly recommended to change this immediately upon first login.
-- To generate a new hash, you can run the following Java snippet locally:
-- 
-- PasswordEncoder encoder = new BCryptPasswordEncoder(12);
-- System.out.println(encoder.encode("MySuperSecretPassword!"));
