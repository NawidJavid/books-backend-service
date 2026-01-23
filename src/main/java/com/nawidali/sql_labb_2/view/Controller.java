package com.nawidali.sql_labb_2.view;

import com.nawidali.sql_labb_2.model.Author;
import com.nawidali.sql_labb_2.model.Book;
import com.nawidali.sql_labb_2.model.Genre;
import com.nawidali.sql_labb_2.model.IBooksDb;
import com.nawidali.sql_labb_2.model.Review;
import com.nawidali.sql_labb_2.model.SearchMode;
import com.nawidali.sql_labb_2.model.User;
import javafx.application.Platform;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static javafx.scene.control.Alert.AlertType.*;


/**
 * The controller is responsible for handling user requests and update the view
 * (and in some cases the model).
 *
 * @author anderslm@kth.se
 */
public class Controller {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/booksdb?user=books_user&password=books_pwd&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private final BooksPane booksView; // view
    private final IBooksDb booksDb; // model
    private User currentUser;

    public Controller(IBooksDb booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }


    protected void onSearchSelected(String searchFor, SearchMode mode) {
        if (searchFor == null || searchFor.trim().length() < 1) {
            booksView.showAlertAndWait("Enter a search string!", WARNING);
            return;
        }
        final String trimmed = searchFor.trim();

        new Thread(() -> {
            try {
                List<Book> result;
                switch (mode) {
                    case Title:
                        result = booksDb.findBooksByTitle(trimmed);
                        break;
                    case ISBN:
                        result = booksDb.findBooksByIsbn(trimmed);
                        break;
                    case Author:
                        result = booksDb.findBooksByAuthorName(trimmed);
                        break;
                    case Genre:
                        result = booksDb.findBooksByGenre(trimmed);
                        break;
                    case Rating:
                        int minRating;
                        try {
                            minRating = Integer.parseInt(trimmed);
                        } catch (NumberFormatException nfe) {
                            Platform.runLater(() ->
                                    booksView.showAlertAndWait("Enter an integer rating.", WARNING)
                            );
                            return;
                        }
                        if (minRating < 1 || minRating > 5) {
                            final int value = minRating;
                            Platform.runLater(() ->
                                    booksView.showAlertAndWait("Rating must be between 1 and 5.", WARNING)
                            );
                            return;
                        }
                        result = booksDb.findBooksByMinRating(minRating);
                        break;
                    default:
                        result = new ArrayList<>();
                }

                List<Book> finalResult = result;
                Platform.runLater(() -> {
                    if (finalResult == null || finalResult.isEmpty()) {
                        booksView.showAlertAndWait("No results found.", INFORMATION);
                    } else {
                        booksView.displayBooks(finalResult);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Database error.", ERROR)
                );
            }
        }).start();
    }

    protected void onConnectSelected() {
        new Thread(() -> {
            try {
                boolean ok = booksDb.connect(DB_URL);
                Platform.runLater(() -> {
                    if (ok) {
                        booksView.showAlertAndWait("Connected to database.", INFORMATION);
                    } else {
                        booksView.showAlertAndWait("Could not connect to database.", ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Database error.", ERROR)
                );
            }
        }).start();
    }

    protected void onDisconnectSelected() {
        new Thread(() -> {
            try {
                booksDb.disconnect();
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Disconnected from database.", INFORMATION)
                );
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Database error.", ERROR)
                );
            }
        }).start();
    }

    protected void onExitSelected() {
        Platform.exit();
    }

    protected void onLoginSelected() {
        TextInputDialog userDialog = new TextInputDialog();
        userDialog.setHeaderText("Login");
        userDialog.setContentText("Username:");
        String username = userDialog.showAndWait().orElse(null);
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        TextInputDialog pwdDialog = new TextInputDialog();
        pwdDialog.setHeaderText("Login");
        pwdDialog.setContentText("Password:");
        pwdDialog.getEditor().setText("");
        String password = pwdDialog.showAndWait().orElse(null);
        if (password == null) {
            return;
        }

        final String u = username.trim();
        final String p = password.trim();

        new Thread(() -> {
            try {
                User user = booksDb.login(u, p);
                Platform.runLater(() -> {
                    if (user == null) {
                        booksView.showAlertAndWait("Login failed.", WARNING);
                    } else {
                        currentUser = user;
                        booksView.showAlertAndWait("Logged in as " + user.getUsername(), INFORMATION);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Database error.", ERROR)
                );
            }
        }).start();
    }


    protected void onAddBookSelected() {
        if (currentUser == null) {
            booksView.showAlertAndWait("Login first.", WARNING);
            return;
        }

        TextInputDialog isbnDialog = new TextInputDialog();
        isbnDialog.setHeaderText("Add book");
        isbnDialog.setContentText("ISBN:");
        String isbn = isbnDialog.showAndWait().orElse(null);
        if (isbn == null || isbn.trim().isEmpty()) {
            return;
        }

        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setHeaderText("Add book");
        titleDialog.setContentText("Title:");
        String title = titleDialog.showAndWait().orElse(null);
        if (title == null || title.trim().isEmpty()) {
            return;
        }

        TextInputDialog dateDialog = new TextInputDialog();
        dateDialog.setHeaderText("Add book");
        dateDialog.setContentText("Published (YYYY-MM-DD, optional):");
        String dateStr = dateDialog.showAndWait().orElse("");
        Date publishedDate = null;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                publishedDate = Date.valueOf(dateStr.trim());
            } catch (IllegalArgumentException e) {
                booksView.showAlertAndWait("Invalid date format.", WARNING);
                return;
            }
        }

        TextInputDialog authorDialog = new TextInputDialog();
        authorDialog.setHeaderText("Add book");
        authorDialog.setContentText("Author ids (comma separated, optional):");
        String authorIdsStr = authorDialog.showAndWait().orElse("");

        TextInputDialog genreDialog = new TextInputDialog();
        genreDialog.setHeaderText("Add book");
        genreDialog.setContentText("Genre ids (comma separated, optional):");
        String genreIdsStr = genreDialog.showAndWait().orElse("");

        List<Integer> authorIds;
        List<Integer> genreIds;
        try {
            authorIds = parseIdList(authorIdsStr);
            genreIds = parseIdList(genreIdsStr);
        } catch (NumberFormatException e) {
            booksView.showAlertAndWait("Invalid id list.", WARNING);
            return;
        }

        List<Author> authors = new ArrayList<>();
        for (int id : authorIds) {
            authors.add(new Author(id, "", null));
        }
        List<Genre> genres = new ArrayList<>();
        for (int id : genreIds) {
            genres.add(new Genre(id, ""));
        }

        Book book = new Book(isbn.trim(), title.trim(), publishedDate);

        new Thread(() -> {
            try {
                Book created = booksDb.addBook(book, authors, genres, currentUser);
                List<Book> list = new ArrayList<>();
                list.add(created);
                Platform.runLater(() -> booksView.displayBooks(list));
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Database error.", ERROR)
                );
            }
        }).start();
    }

    protected void onRemoveBookSelected() {
        if (currentUser == null) {
            booksView.showAlertAndWait("Login first.", WARNING);
            return;
        }
        Book selected = booksView.getSelectedBook();
        if (selected == null) {
            booksView.showAlertAndWait("Select a book first.", WARNING);
            return;
        }
        final int bookId = selected.getBookId();

        new Thread(() -> {
            try {
                booksDb.deleteBook(bookId, currentUser);
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Book deleted.", INFORMATION)
                );
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Database error.", ERROR)
                );
            }
        }).start();
    }


