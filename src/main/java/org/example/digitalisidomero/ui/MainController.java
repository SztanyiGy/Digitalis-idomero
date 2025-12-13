package org.example.digitalisidomero.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;
import org.example.digitalisidomero.service.CategoryService;
import org.example.digitalisidomero.service.StatisticsService;
import org.example.digitalisidomero.service.TrackingService;

import java.io.IOException;
import java.util.Map;

public class MainController {

    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;

    // Navigáció
    @FXML private Button navHomeButton;
    @FXML private Button navStatsButton;
    @FXML private Button navSettingsButton;

    // Nézetek
    @FXML private ScrollPane homeViewScroll;
    @FXML private VBox homeView;
    @FXML private VBox statisticsView;
    @FXML private VBox settingsView;

    @FXML private Label statusLabel;
    @FXML private Label currentAppLabel;
    @FXML private Label currentAppCategoryLabel;
    @FXML private Label currentSessionTimeLabel;

    @FXML private Label todayTotalLabel;
    @FXML private Label todayWorkLabel;
    @FXML private Label todayEntertainmentLabel;

    private TrackingService trackingService;
    private StatisticsService statisticsService;
    private CategoryService categoryService;

    private Timeline updateTimeline;

    private boolean statisticsViewLoaded = false;

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

        // Navigáció - Főoldal aktív alapból
        showHomePage();
    }

    // ===== NAVIGÁCIÓ =====

    @FXML
    private void showHomePage() {
        homeViewScroll.setVisible(true);
        homeViewScroll.setManaged(true);
        statisticsView.setVisible(false);
        statisticsView.setManaged(false);
        settingsView.setVisible(false);
        settingsView.setManaged(false);

        // Aktív gomb kiemelése
        highlightNavButton(navHomeButton);
    }

    @FXML
    private void showStatisticsPage() {
        homeViewScroll.setVisible(false);
        homeViewScroll.setManaged(false);
        settingsView.setVisible(false);
        settingsView.setManaged(false);

        // Statisztika nézet betöltése (csak egyszer)
        if (!statisticsViewLoaded) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/digitalisidomero/statistics-view.fxml")
                );
                // A statistics-view.fxml gyökér eleme ScrollPane, azt töltjük be
                ScrollPane statsContent = loader.load();
                statisticsView.getChildren().add(statsContent);
                statisticsViewLoaded = true;

                System.out.println("✓ Statisztika nézet betöltve");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("✗ Hiba a statisztika nézet betöltésekor: " + e.getMessage());

                // Fallback - hibaüzenet megjelenítése
                Label errorLabel = new Label("⚠️ Nem sikerült betölteni a statisztika nézetet");
                errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
                statisticsView.getChildren().add(errorLabel);
            }
        }

        statisticsView.setVisible(true);
        statisticsView.setManaged(true);

        highlightNavButton(navStatsButton);
    }

    @FXML
    private void showSettingsPage() {
        homeViewScroll.setVisible(false);
        homeViewScroll.setManaged(false);
        statisticsView.setVisible(false);
        statisticsView.setManaged(false);
        settingsView.setVisible(true);
        settingsView.setManaged(true);

        highlightNavButton(navSettingsButton);
    }

    private void highlightNavButton(Button activeButton) {
        // Reset all buttons - CSS osztályok használata
        navHomeButton.getStyleClass().remove("nav-button-active");
        navStatsButton.getStyleClass().remove("nav-button-active");
        navSettingsButton.getStyleClass().remove("nav-button-active");

        // Highlight active
        if (!activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    private void handleStart() {
        trackingService.startTracking();
        updateButtonStates(true, false);
        statusLabel.setText("Fut ✓");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2ecc71; -fx-font-weight: 600;");

        // Timeline indítása
        updateTimeline.play();
    }

    @FXML
    private void handlePause() {
        if (trackingService.isPaused()) {
            // Folytatás
            trackingService.resumeTracking();
            pauseButton.setText("⏸  Szünet");
            statusLabel.setText("Fut ✓");
            statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2ecc71; -fx-font-weight: 600;");
        } else {
            // Szüneteltetés
            trackingService.pauseTracking();
            pauseButton.setText("▶  Folytatás");
            statusLabel.setText("Szünetel ⏸");
            statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f39c12; -fx-font-weight: 600;");
        }
    }

    @FXML
    private void handleStop() {
        trackingService.stopTracking();
        updateButtonStates(false, false);
        statusLabel.setText("Leállítva ✗");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c; -fx-font-weight: 600;");

        // Timeline leállítása
        updateTimeline.stop();

        // Kijelzők törlése
        currentAppLabel.setText("Nincs aktív követés");
        currentAppCategoryLabel.setText("");
        currentSessionTimeLabel.setText("00:00:00");
    }

    /**
     * Gombok állapotának frissítése
     */
    private void updateButtonStates(boolean isRunning, boolean isPaused) {
        startButton.setDisable(isRunning);
        pauseButton.setDisable(!isRunning);
        stopButton.setDisable(!isRunning);

        if (!isRunning) {
            pauseButton.setText("⏸  Szünet");
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
            currentSessionTimeLabel.setText(StatisticsService.formatDurationDetailed(sessionDuration));
        }
    }

    /**
     * Mai statisztikák frissítése (egyszerűsített - csak számok)
     */
    private void updateTodayStatistics() {
        // Összes idő
        long totalSeconds = statisticsService.getTodayTotalSeconds();
        todayTotalLabel.setText(StatisticsService.formatDuration(totalSeconds));

        // Kategóriánkénti bontás
        Map<Category, Long> breakdown = statisticsService.getTodayCategoryBreakdown();

        todayWorkLabel.setText(StatisticsService.formatDuration(breakdown.get(Category.WORK)));
        todayEntertainmentLabel.setText(StatisticsService.formatDuration(breakdown.get(Category.ENTERTAINMENT)));
    }
}