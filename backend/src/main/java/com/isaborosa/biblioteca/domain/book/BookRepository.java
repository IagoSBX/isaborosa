package com.isaborosa.biblioteca.domain.book;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    Optional<Book> findByOpenLibraryKey(String openLibraryKey);

    List<Book> findByOpenLibraryKeyStartingWithAndDescriptionIsNotNull(String openLibraryKeyPrefix);
}
