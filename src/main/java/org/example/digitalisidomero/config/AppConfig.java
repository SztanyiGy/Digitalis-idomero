package org.example.digitalisidomero.config;

import java.io.*;
import java.util.Properties;

/**
 * Alkalmazás konfigurációs beállítások kezelése
 * Properties fájlból tölt és ment beállításokat
 */
public class AppConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static AppConfig instance;
    private Properties properties;

    // Singleton pattern
    private AppConfig() {
        properties = new Properties();
        loadConfig();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /**
     * Konfiguráció betöltése fájlból
     */
    private void loadConfig() {
        File configFile = new File(CONFIG_FILE);

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
                System.out.println("✓ Konfiguráció betöltve: " + CONFIG_FILE);
            } catch (IOException e) {
                System.err.println("✗ Konfiguráció betöltési hiba: " + e.getMessage());
                setDefaultConfig();
            }
        } else {
            System.out.println("⚠ Konfigurációs fájl nem található, alapértelmezések használata");
            setDefaultConfig();
            saveConfig(); // Alapértelmezett konfig mentése
        }
    }

    /**
     * Alapértelmezett konfiguráció beállítása
     */
    private void setDefaultConfig() {
        // Monitor beállítások
        properties.setProperty("monitor.interval.ms", String.valueOf(Constants.MONITORING_INTERVAL_MS));
        properties.setProperty("monitor.idle.threshold.minutes", String.valueOf(Constants.IDLE_THRESHOLD_MINUTES));

        // UI beállítások
        properties.setProperty("ui.update.interval.seconds", String.valueOf(Constants.UI_UPDATE_INTERVAL_SECONDS));
        properties.setProperty("ui.autostart", "false");
        properties.setProperty("ui.minimize.to.tray", "true");
        properties.setProperty("ui.show.notifications", "true");

        // Statisztika beállítások
        properties.setProperty("stats.daily.target.hours", String.valueOf(Constants.DAILY_TARGET_HOURS));
        properties.setProperty("stats.top.apps.limit", String.valueOf(Constants.TOP_APPS_LIMIT));

        // Adattisztítás
        properties.setProperty("data.retention.days", String.valueOf(Constants.DATA_RETENTION_DAYS));
        properties.setProperty("data.auto.cleanup", "false");

        // Export beállítások
        properties.setProperty("export.default.format", "CSV");
        properties.setProperty("export.include.charts", "true");
    }

    /**
     * Konfiguráció mentése fájlba
     */
    public void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Digitális Időmérő - Konfiguráció");
            System.out.println("✓ Konfiguráció mentve: " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("✗ Konfiguráció mentési hiba: " + e.getMessage());
        }
    }

    // ===== GETTER METÓDUSOK =====

    public long getMonitoringIntervalMs() {
        return Long.parseLong(properties.getProperty("monitor.interval.ms",
                String.valueOf(Constants.MONITORING_INTERVAL_MS)));
    }

    public int getIdleThresholdMinutes() {
        return Integer.parseInt(properties.getProperty("monitor.idle.threshold.minutes",
                String.valueOf(Constants.IDLE_THRESHOLD_MINUTES)));
    }

    public int getUiUpdateIntervalSeconds() {
        return Integer.parseInt(properties.getProperty("ui.update.interval.seconds",
                String.valueOf(Constants.UI_UPDATE_INTERVAL_SECONDS)));
    }

    public boolean isAutostart() {
        return Boolean.parseBoolean(properties.getProperty("ui.autostart", "false"));
    }

    public boolean isMinimizeToTray() {
        return Boolean.parseBoolean(properties.getProperty("ui.minimize.to.tray", "true"));
    }

    public boolean isShowNotifications() {
        return Boolean.parseBoolean(properties.getProperty("ui.show.notifications", "true"));
    }

    public int getDailyTargetHours() {
        return Integer.parseInt(properties.getProperty("stats.daily.target.hours",
                String.valueOf(Constants.DAILY_TARGET_HOURS)));
    }

    public int getTopAppsLimit() {
        return Integer.parseInt(properties.getProperty("stats.top.apps.limit",
                String.valueOf(Constants.TOP_APPS_LIMIT)));
    }

    public int getDataRetentionDays() {
        return Integer.parseInt(properties.getProperty("data.retention.days",
                String.valueOf(Constants.DATA_RETENTION_DAYS)));
    }

    public boolean isAutoCleanup() {
        return Boolean.parseBoolean(properties.getProperty("data.auto.cleanup", "false"));
    }

    public String getExportDefaultFormat() {
        return properties.getProperty("export.default.format", "CSV");
    }

    public boolean isExportIncludeCharts() {
        return Boolean.parseBoolean(properties.getProperty("export.include.charts", "true"));
    }

    // ===== SETTER METÓDUSOK =====

    public void setMonitoringIntervalMs(long intervalMs) {
        properties.setProperty("monitor.interval.ms", String.valueOf(intervalMs));
    }

    public void setIdleThresholdMinutes(int minutes) {
        properties.setProperty("monitor.idle.threshold.minutes", String.valueOf(minutes));
    }

    public void setAutostart(boolean autostart) {
        properties.setProperty("ui.autostart", String.valueOf(autostart));
    }

    public void setMinimizeToTray(boolean minimize) {
        properties.setProperty("ui.minimize.to.tray", String.valueOf(minimize));
    }

    public void setShowNotifications(boolean show) {
        properties.setProperty("ui.show.notifications", String.valueOf(show));
    }

    public void setDailyTargetHours(int hours) {
        properties.setProperty("stats.daily.target.hours", String.valueOf(hours));
    }

    public void setTopAppsLimit(int limit) {
        properties.setProperty("stats.top.apps.limit", String.valueOf(limit));
    }

    public void setDataRetentionDays(int days) {
        properties.setProperty("data.retention.days", String.valueOf(days));
    }

    public void setAutoCleanup(boolean autoCleanup) {
        properties.setProperty("data.auto.cleanup", String.valueOf(autoCleanup));
    }

    public void setExportDefaultFormat(String format) {
        properties.setProperty("export.default.format", format);
    }

    public void setExportIncludeCharts(boolean include) {
        properties.setProperty("export.include.charts", String.valueOf(include));
    }

    // ===== ÁLTALÁNOS GETTER/SETTER =====

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}