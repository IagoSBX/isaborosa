-- Permite acompanhar o progresso de leitura (pagina atual) de um livro
-- "Estou lendo". Nulo por padrao: nem todo livro tem contagem de paginas
-- conhecida, e a leitura pode nao ter progresso registrado ainda.
ALTER TABLE user_books
    ADD COLUMN current_page INT NULL AFTER rating;
