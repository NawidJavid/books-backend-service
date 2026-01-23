package com.nawidali.sql_labb_2.rest;

import com.nawidali.sql_labb_2.model.Book;
import com.nawidali.sql_labb_2.model.IBooksDb;
import com.nawidali.sql_labb_2.model.Review;
import com.nawidali.sql_labb_2.model.User;
import com.nawidali.sql_labb_2.model.exceptions.InsertException;
import com.nawidali.sql_labb_2.model.exceptions.SelectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for BookController.
 * 
 * Design decision: Use @WebMvcTest for slice testing - only loads the web layer,
 * not the full application context. IBooksDb is mocked to isolate controller logic.
 */
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IBooksDb booksDb;

    @Test
    @DisplayName("GET /books?title=java - should return matching books")
    void searchByTitle_returnsBooks() throws Exception {
        // Arrange
        Book book = new Book(1, "978-0-13-468599-1", "Effective Java", 
                Date.valueOf("2018-01-06"));
        when(booksDb.findBooksByTitle("java")).thenReturn(List.of(book));

        // Act & Assert
        mockMvc.perform(get("/books")
                        .param("title", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("978-0-13-468599-1"))
                .andExpect(jsonPath("$[0].title").value("Effective Java"));

        verify(booksDb).findBooksByTitle("java");
    }

    @Test
    @DisplayName("GET /books without title parameter - should return 400")
    void searchByTitle_missingParameter_returns400() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /books/{isbn} - should return book when found")
    void getByIsbn_returnsBook() throws Exception {
        // Arrange
        Book book = new Book(1, "978-0-13-468599-1", "Effective Java", 
                Date.valueOf("2018-01-06"));
        when(booksDb.findBooksByIsbn("978-0-13-468599-1")).thenReturn(List.of(book));

        // Act & Assert
        mockMvc.perform(get("/books/978-0-13-468599-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("978-0-13-468599-1"))
                .andExpect(jsonPath("$.title").value("Effective Java"));
    }

    @Test
    @DisplayName("GET /books/{isbn} - should return 404 when not found")
    void getByIsbn_notFound_returns404() throws Exception {
        // Arrange
        when(booksDb.findBooksByIsbn("non-existent")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/books/non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /books/{bookId}/rating - should rate book successfully")
    void rateBook_success() throws Exception {
        // Arrange
        doNothing().when(booksDb).rateBook(anyInt(), anyInt(), any(User.class));

        // Act & Assert
        mockMvc.perform(post("/books/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"rating\": 5}"))
                .andExpect(status().isOk());

        verify(booksDb).rateBook(eq(1), eq(5), any(User.class));
    }

    @Test
    @DisplayName("POST /books/{bookId}/rating - should return 400 for invalid rating")
    void rateBook_invalidRating_returns400() throws Exception {
        mockMvc.perform(post("/books/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"rating\": 10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    @DisplayName("POST /books/{bookId}/rating - should return 400 when rating missing")
    void rateBook_missingRating_returns400() throws Exception {
        mockMvc.perform(post("/books/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /books/{bookId}/reviews - should return reviews")
    void getReviews_returnsReviews() throws Exception {
        // Arrange
        User user = new User(1, "reviewer");
        Review review = new Review(1, 1, user, "Great book!", LocalDate.of(2024, 1, 15));
        when(booksDb.findReviewsByBookId(1)).thenReturn(List.of(review));

        // Act & Assert
        mockMvc.perform(get("/books/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Great book!"))
                .andExpect(jsonPath("$[0].user.username").value("reviewer"));
    }

    @Test
    @DisplayName("Database error should return 500")
    void databaseError_returns500() throws Exception {
        // Arrange
        when(booksDb.findBooksByTitle(anyString()))
                .thenThrow(new SelectException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/books").param("title", "test"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Database query error"));
    }

    @Test
    @DisplayName("Insert error should return 400")
    void insertError_returns400() throws Exception {
        // Arrange
        doThrow(new InsertException("Constraint violation"))
                .when(booksDb).rateBook(anyInt(), anyInt(), any(User.class));

        // Act & Assert
        mockMvc.perform(post("/books/999/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"rating\": 5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Data modification failed"));
    }
}
