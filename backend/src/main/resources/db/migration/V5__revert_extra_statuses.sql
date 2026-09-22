-- Reverte ABANDONADO e QUERO_RELER: a interface nunca chegou a expor essas
-- opcoes ao usuario, entao nenhuma linha usa esses valores. Seguro encolher o
-- ENUM de volta.
ALTER TABLE user_books
    MODIFY COLUMN status ENUM('QUERO_LER', 'LENDO', 'LIDO') NOT NULL;
