package org.example.digitalisidomero.service;

import org.example.digitalisidomero.database.dao.ApplicationDAO;
import org.example.digitalisidomero.database.dao.SessionDAO;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;
import org.example.digitalisidomero.model.Session;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsService {

    private final SessionDAO sessionDAO;
    private final ApplicationDAO applicationDAO;

    public StatisticsService() {
        this.sessionDAO = new SessionDAO();
        this.applicationDAO = new ApplicationDAO();
    }

    /**
     * Mai összes használati idő (másodpercben)
     */
    public long getTodayTotalSeconds() {
        List<Session> todaySessions = sessionDAO.findByDate(LocalDate.now());
        return todaySessions.stream()
                .mapToLong(Session::getDurationSeconds)
                .sum();
    }

    /**
     * Mai összes használati idő (percben)
     */
    public long getTodayTotalMinutes() {
        return getTodayTotalSeconds() / 60;
    }

    /**
     * Mai összes használati idő (órában)
     */
    public double getTodayTotalHours() {
        return getTodayTotalSeconds() / 3600.0;
    }

    /**
     * Egy adott dátum összes használati ideje (másodpercben)
     */
    public long getTotalSecondsByDate(LocalDate date) {
        List<Session> sessions = sessionDAO.findByDate(date);
        return sessions.stream()
                .mapToLong(Session::getDurationSeconds)
                .sum();
    }

    /**
     * Heti összesített idő (másodpercben)
     */
    public long getWeeklyTotalSeconds(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<Session> sessions = sessionDAO.findByDateRange(weekStart, weekEnd);
        return sessions.stream()
                .mapToLong(Session::getDurationSeconds)
                .sum();
    }

    /**
     * Kategóriánkénti bontás (mai nap)
     * @return Map<Category, Long> - kategória és másodpercek
     */
    public Map<Category, Long> getTodayCategoryBreakdown() {
        return getCategoryBreakdownByDate(LocalDate.now());
    }

    /**
     * Kategóriánkénti bontás (adott dátum)
     * @return Map<Category, Long> - kategória és másodpercek
     */
    public Map<Category, Long> getCategoryBreakdownByDate(LocalDate date) {
        List<Session> sessions = sessionDAO.findByDate(date);
        Map<Category, Long> breakdown = new EnumMap<>(Category.class);

        // Összes kategória inicializálása 0-val
        for (Category category : Category.values()) {
            breakdown.put(category, 0L);
        }

        // Session-ök csoportosítása kategória szerint
        // JAVÍTVA: A session saját kategóriáját használjuk, nem az application-ét!
        for (Session session : sessions) {
            Category category = session.getCategory();
            if (category != null) {
                breakdown.put(category, breakdown.get(category) + session.getDurationSeconds());
            }
        }

        return breakdown;
    }

    /**
     * Top N legtöbbet használt alkalmazás (mai nap)
     * @param limit Hány alkalmazást adjon vissza
     * @return Lista (Application, időtartam másodpercben)
     */
    public List<Map.Entry<Application, Long>> getTopApplicationsToday(int limit) {
        return getTopApplicationsByDate(LocalDate.now(), limit);
    }

    /**
     * Top N legtöbbet használt alkalmazás (adott dátum)
     * @param date Dátum
     * @param limit Hány alkalmazást adjon vissza
     * @return Lista (Application, időtartam másodpercben)
     */
    public List<Map.Entry<Application, Long>> getTopApplicationsByDate(LocalDate date, int limit) {
        List<Session> sessions = sessionDAO.findByDate(date);
        Map<Integer, Long> appDurations = new HashMap<>();

        // Alkalmazásonként összesítés
        for (Session session : sessions) {
            int appId = session.getApplicationId();
            appDurations.put(appId, appDurations.getOrDefault(appId, 0L) + session.getDurationSeconds());
        }

        // Rendezés időtartam szerint csökkenő sorrendben
        return appDurations.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Application app = applicationDAO.findById(entry.getKey());
                    return new AbstractMap.SimpleEntry<>(app, entry.getValue());
                })
                .collect(Collectors.toList());
    }

    /**
     * Alkalmazásonkénti idő mai napra
     * @return Map<Application, Long> - alkalmazás és másodpercek
     */
    public Map<Application, Long> getTodayApplicationBreakdown() {
        return getApplicationBreakdownByDate(LocalDate.now());
    }

    /**
     * Alkalmazásonkénti idő adott dátumra
     * @return Map<Application, Long> - alkalmazás és másodpercek
     */
    public Map<Application, Long> getApplicationBreakdownByDate(LocalDate date) {
        List<Session> sessions = sessionDAO.findByDate(date);
        Map<Application, Long> breakdown = new HashMap<>();

        for (Session session : sessions) {
            Application app = applicationDAO.findById(session.getApplicationId());
            if (app != null) {
                breakdown.put(app, breakdown.getOrDefault(app, 0L) + session.getDurationSeconds());
            }
        }

        return breakdown;
    }

    /**
     * Napi összesítés egy hétre
     * @param weekStart A hét első napja
     * @return Map<LocalDate, Long> - dátum és másodpercek
     */
    public Map<LocalDate, Long> getWeeklyDailyBreakdown(LocalDate weekStart) {
        Map<LocalDate, Long> dailyBreakdown = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            long totalSeconds = getTotalSecondsByDate(date);
            dailyBreakdown.put(date, totalSeconds);
        }

        return dailyBreakdown;
    }

    /**
     * Formázott idő string (óra:perc formátumban)
     * @deprecated Használd helyette: TimeFormatter.formatDuration()
     */
    @Deprecated
    public static String formatDuration(long seconds) {
        return org.example.digitalisidomero.util.TimeFormatter.formatDuration(seconds);
    }

    /**
     * Formázott idő string (részletes: óra:perc:másodperc)
     * @deprecated Használd helyette: TimeFormatter.formatDurationDetailed()
     */
    @Deprecated
    public static String formatDurationDetailed(long seconds) {
        return org.example.digitalisidomero.util.TimeFormatter.formatDurationDetailed(seconds);
    }
}