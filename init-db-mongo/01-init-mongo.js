// MongoDB Initialization Script for Books Database
// This runs automatically when the container starts for the first time

// Switch to booksdb
db = db.getSiblingDB('booksdb');

// Create application user
db.createUser({
  user: 'appuser',
  pwd: 'apppass',
  roles: [{ role: 'readWrite', db: 'booksdb' }]
});

// ============ USERS ============
db.app_user.insertMany([
  { user_id: 1, username: 'admin', password_hash: 'admin123' },
  { user_id: 2, username: 'bookworm', password_hash: 'reader456' },
  { user_id: 3, username: 'reviewer', password_hash: 'review789' }
]);

// ============ AUTHORS ============
db.author.insertMany([
  { author_id: 1, name: 'Joshua Bloch', birth_date: new Date('1961-08-28') },
  { author_id: 2, name: 'Robert C. Martin', birth_date: new Date('1952-12-05') },
  { author_id: 3, name: 'Martin Fowler', birth_date: new Date('1963-12-18') },
  { author_id: 4, name: 'Brian Kernighan', birth_date: new Date('1942-01-01') },
  { author_id: 5, name: 'Dennis Ritchie', birth_date: new Date('1941-09-09') },
  { author_id: 6, name: 'Donald Knuth', birth_date: new Date('1938-01-10') },
  { author_id: 7, name: 'Erich Gamma', birth_date: new Date('1961-03-13') },
  { author_id: 8, name: 'Gang of Four', birth_date: null }
]);

// ============ GENRES ============
db.genre.insertMany([
  { genre_id: 1, name: 'Programming' },
  { genre_id: 2, name: 'Software Engineering' },
  { genre_id: 3, name: 'Computer Science' },
  { genre_id: 4, name: 'Java' },
  { genre_id: 5, name: 'Design Patterns' },
  { genre_id: 6, name: 'Algorithms' },
  { genre_id: 7, name: 'C Programming' }
]);

