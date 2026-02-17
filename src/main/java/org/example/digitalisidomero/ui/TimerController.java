package org.example.digitalisidomero.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

public class TimerController {

    @FXML private Label timerDisplayLabel;
    @FXML private Spinner<Integer> hoursSpinner;
    @FXML private Spinner<Integer> minutesSpinner;
    @FXML private Spinner<Integer> secondsSpinner;

    @FXML private Button startTimerButton;
    @FXML private Button pauseTimerButton;
    @FXML private Button resetTimerButton;
    @FXML private Label timerStatusLabel;

    @FXML private CheckBox soundCheckBox;
    @FXML private CheckBox autoRestartCheckBox;

    private Timeline timeline;
    private int totalSeconds = 0;
    private int remainingSeconds = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;

    @FXML
    public void initialize() {
        setupSpinners();
        updateDisplay();
        updateButtonStates(false, false);
    }

    /**
     * Spinnerek beállítása
     */
    private void setupSpinners() {
        hoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        minutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 1));
        secondsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        hoursSpinner.setEditable(true);
        minutesSpinner.setEditable(true);
        secondsSpinner.setEditable(true);
    }

    /**
     * Start / Folytatás gomb
     */
    @FXML
    private void handleStart() {
        if (isPaused) {
            resume();
        } else {
            start();
        }
    }

    /**
     * Időzítő indítása
     */
    private void start() {
        int hours = hoursSpinner.getValue();
        int minutes = minutesSpinner.getValue();
        int seconds = secondsSpinner.getValue();

        totalSeconds = hours * 3600 + minutes * 60 + seconds;

        if (totalSeconds == 0) {
            showError("Adj meg legalább 1 másodpercet!");
            return;
        }

        remainingSeconds = totalSeconds;
        isRunning = true;
        isPaused = false;

        disableSpinners(true);
        updateButtonStates(true, false);
        updateStatusLabel("running");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateDisplay();

            if (remainingSeconds <= 0) {
                timerFinished();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Folytatás szünet után
     */
    private void resume() {
        isPaused = false;
        isRunning = true;

        updateButtonStates(true, false);
        updateStatusLabel("running");

        if (timeline != null) {
            timeline.play();
        }
    }

    /**
     * Szünet
     */
    @FXML
    private void handlePause() {
        if (timeline != null && isRunning) {
            timeline.pause();
            isPaused = true;
            isRunning = false;

            updateButtonStates(false, true);
            updateStatusLabel("paused");
        }
    }

    /**
     * Reset / Újraindítás
     */
    @FXML
    private void handleReset() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }

        isRunning = false;
        isPaused = false;
        remainingSeconds = 0;

        disableSpinners(false);
        updateButtonStates(false, false);
        updateStatusLabel("stopped");
        updateDisplay();
    }

    /**
     * Időzítő lejárt
     */
    private void timerFinished() {
        if (timeline != null) {
            timeline.stop();
        }

        isRunning = false;
        isPaused = false;
        remainingSeconds = 0;

        updateDisplay();
        updateButtonStates(false, false);
        updateStatusLabel("finished");

        // Hang lejátszása
        if (soundCheckBox.isSelected()) {
            playSound();
        }

        // Értesítés megjelenítése
        showTimerFinishedAlert();

        // Automatikus újraindítás
        if (autoRestartCheckBox.isSelected()) {
            Timeline delayedRestart = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                resetSpinnersToLastValue();
                start();
            }));
            delayedRestart.play();
        } else {
            disableSpinners(false);
        }
    }

    /**
     * Értesítés megjelenítése
     */
    private void showTimerFinishedAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("⏰ Időzítő lejárt");
        alert.setHeaderText("Lejárt az idő!");
        alert.setContentText(
                String.format("Beállított idő: %s\n\nAz időzítő befejeződött! 🎉",
                        formatTime(totalSeconds))
        );

        alert.getAlertType();

        ButtonType okButton = new ButtonType("Rendben");
        alert.getButtonTypes().setAll(okButton);

        alert.show();
    }

    /**
     * Hang lejátszása
     */
    private void playSound() {
        try {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("Nem sikerült a hang lejátszása: " + e.getMessage());
        }
    }

    /**
     * Kijelző frissítése
     */
    private void updateDisplay() {
        if (remainingSeconds <= 0) {
            timerDisplayLabel.setText("00:00:00");
            timerDisplayLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 64px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");
        } else {
            int hours = remainingSeconds / 3600;
            int minutes = (remainingSeconds % 3600) / 60;
            int seconds = remainingSeconds % 60;

            timerDisplayLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

            // Szín váltás
            if (remainingSeconds <= 10) {
                timerDisplayLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 64px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");
            } else if (remainingSeconds <= 60) {
                timerDisplayLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 64px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");
            } else {
                timerDisplayLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 64px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");
            }
        }
    }

    /**
     * Gombok állapotának frissítése
     */
    private void updateButtonStates(boolean running, boolean paused) {
        if (running) {
            startTimerButton.setDisable(true);
            pauseTimerButton.setDisable(false);
            resetTimerButton.setDisable(false);
        } else if (paused) {
            startTimerButton.setDisable(false);
            startTimerButton.setText("▶ Folytatás");
            pauseTimerButton.setDisable(true);
            resetTimerButton.setDisable(false);
        } else {
            startTimerButton.setDisable(false);
            startTimerButton.setText("▶ Indítás");
            pauseTimerButton.setDisable(true);
            resetTimerButton.setDisable(remainingSeconds == 0);
        }
    }

    /**
     * Státusz label frissítése
     */
    private void updateStatusLabel(String status) {
        switch (status) {
            case "running":
                timerStatusLabel.setText("⏱️ Futás...");
                timerStatusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 14px;");
                break;
            case "paused":
                timerStatusLabel.setText("⏸️ Szüneteltetve");
                timerStatusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-font-size: 14px;");
                break;
            case "stopped":
                timerStatusLabel.setText("⏹️ Leállítva");
                timerStatusLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold; -fx-font-size: 14px;");
                break;
            case "finished":
                timerStatusLabel.setText("✅ Lejárt!");
                timerStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;");
                break;
        }
    }

    /**
     * Spinnerek engedélyezése/letiltása
     */
    private void disableSpinners(boolean disable) {
        hoursSpinner.setDisable(disable);
        minutesSpinner.setDisable(disable);
        secondsSpinner.setDisable(disable);
    }

    /**
     * Spinnerek visszaállítása az utolsó értékre
     */
    private void resetSpinnersToLastValue() {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        hoursSpinner.getValueFactory().setValue(hours);
        minutesSpinner.getValueFactory().setValue(minutes);
        secondsSpinner.getValueFactory().setValue(seconds);
    }

    /**
     * Hibaüzenet megjelenítése
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Figyelmeztetés");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    /**
     * Idő formázása
     */
    private String formatTime(int totalSec) {
        int h = totalSec / 3600;
        int m = (totalSec % 3600) / 60;
        int s = totalSec % 60;

        if (h > 0) {
            return h + " óra " + m + " perc " + s + " másodperc";
        } else if (m > 0) {
            return m + " perc " + s + " másodperc";
        } else {
            return s + " másodperc";
        }
    }

    // ========== GYORS BEÁLLÍTÁS GOMBOK ==========

    @FXML
    private void setTimer1Min() {
        hoursSpinner.getValueFactory().setValue(0);
        minutesSpinner.getValueFactory().setValue(1);
        secondsSpinner.getValueFactory().setValue(0);
    }

    @FXML
    private void setTimer5Min() {
        hoursSpinner.getValueFactory().setValue(0);
        minutesSpinner.getValueFactory().setValue(5);
        secondsSpinner.getValueFactory().setValue(0);
    }

    @FXML
    private void setTimer10Min() {
        hoursSpinner.getValueFactory().setValue(0);
        minutesSpinner.getValueFactory().setValue(10);
        secondsSpinner.getValueFactory().setValue(0);
    }

    @FXML
    private void setTimer30Min() {
        hoursSpinner.getValueFactory().setValue(0);
        minutesSpinner.getValueFactory().setValue(30);
        secondsSpinner.getValueFactory().setValue(0);
    }

    @FXML
    private void setTimer1Hour() {
        hoursSpinner.getValueFactory().setValue(1);
        minutesSpinner.getValueFactory().setValue(0);
        secondsSpinner.getValueFactory().setValue(0);
    }
}