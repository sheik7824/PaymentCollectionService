-- Demo users. Passwords are BCrypt hashes of "admin123" and "consumer123".
INSERT INTO users (id, username, password_hash, role, status) VALUES
    ('11111111-1111-1111-1111-111111111111', 'admin',
     '$2b$10$1y.ki/Kj5bvZurtVSQ.ac.C4faPW14YNFOZe7wzRi2uyh2nEezFrC', 'ADMIN', 'ACTIVE'),
    ('22222222-2222-2222-2222-222222222222', 'consumer',
     '$2b$10$sGp10co4ed2ZJUJIQYYeZurbY6TiClUVOhPuQcwJAaAbXZO97PYKC', 'API_CONSUMER', 'ACTIVE');
