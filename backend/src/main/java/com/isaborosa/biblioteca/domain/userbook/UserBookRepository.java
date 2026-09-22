package com.isaborosa.biblioteca.domain.userbook;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {

    @Query("select ub from UserBook ub where ub.user.id = :userId")
    List<UserBook> findAllByUserId(@Param("userId") Long userId);

    @Query("select ub from UserBook ub where ub.user.id = :userId and ub.status = :status")
    List<UserBook> findAllByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ReadingStatus status);

    Optional<UserBook> findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
