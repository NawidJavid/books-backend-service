-- Sample Data for Books Database

-- Users (password is stored as plain text for demo - in production use proper hashing)
INSERT INTO app_user (username, password_hash) VALUES 
('admin', 'admin123'),
('bookworm', 'reader456'),
('reviewer', 'review789');

-- Authors
INSERT INTO author (name, birth_date) VALUES 
('Joshua Bloch', '1961-08-28'),
('Robert C. Martin', '1952-12-05'),
('Martin Fowler', '1963-12-18'),
('Brian Kernighan', '1942-01-01'),
('Dennis Ritchie', '1941-09-09'),
('Donald Knuth', '1938-01-10'),
('Erich Gamma', '1961-03-13'),
('Gang of Four', NULL);

-- Genres
INSERT INTO genre (name) VALUES 
('Programming'),
('Software Engineering'),
('Computer Science'),
('Java'),
('Design Patterns'),
('Algorithms'),
('C Programming');

-- Books
INSERT INTO book (isbn, title, published, created_by_user_id) VALUES 
('978-0-13-468599-1', 'Effective Java', '2018-01-06', 1),
('978-0-13-235088-4', 'Clean Code', '2008-08-01', 1),
('978-0-20-161622-4', 'The Pragmatic Programmer', '1999-10-20', 1),
('978-0-13-110362-7', 'The C Programming Language', '1988-04-01', 2),
('978-0-20-163361-0', 'Design Patterns', '1994-10-31', 2),
('978-0-20-189683-1', 'The Art of Computer Programming', '1968-01-01', 1);

-- Book-Author relationships
INSERT INTO book_author (book_id, author_id, created_by_user_id) VALUES 
(1, 1, 1),  -- Effective Java - Joshua Bloch
(2, 2, 1),  -- Clean Code - Robert C. Martin
(4, 4, 2),  -- C Programming - Brian Kernighan
(4, 5, 2),  -- C Programming - Dennis Ritchie
(5, 7, 2),  -- Design Patterns - Erich Gamma
(6, 6, 1);  -- Art of Computer Programming - Donald Knuth

-- Book-Genre relationships
INSERT INTO book_genre (book_id, genre_id) VALUES 
(1, 1), (1, 4),        -- Effective Java: Programming, Java
(2, 1), (2, 2),        -- Clean Code: Programming, Software Engineering
(3, 1), (3, 2),        -- Pragmatic Programmer: Programming, Software Engineering
(4, 1), (4, 7),        -- C Programming: Programming, C Programming
(5, 1), (5, 5),        -- Design Patterns: Programming, Design Patterns
(6, 3), (6, 6);        -- Art of Computer Programming: Computer Science, Algorithms

-- Ratings
INSERT INTO rating (book_id, user_id, rating, rated_at) VALUES 
(1, 1, 5, '2024-01-15'),
(1, 2, 5, '2024-02-20'),
(1, 3, 4, '2024-03-10'),
(2, 1, 5, '2024-01-20'),
(2, 2, 4, '2024-02-25'),
(3, 2, 5, '2024-03-01'),
(4, 1, 5, '2024-01-25'),
(5, 3, 5, '2024-02-15'),
(6, 1, 5, '2024-03-05');

-- Reviews
INSERT INTO review (book_id, user_id, review_text, review_date, created_by_user_id) VALUES 
(1, 2, 'An essential read for any Java developer. The best practices in this book have improved my code quality significantly.', '2024-02-20', 2),
(1, 3, 'Clear, concise, and packed with practical advice. Every item is a gem.', '2024-03-10', 3),
(2, 1, 'Changed how I think about writing code. The principles here apply to any language.', '2024-01-20', 1),
(2, 2, 'A must-read for professional developers. The chapter on naming conventions alone is worth the price.', '2024-02-25', 2),
(4, 1, 'The classic that started it all. Still relevant after all these years.', '2024-01-25', 1),
(5, 3, 'The design patterns in this book are timeless. Essential knowledge for OOP developers.', '2024-02-15', 3);

