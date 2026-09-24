package com.isaborosa.biblioteca.domain.book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 300)
    private String author;

    @Column(length = 20, unique = true)
    private String isbn;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "publish_year")
    private Integer publishYear;

    @Column(length = 200)
    private String publisher;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(length = 100)
    private String genre;

    @Lob
    private String description;

    @Column(name = "open_library_key", length = 50)
    private String openLibraryKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Book() {
    }

    public Book(String title, String author, String isbn, String coverUrl, Integer publishYear,
            String publisher, Integer pageCount, String genre, String description, String openLibraryKey) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.coverUrl = coverUrl;
        this.publishYear = publishYear;
        this.publisher = publisher;
        this.pageCount = pageCount;
        this.genre = genre;
        this.description = description;
        this.openLibraryKey = openLibraryKey;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateFrom(Book other) {
        this.title = other.title;
        this.author = other.author;
        this.coverUrl = other.coverUrl;
        this.publishYear = other.publishYear;
        this.publisher = other.publisher;
        this.pageCount = other.pageCount;
        this.genre = other.genre;
        this.description = other.description;
        this.openLibraryKey = other.openLibraryKey;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public Integer getPublishYear() {
        return publishYear;
    }

    public String getPublisher() {
        return publisher;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public String getGenre() {
        return genre;
    }

    public String getDescription() {
        return description;
    }

    public String getOpenLibraryKey() {
        return openLibraryKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
