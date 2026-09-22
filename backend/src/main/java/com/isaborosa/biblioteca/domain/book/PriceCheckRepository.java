package com.isaborosa.biblioteca.domain.book;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceCheckRepository extends JpaRepository<PriceCheck, Long> {

    Optional<PriceCheck> findByBookIdAndStore(Long bookId, String store);
}
