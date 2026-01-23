package com.nawidali.sql_labb_2;

import com.nawidali.sql_labb_2.model.BooksDbMongo;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.nawidali.sql_labb_2.model.IBooksDb;
import com.nawidali.sql_labb_2.view.BooksPane;


import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        IBooksDb booksDb = new BooksDbMongo(); // model
        BooksPane booksPane = new BooksPane(booksDb); // also creates a controller
        // Don't forget to connect to the db, somewhere...

        Scene scene = new Scene(booksPane, 800, 600);
        primaryStage.setTitle("Books Database Client");
        // add an exit handler to the stage (X)
        primaryStage.setOnCloseRequest(event -> {
            try {
                booksDb.disconnect();
            } catch (Exception e) {
            }
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}