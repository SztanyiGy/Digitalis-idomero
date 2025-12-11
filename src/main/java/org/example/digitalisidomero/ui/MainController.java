package org.example.digitalisidomero.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;
import org.example.digitalisidomero.service.CategoryService;
import org.example.digitalisidomero.service.StatisticsService;
import org.example.digitalisidomero.service.TrackingService;

import java.util.Map;

public class MainController {

    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;

    @FXML private Label statusLabel;
    @FXML private Label currentAppLabel;
    @FXML private Label currentAppCategoryLabel;
    @FXML private Label currentSessionTimeLabel;

    @FXML private Label todayTotalLabel;
    @FXML private Label todayWorkLabel;
    @FXML private Label todayStudyLabel;
    @FXML private Label todayEntertainmentLabel;
    @FXML private Label todaySocialMediaLabel;

    @FXML private ProgressBar todayProgressBar;

    private TrackingService trackingService;
    private StatisticsService statisticsService;
    private CategoryService categoryService;

    private Timeline updateTimeline;

    @FXML
    public void initialize() {
        // Service-ek inicializálása
        trackingService = new TrackingService();
        statisticsService = new StatisticsService();
        categoryService = new CategoryService();

        // Kezdő állapot beállítása
        updateButtonStates(false, false);
        updateCurrentAppDisplay();
        updateTodayStatistics();

        // Timeline létrehozása (1 másodpercenként frissít)
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateCurrentAppDisplay();
            updateTodayStatistics();
        }));
        updateTimeline.setCycleCount(Animation.INDEFINITE);
    }

    @FXML
    private void handleStart() {
        trackingService.startTracking();
        updateButtonStates(true, false);
        statusLabel.setText("Állapot: Fut ✓");
        statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

        // Timeline indítása
        updateTimeline.play();
    }

    @FXML
    private void handlePause() {
        if (trackingService.isPaused()) {
            // Folytatás
            trackingService.resumeTracking();
            pauseButton.setText("Szünet");
            statusLabel.setText("Állapot: Fut ✓");
            statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else {
            // Szüneteltetés
            trackingService.pauseTracking();
            pauseButton.setText("Folytatás");
            statusLabel.setText("Állapot: Szünetel ⏸");
            statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleStop() {
        trackingService.stopTracking();
        updateButtonStates(false, false);
        statusLabel.setText("Állapot: Leállítva ✗");
        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // Timeline leállítása
        updateTimeline.stop();

        // Kijelzők törlése
        currentAppLabel.setText("Nincs aktív követés");
        currentAppCategoryLabel.setText("");
        currentSessionTimeLabel.setText("");
    }

    /**
     * Gombok állapotának frissítése
     */
    private void updateButtonStates(boolean isRunning, boolean isPaused) {
        startButton.setDisable(isRunning);
        pauseButton.setDisable(!isRunning);
        stopButton.setDisable(!isRunning);

        if (!isRunning) {
            pauseButton.setText("Szünet");
        }
    }

    @FXML
    private void handleOpenSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/digitalisidomero/settings-view.fxml"));
            Scene scene = new Scene(loader.load(), 700, 600);

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Beállítások");
            settingsStage.setScene(scene);
            settingsStage.setResizable(false);
            settingsStage.show();

        } catch (Exception e) {
            System.err.println("✗ Beállítások ablak megnyitási hiba: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleOpenStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/digitalisidomero/statistics-view.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);

            Stage statisticsStage = new Stage();
            statisticsStage.setTitle("Részletes statisztikák");
            statisticsStage.setScene(scene);
            statisticsStage.show();

        } catch (Exception e) {
            System.err.println("✗ Statisztikák ablak megnyitási hiba: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Aktuális alkalmazás megjelenítésének frissítése
     */
    private void updateCurrentAppDisplay() {
        if (!trackingService.isTracking()) {
            return;
        }

        Application currentApp = trackingService.getCurrentApplication();
        String processName = trackingService.getCurrentProcessName();
        long sessionDuration = trackingService.getCurrentSessionDuration();

        if (currentApp != null) {
            currentAppLabel.setText(currentApp.getDisplayName());

            Category category = currentApp.getCategory();
            String categoryIcon = CategoryService.getCategoryIcon(category);
            currentAppCategoryLabel.setText(categoryIcon + " " + category.getDisplayName());
            currentAppCategoryLabel.setStyle("-fx-text-fill: " + CategoryService.getCategoryColor(category) + ";");

            currentSessionTimeLabel.setText(StatisticsService.formatDurationDetailed(sessionDuration));
        } else if (processName != null) {
            currentAppLabel.setText(processName);
            currentAppCategoryLabel.setText("⏳ Betöltés...");
            currentSessionTimeLabel.setText("");
        }
    }

    /**
     * Mai statisztikák frissítése
     */
    private void updateTodayStatistics() {
        // Összes idő
        long totalSeconds = statisticsService.getTodayTotalSeconds();
        todayTotalLabel.setText(StatisticsService.formatDuration(totalSeconds));

        // Kategóriánkénti bontás
        Map<Category, Long> breakdown = statisticsService.getTodayCategoryBreakdown();

        todayWorkLabel.setText(StatisticsService.formatDuration(breakdown.get(Category.WORK)));
        todayStudyLabel.setText(StatisticsService.formatDuration(breakdown.get(Category.STUDY)));
        todayEntertainmentLabel.setText(StatisticsService.formatDuration(breakdown.get(Category.ENTERTAINMENT)));
        todaySocialMediaLabel.setText(StatisticsService.formatDuration(breakdown.get(Category.SOCIAL_MEDIA)));

        // Progress bar (8 órás nap = 100%)
        double progress = totalSeconds / (8.0 * 3600.0);
        todayProgressBar.setProgress(Math.min(progress, 1.0));

        // Progress bar színezése
        if (progress < 0.5) {
            todayProgressBar.setStyle("-fx-accent: #2ecc71;"); // Zöld
        } else if (progress < 0.8) {
            todayProgressBar.setStyle("-fx-accent: #f39c12;"); // Narancssárga
        } else {
            todayProgressBar.setStyle("-fx-accent: #e74c3c;"); // Piros
        }
    }
}