package org.example.digitalisidomero.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {

    /**
     * Másodpercek formázása olvasható formátumba
     * Példa: 3665 másodperc → "1ó 1p"
     */
    public static String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dó %dp", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dp %dmp", minutes, secs);
        } else {
            return String.format("%dmp", secs);
        }
    }

    /**
     * Másodpercek formázása részletes formátumba (HH:MM:SS)
     * Példa: 3665 másodperc → "01:01:05"
     */
    public static String formatDurationDetailed(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Másodpercek formázása rövid formátumba (csak óra és perc)
     * Példa: 3665 másodperc → "1ó 1p"
     */
    public static String formatDurationShort(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%dó %dp", hours, minutes);
        } else {
            return String.format("%dp", minutes);
        }
    }

    /**
     * Két időpont közötti különbség formázása
     */
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        return formatDuration(duration.getSeconds());
    }

    /**
     * Másodpercek konvertálása percekre
     */
    public static long toMinutes(long seconds) {
        return seconds / 60;
    }

    /**
     * Másodpercek konvertálása órákra
     */
    public static double toHours(long seconds) {
        return seconds / 3600.0;
    }

    /**
     * Percek konvertálása másodpercekre
     */
    public static long minutesToSeconds(long minutes) {
        return minutes * 60;
    }

    /**
     * Órák konvertálása másodpercekre
     */
    public static long hoursToSeconds(long hours) {
        return hours * 3600;
    }

    /**
     * LocalDateTime formázása megjelenítésre (magyar formátum)
     * Példa: 2025-12-08 09:43:44 → "2025. 12. 08. 09:43:44"
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss");
        return dateTime.format(formatter);
    }

    /**
     * LocalDateTime formázása rövid formátumba
     * Példa: 2025-12-08 09:43:44 → "09:43:44"
     */
    public static String formatTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return dateTime.format(formatter);
    }

    /**
     * Időtartam szöveges leírása (emberi nyelven)
     * Példa: 3665 másodperc → "1 óra és 1 perc"
     */
    public static String formatDurationHumanReadable(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(hours).append(hours == 1 ? " óra" : " óra");
        }

        if (minutes > 0) {
            if (sb.length() > 0) sb.append(" és ");
            sb.append(minutes).append(minutes == 1 ? " perc" : " perc");
        }

        if (seconds < 60) {
            sb.append(secs).append(secs == 1 ? " másodperc" : " másodperc");
        }

        return sb.toString();
    }

    /**
     * Progress százalék kiszámítása
     * @param current Jelenlegi érték (másodpercben)
     * @param target Cél érték (másodpercben)
     * @return Százalék (0.0 - 1.0)
     */
    public static double calculateProgress(long current, long target) {
        if (target == 0) return 0.0;
        return Math.min((double) current / target, 1.0);
    }

    /**
     * Progress százalék kiszámítása string formátumban
     * @param current Jelenlegi érték (másodpercben)
     * @param target Cél érték (másodpercben)
     * @return Százalék string (pl. "75%")
     */
    public static String calculateProgressString(long current, long target) {
        double progress = calculateProgress(current, target);
        return String.format("%.0f%%", progress * 100);
    }

    // Privát konstruktor - utility osztály
    private TimeFormatter() {
        throw new UnsupportedOperationException("TimeFormatter osztály nem példányosítható");
    }
}