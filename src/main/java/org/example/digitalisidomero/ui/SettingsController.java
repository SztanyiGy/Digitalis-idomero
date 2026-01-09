package org.example.digitalisidomero.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SettingsController {

    @FXML private Spinner<Integer> hoursSpinner;
    @FXML private Spinner<Integer> minutesSpinner;
    @FXML private Spinner<Integer> secondsSpinner;
    @FXML private Label timerDisplayLabel;
    @FXML private Button startTimerButton;
    @FXML private Button pauseTimerButton;
    @FXML private Button stopTimerButton;
    @FXML private Label timerStatusLabel;
    @FXML private Label categoryWarningLabel;

    @FXML private ToggleButton workCategoryToggle;
    @FXML private ToggleButton studyCategoryToggle;
    @FXML private ToggleButton entertainmentCategoryToggle;

    private Timeline timeline;
    private int totalSeconds = 0;
    private int remainingSeconds = 0;
    private int elapsedSeconds = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;

    private ToggleGroup categoryToggleGroup;
    private String selectedCategory = null;
    private LocalDateTime sessionStartTime;

    @FXML
    public void initialize() {
        setupSpinners();
        setupCategoryToggleGroup();
        updateTimerDisplay();
    }

    private void setupSpinners() {
        SpinnerValueFactory<Integer> hoursFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0);
        hoursSpinner.setValueFactory(hoursFactory);
        hoursSpinner.setEditable(true);

        SpinnerValueFactory<Integer> minutesFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 25);
        minutesSpinner.setValueFactory(minutesFactory);
        minutesSpinner.setEditable(true);

        SpinnerValueFactory<Integer> secondsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        secondsSpinner.setValueFactory(secondsFactory);
        secondsSpinner.setEditable(true);

        hoursSpinner.valueProperty().addListener((obs, old, newVal) -> updateTotalSeconds());
        minutesSpinner.valueProperty().addListener((obs, old, newVal) -> updateTotalSeconds());
        secondsSpinner.valueProperty().addListener((obs, old, newVal) -> updateTotalSeconds());
    }

    private void setupCategoryToggleGroup() {
        categoryToggleGroup = new ToggleGroup();
        workCategoryToggle.setToggleGroup(categoryToggleGroup);
        studyCategoryToggle.setToggleGroup(categoryToggleGroup);
        entertainmentCategoryToggle.setToggleGroup(categoryToggleGroup);

        // Alapértelmezetten a tanulás legyen kiválasztva
        studyCategoryToggle.setSelected(true);
        selectedCategory = "Egyéb"; // Az adatbázisodban lévő kategória név

        categoryToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == workCategoryToggle) {
                selectedCategory = "Egyéb"; // Vagy "Munka" ha van ilyen kategóriád
            } else if (newToggle == studyCategoryToggle) {
                selectedCategory = "Egyéb"; // Tanuláshoz is használhatod az Egyéb-et
            } else if (newToggle == entertainmentCategoryToggle) {
                selectedCategory = "Szórakozás";
            }
            categoryWarningLabel.setVisible(false);
        });
    }

    private void updateTotalSeconds() {
        if (!isRunning) {
            totalSeconds = hoursSpinner.getValue() * 3600 +
                    minutesSpinner.getValue() * 60 +
                    secondsSpinner.getValue();
            remainingSeconds = totalSeconds;
            updateTimerDisplay();
        }
    }

    @FXML
    private void handleStartTimer() {
        if (categoryToggleGroup.getSelectedToggle() == null) {
            categoryWarningLabel.setText("⚠️ Kérlek válassz kategóriát!");
            categoryWarningLabel.setVisible(true);
            return;
        }

        if (totalSeconds == 0) {
            showAlert("Hiba", "Kérlek állíts be egy időtartamot!");
            return;
        }

        if (isPaused) {
            resumeTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        isRunning = true;
        isPaused = false;
        remainingSeconds = totalSeconds;
        elapsedSeconds = 0;
        sessionStartTime = LocalDateTime.now();

        startTimerButton.setDisable(true);
        pauseTimerButton.setDisable(false);
        stopTimerButton.setDisable(false);
        disableControls(true);

        timerStatusLabel.setText("Futás...");
        timerStatusLabel.getStyleClass().removeAll("status-stopped", "status-paused");
        timerStatusLabel.getStyleClass().add("status-running");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            elapsedSeconds++;
            updateTimerDisplay();

            if (remainingSeconds <= 0) {
                timerFinished();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void resumeTimer() {
        isPaused = false;
        isRunning = true;

        startTimerButton.setDisable(true);
        pauseTimerButton.setDisable(false);

        timerStatusLabel.setText("Futás...");
        timerStatusLabel.getStyleClass().removeAll("status-stopped", "status-paused");
        timerStatusLabel.getStyleClass().add("status-running");

        timeline.play();
    }

    @FXML
    private void handlePauseTimer() {
        if (timeline != null && isRunning) {
            timeline.pause();
            isPaused = true;
            isRunning = false;

            startTimerButton.setDisable(false);
            pauseTimerButton.setDisable(true);

            timerStatusLabel.setText("Szüneteltetve");
            timerStatusLabel.getStyleClass().removeAll("status-running", "status-stopped");
            timerStatusLabel.getStyleClass().add("status-paused");
        }
    }

    @FXML
    private void handleStopTimer() {
        if (timeline != null) {
            timeline.stop();
        }

        // Ha volt futás, mentsük el az időt
        if (elapsedSeconds > 0) {
            saveTimerSession();
        }

        resetTimerState();
    }

    private void timerFinished() {
        timeline.stop();

        // Teljes idő mentése
        saveTimerSession();

        resetTimerState();

        Platform.runLater(this::showTimerFinishedAlert);
        playNotificationSound();
    }

    private void saveTimerSession() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:time_tracker.db")) {
            String sql = "INSERT INTO daily_statistics (date, category, total_seconds) VALUES (?, ?, ?) " +
                    "ON CONFLICT(date, category) DO UPDATE SET total_seconds = total_seconds + ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setString(2, selectedCategory);
            pstmt.setInt(3, elapsedSeconds);
            pstmt.setInt(4, elapsedSeconds);
            pstmt.executeUpdate();

            System.out.println("✓ Időzítő munkamenet mentve: " + elapsedSeconds + " másodperc a(z) " + selectedCategory + " kategóriába");

        } catch (Exception e) {
            System.err.println("Hiba az időzítő munkamenet mentésekor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetTimerState() {
        isRunning = false;
        isPaused = false;
        elapsedSeconds = 0;
        remainingSeconds = totalSeconds;

        startTimerButton.setDisable(false);
        pauseTimerButton.setDisable(true);
        stopTimerButton.setDisable(true);
        disableControls(false);

        timerStatusLabel.setText("Leállítva");
        timerStatusLabel.getStyleClass().removeAll("status-running", "status-paused");
        timerStatusLabel.getStyleClass().add("status-stopped");

        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        int hours = remainingSeconds / 3600;
        int minutes = (remainingSeconds % 3600) / 60;
        int seconds = remainingSeconds % 60;

        timerDisplayLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void showTimerFinishedAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("⏰ Időzítő lejárt");
        alert.setHeaderText("Időzítő befejezve!");
        alert.setContentText(String.format(
                "A beállított időtartam (%s) letelt.\n\n" +
                        "Kategória: %s\n" +
                        "Eltelt idő: %s\n\n" +
                        "Az idő sikeresen mentésre került a statisztikáidba.",
                formatTime(totalSeconds),
                getCategoryDisplayName(),
                formatTime(totalSeconds)
        ));

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        alert.showAndWait();
    }

    private String getCategoryDisplayName() {
        if (workCategoryToggle.isSelected()) return "💼 Munka";
        if (studyCategoryToggle.isSelected()) return "📚 Tanulás";
        if (entertainmentCategoryToggle.isSelected()) return "🎮 Szórakozás";
        return "Egyéb";
    }

    private String formatTime(int totalSecs) {
        int h = totalSecs / 3600;
        int m = (totalSecs % 3600) / 60;
        int s = totalSecs % 60;

        if (h > 0) {
            return String.format("%d óra %d perc", h, m);
        } else if (m > 0) {
            return String.format("%d perc %d másodperc", m, s);
        } else {
            return String.format("%d másodperc", s);
        }
    }

    private void playNotificationSound() {
        try {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("Hang lejátszási hiba: " + e.getMessage());
        }
    }

    private void disableControls(boolean disable) {
        hoursSpinner.setDisable(disable);
        minutesSpinner.setDisable(disable);
        secondsSpinner.setDisable(disable);
        workCategoryToggle.setDisable(disable);
        studyCategoryToggle.setDisable(disable);
        entertainmentCategoryToggle.setDisable(disable);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Preset metódusok
    @FXML private void setTimer5Min() { setTimerPreset(0, 5, 0); }
    @FXML private void setTimer15Min() { setTimerPreset(0, 15, 0); }
    @FXML private void setTimer25Min() { setTimerPreset(0, 25, 0); }
    @FXML private void setTimer30Min() { setTimerPreset(0, 30, 0); }
    @FXML private void setTimer45Min() { setTimerPreset(0, 45, 0); }
    @FXML private void setTimer1Hour() { setTimerPreset(1, 0, 0); }
    @FXML private void setTimer2Hour() { setTimerPreset(2, 0, 0); }
    @FXML private void setTimer3Hour() { setTimerPreset(3, 0, 0); }

    private void setTimerPreset(int hours, int minutes, int seconds) {
        if (!isRunning) {
            hoursSpinner.getValueFactory().setValue(hours);
            minutesSpinner.getValueFactory().setValue(minutes);
            secondsSpinner.getValueFactory().setValue(seconds);
            updateTotalSeconds();
        }
    }
}