package com.nawidali.sql_labb_2.model;


import java.time.LocalDate;

/**
 * Textrecension kopplad till bok och anvandare.
 */
public class Review {

    private final int reviewId;
    private final int bookId;
    private final User user;
    private final String text;
    private final LocalDate date;

    public Review(int reviewId, int bookId, User user, String text, LocalDate date) {
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.user = user;
        this.text = text;
        this.date = date;
    }

    public int getReviewId() {
        return reviewId;
    }

    public int getBookId() {
        return bookId;
    }

    public User getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return date + " " + user + ": " + text;
    }
}
