# Books Backend Service

REST API for book database with support for MySQL and MongoDB.

## Requirements

- Java 21
- MySQL or MongoDB

## Quick Start

```bash
# Run with Maven
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build JAR
./mvnw package
```

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Use mysql or mongo
books.db.type=mysql

# MySQL
books.db.url=jdbc:mysql://localhost:3306/booksdb?user=root&password=root

# MongoDB
books.db.url=mongodb://user:pass@localhost:27017/booksdb?authSource=booksdb
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/books?title={title}` | Search books by title |
| GET | `/books/{isbn}` | Get book by ISBN |
| POST | `/books/{bookId}/rating` | Rate a book |
| GET | `/books/{bookId}/reviews` | Get book reviews |

### Examples

```bash
# Search books
curl "http://localhost:8080/books?title=java"

# Get by ISBN
curl "http://localhost:8080/books/978-0-13-468599-1"

# Rate a book
curl -X POST "http://localhost:8080/books/1/rating" \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "rating": 5}'

# Get reviews
curl "http://localhost:8080/books/1/reviews"
```

## Docker

```bash
# Build
docker build -t books-backend-service .

# Run
docker run -p 8080:8080 \
  -e BOOKS_DB_TYPE=mysql \
  -e BOOKS_DB_URL="jdbc:mysql://host.docker.internal:3306/booksdb?user=root&password=root" \
  books-backend-service
```

## Architecture

```
BookController (REST) --> IBooksDb (interface) <-- BooksDbMySql / BooksDbMongo
```

The REST layer depends only on the `IBooksDb` interface. Database implementation is selected via configuration.
