package org.example.digitalisidomero.service;

import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Session;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrackingService {

    private final MonitorService monitorService;
    private final List<TrackingListener> listeners;

    public TrackingService() {
        this.monitorService = new MonitorService();
        this.listeners = new ArrayList<>();
    }

    /**
     * Tracking indítása
     */
    public void startTracking() {
        monitorService.start();
        notifyListeners(TrackingEvent.STARTED);
    }

    /**
     * Tracking leállítása
     */
    public void stopTracking() {
        monitorService.stop();
        notifyListeners(TrackingEvent.STOPPED);
    }

    /**
     * Tracking szüneteltetése
     */
    public void pauseTracking() {
        monitorService.pause();
        notifyListeners(TrackingEvent.PAUSED);
    }

    /**
     * Tracking folytatása
     */
    public void resumeTracking() {
        monitorService.resume();
        notifyListeners(TrackingEvent.RESUMED);
    }

    /**
     * Tracking állapot lekérdezése
     */
    public boolean isTracking() {
        return monitorService.isRunning();
    }

    /**
     * Szünetel-e a tracking
     */
    public boolean isPaused() {
        return monitorService.isPaused();
    }

    /**
     * Jelenlegi ablak címe
     */
    public String getCurrentWindowTitle() {
        return monitorService.getCurrentWindowTitle();
    }

    /**
     * Jelenlegi folyamat neve
     */
    public String getCurrentProcessName() {
        return monitorService.getCurrentProcessName();
    }

    /**
     * Jelenlegi alkalmazás
     */
    public Application getCurrentApplication() {
        return monitorService.getCurrentApplication();
    }

    /**
     * Jelenlegi session
     */
    public Session getCurrentSession() {
        return monitorService.getCurrentSession();
    }

    /**
     * Jelenlegi session kezdési ideje
     */
    public LocalDateTime getCurrentSessionStartTime() {
        return monitorService.getSessionStartTime();
    }

    /**
     * Jelenlegi session időtartama (másodpercben)
     */
    public long getCurrentSessionDuration() {
        return monitorService.getCurrentSessionDuration();
    }

    /**
     * Jelenlegi session időtartama formázva
     */
    public String getCurrentSessionDurationFormatted() {
        long seconds = getCurrentSessionDuration();
        return StatisticsService.formatDuration(seconds);
    }

    /**
     * MonitorService reference (ha kell közvetlen hozzáférés)
     */
    public MonitorService getMonitorService() {
        return monitorService;
    }

    // ===== LISTENER KEZELÉS =====

    /**
     * Listener hozzáadása
     */
    public void addListener(TrackingListener listener) {
        listeners.add(listener);
    }

    /**
     * Listener eltávolítása
     */
    public void removeListener(TrackingListener listener) {
        listeners.remove(listener);
    }

    /**
     * Listener-ek értesítése
     */
    private void notifyListeners(TrackingEvent event) {
        for (TrackingListener listener : listeners) {
            listener.onTrackingEvent(event);
        }
    }

    // ===== TRACKING ESEMÉNYEK =====

    public enum TrackingEvent {
        STARTED,    // Tracking elindult
        STOPPED,    // Tracking leállt
        PAUSED,     // Tracking szünetel
        RESUMED,    // Tracking folytatódott
        WINDOW_CHANGED // Ablak váltás (opcionális, később)
    }

    // ===== LISTENER INTERFACE =====

    @FunctionalInterface
    public interface TrackingListener {
        void onTrackingEvent(TrackingEvent event);
    }
}