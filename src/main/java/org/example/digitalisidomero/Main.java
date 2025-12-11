package org.example.digitalisidomero;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.digitalisidomero.database.DatabaseInitializer;
import org.example.digitalisidomero.monitor.MonitorService;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Adatbázis inicializálás
        DatabaseInitializer.initializeDatabase();

        // FXML betöltés - ÚJ main-view.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/org/example/digitalisidomero/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 800);

        stage.setTitle("Digitális Időmérő - Time Tracker");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Alkalmazás bezáráskor
        stage.setOnCloseRequest(event -> {
            System.out.println("✓ Alkalmazás bezárva");
        });
    }
    public static void main(String[] args) {
        launch();
    }
}