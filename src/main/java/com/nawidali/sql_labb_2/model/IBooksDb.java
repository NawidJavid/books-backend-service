package com.nawidali.sql_labb_2.model;

import com.nawidali.sql_labb_2.model.exceptions.ConnectionException;
import com.nawidali.sql_labb_2.model.exceptions.SelectException;
import com.nawidali.sql_labb_2.model.exceptions.InsertException;

import java.time.LocalDate;
import java.util.List;

/**
 * Databasgranssnitt for bokdatabasen.
 * Implementationerna (mock/JDBC) maste wrappa DB-specifika undantag
 * i ConnectionException/SelectException/InsertException.
 */
public interface IBooksDb {

    /**
     * Anslut till databasen.
     * @param databaseUrl JDBC-URL inklusive anvandare/losenord
     * @return true vid lyckad anslutning
     */
    boolean connect(String databaseUrl) throws ConnectionException;

    /**
     * Stang anslutningen och ev. resurser.
     */
    void disconnect() throws ConnectionException;

    // --- SOKNING (A) ---

    /**
     * Söker efter böcker vars titel innehåller den angivna strängen (case-insensitive).
     */
    List<Book> findBooksByTitle(String title) throws SelectException;

    /**
     * Söker efter böcker med exakt angivet ISBN.
     */
    List<Book> findBooksByIsbn(String isbn) throws SelectException;

    /**
     * Söker efter böcker där minst en författare matchar namnet (delsträng, case-insensitive).
     */
    List<Book> findBooksByAuthorName(String authorName) throws SelectException;

    /**
     * Söker efter böcker som har en genre vars namn matchar den angivna genren.
     */
    List<Book> findBooksByGenre(String genreName) throws SelectException;

    /**
     * Hamta bocker vars medelbetyg ar minst minRating.
     */
    List<Book> findBooksByMinRating(int minRating) throws SelectException;

    // --- BOKHANTERING (B,F) ---

    /**
     * Lagger till en bok och dess relationer i en transaktion.
     * Antar att authors och genres redan finns i DB (endast koppling).
     */
    Book addBook(Book book,
                 List<Author> authors,
                 List<Genre> genres,
                 User addedBy) throws InsertException;

    /**
     * Tar bort en bok och dess relationer (endast for inloggade).
     */
    void deleteBook(int bookId, User byUser) throws InsertException;

    // --- BETYG (C,G) ---

    /**
     * Satter/uppdaterar betyg for en bok och anvandare.
     * Max ett betyg per (user, book), enligt unik constraint.
     */
    void rateBook(int bookId, int rating, User user) throws InsertException;

    // --- LOGIN (E,F,G,H) ---

    /**
     * Enkel login. Returnerar User vid lyckad inloggning, annars null.
     */
    User login(String username, String password) throws SelectException;

    // --- RECENSIONER (H) ---

    /**
     * Lagger till en recension for en bok och anvandare.
     */
    void addReview(int bookId, User user, String text, LocalDate date) throws InsertException;

    /**
     * Hamta recensioner for en bok.
     */
    List<Review> findReviewsByBookId(int bookId) throws SelectException;

    /**
     * Hamta anvandaren som skapade boken.
     */
    User findBookCreator(int bookId) throws SelectException;
}


