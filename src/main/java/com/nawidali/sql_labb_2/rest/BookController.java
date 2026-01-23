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
 * REST controller for book operations.
 * 
 * Design decision: Controller depends only on IBooksDb interface, not on concrete
 * implementations. This allows switching databases via configuration without
 * changing the REST layer.
 */
@RestController
@RequestMapping("/books")
public class BookController {

    private final IBooksDb booksDb;

    // Constructor injection - Spring will inject the active IBooksDb implementation
    public BookController(IBooksDb booksDb) {
        this.booksDb = booksDb;
    }

    /**
     * GET /books?title={title}
     * Search books by title (case-insensitive partial match).
     * 
     * @param title search string (required)
     * @return list of matching books
     */
    @GetMapping
    public ResponseEntity<List<Book>> searchByTitle(
            @RequestParam @NotBlank(message = "Title parameter is required") String title) 
            throws SelectException {
        
        List<Book> books = booksDb.findBooksByTitle(title);
        return ResponseEntity.ok(books);
    }

    /**
     * GET /books/{isbn}
     * Fetch a book by exact ISBN.
     * 
     * @param isbn the ISBN to look up
     * @return the book if found, 404 otherwise
     */
    @GetMapping("/{isbn}")
    public ResponseEntity<Book> getByIsbn(@PathVariable String isbn) throws SelectException {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN is required");
        }
        
        List<Book> books = booksDb.findBooksByIsbn(isbn.trim());
        
        if (books.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // ISBN should be unique, return first match
        return ResponseEntity.ok(books.get(0));
    }

    /**
     * POST /books/{bookId}/rating
     * Rate a book. Requires JSON body with userId and rating.
     * 
     * Design decision: Rating requires a userId in the request body since this is
     * a stateless REST API. In a real application, this would come from authentication.
     * 
     * @param bookId the book to rate
     * @param request rating details (userId and rating value)
     */
    @PostMapping("/{bookId}/rating")
    public ResponseEntity<Void> rateBook(
            @PathVariable int bookId,
            @Valid @RequestBody RatingRequest request) throws InsertException {
        
        // Create a simple User object for the rating
        // In production, this would come from authentication context
        User user = new User(request.getUserId(), "api-user");
        
        booksDb.rateBook(bookId, request.getRating(), user);
        
        return ResponseEntity.ok().build();
    }

    /**
     * GET /books/{bookId}/reviews
     * List all reviews for a book.
     * 
     * @param bookId the book ID
     * @return list of reviews (may be empty)
     */
    @GetMapping("/{bookId}/reviews")
    public ResponseEntity<List<Review>> getReviews(@PathVariable int bookId) throws SelectException {
        List<Review> reviews = booksDb.findReviewsByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }
}
