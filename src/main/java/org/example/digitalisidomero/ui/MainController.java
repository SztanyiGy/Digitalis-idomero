package org.example.digitalisidomero.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
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
    @FXML private Button navStopperButton;

    // Nézetek
    @FXML private ScrollPane homeViewScroll;
    @FXML private VBox homeView;
    @FXML private VBox statisticsView;
    @FXML private VBox stopperView;

    @FXML private Label statusLabel;
    @FXML private Label currentAppLabel;
    @FXML private Label currentAppCategoryLabel;
    @FXML private Label currentSessionTimeLabel;

    @FXML private Label todayTotalLabel;
    @FXML private Label todayWorkLabel;
    @FXML private Label todayEntertainmentLabel;

    // Kategória választás gombok
    @FXML private ToggleButton workCategoryToggle;
    @FXML private ToggleButton studyCategoryToggle;
    @FXML private ToggleButton entertainmentCategoryToggle;

    private TrackingService trackingService;
    private StatisticsService statisticsService;
    private CategoryService categoryService;

    private Timeline updateTimeline;

    private boolean statisticsViewLoaded = false;
    private boolean stopperViewLoaded = false;

    private ToggleGroup categoryToggleGroup;
    private Category selectedCategory = null;

    @FXML
    public void initialize() {
        // Service-ek inicializálása
        trackingService = new TrackingService();
        statisticsService = new StatisticsService();
        categoryService = new CategoryService();

        // Kategória toggle group beállítása
        setupCategoryToggleGroup();

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

    // ===== KATEGÓRIA KEZELÉS =====

    private void setupCategoryToggleGroup() {
        categoryToggleGroup = new ToggleGroup();
        workCategoryToggle.setToggleGroup(categoryToggleGroup);
        studyCategoryToggle.setToggleGroup(categoryToggleGroup);
        entertainmentCategoryToggle.setToggleGroup(categoryToggleGroup);

        // Listener a kategória változáshoz
        categoryToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                updateSelectedCategory();
            }
        });
    }

    private void updateSelectedCategory() {
        if (workCategoryToggle.isSelected()) {
            selectedCategory = Category.WORK;
            System.out.println("DEBUG: Munka kategória kiválasztva");
        } else if (studyCategoryToggle.isSelected()) {
            selectedCategory = Category.STUDY; // Ha nincs külön STUDY kategóriád
            System.out.println("DEBUG: Tanulás kategória kiválasztva");
        } else if (entertainmentCategoryToggle.isSelected()) {
            selectedCategory = Category.ENTERTAINMENT;
            System.out.println("DEBUG: Szórakozás kategória kiválasztva");
        }
    }

    // ===== NAVIGÁCIÓ =====

    @FXML
    private void showHomePage() {
        homeViewScroll.setVisible(true);
        homeViewScroll.setManaged(true);
        statisticsView.setVisible(false);
        statisticsView.setManaged(false);
        stopperView.setVisible(false);
        stopperView.setManaged(false);

        // Aktív gomb kiemelése
        highlightNavButton(navHomeButton);
    }

    @FXML
    private void showStatisticsPage() {
        homeViewScroll.setVisible(false);
        homeViewScroll.setManaged(false);
        stopperView.setVisible(false);
        stopperView.setManaged(false);

        // Statisztika nézet betöltése (csak egyszer)
        if (!statisticsViewLoaded) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/digitalisidomero/statistics-view.fxml")
                );
                ScrollPane statsContent = loader.load();
                statisticsView.getChildren().add(statsContent);
                statisticsViewLoaded = true;

                System.out.println("✓ Statisztika nézet betöltve");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("✗ Hiba a statisztika nézet betöltésekor: " + e.getMessage());

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
    private void showStopperPage() {
        homeViewScroll.setVisible(false);
        homeViewScroll.setManaged(false);
        statisticsView.setVisible(false);
        statisticsView.setManaged(false);


        // Stopper nézet betöltése (csak egyszer)
        if (!stopperViewLoaded) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/digitalisidomero/timer-view.fxml")
                );
                Node stopperContent = loader.load();
                stopperView.getChildren().add(stopperContent);
                stopperViewLoaded = true;

                System.out.println("✓ Stopper nézet betöltve");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("✗ Hiba a stopper nézet betöltésekor: " + e.getMessage());

                Label errorLabel = new Label("⚠️ Stopper hiba: " + e.getMessage());
                errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
                stopperView.getChildren().add(errorLabel);
            }
        }

        stopperView.setVisible(true);
        stopperView.setManaged(true);

        highlightNavButton(navStopperButton);
    }



    private void highlightNavButton(Button activeButton) {
        navHomeButton.getStyleClass().remove("nav-button-active");
        navStatsButton.getStyleClass().remove("nav-button-active");
        navStopperButton.getStyleClass().remove("nav-button-active");

        if (!activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    // ===== TRACKING VEZÉRLÉS =====

    @FXML
    private void handleStart() {
        System.out.println("DEBUG: selectedCategory = " + selectedCategory);

        // Ellenőrzés: van-e kiválasztva kategória
        if (selectedCategory == null) {
            showAlert("Kategória szükséges", "Kérlek válassz egy kategóriát a naplózás indítása előtt!");
            return;
        }

        System.out.println("DEBUG: setForcedCategory ELŐTT");

        // A kiválasztott kategória beállítása a MonitorService-ben
        trackingService.getMonitorService().setForcedCategory(selectedCategory);

        System.out.println("DEBUG: setForcedCategory UTÁN");

        trackingService.startTracking();

        updateButtonStates(true, false);

        // Kategória gombok letiltása futás közben
        disableCategoryToggles(true);

        statusLabel.setText("Futás...");
        statusLabel.getStyleClass().removeAll("status-stopped", "status-paused");
        statusLabel.getStyleClass().add("status-running");

        // Timeline indítása
        updateTimeline.play();

        System.out.println("✓ Naplózás elindítva - Kényszerített kategória: " + selectedCategory);
    }

    @FXML
    private void handlePause() {
        if (trackingService.isPaused()) {
            // Folytatás
            trackingService.resumeTracking();
            pauseButton.setText("⏸  Szünet");
            statusLabel.setText("Futás...");
            statusLabel.getStyleClass().removeAll("status-stopped", "status-paused");
            statusLabel.getStyleClass().add("status-running");
        } else {
            // Szüneteltetés
            trackingService.pauseTracking();
            pauseButton.setText("▶  Folytatás");
            statusLabel.setText("Szüneteltetve");
            statusLabel.getStyleClass().removeAll("status-running", "status-stopped");
            statusLabel.getStyleClass().add("status-paused");
        }
    }

    @FXML
    private void handleStop() {
        // Kényszerített kategória törlése
        trackingService.getMonitorService().clearForcedCategory();

        trackingService.stopTracking();
        updateButtonStates(false, false);

        // Kategória gombok újra engedélyezése
        disableCategoryToggles(false);

        statusLabel.setText("Leállítva");
        statusLabel.getStyleClass().removeAll("status-running", "status-paused");
        statusLabel.getStyleClass().add("status-stopped");

        // Timeline leállítása
        updateTimeline.stop();

        // Kijelzők törlése
        currentAppLabel.setText("Nincs aktív követés");
        currentAppCategoryLabel.setText("");
        currentSessionTimeLabel.setText("00:00:00");

        System.out.println("✓ Naplózás leállítva");
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
     * Kategória gombok engedélyezése/letiltása
     */
    private void disableCategoryToggles(boolean disable) {
        workCategoryToggle.setDisable(disable);
        studyCategoryToggle.setDisable(disable);
        entertainmentCategoryToggle.setDisable(disable);
    }

    /**
     * Figyelmeztető ablak megjelenítése
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
     * Mai statisztikák frissítése
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