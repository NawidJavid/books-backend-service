package com.nawidali.sql_labb_2.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation av en bok med forfattare, genrer och medelbetyg.
 */
public class Book {

    private final int bookId;
    private final String isbn;
    private final String title;
    private final Date published;

    private final List<Author> authors;
    private final List<Genre> genres;
    private final double averageRating;

    public Book(int bookId, String isbn, String title, Date published) {
        this(bookId, isbn, title, published,
                new ArrayList<>(), new ArrayList<>(), 0.0);
    }

    public Book(String isbn, String title, Date published) {
        this(-1, isbn, title, published);
    }

    public Book(int bookId,
                String isbn,
                String title,
                Date published,
                List<Author> authors,
                List<Genre> genres,
                double averageRating) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.authors = authors != null ? authors : new ArrayList<>();
        this.genres = genres != null ? genres : new ArrayList<>();
        this.averageRating = averageRating;
    }

    public int getBookId() {
        return bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public Date getPublished() {
        return published;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public double getAverageRating() {
        return averageRating;
    }

    @Override
    public String toString() {
        return title + ", " + isbn + ", " + published;
    }
}
