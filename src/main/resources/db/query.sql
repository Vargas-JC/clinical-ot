CREATE DATABASE hubble_db;
CREATE SCHEMA hubble_db;

SELECT *FROM hubble_db.users;
SELECT *FROM hubble_db.user_sessions;
-- usuario administrador inicial

INSERT INTO hubble_db.users (id, email, password_hash, full_name, phone, role, active, document_number, birth_date)
VALUES (uuid_generate_v4(),
        'jcvargas.dev@gmail.com',
        '$2a$12$4BJ9qbHEYVaQFPmMtL3G..dQLEoz3K86fhw.bMuRxxucU/2ARzSjC',
        'Juan Carlos Vargas',
        '939345851',
        'ADMIN',
        TRUE,
        '71633609',
        '2002-01-06');