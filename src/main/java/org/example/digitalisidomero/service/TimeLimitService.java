package org.example.digitalisidomero.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;

/**
 * Időkorlát kezelő service - Singleton
 */
public class TimeLimitService {

    private static TimeLimitService instance;

    private boolean enabled = false;
    private int timeLimitMinutes = 0;
    private boolean soundEnabled = false;

    private LocalDateTime startTime;
    private Timeline checkTimer;
    private Runnable onTimeLimitReached;

    private TimeLimitService() {
        setupTimer();
    }

    public static synchronized TimeLimitService getInstance() {
        if (instance == null) {
            instance = new TimeLimitService();
        }
        return instance;
    }

    /**
     * Időkorlát beállítása
     */
    public void setTimeLimit(int minutes, boolean sound) {
        this.timeLimitMinutes = minutes;
        this.soundEnabled = sound;
        this.enabled = true;
        this.startTime = LocalDateTime.now();

        System.out.println("✓ Időkorlát beállítva: " + minutes + " perc");

        if (!checkTimer.getStatus().equals(Timeline.Status.RUNNING)) {
            checkTimer.play();
        }
    }

    /**
     * Időkorlát kikapcsolása
     */
    public void disableTimeLimit() {
        this.enabled = false;
        checkTimer.stop();
        System.out.println("✓ Időkorlát kikapcsolva");
    }

    /**
     * Timer visszaállítása (újraindítás)
     */
    public void resetTimer() {
        this.startTime = LocalDateTime.now();
        System.out.println("✓ Időkorlát timer újraindítva");
    }

    /**
     * Callback beállítása, ami lefut amikor lejár az idő
     */
    public void setOnTimeLimitReached(Runnable callback) {
        this.onTimeLimitReached = callback;
    }

    /**
     * Hátralévő percek lekérése
     */
    public int getRemainingMinutes() {
        if (!enabled || startTime == null) {
            return 0;
        }

        long elapsedMinutes = java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
        int remaining = (int) (timeLimitMinutes - elapsedMinutes);

        return Math.max(0, remaining);
    }

    /**
     * Eltelt percek lekérése
     */
    public int getElapsedMinutes() {
        if (startTime == null) {
            return 0;
        }

        return (int) java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
    }

    /**
     * Timer beállítása (minden percben ellenőrzi)
     */
    private void setupTimer() {
        checkTimer = new Timeline(new KeyFrame(Duration.minutes(1), event -> {
            if (!enabled) {
                return;
            }

            int remaining = getRemainingMinutes();

            // Ha lejárt az idő
            if (remaining <= 0) {
                System.out.println("⏰ Időkorlát elérve!");

                if (onTimeLimitReached != null) {
                    onTimeLimitReached.run();
                }

                // Leállítjuk, nehogy spammelje az értesítéseket
                disableTimeLimit();
            } else if (remaining <= 5) {
                // 5 perc ha van hátra, figyelmeztetés
                System.out.println("⚠️ " + remaining + " perc van hátra!");
            }
        }));

        checkTimer.setCycleCount(Timeline.INDEFINITE);
    }

    // ========== GETTEREK ==========

    public boolean isEnabled() {
        return enabled;
    }

    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}