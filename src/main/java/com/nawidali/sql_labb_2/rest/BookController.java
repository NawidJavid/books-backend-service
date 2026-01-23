package com.nawidali.sql_labb_2.rest;

import com.nawidali.sql_labb_2.model.Book;
import com.nawidali.sql_labb_2.model.IBooksDb;
import com.nawidali.sql_labb_2.model.Review;
import com.nawidali.sql_labb_2.model.User;
import com.nawidali.sql_labb_2.model.exceptions.InsertException;
import com.nawidali.sql_labb_2.model.exceptions.SelectException;
import com.nawidali.sql_labb_2.rest.dto.RatingRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller depending only on IBooksDb interface.
 */
@RestController
@RequestMapping("/books")
public class BookController {

    private final IBooksDb booksDb;

    public BookController(IBooksDb booksDb) {
        this.booksDb = booksDb;
    }

    @GetMapping
    public ResponseEntity<List<Book>> searchByTitle(
            @RequestParam @NotBlank(message = "Title parameter is required") String title)
            throws SelectException {
        return ResponseEntity.ok(booksDb.findBooksByTitle(title));
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<Book> getByIsbn(@PathVariable String isbn) throws SelectException {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN is required");
        }

        List<Book> books = booksDb.findBooksByIsbn(isbn.trim());
        if (books.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(books.get(0));
    }

    @PostMapping("/{bookId}/rating")
    public ResponseEntity<Void> rateBook(
            @PathVariable int bookId,
            @Valid @RequestBody RatingRequest request) throws InsertException {
        User user = new User(request.getUserId(), "api-user");
        booksDb.rateBook(bookId, request.getRating(), user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bookId}/reviews")
    public ResponseEntity<List<Review>> getReviews(@PathVariable int bookId) throws SelectException {
        return ResponseEntity.ok(booksDb.findReviewsByBookId(bookId));
    }
}
