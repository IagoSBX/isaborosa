CREATE TABLE price_checks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    store VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    url VARCHAR(500) NOT NULL,
    checked_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_price_checks_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT uk_price_checks_book_store UNIQUE (book_id, store)
) ENGINE=InnoDB;
