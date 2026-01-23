package com.nawidali.sql_labb_2.config;

import com.nawidali.sql_labb_2.model.BooksDbMongo;
import com.nawidali.sql_labb_2.model.BooksDbMySql;
import com.nawidali.sql_labb_2.model.IBooksDb;
import com.nawidali.sql_labb_2.model.exceptions.ConnectionException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for database selection.
 * 
 * Design decision: Use @ConditionalOnProperty to select between MySQL and MongoDB
 * implementations based on application.properties. This keeps the REST layer
 * decoupled from concrete database implementations.
 * 
 * Set books.db.type=mysql or books.db.type=mongo in application.properties.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${books.db.url}")
    private String databaseUrl;

    /**
     * MySQL implementation bean.
     * Created only when books.db.type=mysql.
     */
    @Bean
    @ConditionalOnProperty(name = "books.db.type", havingValue = "mysql")
    public IBooksDb mysqlDatabase() {
        log.info("Initializing MySQL database connection");
        return new BooksDbMySql();
    }

    /**
     * MongoDB implementation bean.
     * Created only when books.db.type=mongo.
     */
    @Bean
    @ConditionalOnProperty(name = "books.db.type", havingValue = "mongo")
    public IBooksDb mongoDatabase() {
        log.info("Initializing MongoDB database connection");
        return new BooksDbMongo();
    }

    /**
     * Wrapper that manages the connection lifecycle.
     * Connects on startup and disconnects on shutdown.
     */
    @Bean
    public DatabaseConnectionManager connectionManager(IBooksDb database) {
        return new DatabaseConnectionManager(database, databaseUrl);
    }

    /**
     * Inner class to handle connection lifecycle with Spring's @PostConstruct/@PreDestroy.
     */
    public static class DatabaseConnectionManager {

        private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionManager.class);
        private final IBooksDb database;
        private final String databaseUrl;

        public DatabaseConnectionManager(IBooksDb database, String databaseUrl) {
            this.database = database;
            this.databaseUrl = databaseUrl;
        }

        @PostConstruct
        public void connect() throws ConnectionException {
            log.info("Connecting to database...");
            database.connect(databaseUrl);
            log.info("Database connection established");
        }

        @PreDestroy
        public void disconnect() {
            log.info("Disconnecting from database...");
            try {
                database.disconnect();
                log.info("Database disconnected");
            } catch (ConnectionException e) {
                log.warn("Error during disconnect: {}", e.getMessage());
            }
        }
    }
}
