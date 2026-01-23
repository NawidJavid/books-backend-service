/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nawidali.sql_labb_2.model;

import com.nawidali.sql_labb_2.model.exceptions.ConnectionException;
import com.nawidali.sql_labb_2.model.exceptions.SelectException;
import com.nawidali.sql_labb_2.model.exceptions.InsertException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enkel mock-implementation som bara stoder grundlaggande sokning.
 * Ovriga metoder gor inget eller returnerar tomma listor.
 */
public class IBooksDbMockImpl implements IBooksDb {

    private final List<Book> books; // den "fejkade" databasen

    public IBooksDbMockImpl() {
        books = Arrays.asList(DATA);
    }

    @Override
    public boolean connect(String database) throws ConnectionException {
        return true;
    }

    @Override
    public void disconnect() throws ConnectionException {
        // ingen riktig resurs
    }

    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        List<Book> result = new ArrayList<>();
        String t = title.trim().toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(t)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        List<Book> result = new ArrayList<>();
        String s = isbn.trim().toLowerCase();
        for (Book book : books) {
            if (book.getIsbn().toLowerCase().equals(s)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> findBooksByAuthorName(String authorName) throws SelectException {
        // mock: inget forfattar-stod, returnera bara tom lista
        return new ArrayList<>();
    }

    @Override
    public List<Book> findBooksByGenre(String genreName) throws SelectException {
        return new ArrayList<>();
    }

    @Override
    public List<Book> findBooksByMinRating(int minRating) throws SelectException {
        return new ArrayList<>();
    }

    @Override
    public Book addBook(Book book,
                        List<Author> authors,
                        List<Genre> genres,
                        User addedBy) throws InsertException {
        // mock: gor ingenting, returnerar bara samma bok
        return book;
    }

    @Override
    public void deleteBook(int bookId, User byUser) throws InsertException {
        // mock: gor ingenting
    }

    @Override
    public void rateBook(int bookId, int rating, User user) throws InsertException {
        // mock: gor ingenting
    }

    @Override
    public User login(String username, String password) throws SelectException {
        // mock: godkann alla kombinationer
        return new User(1, username);
    }

    @Override
    public void addReview(int bookId, User user, String text, LocalDate date) throws InsertException {
        // mock: gor ingenting
    }

    @Override
    public List<Review> findReviewsByBookId(int bookId) throws SelectException {
        return new ArrayList<>();
    }

    @Override
    public User findBookCreator(int bookId) throws SelectException {
        // mock: returnera en fast anvandare
        return new User(1, "mockuser");
    }


    private static final Book[] DATA = {
            new Book(1, "123456789", "Databases Illuminated", new Date(2018, 1, 1)),
            new Book(2, "234567891", "Dark Databases", new Date(1990, 1, 1)),
            new Book(3, "456789012", "The buried giant", new Date(2000, 1, 1)),
            new Book(4, "567890123", "Never let me go", new Date(2000, 1, 1)),
            new Book(5, "678901234", "The remains of the day", new Date(2000, 1, 1)),
            new Book(6, "234567890", "Alias Grace", new Date(2000, 1, 1)),
            new Book(7, "345678911", "The handmaids tale", new Date(2010, 1, 1)),
            new Book(8, "345678901", "Shuggie Bain", new Date(2020, 1, 1)),
            new Book(9, "345678912", "Microserfs", new Date(2000, 1, 1)),
    };
}