    protected void onBookActionsSelected() {
        Book selected = booksView.getSelectedBook();
        if (selected == null) {
            booksView.showAlertAndWait("Select a book first.", WARNING);
            return;
        }

        List<String> choices = new ArrayList<>();
        choices.add("Show authors");
        choices.add("Set rating");
        choices.add("Add review");
        choices.add("Show reviews");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Show authors", choices);
        dialog.setHeaderText("Book actions");
        dialog.setContentText("Action:");
        String choice = dialog.showAndWait().orElse(null);
        if (choice == null) {
            return;
        }

        switch (choice) {
            case "Show authors":
                onShowAuthors(selected);
                break;
            case "Set rating":
                onRateBook(selected);
                break;
            case "Add review":
                onAddReview(selected);
                break;
            case "Show reviews":
                onShowReviews(selected);
                break;
            default:
                break;
        }
    }


    private void onShowAuthors(Book book) {
        new Thread(() -> {
            try {
                User creator = booksDb.findBookCreator(book.getBookId());
                List<Author> authors = book.getAuthors();
                StringBuilder authorsStr = new StringBuilder();
                if (authors == null || authors.isEmpty()) {
                    authorsStr.append("No authors.");
                } else {
                    for (int i = 0; i < authors.size(); i++) {
                        if (i > 0) {
                            authorsStr.append(", ");
                        }
                        authorsStr.append(authors.get(i).getName());
                    }
                }
                double avg = book.getAverageRating();
                Platform.runLater(() -> {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Title: ").append(book.getTitle()).append("\n");
                    msg.append("Authors: ").append(authorsStr).append("\n");
                    if (creator != null) {
                        msg.append("Added by: ").append(creator.getUsername()).append("\n");
                    }
                    msg.append("Average rating: ").append(String.format("%.1f", avg));
                    booksView.showAlertAndWait(msg.toString(), INFORMATION);
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Database error.", ERROR)
                );
            }
        }).start();
    }

    private void onRateBook(Book book) {
        if (currentUser == null) {
            booksView.showAlertAndWait("Login first.", WARNING);
            return;
        }

        TextInputDialog ratingDialog = new TextInputDialog("3");
        ratingDialog.setHeaderText("Set rating");
        ratingDialog.setContentText("Rating (1-5):");
        String ratingStr = ratingDialog.showAndWait().orElse(null);
        if (ratingStr == null) {
            return;
        }
        final int rating;
        try {
            rating = Integer.parseInt(ratingStr.trim());
        } catch (NumberFormatException e) {
            booksView.showAlertAndWait("Enter an integer rating.", WARNING);
            return;
        }
        if (rating < 1 || rating > 5) {
            booksView.showAlertAndWait("Rating must be between 1 and 5.", WARNING);
            return;
        }

        final int bookId = book.getBookId();
        new Thread(() -> {
            try {
                booksDb.rateBook(bookId, rating, currentUser);
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Rating saved.", INFORMATION)
                );
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Database error.", ERROR)
                );
            }
        }).start();
    }

    private void onAddReview(Book book) {
        if (currentUser == null) {
            booksView.showAlertAndWait("Login first.", WARNING);
            return;
        }

        TextInputDialog reviewDialog = new TextInputDialog();
        reviewDialog.setHeaderText("Add review");
        reviewDialog.setContentText("Text:");
        String text = reviewDialog.showAndWait().orElse(null);
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        final String reviewText = text.trim();
        final int bookId = book.getBookId();

        new Thread(() -> {
            try {
                booksDb.addReview(bookId, currentUser, reviewText, LocalDate.now());
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Review saved.", INFORMATION)
                );
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Database error.", ERROR)
                );
            }
        }).start();
    }

    private void onShowReviews(Book book) {
        final int bookId = book.getBookId();

        new Thread(() -> {
            try {
                List<Review> reviews = booksDb.findReviewsByBookId(bookId);
                Platform.runLater(() -> {
                    if (reviews == null || reviews.isEmpty()) {
                        booksView.showAlertAndWait("No reviews.", INFORMATION);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (Review r : reviews) {
                            sb.append(r.toString()).append("\n");
                        }
                        booksView.showAlertAndWait(sb.toString(), INFORMATION);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Database error.", ERROR)
                );
            }
        }).start();
    }

    private List<Integer> parseIdList(String input) throws NumberFormatException {
        List<Integer> ids = new ArrayList<>();
        if (input == null) {
            return ids;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return ids;
        }
        String[] parts = trimmed.split(",");
        for (String p : parts) {
            String s = p.trim();
            if (!s.isEmpty()) {
                ids.add(Integer.parseInt(s));
            }
        }
        return ids;
    }

}
