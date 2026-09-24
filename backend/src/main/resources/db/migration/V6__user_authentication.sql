-- Adapta a tabela "users" ja existente (antes so guardava nome, para o
-- usuario fixo sem autenticacao) para suportar login de verdade, em vez de
-- criar uma tabela paralela.
ALTER TABLE users
    ADD COLUMN username VARCHAR(50) NULL AFTER name,
    ADD COLUMN password_hash VARCHAR(100) NOT NULL DEFAULT '' AFTER username,
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT TRUE AFTER password_hash,
    ADD COLUMN last_login TIMESTAMP NULL AFTER must_change_password,
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE AFTER last_login,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- O unico usuario ja existente (seed da V1) vira o usuario de login.
-- password_hash comeca vazio de proposito: a aplicacao gera o hash BCrypt no
-- primeiro start (ApplicationRunner), nunca em SQL puro.
UPDATE users SET username = 'Isaborosa' WHERE username IS NULL;

ALTER TABLE users MODIFY COLUMN username VARCHAR(50) NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
