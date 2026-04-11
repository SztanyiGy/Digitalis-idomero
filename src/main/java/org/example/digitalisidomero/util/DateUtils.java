package org.example.digitalisidomero.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd.");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Mai dátum
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Tegnapi dátum
     */
    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }

    /**
     * Holnapi dátum
     */
    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }

    /**
     * A hét első napja (hétfő)
     */
    public static LocalDate getStartOfWeek() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * A hét utolsó napja (vasárnap)
     */
    public static LocalDate getEndOfWeek() {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Egy adott dátum hét első napja (hétfő)
     */
    public static LocalDate getStartOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Egy adott dátum hét utolsó napja (vasárnap)
     */
    public static LocalDate getEndOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Hónap első napja
     */
    public static LocalDate getStartOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Hónap utolsó napja
     */
    public static LocalDate getEndOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Egy adott hónap első napja
     */
    public static LocalDate getStartOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Egy adott hónap utolsó napja
     */
    public static LocalDate getEndOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Év első napja
     */
    public static LocalDate getStartOfYear() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * Év utolsó napja
     */
    public static LocalDate getEndOfYear() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * Két dátum közötti napok száma
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Két dátum között eltelt napok listája
     */
    public static List<LocalDate> getDateRange(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * Aktuális hét napjainak listája (hétfő-vasárnap)
     */
    public static List<LocalDate> getCurrentWeekDates() {
        LocalDate start = getStartOfWeek();
        LocalDate end = getEndOfWeek();
        return getDateRange(start, end);
    }

    /**
     * Egy adott hét napjainak listája
     */
    public static List<LocalDate> getWeekDates(LocalDate weekStart) {
        LocalDate start = getStartOfWeek(weekStart);
        LocalDate end = getEndOfWeek(weekStart);
        return getDateRange(start, end);
    }

    /**
     * Dátum formázása string-re (adatbázishoz)
     * Formátum: yyyy-MM-dd
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * Dátum formázása megjelenítéshez (magyar formátum)
     * Formátum: yyyy. MM. dd.
     */
    public static String formatDateForDisplay(LocalDate date) {
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    /**
     * DateTime formázása string-re (adatbázishoz)
     * Formátum: yyyy-MM-dd HH:mm:ss
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * String parsing dátummá (yyyy-MM-dd formátumból)
     */
    public static LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }

    /**
     * String parsing datetime-má (yyyy-MM-dd HH:mm:ss formátumból)
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
    }

    /**
     * Ellenőrzi, hogy egy dátum mai-e
     */
    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }

    /**
     * Ellenőrzi, hogy egy dátum tegnapi-e
     */
    public static boolean isYesterday(LocalDate date) {
        return date.equals(LocalDate.now().minusDays(1));
    }

    /**
     * Ellenőrzi, hogy egy dátum ezen a héten van-e
     */
    public static boolean isThisWeek(LocalDate date) {
        LocalDate start = getStartOfWeek();
        LocalDate end = getEndOfWeek();
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Ellenőrzi, hogy egy dátum ebben a hónapban van-e
     */
    public static boolean isThisMonth(LocalDate date) {
        LocalDate start = getStartOfMonth();
        LocalDate end = getEndOfMonth();
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Ellenőrzi, hogy egy dátum ebben az évben van-e
     */
    public static boolean isThisYear(LocalDate date) {
        return date.getYear() == LocalDate.now().getYear();
    }

    /**
     * Napok száma az elmúlt N napban (beleértve a mai napot)
     */
    public static List<LocalDate> getLastNDays(int n) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = n - 1; i >= 0; i--) {
            dates.add(today.minusDays(i));
        }

        return dates;
    }

    /**
     * Nap neve magyarul
     */
    public static String getDayNameHungarian(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Hétfő";
            case TUESDAY -> "Kedd";
            case WEDNESDAY -> "Szerda";
            case THURSDAY -> "Csütörtök";
            case FRIDAY -> "Péntek";
            case SATURDAY -> "Szombat";
            case SUNDAY -> "Vasárnap";
        };
    }

    /**
     * Nap neve magyarul (rövid)
     */
    public static String getDayNameShortHungarian(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "H";
            case TUESDAY -> "K";
            case WEDNESDAY -> "Sze";
            case THURSDAY -> "Cs";
            case FRIDAY -> "P";
            case SATURDAY -> "Szo";
            case SUNDAY -> "V";
        };
    }

    /**
     * Hónap neve magyarul
     */
    public static String getMonthNameHungarian(LocalDate date) {
        return switch (date.getMonth()) {
            case JANUARY -> "Január";
            case FEBRUARY -> "Február";
            case MARCH -> "Március";
            case APRIL -> "Április";
            case MAY -> "Május";
            case JUNE -> "Június";
            case JULY -> "Július";
            case AUGUST -> "Augusztus";
            case SEPTEMBER -> "Szeptember";
            case OCTOBER -> "Október";
            case NOVEMBER -> "November";
            case DECEMBER -> "December";
        };
    }

    /**
     * Relatív idő string (pl. "ma", "tegnap", "2 napja")
     */
    public static String getRelativeDateString(LocalDate date) {
        long daysDiff = ChronoUnit.DAYS.between(date, LocalDate.now());

        if (daysDiff == 0) {
            return "Ma";
        } else if (daysDiff == 1) {
            return "Tegnap";
        } else if (daysDiff > 1 && daysDiff < 7) {
            return daysDiff + " napja";
        } else if (daysDiff >= 7 && daysDiff < 14) {
            return "1 hete";
        } else if (daysDiff >= 14 && daysDiff < 30) {
            return (daysDiff / 7) + " hete";
        } else if (daysDiff >= 30 && daysDiff < 60) {
            return "1 hónapja";
        } else if (daysDiff >= 60 && daysDiff < 365) {
            return (daysDiff / 30) + " hónapja";
        } else if (daysDiff >= 365) {
            return (daysDiff / 365) + " éve";
        }

        return formatDateForDisplay(date);
    }

    // Privát konstruktor - utility osztály
    private DateUtils() {
        throw new UnsupportedOperationException("DateUtils osztály nem példányosítható");
    }
}