package com.nawidali.sql_labb_2.model;

import java.time.LocalDate;

/**
 * Enkel modellklass for en forfattare.
 */
public class Author {

    private final int authorId;
    private final String name;
    private final LocalDate birthDate;

    public Author(int authorId, String name, LocalDate birthDate) {
        this.authorId = authorId;
        this.name = name;
        this.birthDate = birthDate;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    @Override
    public String toString() {
        return name;
    }
}
