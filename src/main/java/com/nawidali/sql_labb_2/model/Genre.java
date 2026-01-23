package com.nawidali.sql_labb_2.model;

/**
 * Genre / kategori for en bok.
 */
public class Genre {

    private final int genreId;
    private final String name;

    public Genre(int genreId, String name) {
        this.genreId = genreId;
        this.name = name;
    }

    public Genre(String name) {
        this(-1, name);
    }

    public int getGenreId() {
        return genreId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}

