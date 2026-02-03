package org.example.digitalisidomero.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Session {
    private int id;
    private Category category;
    private int applicationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;

    // Üres konstruktor
    public Session() {}

    // Konstruktor új session indításához
    public Session(int applicationId, LocalDateTime startTime) {
        this.applicationId = applicationId;
        this.startTime = startTime;
    }

    // Teljes konstruktor
    public Session(int id, int applicationId, LocalDateTime startTime,
                   LocalDateTime endTime, long durationSeconds) {
        this.id = id;
        this.applicationId = applicationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = durationSeconds;
    }

    // Session lezárása és időtartam kiszámítása
    public void endSession(LocalDateTime endTime) {
        this.endTime = endTime;
        if (startTime != null && endTime != null) {
            this.durationSeconds = ChronoUnit.SECONDS.between(startTime, endTime);
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        // Automatikus duration frissítés
        if (startTime != null && endTime != null) {
            this.durationSeconds = ChronoUnit.SECONDS.between(startTime, endTime);
        }
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    // Segédfüggvény: időtartam percben
    public long getDurationMinutes() {
        return durationSeconds / 60;
    }

    // Segédfüggvény: időtartam órában
    public double getDurationHours() {
        return durationSeconds / 3600.0;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", applicationId=" + applicationId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", durationSeconds=" + durationSeconds +
                '}';
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }
}