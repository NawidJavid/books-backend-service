module com.nawidali.sql_labb_2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;

    opens com.nawidali.sql_labb_2 to javafx.base;
    opens com.nawidali.sql_labb_2.model to javafx.base;

    exports com.nawidali.sql_labb_2;
}
