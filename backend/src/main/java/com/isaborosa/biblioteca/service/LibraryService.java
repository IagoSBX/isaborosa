package com.isaborosa.biblioteca.service;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.book.BookRepository;
import com.isaborosa.biblioteca.domain.user.User;
import com.isaborosa.biblioteca.domain.user.UserRepository;
import com.isaborosa.biblioteca.domain.userbook.ReadingStatus;
import com.isaborosa.biblioteca.domain.userbook.UserBook;
import com.isaborosa.biblioteca.domain.userbook.UserBookRepository;
import com.isaborosa.biblioteca.dto.AddToLibraryRequest;
import com.isaborosa.biblioteca.dto.BookResponseDto;
import com.isaborosa.biblioteca.dto.UpdateLibraryRequest;
import com.isaborosa.biblioteca.dto.UserBookResponseDto;
import com.isaborosa.biblioteca.exception.BookNotFoundException;
import com.isaborosa.biblioteca.exception.DuplicateBookInLibraryException;
import com.isaborosa.biblioteca.exception.InvalidCurrentPageException;
import com.isaborosa.biblioteca.exception.RatingRequiredException;
import com.isaborosa.biblioteca.exception.UserBookNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Biblioteca pessoal de um unico usuario fixo (sem autenticacao nesta fase).
 */
@Service
public class LibraryService {

    private final UserBookRepository userBookRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public LibraryService(UserBookRepository userBookRepository, BookRepository bookRepository,
            UserRepository userRepository) {
        this.userBookRepository = userBookRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserBookResponseDto> list(ReadingStatus status) {
        Long userId = defaultUser().getId();
        List<UserBook> userBooks = status == null
                ? userBookRepository.findAllByUserId(userId)
                : userBookRepository.findAllByUserIdAndStatus(userId, status);
        return userBooks.stream().map(this::toDto).toList();
    }

    @Transactional
    public UserBookResponseDto add(AddToLibraryRequest request) {
        User user = defaultUser();
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));

        if (userBookRepository.existsByUserIdAndBookId(user.getId(), book.getId())) {
            throw new DuplicateBookInLibraryException(book.getId());
        }

        if (request.status() == ReadingStatus.LIDO && request.rating() == null) {
            throw new RatingRequiredException();
        }

        boolean favorite = request.favorite() != null && request.favorite();
        UserBook userBook = new UserBook(user, book, request.status(), request.rating(), favorite);
        return toDto(userBookRepository.save(userBook));
    }

    @Transactional
    public UserBookResponseDto update(Long userBookId, UpdateLibraryRequest request) {
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(() -> new UserBookNotFoundException(userBookId));

        ReadingStatus resultingStatus = request.status() != null ? request.status() : userBook.getStatus();
        Integer resultingRating = request.rating() != null ? request.rating() : userBook.getRating();
        if (resultingStatus == ReadingStatus.LIDO && resultingRating == null) {
            throw new RatingRequiredException();
        }

        Integer pageCount = userBook.getBook().getPageCount();
        if (request.currentPage() != null && pageCount != null && request.currentPage() > pageCount) {
            throw new InvalidCurrentPageException(request.currentPage(), pageCount);
        }

        if (request.status() != null) {
            userBook.updateStatus(request.status());
        }
        if (request.rating() != null) {
            userBook.updateRating(request.rating());
        }
        if (request.favorite() != null) {
            userBook.updateFavorite(request.favorite());
        }
        if (request.currentPage() != null) {
            userBook.updateCurrentPage(request.currentPage());
        }
        return toDto(userBook);
    }

    @Transactional
    public void remove(Long userBookId) {
        if (!userBookRepository.existsById(userBookId)) {
            throw new UserBookNotFoundException(userBookId);
        }
        userBookRepository.deleteById(userBookId);
    }

    private User defaultUser() {
        return userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Nenhum usuario cadastrado"));
    }

    private UserBookResponseDto toDto(UserBook userBook) {
        Book book = userBook.getBook();
        BookResponseDto bookDto = new BookResponseDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCoverUrl(),
                book.getPublishYear(),
                book.getPublisher(),
                book.getPageCount(),
                book.getGenre(),
                book.getDescription(),
                book.getOpenLibraryKey());
        return new UserBookResponseDto(
                userBook.getId(),
                bookDto,
                userBook.getStatus(),
                userBook.getRating(),
                userBook.isFavorite(),
                userBook.getCurrentPage(),
                userBook.getCreatedAt(),
                userBook.getUpdatedAt());
    }
}
