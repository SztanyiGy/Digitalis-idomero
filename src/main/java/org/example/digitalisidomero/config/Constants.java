package org.example.digitalisidomero.config;

public class Constants {

    // ===== ALKALMAZÁS INFORMÁCIÓK =====
    public static final String APP_NAME = "Digitális Időmérő";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_AUTHOR = "Digitális Időmérő Csapat";

    // ===== ADATBÁZIS =====
    public static final String DB_NAME = "timetracker.db";
    public static final String DB_URL = "jdbc:sqlite:" + DB_NAME;

    // ===== MONITOR BEÁLLÍTÁSOK =====
    public static final long MONITORING_INTERVAL_MS = 2000; // 2 másodperc
    public static final long IDLE_THRESHOLD_MS = 5 * 60 * 1000; // 5 perc (300 000 ms)
    public static final int IDLE_THRESHOLD_MINUTES = 5;

    // ===== UI FRISSÍTÉSI INTERVALLUMOK =====
    public static final int UI_UPDATE_INTERVAL_SECONDS = 1; // UI frissítés másodpercenként
    public static final int STATISTICS_REFRESH_INTERVAL_SECONDS = 5; // Statisztikák frissítése 5 mp-enként

    // ===== NAPI CÉLOK =====
    public static final int DAILY_TARGET_HOURS = 8; // 8 órás munkaidő = 100%
    public static final int DAILY_TARGET_SECONDS = DAILY_TARGET_HOURS * 3600;

    // ===== STATISZTIKÁK =====
    public static final int TOP_APPS_LIMIT = 10; // Top 10 alkalmazás megjelenítése
    public static final int WEEK_DAYS = 7;
    public static final int MONTH_DAYS = 30;

    // ===== UI MÉRETEK =====
    public static final int MAIN_WINDOW_WIDTH = 600;
    public static final int MAIN_WINDOW_HEIGHT = 700;
    public static final int SETTINGS_WINDOW_WIDTH = 500;
    public static final int SETTINGS_WINDOW_HEIGHT = 400;
    public static final int STATISTICS_WINDOW_WIDTH = 800;
    public static final int STATISTICS_WINDOW_HEIGHT = 600;

    // ===== SZÍNEK (HEX) =====
    public static final String COLOR_PRIMARY = "#3498db";
    public static final String COLOR_SUCCESS = "#2ecc71";
    public static final String COLOR_WARNING = "#f39c12";
    public static final String COLOR_DANGER = "#e74c3c";
    public static final String COLOR_INFO = "#9b59b6";
    public static final String COLOR_DARK = "#2c3e50";
    public static final String COLOR_LIGHT = "#ecf0f1";
    public static final String COLOR_GRAY = "#95a5a6";

    // ===== KATEGÓRIA SZÍNEK =====
    public static final String COLOR_WORK = "#3498db";
    public static final String COLOR_STUDY = "#2ecc71";
    public static final String COLOR_ENTERTAINMENT = "#e74c3c";
    public static final String COLOR_SOCIAL_MEDIA = "#9b59b6";
    public static final String COLOR_OTHER = "#95a5a6";

    // ===== EXPORT BEÁLLÍTÁSOK =====
    public static final String EXPORT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String EXPORT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String EXPORT_CSV_DELIMITER = ",";
    public static final String EXPORT_DEFAULT_FILENAME = "timetracker_export";

    // ===== DÁTUM FORMÁTUMOK =====
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "yyyy. MM. dd.";
    public static final String DISPLAY_DATETIME_FORMAT = "yyyy. MM. dd. HH:mm:ss";

    // ===== ADATTISZTÍTÁS =====
    public static final int DATA_RETENTION_DAYS = 90; // 90 nap után törlés

    // ===== ÜZENETEK =====
    public static final String MSG_TRACKING_STARTED = "Figyelés elindítva";
    public static final String MSG_TRACKING_STOPPED = "Figyelés leállítva";
    public static final String MSG_TRACKING_PAUSED = "Figyelés szüneteltetve";
    public static final String MSG_TRACKING_RESUMED = "Figyelés folytatva";

    // ===== HIBÁK =====
    public static final String ERR_DB_CONNECTION = "Adatbázis kapcsolat hiba";
    public static final String ERR_DB_QUERY = "Adatbázis lekérdezési hiba";
    public static final String ERR_WINDOW_MONITOR = "Ablakfigyelési hiba";
    public static final String ERR_ACTIVITY_DETECTOR = "Aktivitás érzékelési hiba";

    // Privát konstruktor - nem példányosítható
    private Constants() {
        throw new UnsupportedOperationException("Constants osztály nem példányosítható");
    }
}