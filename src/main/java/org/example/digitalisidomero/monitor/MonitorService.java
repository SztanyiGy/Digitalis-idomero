package org.example.digitalisidomero.monitor;

import org.example.digitalisidomero.database.dao.ApplicationDAO;
import org.example.digitalisidomero.database.dao.SessionDAO;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;
import org.example.digitalisidomero.model.Session;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorService {

    private final WindowMonitor windowMonitor;
    private final ActivityDetector activityDetector;
    private final ApplicationDAO applicationDAO;
    private final SessionDAO sessionDAO;

    private Timer monitorTimer;
    private boolean isRunning = false;
    private boolean isPaused = false;

    // Jelenlegi tracking állapot
    private String currentWindowTitle = null;
    private String currentProcessName = null;
    private Application currentApplication = null;
    private Session currentSession = null;
    private LocalDateTime sessionStartTime = null;

    // Figyelési intervallum (milliszekundumban) - alapértelmezett: 2 mp
    private static final long MONITORING_INTERVAL_MS = 2000;

    public MonitorService() {
        this.windowMonitor = new WindowMonitor();
        this.activityDetector = new ActivityDetector();
        this.applicationDAO = new ApplicationDAO();
        this.sessionDAO = new SessionDAO();
    }

    /**
     * Figyelés indítása
     */
    public void start() {
        if (isRunning) {
            System.out.println("⚠ A figyelés már fut!");
            return;
        }

        isRunning = true;
        isPaused = false;

        System.out.println("✓ Figyelés elindítva!");

        monitorTimer = new Timer("WindowMonitorTimer", true);
        monitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused) {
                    checkActiveWindow();
                }
            }
        }, 0, MONITORING_INTERVAL_MS);
    }

    /**
     * Figyelés leállítása
     */
    public void stop() {
        if (!isRunning) {
            System.out.println("⚠ A figyelés nem fut!");
            return;
        }

        // Jelenlegi session lezárása, ha van
        endCurrentSession();

        if (monitorTimer != null) {
            monitorTimer.cancel();
            monitorTimer = null;
        }

        isRunning = false;
        isPaused = false;

        System.out.println("✓ Figyelés leállítva!");
    }

    /**
     * Figyelés szüneteltetése
     */
    public void pause() {
        if (!isRunning) {
            System.out.println("⚠ A figyelés nem fut!");
            return;
        }

        if (isPaused) {
            System.out.println("⚠ A figyelés már szünetel!");
            return;
        }

        isPaused = true;
        endCurrentSession(); // Jelenlegi session lezárása
        System.out.println("⏸ Figyelés szüneteltetve!");
    }

    /**
     * Figyelés folytatása
     */
    public void resume() {
        if (!isRunning) {
            System.out.println("⚠ A figyelés nem fut!");
            return;
        }

        if (!isPaused) {
            System.out.println("⚠ A figyelés nem szünetel!");
            return;
        }

        isPaused = false;
        System.out.println("▶ Figyelés folytatva!");
    }

    /**
     * Aktív ablak ellenőrzése (ez fut 2 másodpercenként)
     */
    private void checkActiveWindow() {
        // 1. Inaktivitás ellenőrzése
        if (!activityDetector.isUserActive()) {
            if (currentSession != null) {
                System.out.println("💤 Felhasználó inaktív - session szüneteltetése");
                endCurrentSession();
            }
            return;
        }

        // 2. Aktív ablak információk lekérése
        String windowTitle = windowMonitor.getActiveWindowTitle();
        String processName = windowMonitor.getActiveWindowProcessName();

        // 3. Ha megváltozott az ablak
        if (!processName.equals(currentProcessName) || !windowTitle.equals(currentWindowTitle)) {
            System.out.println("🔄 Ablak váltás észlelve: " + processName + " - " + windowTitle);

            // Előző session lezárása
            endCurrentSession();

            // Új session indítása
            startNewSession(processName, windowTitle);

            currentProcessName = processName;
            currentWindowTitle = windowTitle;
        }

        // 4. Ha ugyanaz az ablak, csak folytatjuk (a session már fut)
    }

    /**
     * Új session indítása
     */
    private void startNewSession(String processName, String windowTitle) {
        // 1. Alkalmazás keresése vagy létrehozása
        currentApplication = applicationDAO.findByName(processName);

        if (currentApplication == null) {
            // Új alkalmazás - alapértelmezett kategória: OTHER
            currentApplication = new Application(
                    processName,
                    windowTitle,
                    Category.OTHER
            );
            applicationDAO.save(currentApplication);
            System.out.println("➕ Új alkalmazás rögzítve: " + processName);
        }

        // 2. Új session létrehozása
        sessionStartTime = LocalDateTime.now();
        currentSession = new Session(currentApplication.getId(), sessionStartTime);

        System.out.println("▶ Session indítva: " + processName + " (" + sessionStartTime + ")");
    }

    /**
     * Jelenlegi session lezárása
     */
    private void endCurrentSession() {
        if (currentSession == null) {
            return; // Nincs aktív session
        }

        // Session lezárása
        LocalDateTime endTime = LocalDateTime.now();
        currentSession.endSession(endTime);

        // Mentés adatbázisba
        sessionDAO.save(currentSession);

        System.out.println("⏹ Session lezárva: " +
                currentApplication.getName() +
                " (Időtartam: " + currentSession.getDurationSeconds() + "s / " +
                currentSession.getDurationMinutes() + " perc)");

        // Állapot törlése
        currentSession = null;
        currentApplication = null;
        sessionStartTime = null;
    }

    /**
     * Állapot lekérdezések
     */
    public boolean isRunning() {
        return isRunning;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public String getCurrentWindowTitle() {
        return currentWindowTitle;
    }

    public String getCurrentProcessName() {
        return currentProcessName;
    }

    public Application getCurrentApplication() {
        return currentApplication;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public LocalDateTime getSessionStartTime() {
        return sessionStartTime;
    }

    /**
     * Jelenlegi session időtartama másodpercben
     */
    public long getCurrentSessionDuration() {
        if (sessionStartTime == null) {
            return 0;
        }
        return java.time.Duration.between(sessionStartTime, LocalDateTime.now()).getSeconds();
    }
}