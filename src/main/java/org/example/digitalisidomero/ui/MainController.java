package org.example.digitalisidomero.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
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

    // Navigáció
    @FXML private Button navHomeButton;
    @FXML private Button navStatsButton;
    @FXML private Button navSettingsButton;

    // Nézetek
    @FXML private VBox homeView;
    @FXML private Label statisticsView;
    @FXML private Label settingsView;

    @FXML private Label statusLabel;
    @FXML private Label currentAppLabel;
    @FXML private Label currentAppCategoryLabel;
    @FXML private Label currentSessionTimeLabel;

    @FXML private Label todayTotalLabel;
    @FXML private Label todayWorkLabel;
    @FXML private Label todayStudyLabel;
    @FXML private Label todayEntertainmentLabel;
    @FXML private Label todaySocialMediaLabel;

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

        // Navigáció - Főoldal aktív alapból
        showHomePage();
    }

    // ===== NAVIGÁCIÓ =====

    @FXML
    private void showHomePage() {
        homeView.setVisible(true);
        homeView.setManaged(true);
        statisticsView.setVisible(false);
        statisticsView.setManaged(false);
        settingsView.setVisible(false);
        settingsView.setManaged(false);

        // Aktív gomb kiemelése
        highlightNavButton(navHomeButton);
    }

    @FXML
    private void showStatisticsPage() {
        homeView.setVisible(false);
        homeView.setManaged(false);
        statisticsView.setVisible(true);
        statisticsView.setManaged(true);
        settingsView.setVisible(false);
        settingsView.setManaged(false);

        highlightNavButton(navStatsButton);

        // Itt majd betöltjük a statisztikákat (később)
        updateTodayStatistics();
    }

    @FXML
    private void showSettingsPage() {
        homeView.setVisible(false);
        homeView.setManaged(false);
        statisticsView.setVisible(false);
        statisticsView.setManaged(false);
        settingsView.setVisible(true);
        settingsView.setManaged(true);

        highlightNavButton(navSettingsButton);

        // Itt majd betöltjük a beállításokat (később)
        handlePause();
    }

    private void highlightNavButton(Button activeButton) {
        // Reset all buttons
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 15 10; -fx-background-radius: 10; -fx-cursor: hand;";
        String activeStyle = "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 15 10; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2; -fx-border-radius: 10;";

        navHomeButton.setStyle(inactiveStyle);
        navStatsButton.setStyle(inactiveStyle);
        navSettingsButton.setStyle(inactiveStyle);

        // Highlight active
        activeButton.setStyle(activeStyle);
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
     * Mai statisztikák frissítése (egyszerűsített - csak számok)
     */
    private void updateTodayStatistics() {
        // Összes idő
        long totalSeconds = statisticsService.getTodayTotalSeconds();
        todayTotalLabel.setText(StatisticsService.formatDuration(totalSeconds));

        // Kategóriánkénti bontás
        Map<Category, Long> breakdown = statisticsService.getTodayCategoryBreakdown();

        todayWorkLabel.setText(StatisticsService.formatDuration(breakdown.get(Category.WORK)));
        //todayStudyLabel.setText(StatisticsService.formatDuration(breakdown.get(Category.STUDY)));
        todayEntertainmentLabel.setText(StatisticsService.formatDuration(breakdown.get(Category.ENTERTAINMENT)));
        //todaySocialMediaLabel.setText(StatisticsService.formatDuration(breakdown.get(Category.SOCIAL_MEDIA)));
    }
}