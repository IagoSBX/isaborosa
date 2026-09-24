package com.isaborosa.biblioteca.domain.userbook;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user_books")
public class UserBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReadingStatus status;

    @Column(columnDefinition = "TINYINT")
    private Integer rating;

    @Column(name = "current_page")
    private Integer currentPage;

    @Column(nullable = false)
    private boolean favorite;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserBook() {
    }

    public UserBook(User user, Book book, ReadingStatus status, Integer rating) {
        this(user, book, status, rating, false);
    }

    public UserBook(User user, Book book, ReadingStatus status, Integer rating, boolean favorite) {
        this.user = user;
        this.book = book;
        this.status = status;
        this.rating = rating;
        this.favorite = favorite;
    }

    public void updateStatus(ReadingStatus status) {
        this.status = status;
    }

    public void updateRating(Integer rating) {
        this.rating = rating;
    }

    public void updateFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void updateCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }

    public ReadingStatus getStatus() {
        return status;
    }

    public Integer getRating() {
        return rating;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
