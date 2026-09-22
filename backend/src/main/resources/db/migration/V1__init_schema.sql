CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(300),
    isbn VARCHAR(20) UNIQUE,
    cover_url VARCHAR(500),
    publish_year INT,
    publisher VARCHAR(200),
    page_count INT,
    description LONGTEXT,
    open_library_key VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_books_open_library_key ON books (open_library_key);

CREATE TABLE user_books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    status ENUM('QUERO_LER', 'LENDO', 'LIDO') NOT NULL,
    rating TINYINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_books_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_books_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT uk_user_books_user_book UNIQUE (user_id, book_id),
    CONSTRAINT chk_user_books_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

INSERT INTO users (name) VALUES ('Isaborosa');
