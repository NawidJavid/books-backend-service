-- Books Database Schema

CREATE TABLE IF NOT EXISTS app_user (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS author (
    author_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    birth_date DATE
);

CREATE TABLE IF NOT EXISTS genre (
    genre_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS book (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(300) NOT NULL,
    published DATE,
    created_by_user_id INT,
    FOREIGN KEY (created_by_user_id) REFERENCES app_user(user_id)
);

CREATE TABLE IF NOT EXISTS book_author (
    book_id INT NOT NULL,
    author_id INT NOT NULL,
    created_by_user_id INT,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES book(book_id),
    FOREIGN KEY (author_id) REFERENCES author(author_id),
    FOREIGN KEY (created_by_user_id) REFERENCES app_user(user_id)
);

CREATE TABLE IF NOT EXISTS book_genre (
    book_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (book_id, genre_id),
    FOREIGN KEY (book_id) REFERENCES book(book_id),
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id)
);

CREATE TABLE IF NOT EXISTS rating (
    rating_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    rated_at DATE,
    UNIQUE KEY unique_user_book (user_id, book_id),
    FOREIGN KEY (book_id) REFERENCES book(book_id),
    FOREIGN KEY (user_id) REFERENCES app_user(user_id)
);

CREATE TABLE IF NOT EXISTS review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    user_id INT NOT NULL,
    review_text TEXT,
    review_date DATE,
    created_by_user_id INT,
    FOREIGN KEY (book_id) REFERENCES book(book_id),
    FOREIGN KEY (user_id) REFERENCES app_user(user_id),
    FOREIGN KEY (created_by_user_id) REFERENCES app_user(user_id)
);

