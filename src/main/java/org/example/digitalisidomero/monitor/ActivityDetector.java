package org.example.digitalisidomero.monitor;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinUser.LASTINPUTINFO;

public class ActivityDetector {

    // Inaktivitási határidő (milliszekundumban) - alapértelmezett: 5 perc
    private static final long DEFAULT_IDLE_THRESHOLD_MS = 5 * 60 * 1000; // 30 perc
    private long idleThresholdMs;

    public ActivityDetector() {
        this.idleThresholdMs = DEFAULT_IDLE_THRESHOLD_MS;
    }

    public ActivityDetector(long idleThresholdMs) {
        this.idleThresholdMs = idleThresholdMs;
    }

    /**
     * Ellenőrzi, hogy a felhasználó aktív-e
     * @return true ha aktív (volt input az utóbbi X percben), false ha inaktív
     */
    public boolean isUserActive() {
        long idleTime = getIdleTimeMillis();
        return idleTime < idleThresholdMs;
    }

    /**
     * Megadja, mennyi ideje inaktív a felhasználó (milliszekundumban)
     * @return Inaktivitás ideje ms-ban
     */
    public long getIdleTimeMillis() {
        LASTINPUTINFO lastInputInfo = new LASTINPUTINFO();
        User32.INSTANCE.GetLastInputInfo(lastInputInfo);

        // Rendszer indulása óta eltelt idő
        int tickCount = Kernel32.INSTANCE.GetTickCount();

        // Utolsó input óta eltelt idő
        long idleTime = tickCount - lastInputInfo.dwTime;

        return idleTime;
    }

    /**
     * Inaktivitás ideje másodpercben
     * @return Inaktivitás ideje másodpercben
     */
    public long getIdleTimeSeconds() {
        return getIdleTimeMillis() / 1000;
    }

    /**
     * Inaktivitás ideje percben
     * @return Inaktivitás ideje percben
     */
    public long getIdleTimeMinutes() {
        return getIdleTimeSeconds() / 60;
    }

    /**
     * Beállítja az inaktivitási határidőt
     * @param thresholdMs Határidő milliszekundumban
     */
    public void setIdleThreshold(long thresholdMs) {
        this.idleThresholdMs = thresholdMs;
    }

    /**
     * Beállítja az inaktivitási határidőt percben
     * @param minutes Határidő percben
     */
    public void setIdleThresholdMinutes(int minutes) {
        this.idleThresholdMs = minutes * 60 * 1000L;
    }

    /**
     * Lekéri az aktuális inaktivitási határidőt percben
     * @return Határidő percben
     */
    public int getIdleThresholdMinutes() {
        return (int) (idleThresholdMs / (60 * 1000));
    }

    /**
     * Teszteléshez: kiírja az aktuális inaktivitási időt
     */
    public void printIdleStatus() {
        long idleSeconds = getIdleTimeSeconds();
        long idleMinutes = getIdleTimeMinutes();
        boolean active = isUserActive();

        System.out.println("─────────────────────────────────");
        System.out.println("Inaktivitás: " + idleMinutes + " perc " + (idleSeconds % 60) + " másodperc");
        System.out.println("Felhasználó aktív: " + (active ? "✓ IGEN" : "✗ NEM"));
        System.out.println("Határidő: " + getIdleThresholdMinutes() + " perc");
        System.out.println("─────────────────────────────────");
    }
}