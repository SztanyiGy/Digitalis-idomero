package org.example.digitalisidomero.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * Értesítések kezelése - Singleton
 */
public class NotificationService {

    private static NotificationService instance;

    private NotificationService() {}

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Időkorlát lejárt értesítés
     */
    public void showTimeLimitAlert(int minutes) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("⏰ Időkorlát elérve");
            alert.setHeaderText("Lejárt a beállított idő!");
            alert.setContentText(
                    "Beállított időkorlát: " + formatDuration(minutes) + "\n\n" +
                            "Ideje szünetet tartani! 💪"
            );

            // Stílus
            alert.initStyle(StageStyle.UTILITY);

            // Gomb szöveg testreszabása
            ButtonType okButton = new ButtonType("Rendben");
            alert.getButtonTypes().setAll(okButton);

            // Hang lejátszása (ha engedélyezve van)
            TimeLimitService timeLimitService = TimeLimitService.getInstance();
            if (timeLimitService.isSoundEnabled()) {
                playNotificationSound();
            }

            alert.showAndWait();
        });
    }

    /**
     * Figyelmeztetés - 5 perc van hátra
     */
    public void showWarningAlert(int remainingMinutes) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠️ Figyelmeztetés");
            alert.setHeaderText("Hamarosan lejár az idő!");
            alert.setContentText(
                    "Hátralévő idő: " + remainingMinutes + " perc\n\n" +
                            "Készülj fel a szünetre!"
            );

            alert.initStyle(StageStyle.UTILITY);
            alert.show();
        });
    }

    /**
     * Egyedi értesítés
     */
    public void showNotification(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initStyle(StageStyle.UTILITY);
            alert.show();
        });
    }

    /**
     * Megerősítő dialog
     */
    public boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Hang lejátszása
     */
    private void playNotificationSound() {
        try {
            // Windows rendszer hang
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("Nem sikerült a hang lejátszása: " + e.getMessage());
        }
    }

    /**
     * Időformázás
     */
    private String formatDuration(int totalMinutes) {
        int h = totalMinutes / 60;
        int m = totalMinutes % 60;

        if (h > 0) {
            return h + " óra " + m + " perc";
        }
        return m + " perc";
    }
}