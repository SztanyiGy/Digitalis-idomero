package org.example.digitalisidomero.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.digitalisidomero.database.dao.ApplicationDAO;
import org.example.digitalisidomero.database.dao.SessionDAO;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;
import org.example.digitalisidomero.model.Session;
import org.example.digitalisidomero.util.DateUtils;
import org.example.digitalisidomero.util.TimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
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

    // ========== CSV EXPORTOK ==========

    /**
     * Napi adatok exportálása CSV formátumba
     */
    public boolean exportDayToCSV(LocalDate date, File outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.append("Alkalmazás,Kategória,Kezdés,Befejezés,Időtartam (mp),Időtartam (formázott)\n");

            List<Session> sessions = sessionDAO.findByDate(date);

            for (Session session : sessions) {
                Application app = applicationDAO.findById(session.getApplicationId());

                if (app != null) {
                    writer.append(escapeCSV(app.getDisplayName())).append(",");
                    writer.append(getCategoryDisplayName(session, app)).append(",");
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
     * Heti összesítés exportálása CSV-be
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

            System.out.println("✓ Heti összesítés CSV export sikeres: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("✗ Heti összesítés CSV export hiba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Teljes riport exportálása CSV-be
     */
    public boolean exportFullReportToCSV(LocalDate date, File outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.append("=== DIGITÁLIS IDŐMÉRŐ - NAPI RIPORT ===\n");
            writer.append("Dátum:," + DateUtils.formatDateForDisplay(date) + "\n");
            writer.append("Generálás ideje:," + DateUtils.formatDateTime(java.time.LocalDateTime.now()) + "\n");
            writer.append("\n");

            long totalSeconds = statisticsService.getTotalSecondsByDate(date);
            writer.append("=== ÖSSZESÍTÉS ===\n");
            writer.append("Összes idő (mp):," + totalSeconds + "\n");
            writer.append("Összes idő (formázott):," + TimeFormatter.formatDuration(totalSeconds) + "\n");
            writer.append("\n");

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

            writer.append("=== TOP 10 ALKALMAZÁS ===\n");
            writer.append("Sorszám,Alkalmazás,Kategória,Időtartam (mp),Időtartam (formázott)\n");

            List<Map.Entry<Application, Long>> topApps = statisticsService.getTopApplicationsByDate(date, 10);
            int rank = 1;
            for (Map.Entry<Application, Long> entry : topApps) {
                Application app = entry.getKey();
                Long seconds = entry.getValue();

                writer.append(String.valueOf(rank++)).append(",");
                writer.append(escapeCSV(app.getDisplayName())).append(",");
                writer.append(getCategoryDisplayName(null, app)).append(",");
                writer.append(String.valueOf(seconds)).append(",");
                writer.append(TimeFormatter.formatDuration(seconds)).append("\n");
            }
            writer.append("\n");

            writer.append("=== RÉSZLETES NAPLÓ ===\n");
            writer.append("Alkalmazás,Kategória,Kezdés,Befejezés,Időtartam (mp),Időtartam (formázott)\n");

            List<Session> sessions = sessionDAO.findByDate(date);
            for (Session session : sessions) {
                Application app = applicationDAO.findById(session.getApplicationId());
                if (app != null) {
                    writer.append(escapeCSV(app.getDisplayName())).append(",");
                    writer.append(getCategoryDisplayName(session, app)).append(",");
                    writer.append(DateUtils.formatDateTime(session.getStartTime())).append(",");
                    writer.append(session.getEndTime() != null ? DateUtils.formatDateTime(session.getEndTime()) : "").append(",");
                    writer.append(String.valueOf(session.getDurationSeconds())).append(",");
                    writer.append(TimeFormatter.formatDuration(session.getDurationSeconds())).append("\n");
                }
            }

            System.out.println("✓ Teljes riport CSV export sikeres: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("✗ Teljes riport CSV export hiba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ========== EXCEL EXPORTOK ==========

    /**
     * Napi adatok exportálása Excel formátumba (.xlsx)
     */
    public boolean exportDayToExcel(LocalDate date, File outputFile) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Napi adatok - " + DateUtils.formatDate(date));

            // Stílusok
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Fejléc
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Alkalmazás", "Kategória", "Kezdés", "Befejezés", "Időtartam (mp)", "Időtartam"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Adatok
            List<Session> sessions = sessionDAO.findByDate(date);
            int rowNum = 1;

            for (Session session : sessions) {
                Application app = applicationDAO.findById(session.getApplicationId());

                if (app != null) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(app.getDisplayName());
                    row.createCell(1).setCellValue(getCategoryDisplayName(session, app));
                    row.createCell(2).setCellValue(DateUtils.formatDateTime(session.getStartTime()));
                    row.createCell(3).setCellValue(session.getEndTime() != null ? DateUtils.formatDateTime(session.getEndTime()) : "");
                    row.createCell(4).setCellValue(session.getDurationSeconds());
                    row.createCell(5).setCellValue(TimeFormatter.formatDuration(session.getDurationSeconds()));
                }
            }

            // Oszlopszélességek automatikus beállítása
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Fájl mentése
            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                workbook.write(fileOut);
            }

            System.out.println("✓ Excel export sikeres: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("✗ Excel export hiba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Heti összesítés exportálása Excel formátumba
     */
    public boolean exportWeekToExcel(LocalDate weekStart, File outputFile) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Heti összesítés");

            CellStyle headerStyle = createHeaderStyle(workbook);

            // Fejléc
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Dátum", "Nap", "Időtartam (mp)", "Időtartam"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Adatok
            Map<LocalDate, Long> weeklyBreakdown = statisticsService.getWeeklyDailyBreakdown(weekStart);
            int rowNum = 1;

            for (Map.Entry<LocalDate, Long> entry : weeklyBreakdown.entrySet()) {
                LocalDate date = entry.getKey();
                Long seconds = entry.getValue();

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(DateUtils.formatDate(date));
                row.createCell(1).setCellValue(DateUtils.getDayNameHungarian(date));
                row.createCell(2).setCellValue(seconds);
                row.createCell(3).setCellValue(TimeFormatter.formatDuration(seconds));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                workbook.write(fileOut);
            }

            System.out.println("✓ Heti Excel export sikeres: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("✗ Heti Excel export hiba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Teljes riport exportálása Excel formátumba (több munkalap)
     */
    public boolean exportFullReportToExcel(LocalDate date, File outputFile) {
        try (Workbook workbook = new XSSFWorkbook()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            // ===== 1. ÖSSZEFOGLALÓ MUNKALAP =====
            Sheet summarySheet = workbook.createSheet("Összefoglaló");
            int rowNum = 0;

            // Cím
            Row titleRow = summarySheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DIGITÁLIS IDŐMÉRŐ - NAPI RIPORT");
            titleCell.setCellStyle(titleStyle);
            rowNum++;

            // Alapinformációk
            summarySheet.createRow(rowNum++).createCell(0).setCellValue("Dátum: " + DateUtils.formatDateForDisplay(date));
            summarySheet.createRow(rowNum++).createCell(0).setCellValue("Generálás: " + DateUtils.formatDateTime(java.time.LocalDateTime.now()));
            rowNum++;

            long totalSeconds = statisticsService.getTotalSecondsByDate(date);
            summarySheet.createRow(rowNum++).createCell(0).setCellValue("Összes idő: " + TimeFormatter.formatDuration(totalSeconds));
            rowNum++;

            // ===== 2. KATEGÓRIÁK MUNKALAP =====
            Sheet categorySheet = workbook.createSheet("Kategóriák");
            rowNum = 0;

            Row catHeaderRow = categorySheet.createRow(rowNum++);
            String[] catHeaders = {"Kategória", "Időtartam (mp)", "Időtartam", "Százalék"};
            for (int i = 0; i < catHeaders.length; i++) {
                Cell cell = catHeaderRow.createCell(i);
                cell.setCellValue(catHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            Map<Category, Long> categoryBreakdown = statisticsService.getCategoryBreakdownByDate(date);
            for (Map.Entry<Category, Long> entry : categoryBreakdown.entrySet()) {
                if (entry.getValue() > 0) {
                    double percentage = totalSeconds > 0 ? (entry.getValue() * 100.0 / totalSeconds) : 0;
                    Row row = categorySheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey().getDisplayName());
                    row.createCell(1).setCellValue(entry.getValue());
                    row.createCell(2).setCellValue(TimeFormatter.formatDuration(entry.getValue()));
                    row.createCell(3).setCellValue(String.format("%.1f%%", percentage));
                }
            }

            for (int i = 0; i < catHeaders.length; i++) {
                categorySheet.autoSizeColumn(i);
            }

            // ===== 3. TOP ALKALMAZÁSOK MUNKALAP =====
            Sheet topAppsSheet = workbook.createSheet("Top alkalmazások");
            rowNum = 0;

            Row topHeaderRow = topAppsSheet.createRow(rowNum++);
            String[] topHeaders = {"#", "Alkalmazás", "Kategória", "Időtartam (mp)", "Időtartam"};
            for (int i = 0; i < topHeaders.length; i++) {
                Cell cell = topHeaderRow.createCell(i);
                cell.setCellValue(topHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Map.Entry<Application, Long>> topApps = statisticsService.getTopApplicationsByDate(date, 10);
            int rank = 1;
            for (Map.Entry<Application, Long> entry : topApps) {
                Application app = entry.getKey();
                Long seconds = entry.getValue();

                Row row = topAppsSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rank++);
                row.createCell(1).setCellValue(app.getDisplayName());
                row.createCell(2).setCellValue(app.getCategory().getDisplayName());
                row.createCell(3).setCellValue(seconds);
                row.createCell(4).setCellValue(TimeFormatter.formatDuration(seconds));
            }

            for (int i = 0; i < topHeaders.length; i++) {
                topAppsSheet.autoSizeColumn(i);
            }

            // ===== 4. RÉSZLETES NAPLÓ MUNKALAP =====
            Sheet detailSheet = workbook.createSheet("Részletes napló");
            rowNum = 0;

            Row detailHeaderRow = detailSheet.createRow(rowNum++);
            String[] detailHeaders = {"Alkalmazás", "Kategória", "Kezdés", "Befejezés", "Időtartam (mp)", "Időtartam"};
            for (int i = 0; i < detailHeaders.length; i++) {
                Cell cell = detailHeaderRow.createCell(i);
                cell.setCellValue(detailHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Session> sessions = sessionDAO.findByDate(date);
            for (Session session : sessions) {
                Application app = applicationDAO.findById(session.getApplicationId());
                if (app != null) {
                    Row row = detailSheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(app.getDisplayName());
                    row.createCell(1).setCellValue(getCategoryDisplayName(session, app));
                    row.createCell(2).setCellValue(DateUtils.formatDateTime(session.getStartTime()));
                    row.createCell(3).setCellValue(session.getEndTime() != null ? DateUtils.formatDateTime(session.getEndTime()) : "");
                    row.createCell(4).setCellValue(session.getDurationSeconds());
                    row.createCell(5).setCellValue(TimeFormatter.formatDuration(session.getDurationSeconds()));
                }
            }

            for (int i = 0; i < detailHeaders.length; i++) {
                detailSheet.autoSizeColumn(i);
            }

            // Fájl mentése
            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                workbook.write(fileOut);
            }

            System.out.println("✓ Teljes Excel riport export sikeres: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("✗ Teljes Excel riport export hiba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ========== SEGÉD METÓDUSOK ==========

    /**
     * Fejléc stílus létrehozása
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Cím stílus létrehozása
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    /**
     * Dátum stílus létrehozása
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
        return style;
    }

    /**
     * Session szintű kategórianév feloldása exporthoz
     */
    private String getCategoryDisplayName(Session session, Application app) {
        if (session != null && session.getCategory() != null) {
            return session.getCategory().getDisplayName();
        }

        if (app != null && app.getCategory() != null) {
            return app.getCategory().getDisplayName();
        }

        return Category.OTHER.getDisplayName();
    }

    /**
     * CSV string escape
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Alapértelmezett fájlnév generálása
     */
    public static String generateDefaultFilename(String prefix, LocalDate date, String extension) {
        return prefix + "_" + DateUtils.formatDate(date) + "." + extension;
    }
}