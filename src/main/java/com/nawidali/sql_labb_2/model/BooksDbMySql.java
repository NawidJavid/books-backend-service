package com.nawidali.sql_labb_2.model;

import com.nawidali.sql_labb_2.model.exceptions.ConnectionException;
import com.nawidali.sql_labb_2.model.exceptions.InsertException;
import com.nawidali.sql_labb_2.model.exceptions.SelectException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-implementation av IBooksDb mot MySQL.
 */
public class BooksDbMySql implements IBooksDb {

    private Connection conn;

    @Override
    public boolean connect(String databaseUrl) throws ConnectionException {
        try {
            conn = DriverManager.getConnection(databaseUrl);
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            throw new ConnectionException("Kunde inte ansluta till databasen", e);
        }
    }

    @Override
    public void disconnect() throws ConnectionException {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new ConnectionException("Kunde inte stanga anslutningen", e);
            } finally {
                conn = null;
            }
        }
    }

    // ---------------- SOKNING ----------------

    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        ensureConnectedForSelect();
        String sql =
                "SELECT b.book_id, b.isbn, b.title, b.published, " +
                        "       AVG(r.rating) AS avg_rating " +
                        "FROM book b " +
                        "LEFT JOIN rating r ON r.book_id = b.book_id " +
                        "WHERE LOWER(b.title) LIKE ? " +
                        "GROUP BY b.book_id";
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + title.toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new SelectException("Fel vid sokning pa titel", e);
        }
        return books;
    }

    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        ensureConnectedForSelect();
        String sql =
                "SELECT b.book_id, b.isbn, b.title, b.published, " +
                        "       AVG(r.rating) AS avg_rating " +
                        "FROM book b " +
                        "LEFT JOIN rating r ON r.book_id = b.book_id " +
                        "WHERE b.isbn = ? " +
                        "GROUP BY b.book_id";
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new SelectException("Fel vid sokning pa ISBN", e);
        }
        return books;
    }

    @Override
    public List<Book> findBooksByAuthorName(String authorName) throws SelectException {
        ensureConnectedForSelect();
        String sql =
                "SELECT DISTINCT b.book_id, b.isbn, b.title, b.published, " +
                        "       AVG(r.rating) AS avg_rating " +
                        "FROM book b " +
                        "JOIN book_author ba ON ba.book_id = b.book_id " +
                        "JOIN author a ON a.author_id = ba.author_id " +
                        "LEFT JOIN rating r ON r.book_id = b.book_id " +
                        "WHERE LOWER(a.name) LIKE ? " +
                        "GROUP BY b.book_id";
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + authorName.toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new SelectException("Fel vid sokning pa forfattare", e);
        }
        return books;
    }

    @Override
    public List<Book> findBooksByGenre(String genreName) throws SelectException {
        ensureConnectedForSelect();
        String sql =
                "SELECT DISTINCT b.book_id, b.isbn, b.title, b.published, " +
                        "       AVG(r.rating) AS avg_rating " +
                        "FROM book b " +
                        "JOIN book_genre bg ON bg.book_id = b.book_id " +
                        "JOIN genre g ON g.genre_id = bg.genre_id " +
                        "LEFT JOIN rating r ON r.book_id = b.book_id " +
                        "WHERE LOWER(g.name) LIKE ? " +
                        "GROUP BY b.book_id";
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + genreName.toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new SelectException("Fel vid sokning pa genre", e);
        }
        return books;
    }

    @Override
    public List<Book> findBooksByMinRating(int minRating) throws SelectException {
        ensureConnectedForSelect();
        String sql =
                "SELECT b.book_id, b.isbn, b.title, b.published, " +
                        "       AVG(r.rating) AS avg_rating " +
                        "FROM book b " +
                        "JOIN rating r ON r.book_id = b.book_id " +
                        "GROUP BY b.book_id " +
                        "HAVING AVG(r.rating) >= ?";
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, minRating);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new SelectException("Fel vid sokning pa betyg", e);
        }
        return books;
    }

    // ---------------- BOKHANTERING ----------------

    /**
     * Lägger till en bok samt kopplingar till författare och genrer i en transaktion.
     * Sätter autoCommit till false, gör rollback vid fel och kastar InsertException.
     */
    @Override
    public Book addBook(Book book,
                        List<Author> authors,
                        List<Genre> genres,
                        User addedBy) throws InsertException {
        ensureConnectedForInsert();
        if (addedBy == null) {
            throw new InsertException("Anvandare kravs for att lagga till bok");
        }

        String insertBookSql =
                "INSERT INTO book (isbn, title, published, created_by_user_id) " +
                        "VALUES (?, ?, ?, ?)";
        String insertBookAuthorSql =
                "INSERT INTO book_author (book_id, author_id, created_by_user_id) " +
                        "VALUES (?, ?, ?)";
        String insertBookGenreSql =
                "INSERT INTO book_genre (book_id, genre_id) VALUES (?, ?)";

        try {
            conn.setAutoCommit(false);

            int newBookId;
            try (PreparedStatement stmt = conn.prepareStatement(
                    insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, book.getIsbn());
                stmt.setString(2, book.getTitle());
                stmt.setDate(3, book.getPublished());
                stmt.setInt(4, addedBy.getUserId());
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        newBookId = keys.getInt(1);
                    } else {
                        throw new InsertException("Kunde inte hamta nytt bok-id");
                    }
                }
            }

            if (authors != null && !authors.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(insertBookAuthorSql)) {
                    for (Author a : authors) {
                        stmt.setInt(1, newBookId);
                        stmt.setInt(2, a.getAuthorId());
                        stmt.setInt(3, addedBy.getUserId());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }

            if (genres != null && !genres.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(insertBookGenreSql)) {
                    for (Genre g : genres) {
                        stmt.setInt(1, newBookId);
                        stmt.setInt(2, g.getGenreId());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }

            conn.commit();

            return new Book(newBookId, book.getIsbn(), book.getTitle(),
                    book.getPublished(), authors, genres, 0.0);

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignore) {
            }
            throw new InsertException("Fel vid insattning av bok", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * Tar bort en bok och alla relaterade rader (betyg, recensioner, kopplingar).
     * Riktiga behörighetskontroller görs i controllern; här utförs bara SQL-operationen.
     */
    @Override
    public void deleteBook(int bookId, User byUser) throws InsertException {
        ensureConnectedForInsert();
        String deleteRatingSql = "DELETE FROM rating WHERE book_id = ?";
        String deleteReviewSql = "DELETE FROM review WHERE book_id = ?";
        String deleteBookAuthorSql = "DELETE FROM book_author WHERE book_id = ?";
        String deleteBookGenreSql = "DELETE FROM book_genre WHERE book_id = ?";
        String deleteBookSql = "DELETE FROM book WHERE book_id = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(deleteRatingSql)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(deleteReviewSql)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(deleteBookAuthorSql)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(deleteBookGenreSql)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(deleteBookSql)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignore) {
            }
            throw new InsertException("Fel vid borttagning av bok", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }
    }

    // ---------------- BETYG ----------------

    /**
     * Sätter eller uppdaterar betyg för en viss bok och användare.
     * Bygger på att (user_id, book_id) är unikt i tabellen rating.
     */
    @Override
    public void rateBook(int bookId, int rating, User user) throws InsertException {
        ensureConnectedForInsert();
        if (user == null) {
            throw new InsertException("Anvandare kravs for att satta betyg");
        }
        String sql =
                "INSERT INTO rating (book_id, user_id, rating, rated_at) " +
                        "VALUES (?, ?, ?, CURRENT_DATE) " +
                        "ON DUPLICATE KEY UPDATE rating = VALUES(rating), rated_at = VALUES(rated_at)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, user.getUserId());
            stmt.setInt(3, rating);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new InsertException("Fel vid sparande av betyg", e);
        }
    }

    // ---------------- LOGIN ----------------

    /**
     * Försöker logga in användare med angivet användarnamn och lösenord.
     * Returnerar en User vid lyckad inloggning, annars null.
     */
    @Override
    public User login(String username, String password) throws SelectException {
        ensureConnectedForSelect();
        String sql =
                "SELECT user_id, username " +
                        "FROM app_user " +
                        "WHERE username = ? AND password_hash = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String uname = rs.getString("username");
                    return new User(userId, uname);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new SelectException("Fel vid login", e);
        }
    }

    // ---------------- RECENSIONER ----------------

    /**
     * Lägger till en textrecension för en bok och kopplar den till användaren.
     */
    @Override
    public void addReview(int bookId, User user, String text, LocalDate date) throws InsertException {
        ensureConnectedForInsert();
        if (user == null) {
            throw new InsertException("Anvandare kravs for att skriva recension");
        }
        if (date == null) {
            date = LocalDate.now();
        }
        String sql =
                "INSERT INTO review (book_id, user_id, review_text, review_date, created_by_user_id) " +
                        "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, user.getUserId());
            stmt.setString(3, text);
            stmt.setDate(4, Date.valueOf(date));
            stmt.setInt(5, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new InsertException("Fel vid sparande av recension", e);
        }
    }

    @Override
    public List<Review> findReviewsByBookId(int bookId) throws SelectException {
        ensureConnectedForSelect();
        String sql =
                "SELECT r.review_id, r.review_text, r.review_date, " +
                        "       u.user_id, u.username " +
                        "FROM review r " +
                        "JOIN app_user u ON u.user_id = r.user_id " +
                        "WHERE r.book_id = ? " +
                        "ORDER BY r.review_date DESC";
        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int reviewId = rs.getInt("review_id");
                    String text = rs.getString("review_text");
                    Date d = rs.getDate("review_date");
                    LocalDate ld = d != null ? d.toLocalDate() : null;
                    int userId = rs.getInt("user_id");
                    String username = rs.getString("username");
                    User user = new User(userId, username);
                    reviews.add(new Review(reviewId, bookId, user, text, ld));
                }
            }
        } catch (SQLException e) {
            throw new SelectException("Fel vid hamtning av recensioner", e);
        }
        return reviews;
    }

    @Override
    public User findBookCreator(int bookId) throws SelectException {
        ensureConnectedForSelect();
        String sql =
                "SELECT u.user_id, u.username " +
                        "FROM book b " +
                        "JOIN app_user u ON u.user_id = b.created_by_user_id " +
                        "WHERE b.book_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String username = rs.getString("username");
                    return new User(userId, username);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new SelectException("Fel vid hamtning av skapare av bok", e);
        }
    }

    // ---------------- HJÄLPMETODER ----------------

    private Book mapBook(ResultSet rs) throws SQLException {
        int bookId = rs.getInt("book_id");
        String isbn = rs.getString("isbn");
        String title = rs.getString("title");
        Date published = rs.getDate("published");
        double avgRating = rs.getDouble("avg_rating");
        List<Author> authors = loadAuthorsForBook(bookId);
        List<Genre> genres = loadGenresForBook(bookId);
        return new Book(bookId, isbn, title, published, authors, genres, avgRating);
    }

    private List<Author> loadAuthorsForBook(int bookId) throws SQLException {
        String sql =
                "SELECT a.author_id, a.name, a.birth_date " +
                        "FROM author a " +
                        "JOIN book_author ba ON ba.author_id = a.author_id " +
                        "WHERE ba.book_id = ?";
        List<Author> authors = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int authorId = rs.getInt("author_id");
                    String name = rs.getString("name");
                    Date birth = rs.getDate("birth_date");
                    LocalDate birthDate = birth != null ? birth.toLocalDate() : null;
                    authors.add(new Author(authorId, name, birthDate));
                }
            }
        }
        return authors;
    }

    private List<Genre> loadGenresForBook(int bookId) throws SQLException {
        String sql =
                "SELECT g.genre_id, g.name " +
                        "FROM genre g " +
                        "JOIN book_genre bg ON bg.genre_id = g.genre_id " +
                        "WHERE bg.book_id = ?";
        List<Genre> genres = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int genreId = rs.getInt("genre_id");
                    String name = rs.getString("name");
                    genres.add(new Genre(genreId, name));
                }
            }
        }
        return genres;
    }

    private void ensureConnectedForSelect() throws SelectException {
        if (conn == null) {
            throw new SelectException("Inte ansluten till databasen");
        }
    }

    private void ensureConnectedForInsert() throws InsertException {
        if (conn == null) {
            throw new InsertException("Inte ansluten till databasen");
        }
    }
}
