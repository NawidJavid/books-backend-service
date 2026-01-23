package com.nawidali.sql_labb_2.rest;

import com.nawidali.sql_labb_2.model.Book;
import com.nawidali.sql_labb_2.model.IBooksDb;
import com.nawidali.sql_labb_2.model.Review;
import com.nawidali.sql_labb_2.model.User;
import com.nawidali.sql_labb_2.model.exceptions.InsertException;
import com.nawidali.sql_labb_2.model.exceptions.SelectException;
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

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IBooksDb booksDb;

    @Test
    void searchByTitle_returnsBooks() throws Exception {
        Book book = new Book(1, "978-0-13-468599-1", "Effective Java", Date.valueOf("2018-01-06"));
        when(booksDb.findBooksByTitle("java")).thenReturn(List.of(book));

        mockMvc.perform(get("/books").param("title", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("978-0-13-468599-1"))
                .andExpect(jsonPath("$[0].title").value("Effective Java"));

        verify(booksDb).findBooksByTitle("java");
    }

    @Test
    void searchByTitle_missingParameter_returns400() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIsbn_returnsBook() throws Exception {
        Book book = new Book(1, "978-0-13-468599-1", "Effective Java", Date.valueOf("2018-01-06"));
        when(booksDb.findBooksByIsbn("978-0-13-468599-1")).thenReturn(List.of(book));

        mockMvc.perform(get("/books/978-0-13-468599-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("978-0-13-468599-1"));
    }

    @Test
    void getByIsbn_notFound_returns404() throws Exception {
        when(booksDb.findBooksByIsbn("non-existent")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/books/non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rateBook_success() throws Exception {
        doNothing().when(booksDb).rateBook(anyInt(), anyInt(), any(User.class));

        mockMvc.perform(post("/books/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"rating\": 5}"))
                .andExpect(status().isOk());

        verify(booksDb).rateBook(eq(1), eq(5), any(User.class));
    }

    @Test
    void rateBook_invalidRating_returns400() throws Exception {
        mockMvc.perform(post("/books/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"rating\": 10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void rateBook_missingRating_returns400() throws Exception {
        mockMvc.perform(post("/books/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReviews_returnsReviews() throws Exception {
        User user = new User(1, "reviewer");
        Review review = new Review(1, 1, user, "Great book!", LocalDate.of(2024, 1, 15));
        when(booksDb.findReviewsByBookId(1)).thenReturn(List.of(review));

        mockMvc.perform(get("/books/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Great book!"));
    }

    @Test
    void databaseError_returns500() throws Exception {
        when(booksDb.findBooksByTitle(anyString()))
                .thenThrow(new SelectException("Database connection failed"));

        mockMvc.perform(get("/books").param("title", "test"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Database query error"));
    }

    @Test
    void insertError_returns400() throws Exception {
        doThrow(new InsertException("Constraint violation"))
                .when(booksDb).rateBook(anyInt(), anyInt(), any(User.class));

        mockMvc.perform(post("/books/999/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"rating\": 5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Data modification failed"));
    }
}
