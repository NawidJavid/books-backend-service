package com.nawidali.sql_labb_2.model;

/**
 * Anvandare som kan logga in och skapa data.
 */
public class User {

    private final int userId;
    private final String username;

    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }
}
