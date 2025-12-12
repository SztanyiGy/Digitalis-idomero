package org.example.digitalisidomero.service;

import org.example.digitalisidomero.config.Constants;
import org.example.digitalisidomero.database.dao.ApplicationDAO;
import org.example.digitalisidomero.database.dao.SessionDAO;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;
import org.example.digitalisidomero.model.Session;
import org.example.digitalisidomero.util.DateUtils;
import org.example.digitalisidomero.util.TimeFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ExportService {

    private final SessionDAO sessionDAO;
    private final ApplicationDAO applicationDAO;
    private final StatisticsService statisticsService;

    public ExportService() {
        this.sessionDAO = new SessionDAO();
        this.applicationDAO = new ApplicationDAO();
        this.statisticsService = new StatisticsService();
    }

    /**
     * Napi adatok exportálása CSV formátumba
     * @param date Dátum
     * @param outputFile Kimeneti fájl
     * @return true ha sikeres, false ha hiba történt
     */
    public boolean exportDayToCSV(LocalDate date, File outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            // CSV fejléc
            writer.append("Alkalmazás,Kategória,Kezdés,Befejezés,Időtartam (mp),Időtartam (formázott)\n");

            // Session-ök lekérése
            List<Session> sessions = sessionDAO.findByDate(date);

            for (Session session : sessions) {
                Application app = applicationDAO.findById(session.getApplicationId());

                if (app != null) {
                    writer.append(escapeCSV(app.getDisplayName())).append(",");
                    writer.append(app.getCategory().getDisplayName()).append(",");
                    writer.append(DateUtils.formatDateTime(session.getStartTime())).append(",");
                    writer.append(session.getEndTime() != null ? DateUtils.formatDateTime(session.getEndTime()) : "").append(",");
                    writer.append(String.valueOf(session.getDurationSeconds())).append(",");
                    writer.append(TimeFormatter.formatDuration(session.getDurationSeconds())).append("\n");
                }
            }

            System.out.println("✓ CSV export sikeres: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("✗ CSV export hiba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kategóriánkénti összesítés exportálása CSV-be
     * @param date Dátum
     * @param outputFile Kimeneti fájl
     * @return true ha sikeres
     */
    public boolean exportCategorySummaryToCSV(LocalDate date, File outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.append("Kategória,Időtartam (mp),Időtartam (formázott),Százalék\n");

            Map<Category, Long> breakdown = statisticsService.getCategoryBreakdownByDate(date);
            long totalSeconds = statisticsService.getTotalSecondsByDate(date);

            for (Map.Entry<Category, Long> entry : breakdown.entrySet()) {
                Category category = entry.getKey();
                Long seconds = entry.getValue();

                if (seconds > 0) {
                    double percentage = totalSeconds > 0 ? (seconds * 100.0 / totalSeconds) : 0;

                    writer.append(category.getDisplayName()).append(",");
                    writer.append(String.valueOf(seconds)).append(",");
                    writer.append(TimeFormatter.formatDuration(seconds)).append(",");
                    writer.append(String.format("%.1f%%", percentage)).append("\n");
                }
            }

            System.out.println("✓ Kategória összesítés export sikeres: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("✗ Kategória összesítés export hiba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Alkalmazásonkénti összesítés exportálása CSV-be
     * @param date Dátum
     * @param outputFile Kimeneti fájl
     * @return true ha sikeres
     */
    public boolean exportApplicationSummaryToCSV(LocalDate date, File outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.append("Alkalmazás,Kategória,Időtartam (mp),Időtartam (formázott)\n");

            Map<Application, Long> breakdown = statisticsService.getApplicationBreakdownByDate(date);

            // Rendezés időtartam szerint csökkenő sorrendben
            breakdown.entrySet().stream()
                    .sorted(Map.Entry.<Application, Long>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            Application app = entry.getKey();
                            Long seconds = entry.getValue();

                            writer.append(escapeCSV(app.getDisplayName())).append(",");
                            writer.append(app.getCategory().getDisplayName()).append(",");
                            writer.append(String.valueOf(seconds)).append(",");
                            writer.append(TimeFormatter.formatDuration(seconds)).append("\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            System.out.println("✓ Alkalmazás összesítés export sikeres: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("✗ Alkalmazás összesítés export hiba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Heti összesítés exportálása CSV-be
     * @param weekStart A hét első napja
     * @param outputFile Kimeneti fájl
     * @return true ha sikeres
     */
    public boolean exportWeekToCSV(LocalDate weekStart, File outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.append("Dátum,Nap,Időtartam (mp),Időtartam (formázott)\n");

            Map<LocalDate, Long> weeklyBreakdown = statisticsService.getWeeklyDailyBreakdown(weekStart);

            for (Map.Entry<LocalDate, Long> entry : weeklyBreakdown.entrySet()) {
                LocalDate date = entry.getKey();
                Long seconds = entry.getValue();

                writer.append(DateUtils.formatDate(date)).append(",");
                writer.append(DateUtils.getDayNameHungarian(date)).append(",");
                writer.append(String.valueOf(seconds)).append(",");
                writer.append(TimeFormatter.formatDuration(seconds)).append("\n");
            }

            System.out.println("✓ Heti összesítés export sikeres: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("✗ Heti összesítés export hiba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Teljes riport exportálása (részletes + összesítések)
     * @param date Dátum
     * @param outputFile Kimeneti fájl
     * @return true ha sikeres
     */
    public boolean exportFullReportToCSV(LocalDate date, File outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            // Fejléc
            writer.append("=== DIGITÁLIS IDŐMÉRŐ - NAPI RIPORT ===\n");
            writer.append("Dátum:," + DateUtils.formatDateForDisplay(date) + "\n");
            writer.append("Generálás ideje:," + DateUtils.formatDateTime(java.time.LocalDateTime.now()) + "\n");
            writer.append("\n");

            // Összesített statisztikák
            long totalSeconds = statisticsService.getTotalSecondsByDate(date);
            writer.append("=== ÖSSZESÍTÉS ===\n");
            writer.append("Összes idő (mp):," + totalSeconds + "\n");
            writer.append("Összes idő (formázott):," + TimeFormatter.formatDuration(totalSeconds) + "\n");
            writer.append("\n");

            // Kategóriánkénti bontás
            writer.append("=== KATEGÓRIÁK ===\n");
            writer.append("Kategória,Időtartam (mp),Időtartam (formázott),Százalék\n");

            Map<Category, Long> categoryBreakdown = statisticsService.getCategoryBreakdownByDate(date);
            for (Map.Entry<Category, Long> entry : categoryBreakdown.entrySet()) {
                if (entry.getValue() > 0) {
                    double percentage = totalSeconds > 0 ? (entry.getValue() * 100.0 / totalSeconds) : 0;
                    writer.append(entry.getKey().getDisplayName()).append(",");
                    writer.append(String.valueOf(entry.getValue())).append(",");
                    writer.append(TimeFormatter.formatDuration(entry.getValue())).append(",");
                    writer.append(String.format("%.1f%%", percentage)).append("\n");
                }
            }
            writer.append("\n");

            // Top alkalmazások
            writer.append("=== TOP 10 ALKALMAZÁS ===\n");
            writer.append("Sorszám,Alkalmazás,Kategória,Időtartam (mp),Időtartam (formázott)\n");

            List<Map.Entry<Application, Long>> topApps = statisticsService.getTopApplicationsByDate(date, 10);
            int rank = 1;
            for (Map.Entry<Application, Long> entry : topApps) {
                Application app = entry.getKey();
                Long seconds = entry.getValue();

                writer.append(String.valueOf(rank++)).append(",");
                writer.append(escapeCSV(app.getDisplayName())).append(",");
                writer.append(app.getCategory().getDisplayName()).append(",");
                writer.append(String.valueOf(seconds)).append(",");
                writer.append(TimeFormatter.formatDuration(seconds)).append("\n");
            }
            writer.append("\n");

            // Részletes session-ök
            writer.append("=== RÉSZLETES NAPLÓ ===\n");
            writer.append("Alkalmazás,Kategória,Kezdés,Befejezés,Időtartam (mp),Időtartam (formázott)\n");

            List<Session> sessions = sessionDAO.findByDate(date);
            for (Session session : sessions) {
                Application app = applicationDAO.findById(session.getApplicationId());
                if (app != null) {
                    writer.append(escapeCSV(app.getDisplayName())).append(",");
                    writer.append(app.getCategory().getDisplayName()).append(",");
                    writer.append(DateUtils.formatDateTime(session.getStartTime())).append(",");
                    writer.append(session.getEndTime() != null ? DateUtils.formatDateTime(session.getEndTime()) : "").append(",");
                    writer.append(String.valueOf(session.getDurationSeconds())).append(",");
                    writer.append(TimeFormatter.formatDuration(session.getDurationSeconds())).append("\n");
                }
            }

            System.out.println("✓ Teljes riport export sikeres: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("✗ Teljes riport export hiba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * CSV string escape (vesszők és idézőjelek kezelése)
     */
    private String escapeCSV(String value) {
        if (value == null) return "";

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * Alapértelmezett fájlnév generálása exporthoz
     * @param prefix Előtag (pl. "daily", "weekly")
     * @param date Dátum
     * @return Fájlnév (pl. "daily_2025-12-08.csv")
     */
    public static String generateDefaultFilename(String prefix, LocalDate date) {
        return prefix + "_" + DateUtils.formatDate(date) + ".csv";
    }
}