package com.nawidali.sql_labb_2.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for rating request body.
 * 
 * Design decision: Use a dedicated DTO instead of exposing domain models directly.
 * This allows independent evolution of API contracts and domain models.
 */
public class RatingRequest {

    @NotNull(message = "userId is required")
    private Integer userId;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    // Default constructor for JSON deserialization
    public RatingRequest() {
    }

    public RatingRequest(Integer userId, Integer rating) {
        this.userId = userId;
        this.rating = rating;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
