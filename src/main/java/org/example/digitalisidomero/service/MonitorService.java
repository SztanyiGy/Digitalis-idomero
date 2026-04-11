package org.example.digitalisidomero.service;

import org.example.digitalisidomero.database.dao.ApplicationDAO;
import org.example.digitalisidomero.database.dao.SessionDAO;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;
import org.example.digitalisidomero.model.Session;
import org.example.digitalisidomero.monitor.ActivityDetector;
import org.example.digitalisidomero.monitor.WindowMonitor;

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

    // Kényszerített kategória (ha be van állítva, minden alkalmazást ebbe menti)
    private Category forcedCategory = null;

    // Figyelési intervallum (milliszekundumban) - alapértelmezett: 2 mp
    private static final long MONITORING_INTERVAL_MS = 2000;

    public MonitorService() {
        this.windowMonitor = new WindowMonitor();
        this.activityDetector = new ActivityDetector();
        this.applicationDAO = new ApplicationDAO();
        this.sessionDAO = new SessionDAO();
    }

    /**
     * Kényszerített kategória beállítása
     * Ha ez be van állítva, akkor minden alkalmazást ebbe a kategóriába fog menteni
     */
    public void setForcedCategory(Category category) {
        this.forcedCategory = category;
        System.out.println("✓ Kényszerített kategória beállítva: " + category);
    }

    /**
     * Kényszerített kategória törlése
     */
    public void clearForcedCategory() {
        this.forcedCategory = null;
        System.out.println("✓ Kényszerített kategória törölve");
    }

    /**
     * Van-e kényszerített kategória
     */
    public boolean hasForcedCategory() {
        return forcedCategory != null;
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

        // Ha van kényszerített kategória, azt használjuk minden esetben
        Category categoryToUse;
        if (forcedCategory != null) {
            categoryToUse = forcedCategory;
        } else if (currentApplication != null) {
            // Ha nincs kényszerített kategória, használjuk a meglévő alkalmazás kategóriáját
            categoryToUse = currentApplication.getCategory();
        } else {
            // Ha nincs sem kényszerített, sem meglévő, akkor OTHER
            categoryToUse = Category.OTHER;
        }

        if (currentApplication == null) {
            // Új alkalmazás létrehozása
            currentApplication = new Application(
                    processName,
                    formatDisplayName(processName, windowTitle),  // ← displayName dinamikusan formázva
                    categoryToUse
            );
            applicationDAO.save(currentApplication);
            System.out.println("➕ Új alkalmazás rögzítve: " + processName + " (DisplayName: " + currentApplication.getDisplayName() + ", Kategória: " + categoryToUse + ")");
        } else {
            // Meglévő alkalmazás - ha van kényszerített kategória ÉS eltér, akkor NEM frissítjük az adatbázist
            // Csak a session mentésnél használjuk a kényszerített kategóriát
            if (forcedCategory != null) {
                System.out.println("🔄 Session kategória felülírva: " + processName + " -> " + forcedCategory + " (eredeti: " + currentApplication.getCategory() + ")");
            }
        }

        // 2. Új session létrehozása a kiválasztott kategóriával
        sessionStartTime = LocalDateTime.now();
        currentSession = new Session(currentApplication.getId(), sessionStartTime);

        // FONTOS: A session-be is bele kell írni a kategóriát!
        currentSession.setCategory(categoryToUse);

        System.out.println("▶ Session indítva: " + processName + " (" + sessionStartTime + ") - Kategória: " + categoryToUse);
        System.out.println("DEBUG: Session kategória check: " + currentSession.getCategory());
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

        System.out.println("DEBUG: Mentés előtt - Session kategória: " + currentSession.getCategory());

        // Mentés adatbázisba
        sessionDAO.save(currentSession);

        System.out.println("⏹ Session lezárva: " +
                currentApplication.getName() +
                " (Időtartam: " + currentSession.getDurationSeconds() + "s / " +
                currentSession.getDurationMinutes() + " perc) - Kategória: " + currentSession.getCategory());

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

    /**
     * Formázott alkalmazás név generálása
     * @param processName Folyamat neve (pl. "firefox.exe")
     * @param windowTitle Ablak címe (pl. "GitHub - Mozilla Firefox")
     * @return Szebb formátum, lehetőleg az ablak címből kinyerve
     */
    private String formatDisplayName(String processName, String windowTitle) {
        if (processName == null || processName.isEmpty()) {
            return "Ismeretlen";
        }

        // 1. Próbáljuk kinyerni a szép nevet az ablak címből
        String displayName = extractAppNameFromWindowTitle(windowTitle);

        // 2. Ha nem sikerült, használjuk a processName-et formázva
        if (displayName == null || displayName.isEmpty()) {
            displayName = processName;

            // .exe eltávolítása
            if (displayName.toLowerCase().endsWith(".exe")) {
                displayName = displayName.substring(0, displayName.length() - 4);
            }

            // Első betű nagybetű, többi kisbetű
            if (!displayName.isEmpty()) {
                displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1).toLowerCase();
            }
        }

        return displayName;
    }

    /**
     * Alkalmazás nevének kinyerése az ablak címből
     * @param windowTitle Ablak címe (pl. "GitHub - Mozilla Firefox")
     * @return Kinyert alkalmazás név vagy null
     */
    private String extractAppNameFromWindowTitle(String windowTitle) {
        if (windowTitle == null || windowTitle.isEmpty()) {
            return null;
        }

        // Gyakori elválasztók az ablak címekben
        String[] separators = {" - ", " – ", " | ", " — ", " :: "};

        for (String separator : separators) {
            if (windowTitle.contains(separator)) {
                String[] parts = windowTitle.split(separator);

                // Az utolsó rész általában az alkalmazás neve
                // Pl: "GitHub - Mozilla Firefox" -> "Mozilla Firefox"
                String lastPart = parts[parts.length - 1].trim();

                // Ellenőrzések:
                // - Ne legyen túl hosszú (max 50 karakter)
                // - Ne legyen üres
                // - Ne tartalmazzon URL-t vagy fájl útvonalat
                if (!lastPart.isEmpty() &&
                        lastPart.length() < 50 &&
                        !lastPart.contains("://") &&
                        !lastPart.contains("\\") &&
                        !lastPart.contains("/")) {
                    return lastPart;
                }
            }
        }

        // Ha nincs elválasztó, de rövid a cím, akkor azt használjuk
        if (windowTitle.length() < 50 &&
                !windowTitle.contains("://") &&
                !windowTitle.contains("\\")) {
            return windowTitle;
        }

        return null;
    }
}