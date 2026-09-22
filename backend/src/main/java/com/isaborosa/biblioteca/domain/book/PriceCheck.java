package com.isaborosa.biblioteca.domain.book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_checks")
public class PriceCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false, length = 50)
    private String store;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    protected PriceCheck() {
    }

    public PriceCheck(Book book, String store, BigDecimal price, String url, Instant checkedAt) {
        this.book = book;
        this.store = store;
        this.price = price;
        this.url = url;
        this.checkedAt = checkedAt;
    }

    public void update(BigDecimal price, String url, Instant checkedAt) {
        this.price = price;
        this.url = url;
        this.checkedAt = checkedAt;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public String getStore() {
        return store;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getUrl() {
        return url;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }
}
