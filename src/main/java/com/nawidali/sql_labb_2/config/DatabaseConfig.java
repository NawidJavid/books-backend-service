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
 * Selects database implementation based on books.db.type property.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${books.db.url}")
    private String databaseUrl;

    @Bean
    @ConditionalOnProperty(name = "books.db.type", havingValue = "mysql")
    public IBooksDb mysqlDatabase() {
        log.info("Using MySQL database");
        return new BooksDbMySql();
    }

    @Bean
    @ConditionalOnProperty(name = "books.db.type", havingValue = "mongo")
    public IBooksDb mongoDatabase() {
        log.info("Using MongoDB database");
        return new BooksDbMongo();
    }

    @Bean
    public DatabaseConnectionManager connectionManager(IBooksDb database) {
        return new DatabaseConnectionManager(database, databaseUrl);
    }

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
            log.info("Database connected");
        }

        @PreDestroy
        public void disconnect() {
            log.info("Disconnecting from database...");
            try {
                database.disconnect();
            } catch (ConnectionException e) {
                log.warn("Disconnect error: {}", e.getMessage());
            }
        }
    }
}
