package com.nawidali.sql_labb_2.model;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.nawidali.sql_labb_2.model.exceptions.ConnectionException;
import com.nawidali.sql_labb_2.model.exceptions.InsertException;
import com.nawidali.sql_labb_2.model.exceptions.SelectException;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;

public class BooksDbMongo implements IBooksDb {

    // Controller will pass a MySQL JDBC string in Lab 1.
    // For Lab 2, we keep UI code unchanged and use this Mongo URI.
    private static final String DEFAULT_MONGO_URI =
            "mongodb://clientApp:clientPassword@localhost:27017/booksdb?authSource=booksdb";

    private static final String DB_NAME = "booksdb";

    private MongoClient client;
    private MongoDatabase db;

    private MongoCollection<Document> colBooks;
    private MongoCollection<Document> colAuthors;
    private MongoCollection<Document> colGenres;
    private MongoCollection<Document> colUsers;
    private MongoCollection<Document> colCounter;

    /**
     * Connects to MongoDB using the given URI (if it starts with {@code mongodb://}),
     * otherwise uses a default URI.
     *
     * @param databaseUrl MongoDB connection URI or other string (fallback to default URI).
     * @return {@code true} if connection was established and ping succeeded.
     * @throws ConnectionException if connection/authentication fails or ping fails.
     */
    @Override
    public boolean connect(String databaseUrl) throws ConnectionException {
        try {
            disconnect(); // close if already connected

            String uri = (databaseUrl != null && databaseUrl.trim().startsWith("mongodb://"))
                    ? databaseUrl.trim()
                    : DEFAULT_MONGO_URI;

            client = MongoClients.create(uri);
            db = client.getDatabase(DB_NAME);

            // init collections
            colBooks = db.getCollection("book");
            colAuthors = db.getCollection("author");
            colGenres = db.getCollection("genre");
            colUsers = db.getCollection("app_user");
            colCounter = db.getCollection("counter");

            // ping to confirm connection/auth
            db.runCommand(new Document("ping", 1));
            return true;

        } catch (Exception e) {
            throw new ConnectionException("Kunde inte ansluta till MongoDB", e);
        }
    }