// ============ BOOKS (with embedded data) ============
db.book.insertMany([
  {
    book_id: 1,
    isbn: '978-0-13-468599-1',
    title: 'Effective Java',
    published: new Date('2018-01-06'),
    created_by: { user_id: 1, username: 'admin' },
    authors: [
      { author_id: 1, name: 'Joshua Bloch', birth_date: new Date('1961-08-28') }
    ],
    genres: [
      { genre_id: 1, name: 'Programming' },
      { genre_id: 4, name: 'Java' }
    ],
    ratings: [
      { user_id: 1, rating: 5, rated_at: new Date('2024-01-15') },
      { user_id: 2, rating: 5, rated_at: new Date('2024-02-20') },
      { user_id: 3, rating: 4, rated_at: new Date('2024-03-10') }
    ],
    average_rating: 4.67,
    reviews: [
      {
        review_id: 1,
        user_id: 2,
        username: 'bookworm',
        review_text: 'An essential read for any Java developer. The best practices in this book have improved my code quality significantly.',
        review_date: new Date('2024-02-20')
      },
      {
        review_id: 2,
        user_id: 3,
        username: 'reviewer',
        review_text: 'Clear, concise, and packed with practical advice. Every item is a gem.',
        review_date: new Date('2024-03-10')
      }
    ]
  },
  {
    book_id: 2,
    isbn: '978-0-13-235088-4',
    title: 'Clean Code',
    published: new Date('2008-08-01'),
    created_by: { user_id: 1, username: 'admin' },
    authors: [
      { author_id: 2, name: 'Robert C. Martin', birth_date: new Date('1952-12-05') }
    ],
    genres: [
      { genre_id: 1, name: 'Programming' },
      { genre_id: 2, name: 'Software Engineering' }
    ],
    ratings: [
      { user_id: 1, rating: 5, rated_at: new Date('2024-01-20') },
      { user_id: 2, rating: 4, rated_at: new Date('2024-02-25') }
    ],
    average_rating: 4.5,
    reviews: [
      {
        review_id: 3,
        user_id: 1,
        username: 'admin',
        review_text: 'Changed how I think about writing code. The principles here apply to any language.',
        review_date: new Date('2024-01-20')
      },
      {
        review_id: 4,
        user_id: 2,
        username: 'bookworm',
        review_text: 'A must-read for professional developers. The chapter on naming conventions alone is worth the price.',
        review_date: new Date('2024-02-25')
      }
    ]
  },
  {
    book_id: 3,
    isbn: '978-0-20-161622-4',
    title: 'The Pragmatic Programmer',
    published: new Date('1999-10-20'),
    created_by: { user_id: 1, username: 'admin' },
    authors: [],
    genres: [
      { genre_id: 1, name: 'Programming' },
      { genre_id: 2, name: 'Software Engineering' }
    ],
    ratings: [
      { user_id: 2, rating: 5, rated_at: new Date('2024-03-01') }
    ],
    average_rating: 5.0,
    reviews: []
  },
  {
    book_id: 4,
    isbn: '978-0-13-110362-7',
    title: 'The C Programming Language',
    published: new Date('1988-04-01'),
    created_by: { user_id: 2, username: 'bookworm' },
    authors: [
      { author_id: 4, name: 'Brian Kernighan', birth_date: new Date('1942-01-01') },
      { author_id: 5, name: 'Dennis Ritchie', birth_date: new Date('1941-09-09') }
    ],
    genres: [
      { genre_id: 1, name: 'Programming' },
      { genre_id: 7, name: 'C Programming' }
    ],
    ratings: [
      { user_id: 1, rating: 5, rated_at: new Date('2024-01-25') }
    ],
    average_rating: 5.0,
    reviews: [
      {
        review_id: 5,
        user_id: 1,
        username: 'admin',
        review_text: 'The classic that started it all. Still relevant after all these years.',
        review_date: new Date('2024-01-25')
      }
    ]
  },
  {
    book_id: 5,
    isbn: '978-0-20-163361-0',
    title: 'Design Patterns',
    published: new Date('1994-10-31'),
    created_by: { user_id: 2, username: 'bookworm' },
    authors: [
      { author_id: 7, name: 'Erich Gamma', birth_date: new Date('1961-03-13') }
    ],
    genres: [
      { genre_id: 1, name: 'Programming' },
      { genre_id: 5, name: 'Design Patterns' }
    ],
    ratings: [
      { user_id: 3, rating: 5, rated_at: new Date('2024-02-15') }
    ],
    average_rating: 5.0,
    reviews: [
      {
        review_id: 6,
        user_id: 3,
        username: 'reviewer',
        review_text: 'The design patterns in this book are timeless. Essential knowledge for OOP developers.',
        review_date: new Date('2024-02-15')
      }
    ]
  },
  {
    book_id: 6,
    isbn: '978-0-20-189683-1',
    title: 'The Art of Computer Programming',
    published: new Date('1968-01-01'),
    created_by: { user_id: 1, username: 'admin' },
    authors: [
      { author_id: 6, name: 'Donald Knuth', birth_date: new Date('1938-01-10') }
    ],
    genres: [
      { genre_id: 3, name: 'Computer Science' },
      { genre_id: 6, name: 'Algorithms' }
    ],
    ratings: [
      { user_id: 1, rating: 5, rated_at: new Date('2024-03-05') }
    ],
    average_rating: 5.0,
    reviews: []
  }
]);

// ============ COUNTER (for auto-increment IDs) ============
db.counter.insertOne({
  _id: 'counters',
  next_book_id: 7,
  next_review_id: 7
});

// Create indexes for better query performance
db.book.createIndex({ isbn: 1 }, { unique: true });
db.book.createIndex({ title: 'text' });
db.book.createIndex({ 'authors.name': 1 });
db.book.createIndex({ 'genres.name': 1 });
db.author.createIndex({ author_id: 1 }, { unique: true });
db.genre.createIndex({ genre_id: 1 }, { unique: true });
db.app_user.createIndex({ user_id: 1 }, { unique: true });
db.app_user.createIndex({ username: 1 }, { unique: true });

print('✅ MongoDB initialization complete! Inserted 6 books, 8 authors, 7 genres, 3 users.');

