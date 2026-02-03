package org.example.digitalisidomero.ui;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;
import org.example.digitalisidomero.service.CategoryService;
import org.example.digitalisidomero.service.ExportService;
import org.example.digitalisidomero.service.StatisticsService;
import org.example.digitalisidomero.util.DateUtils;
import org.example.digitalisidomero.util.TimeFormatter;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StatisticsController {

    // Dátumválasztó
    @FXML private DatePicker datePicker;
    @FXML private Button todayButton;
    @FXML private Button yesterdayButton;
    @FXML private Button thisWeekButton;

    // Összesítő labelek
    @FXML private Label selectedDateLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label sessionCountLabel;
    @FXML private Label avgSessionLabel;

    // Grafikonok
    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> weeklyBarChart;
    @FXML private CategoryAxis weeklyXAxis;
    @FXML private NumberAxis weeklyYAxis;

    // Top alkalmazások lista
    @FXML private ListView<String> topAppsListView;

    // Export gombok - CSV
    @FXML private Button exportDailyButton;
    @FXML private Button exportWeeklyButton;
    @FXML private Button exportFullReportButton;

    // Export gombok - Excel
    @FXML private Button exportDailyExcelButton;
    @FXML private Button exportWeeklyExcelButton;
    @FXML private Button exportFullReportExcelButton;

    @FXML private Label statusLabel;

    private StatisticsService statisticsService;
    private ExportService exportService;
    private CategoryService categoryService;

    private LocalDate selectedDate;

    @FXML
    public void initialize() {
        statisticsService = new StatisticsService();
        exportService = new ExportService();
        categoryService = new CategoryService();

        selectedDate = LocalDate.now();
        datePicker.setValue(selectedDate);

        // DatePicker change listener
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                selectedDate = newDate;
                refreshStatistics();
            }
        });

        // Első betöltés
        refreshStatistics();
    }

    // ========== DÁTUM NAVIGÁCIÓ ==========

    @FXML
    private void handleToday() {
        selectedDate = LocalDate.now();
        datePicker.setValue(selectedDate);
        refreshStatistics();
    }

    @FXML
    private void handleYesterday() {
        selectedDate = LocalDate.now().minusDays(1);
        datePicker.setValue(selectedDate);
        refreshStatistics();
    }

    @FXML
    private void handleThisWeek() {
        selectedDate = DateUtils.getStartOfWeek();
        datePicker.setValue(selectedDate);
        refreshWeeklyChart();
        showStatus("Heti nézet betöltve", "info");
    }

    @FXML
    private void handleRefresh() {
        refreshStatistics();
        showStatus("✓ Statisztikák frissítve", "success");
    }

    // ========== STATISZTIKÁK FRISSÍTÉSE ==========

    private void refreshStatistics() {
        refreshSummaryLabels();
        refreshCategoryPieChart();
        refreshWeeklyChart();
        refreshTopAppsList();
    }

    private void refreshSummaryLabels() {
        selectedDateLabel.setText(DateUtils.formatDateForDisplay(selectedDate) +
                " (" + DateUtils.getDayNameHungarian(selectedDate) + ")");

        long totalSeconds = statisticsService.getTotalSecondsByDate(selectedDate);
        totalTimeLabel.setText(TimeFormatter.formatDuration(totalSeconds));

        // Session szám (később bővíthető)
        sessionCountLabel.setText("-");
        avgSessionLabel.setText("-");
    }

    private void refreshCategoryPieChart() {
        categoryPieChart.getData().clear();

        Map<Category, Long> breakdown = statisticsService.getCategoryBreakdownByDate(selectedDate);

        for (Map.Entry<Category, Long> entry : breakdown.entrySet()) {
            if (entry.getValue() > 0) {
                Category category = entry.getKey();
                long seconds = entry.getValue();

                String label = CategoryService.getCategoryIcon(category) + " " +
                        category.getDisplayName() + " (" +
                        TimeFormatter.formatDuration(seconds) + ")";

                PieChart.Data slice = new PieChart.Data(label, seconds);
                categoryPieChart.getData().add(slice);
            }
        }

        categoryPieChart.setLegendVisible(true);
    }

    private void refreshWeeklyChart() {
        weeklyBarChart.getData().clear();

        LocalDate weekStart = DateUtils.getStartOfWeek(selectedDate);
        Map<LocalDate, Long> weeklyData = statisticsService.getWeeklyDailyBreakdown(weekStart);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Napi használati idő (perc)");

        for (Map.Entry<LocalDate, Long> entry : weeklyData.entrySet()) {
            LocalDate date = entry.getKey();
            long seconds = entry.getValue();
            long minutes = TimeFormatter.toMinutes(seconds);

            String dayLabel = DateUtils.getDayNameShortHungarian(date) + "\n" +
                    date.getDayOfMonth() + ".";

            series.getData().add(new XYChart.Data<>(dayLabel, minutes));
        }

        weeklyBarChart.getData().add(series);
    }

    private void refreshTopAppsList() {
        topAppsListView.getItems().clear();

        List<Map.Entry<Application, Long>> topApps =
                statisticsService.getTopApplicationsByDate(selectedDate, 10);

        int rank = 1;
        for (Map.Entry<Application, Long> entry : topApps) {
            Application app = entry.getKey();
            long seconds = entry.getValue();

            String icon = CategoryService.getCategoryIcon(app.getCategory());
            String item = String.format("%d. %s %s - %s",
                    rank++,
                    icon,
                    app.getDisplayName(),
                    TimeFormatter.formatDuration(seconds));

            topAppsListView.getItems().add(item);
        }

        if (topApps.isEmpty()) {
            topAppsListView.getItems().add("Nincs adat erre a napra");
        }
    }

    // ========== CSV EXPORTOK ==========

    @FXML
    private void handleExportDaily() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Napi riport exportálása CSV formátumban");
        fileChooser.setInitialFileName(ExportService.generateDefaultFilename("daily", selectedDate, "csv"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV fájl", "*.csv")
        );

        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            boolean success = exportService.exportDayToCSV(selectedDate, file);

            if (success) {
                showStatus("✓ Napi CSV riport exportálva: " + file.getName(), "success");
            } else {
                showStatus("✗ Export sikertelen!", "error");
            }
        }
    }

    @FXML
    private void handleExportWeekly() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Heti riport exportálása CSV formátumban");

        LocalDate weekStart = DateUtils.getStartOfWeek(selectedDate);
        fileChooser.setInitialFileName(ExportService.generateDefaultFilename("weekly", weekStart, "csv"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV fájl", "*.csv")
        );

        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            boolean success = exportService.exportWeekToCSV(weekStart, file);

            if (success) {
                showStatus("✓ Heti CSV riport exportálva: " + file.getName(), "success");
            } else {
                showStatus("✗ Export sikertelen!", "error");
            }
        }
    }

    @FXML
    private void handleExportFullReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Teljes riport exportálása CSV formátumban");
        fileChooser.setInitialFileName(ExportService.generateDefaultFilename("full_report", selectedDate, "csv"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV fájl", "*.csv")
        );

        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            boolean success = exportService.exportFullReportToCSV(selectedDate, file);

            if (success) {
                showStatus("✓ Teljes CSV riport exportálva: " + file.getName(), "success");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export sikeres");
                alert.setHeaderText("A teljes riport sikeresen exportálva!");
                alert.setContentText("Fájl: " + file.getAbsolutePath());
                alert.showAndWait();
            } else {
                showStatus("✗ Export sikertelen!", "error");
            }
        }
    }

    // ========== EXCEL EXPORTOK ==========

    @FXML
    private void handleExportDailyExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Napi riport exportálása Excel formátumban");
        fileChooser.setInitialFileName(ExportService.generateDefaultFilename("daily", selectedDate, "xlsx"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel fájl (*.xlsx)", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            boolean success = exportService.exportDayToExcel(selectedDate, file);

            if (success) {
                showStatus("✓ Napi Excel riport exportálva: " + file.getName(), "success");
            } else {
                showStatus("✗ Export sikertelen!", "error");
            }
        }
    }

    @FXML
    private void handleExportWeeklyExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Heti riport exportálása Excel formátumban");

        LocalDate weekStart = DateUtils.getStartOfWeek(selectedDate);
        fileChooser.setInitialFileName(ExportService.generateDefaultFilename("weekly", weekStart, "xlsx"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel fájl (*.xlsx)", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            boolean success = exportService.exportWeekToExcel(weekStart, file);

            if (success) {
                showStatus("✓ Heti Excel riport exportálva: " + file.getName(), "success");
            } else {
                showStatus("✗ Export sikertelen!", "error");
            }
        }
    }

    @FXML
    private void handleExportFullReportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Teljes riport exportálása Excel formátumban");
        fileChooser.setInitialFileName(ExportService.generateDefaultFilename("full_report", selectedDate, "xlsx"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel fájl (*.xlsx)", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            boolean success = exportService.exportFullReportToExcel(selectedDate, file);

            if (success) {
                showStatus("✓ Teljes Excel riport exportálva: " + file.getName(), "success");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export sikeres");
                alert.setHeaderText("A teljes riport sikeresen exportálva Excel formátumban!");
                alert.setContentText("Fájl: " + file.getAbsolutePath() +
                        "\n\nA riport 4 munkalapot tartalmaz:" +
                        "\n- Összefoglaló" +
                        "\n- Kategóriák" +
                        "\n- Top alkalmazások" +
                        "\n- Részletes napló");
                alert.showAndWait();
            } else {
                showStatus("✗ Export sikertelen!", "error");
            }
        }
    }

    // ========== SEGÉD METÓDUSOK ==========

    private Stage getStage() {
        return (Stage) statusLabel.getScene().getWindow();
    }

    private void showStatus(String message, String type) {
        statusLabel.setText(message);

        String color = switch (type) {
            case "success" -> "#2ecc71";
            case "error" -> "#e74c3c";
            case "warning" -> "#f39c12";
            default -> "#3498db";
        };

        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }
}