    /**
     * Closes the MongoDB connection and clears internal references.
     *
     * @throws ConnectionException if closing the client fails.
     */
    @Override
    public void disconnect() throws ConnectionException {
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            throw new ConnectionException("Kunde inte stanga MongoDB-anslutningen", e);
        } finally {
            client = null;
            db = null;
            colBooks = null;
            colAuthors = null;
            colGenres = null;
            colUsers = null;
            colCounter = null;
        }
    }

    // ---------------- SOKNING ----------------

    /**
     * Finds books whose title contains the given text (case-insensitive).
     *
     * @param title title fragment to search for.
     * @return matching books (possibly empty).
     * @throws SelectException if not connected or query fails.
     */
    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        ensureConnectedForSelect();
        Pattern p = containsIgnoreCase(title);
        Bson filter = regex("title", p);
        return findBooks(filter);
    }

    /**
     * Finds books by exact ISBN.
     *
     * @param isbn ISBN to search for.
     * @return matching books (possibly empty).
     * @throws SelectException if not connected or query fails.
     */
    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        ensureConnectedForSelect();
        Bson filter = eq("isbn", isbn.trim());
        return findBooks(filter);
    }

    /**
     * Finds books by author name (case-insensitive) in embedded author documents.
     *
     * @param authorName author name fragment to search for.
     * @return matching books (possibly empty).
     * @throws SelectException if not connected or query fails.
     */
    @Override
    public List<Book> findBooksByAuthorName(String authorName) throws SelectException {
        ensureConnectedForSelect();
        Pattern p = containsIgnoreCase(authorName);
        Bson filter = regex("authors.name", p);
        return findBooks(filter);
    }

    /**
     * Finds books by genre name (case-insensitive) in embedded genre documents.
     *
     * @param genreName genre name fragment to search for.
     * @return matching books (possibly empty).
     * @throws SelectException if not connected or query fails.
     */
    @Override
    public List<Book> findBooksByGenre(String genreName) throws SelectException {
        ensureConnectedForSelect();
        Pattern p = containsIgnoreCase(genreName);
        Bson filter = regex("genres.name", p);
        return findBooks(filter);
    }

    /**
     * Finds books with average rating greater than or equal to the given minimum.
     *
     * @param minRating minimum rating (inclusive).
     * @return matching books (possibly empty).
     * @throws SelectException if not connected or query fails.
     */
    @Override
    public List<Book> findBooksByMinRating(int minRating) throws SelectException {
        ensureConnectedForSelect();
        Bson filter = gte("average_rating", (double) minRating);
        return findBooks(filter);
    }

    private List<Book> findBooks(Bson filter) throws SelectException {
        List<Book> out = new ArrayList<>();
        try (MongoCursor<Document> cursor = colBooks.find(filter).iterator()) {
            while (cursor.hasNext()) {
                out.add(mapBook(cursor.next()));
            }
            return out;
        } catch (MongoException e) {
            throw new SelectException("Fel vid sokning i MongoDB", e);
        }
    }

    // ---------------- BOKHANTERING ----------------

    /**
     * Adds a book document and returns the created {@link Book} including authors/genres and initial rating.
     *
     * @param book    basic book data.
     * @param authors referenced authors (by id).
     * @param genres  referenced genres (by id).
     * @param addedBy user performing the action (required).
     * @return created book with generated book_id.
     * @throws InsertException if validation fails, counter is missing, or insert fails.
     */
    @Override
    public Book addBook(Book book, List<Author> authors, List<Genre> genres, User addedBy) throws InsertException {
        ensureConnectedForInsert();
        if (addedBy == null) {
            throw new InsertException("Anvandare kravs for att lagga till bok");
        }

        try {
            int bookId = getNextId("next_book_id");

            List<Document> embeddedAuthors = resolveAndEmbedAuthors(authors);
            List<Document> embeddedGenres = resolveAndEmbedGenres(genres);

            Document createdBy = new Document("user_id", addedBy.getUserId())
                    .append("username", addedBy.getUsername());

            Document doc = new Document("book_id", bookId)
                    .append("isbn", book.getIsbn())
                    .append("title", book.getTitle())
                    .append("published", book.getPublished() != null ? new java.util.Date(book.getPublished().getTime()) : null)
                    .append("created_by", createdBy)
                    .append("authors", embeddedAuthors)
                    .append("genres", embeddedGenres)
                    .append("ratings", new ArrayList<Document>())
                    .append("average_rating", 0.0)
                    .append("reviews", new ArrayList<Document>());

            // Remove null published to keep docs clean
            if (doc.get("published") == null) {
                doc.remove("published");
            }

            colBooks.insertOne(doc);

            // Return Book object with real authors/genres
            List<Author> fullAuthors = mapEmbeddedAuthors(embeddedAuthors);
            List<Genre> fullGenres = mapEmbeddedGenres(embeddedGenres);

            return new Book(bookId, book.getIsbn(), book.getTitle(),
                    book.getPublished(), fullAuthors, fullGenres, 0.0);

        } catch (MongoException e) {
            throw new InsertException("Fel vid insattning av bok", e);
        }
    }

    /**
     * Deletes a book by book_id.
     *
     * @param bookId book id to delete.
     * @param byUser user performing the action (required).
     * @throws InsertException if not found, not connected, or delete fails.
     */
    @Override
    public void deleteBook(int bookId, User byUser) throws InsertException {
        ensureConnectedForInsert();
        if (byUser == null) {
            throw new InsertException("Anvandare kravs for att ta bort bok");
        }

        try {
            DeleteResult res = colBooks.deleteOne(eq("book_id", bookId));
            if (res.getDeletedCount() == 0) {
                throw new InsertException("Ingen bok hittades att ta bort");
            }
        } catch (MongoException e) {
            throw new InsertException("Fel vid borttagning av bok", e);
        }
    }

    // ---------------- BETYG ----------------

    /**
     * Adds or updates a user's rating for a book and recomputes the average rating.
     *
     * @param bookId book id.
     * @param rating rating value.
     * @param user   user performing the action (required).
     * @throws InsertException if not found, not connected, or update fails.
     */
    @Override
    public void rateBook(int bookId, int rating, User user) throws InsertException {
        ensureConnectedForInsert();
        if (user == null) {
            throw new InsertException("Anvandare kravs for att satta betyg");
        }

        try {
            java.util.Date now = new java.util.Date();

            // First try update existing rating for this user
            Bson filterExisting = and(eq("book_id", bookId), eq("ratings.user_id", user.getUserId()));
            Bson updateExisting = Updates.combine(
                    Updates.set("ratings.$.rating", rating),
                    Updates.set("ratings.$.rated_at", now)
            );
            UpdateResult res = colBooks.updateOne(filterExisting, updateExisting);

            if (res.getMatchedCount() == 0) {
                // Push new rating
                Document newRating = new Document("user_id", user.getUserId())
                        .append("rating", rating)
                        .append("rated_at", now);
                UpdateResult pushRes = colBooks.updateOne(eq("book_id", bookId), Updates.push("ratings", newRating));
                if (pushRes.getMatchedCount() == 0) {
                    throw new InsertException("Ingen bok hittades att betygsatta");
                }
            }

            // Recompute average_rating (only reading this one book)
            Document bookDoc = colBooks.find(eq("book_id", bookId))
                    .projection(Projections.include("ratings"))
                    .first();
            if (bookDoc == null) {
                throw new InsertException("Ingen bok hittades att betygsatta");
            }

            List<Document> ratingsList = bookDoc.getList("ratings", Document.class, new ArrayList<>());
            double avg = computeAverage(ratingsList);

            colBooks.updateOne(eq("book_id", bookId), Updates.set("average_rating", avg));

        } catch (MongoException e) {
            throw new InsertException("Fel vid sparande av betyg", e);
        }
    }

    // ---------------- LOGIN ----------------

    /**
     * Logs in a user by username and password hash.
     *
     * @param username username.
     * @param password password hash (as stored).
     * @return {@link User} if match exists, otherwise {@code null}.
     * @throws SelectException if not connected or query fails.
     */
    @Override
    public User login(String username, String password) throws SelectException {
        ensureConnectedForSelect();
        try {
            Document doc = colUsers.find(and(eq("username", username), eq("password_hash", password)))
                    .projection(Projections.include("user_id", "username"))
                    .first();
            if (doc == null) return null;

            int userId = doc.getInteger("user_id");
            String uname = doc.getString("username");
            return new User(userId, uname);

        } catch (MongoException e) {
            throw new SelectException("Fel vid login", e);
        }
    }

    // ---------------- RECENSIONER ----------------

    /**
     * Adds a review to a book.
     *
     * @param bookId book id.
     * @param user   user writing the review (required).
     * @param text   review text.
     * @param date   review date (if null, today is used).
     * @throws InsertException if not found, not connected, or update fails.
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

        try {
            int reviewId = getNextId("next_review_id");

            Document review = new Document("review_id", reviewId)
                    .append("user_id", user.getUserId())
                    .append("username", user.getUsername())
                    .append("review_text", text)
                    .append("review_date", java.util.Date.from(date.atStartOfDay(ZoneId.of("UTC")).toInstant()));

            UpdateResult res = colBooks.updateOne(eq("book_id", bookId), Updates.push("reviews", review));
            if (res.getMatchedCount() == 0) {
                throw new InsertException("Ingen bok hittades att recensera");
            }

        } catch (MongoException e) {
            throw new InsertException("Fel vid sparande av recension", e);
        }
    }

    /**
     * Returns reviews for a book (newest first).
     *
     * @param bookId book id.
     * @return list of reviews (possibly empty).
     * @throws SelectException if not connected or query fails.
     */
    @Override
    public List<Review> findReviewsByBookId(int bookId) throws SelectException {
        ensureConnectedForSelect();
        try {
            Document doc = colBooks.find(eq("book_id", bookId))
                    .projection(Projections.include("reviews"))
                    .first();
            List<Review> out = new ArrayList<>();
            if (doc == null) return out;

            List<Document> reviews = doc.getList("reviews", Document.class, new ArrayList<>());
            for (Document r : reviews) {
                int reviewId = r.getInteger("review_id", -1);
                String reviewText = r.getString("review_text");
                java.util.Date d = r.getDate("review_date");
                LocalDate ld = (d != null) ? d.toInstant().atZone(ZoneId.of("UTC")).toLocalDate() : null;

                int userId = r.getInteger("user_id", -1);
                String uname = r.getString("username");
                User u = new User(userId, uname != null ? uname : "");

                out.add(new Review(reviewId, bookId, u, reviewText, ld));
            }

            // newest first (like your SQL ORDER BY DESC)
            out.sort((a, b) -> {
                if (a.getDate() == null && b.getDate() == null) return 0;
                if (a.getDate() == null) return 1;
                if (b.getDate() == null) return -1;
                return b.getDate().compareTo(a.getDate());
            });

            return out;

        } catch (MongoException e) {
            throw new SelectException("Fel vid hamtning av recensioner", e);
        }
    }

    /**
     * Returns the creator of a book (from embedded created_by).
     *
     * @param bookId book id.
     * @return creator user or {@code null} if not found.
     * @throws SelectException if not connected or query fails.
     */
    @Override
    public User findBookCreator(int bookId) throws SelectException {
        ensureConnectedForSelect();
        try {
            Document doc = colBooks.find(eq("book_id", bookId))
                    .projection(Projections.include("created_by"))
                    .first();
            if (doc == null) return null;

            Document cb = doc.get("created_by", Document.class);
            if (cb == null) return null;

            int userId = cb.getInteger("user_id", -1);
            String uname = cb.getString("username");
            if (uname == null) uname = "";
            return new User(userId, uname);

        } catch (MongoException e) {
            throw new SelectException("Fel vid hamtning av skapare av bok", e);
        }
    }

    // ---------------- COUNTER ----------------

    private int getNextId(String fieldName) throws InsertException {
        Document filter = new Document("_id", "counters");
        Document update = new Document("$inc", new Document(fieldName, 1));

        FindOneAndUpdateOptions opts = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.BEFORE)
                .upsert(false);

        Document before = colCounter.findOneAndUpdate(filter, update, opts);

        if (before == null) {
            throw new InsertException("Counter-dokument saknas. Skapa db.counter {_id:'counters', ...} i mongosh.");
        }
        Integer next = before.getInteger(fieldName);
        if (next == null) {
            throw new InsertException("Counter-falt saknas: " + fieldName);
        }
        return next;
    }

    // ---------------- MAPPING ----------------

    private Book mapBook(Document doc) {
        int bookId = doc.getInteger("book_id", -1);
        String isbn = doc.getString("isbn");
        String title = doc.getString("title");

        java.util.Date pub = doc.getDate("published");
        Date published = (pub != null) ? new Date(pub.getTime()) : null;

        Object avgRaw = doc.get("average_rating");
        double avg = (avgRaw instanceof Number) ? ((Number) avgRaw).doubleValue() : 0.0;

        List<Document> aDocs = doc.getList("authors", Document.class, new ArrayList<>());
        List<Document> gDocs = doc.getList("genres", Document.class, new ArrayList<>());

        List<Author> authors = mapEmbeddedAuthors(aDocs);
        List<Genre> genres = mapEmbeddedGenres(gDocs);

        return new Book(bookId, isbn, title, published, authors, genres, avg);
    }

    private List<Author> mapEmbeddedAuthors(List<Document> embedded) {
        List<Author> out = new ArrayList<>();
        for (Document a : embedded) {
            int id = a.getInteger("author_id", -1);
            String name = a.getString("name");
            java.util.Date bd = a.getDate("birth_date");
            LocalDate birthDate = (bd != null)
                    ? bd.toInstant().atZone(ZoneId.of("UTC")).toLocalDate()
                    : null;
            out.add(new Author(id, name != null ? name : "", birthDate));
        }
        return out;
    }

    private List<Genre> mapEmbeddedGenres(List<Document> embedded) {
        List<Genre> out = new ArrayList<>();
        for (Document g : embedded) {
            int id = g.getInteger("genre_id", -1);
            String name = g.getString("name");
            out.add(new Genre(id, name != null ? name : ""));
        }
        return out;
    }

    private List<Document> resolveAndEmbedAuthors(List<Author> authorRefs) throws InsertException {
        List<Document> out = new ArrayList<>();
        if (authorRefs == null || authorRefs.isEmpty()) return out;

        List<Integer> ids = new ArrayList<>();
        for (Author a : authorRefs) ids.add(a.getAuthorId());

        List<Document> found = new ArrayList<>();
        try (MongoCursor<Document> cursor = colAuthors.find(in("author_id", ids))
                .projection(Projections.include("author_id", "name", "birth_date"))
                .iterator()) {
            while (cursor.hasNext()) found.add(cursor.next());
        }

        if (found.size() != ids.size()) {
            throw new InsertException("Minst en author_id finns inte i databasen");
        }

        // Keep input order
        for (Integer id : ids) {
            Document match = null;
            for (Document d : found) {
                if (id.equals(d.getInteger("author_id"))) {
                    match = d;
                    break;
                }
            }
            if (match == null) throw new InsertException("Author saknas: " + id);

            out.add(new Document("author_id", match.getInteger("author_id"))
                    .append("name", match.getString("name"))
                    .append("birth_date", match.getDate("birth_date")));
        }

        return out;
    }

    private List<Document> resolveAndEmbedGenres(List<Genre> genreRefs) throws InsertException {
        List<Document> out = new ArrayList<>();
        if (genreRefs == null || genreRefs.isEmpty()) return out;

        List<Integer> ids = new ArrayList<>();
        for (Genre g : genreRefs) ids.add(g.getGenreId());

        List<Document> found = new ArrayList<>();
        try (MongoCursor<Document> cursor = colGenres.find(in("genre_id", ids))
                .projection(Projections.include("genre_id", "name"))
                .iterator()) {
            while (cursor.hasNext()) found.add(cursor.next());
        }

        if (found.size() != ids.size()) {
            throw new InsertException("Minst en genre_id finns inte i databasen");
        }

        for (Integer id : ids) {
            Document match = null;
            for (Document d : found) {
                if (id.equals(d.getInteger("genre_id"))) {
                    match = d;
                    break;
                }
            }
            if (match == null) throw new InsertException("Genre saknas: " + id);

            out.add(new Document("genre_id", match.getInteger("genre_id"))
                    .append("name", match.getString("name")));
        }

        return out;
    }

    private double computeAverage(List<Document> ratingsList) {
        if (ratingsList == null || ratingsList.isEmpty()) return 0.0;

        double sum = 0.0;
        int count = 0;
        for (Document r : ratingsList) {
            Integer v = r.getInteger("rating");
            if (v != null) {
                sum += v;
                count++;
            }
        }
        return count == 0 ? 0.0 : (sum / count);
    }

    private Pattern containsIgnoreCase(String s) {
        String needle = (s == null) ? "" : s.trim();
        String quoted = Pattern.quote(needle);
        return Pattern.compile(".*" + quoted + ".*", Pattern.CASE_INSENSITIVE);
    }

    private void ensureConnectedForSelect() throws SelectException {
        if (db == null || client == null) {
            throw new SelectException("Inte ansluten till databasen");
        }
    }

    private void ensureConnectedForInsert() throws InsertException {
        if (db == null || client == null) {
            throw new InsertException("Inte ansluten till databasen");
        }
    }
}