package org.example.digitalisidomero.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.digitalisidomero.config.AppConfig;
import org.example.digitalisidomero.database.dao.ApplicationDAO;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;
import org.example.digitalisidomero.service.CategoryService;

import java.util.List;

public class SettingsController {

    // Általános beállítások
    @FXML private Spinner<Integer> idleThresholdSpinner;
    @FXML private Spinner<Integer> dailyTargetSpinner;
    @FXML private CheckBox autostartCheckBox;
    @FXML private CheckBox notificationsCheckBox;
    @FXML private CheckBox minimizeToTrayCheckBox;

    // Alkalmazás kategória kezelés
    @FXML private TableView<Application> applicationsTable;
    @FXML private TableColumn<Application, String> appNameColumn;
    @FXML private TableColumn<Application, String> appDisplayNameColumn;
    @FXML private TableColumn<Application, String> appCategoryColumn;

    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private Button updateCategoryButton;
    @FXML private Button deleteAppButton;

    @FXML private Label statusLabel;

    private ApplicationDAO applicationDAO;
    private CategoryService categoryService;
    private AppConfig appConfig;

    @FXML
    public void initialize() {
        applicationDAO = new ApplicationDAO();
        categoryService = new CategoryService();
        appConfig = AppConfig.getInstance();

        // Spinner-ek inicializálása
        initializeSpinners();

        // Táblázat inicializálása
        initializeTable();

        // Kategória combo box inicializálása
        initializeCategoryComboBox();

        // Beállítások betöltése
        loadSettings();

        // Alkalmazások betöltése
        loadApplications();

        // Táblázat kiválasztás kezelése
        applicationsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean hasSelection = newSelection != null;
                    updateCategoryButton.setDisable(!hasSelection);
                    deleteAppButton.setDisable(!hasSelection);

                    if (hasSelection) {
                        categoryComboBox.setValue(newSelection.getCategory());
                    }
                }
        );
    }

    private void initializeSpinners() {
        // Inaktivitási határidő (1-60 perc)
        SpinnerValueFactory<Integer> idleFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 5, 1);
        idleThresholdSpinner.setValueFactory(idleFactory);

        // Napi cél (1-24 óra)
        SpinnerValueFactory<Integer> targetFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24, 8, 1);
        dailyTargetSpinner.setValueFactory(targetFactory);
    }

    private void initializeTable() {
        appNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        appDisplayNameColumn.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        appCategoryColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        CategoryService.getCategoryIcon(cellData.getValue().getCategory()) + " " +
                                cellData.getValue().getCategory().getDisplayName()
                )
        );

        // Sor színezése kategória szerint
        applicationsTable.setRowFactory(tv -> new TableRow<Application>() {
            @Override
            protected void updateItem(Application app, boolean empty) {
                super.updateItem(app, empty);
                if (app == null || empty) {
                    setStyle("");
                } else {
                    String color = CategoryService.getCategoryColor(app.getCategory());
                    setStyle("-fx-background-color: " + color + "22;"); // 22 = átlátszóság
                }
            }
        });
    }

    private void initializeCategoryComboBox() {
        categoryComboBox.setItems(FXCollections.observableArrayList(Category.values()));

        // Custom cell factory a kategória ikonokkal
        categoryComboBox.setCellFactory(listView -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(CategoryService.getCategoryIcon(category) + " " + category.getDisplayName());
                }
            }
        });

        // Button cell (kiválasztott elem megjelenítése)
        categoryComboBox.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(CategoryService.getCategoryIcon(category) + " " + category.getDisplayName());
                }
            }
        });
    }

    private void loadSettings() {
        idleThresholdSpinner.getValueFactory().setValue(appConfig.getIdleThresholdMinutes());
        dailyTargetSpinner.getValueFactory().setValue(appConfig.getDailyTargetHours());
        autostartCheckBox.setSelected(appConfig.isAutostart());
        notificationsCheckBox.setSelected(appConfig.isShowNotifications());
        minimizeToTrayCheckBox.setSelected(appConfig.isMinimizeToTray());
    }

    private void loadApplications() {
        List<Application> apps = applicationDAO.findAll();
        applicationsTable.setItems(FXCollections.observableArrayList(apps));
    }

    @FXML
    private void handleUpdateCategory() {
        Application selectedApp = applicationsTable.getSelectionModel().getSelectedItem();
        Category newCategory = categoryComboBox.getValue();

        if (selectedApp == null) {
            showStatus("⚠ Válassz ki egy alkalmazást!", "warning");
            return;
        }

        if (newCategory == null) {
            showStatus("⚠ Válassz ki egy kategóriát!", "warning");
            return;
        }

        // Kategória frissítése
        boolean success = categoryService.updateApplicationCategory(selectedApp.getId(), newCategory);

        if (success) {
            showStatus("✓ Kategória frissítve: " + selectedApp.getDisplayName(), "success");
            loadApplications(); // Táblázat frissítése
        } else {
            showStatus("✗ Kategória frissítése sikertelen!", "error");
        }
    }

    @FXML
    private void handleDeleteApplication() {
        Application selectedApp = applicationsTable.getSelectionModel().getSelectedItem();

        if (selectedApp == null) {
            showStatus("⚠ Válassz ki egy alkalmazást!", "warning");
            return;
        }

        // Megerősítés dialógus
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Alkalmazás törlése");
        alert.setHeaderText("Biztosan törölni szeretnéd ezt az alkalmazást?");
        alert.setContentText(selectedApp.getDisplayName() + "\n\n" +
                "Figyelem! Az alkalmazáshoz tartozó összes session is törlésre kerül!");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = applicationDAO.delete(selectedApp.getId());

                if (success) {
                    showStatus("✓ Alkalmazás törölve: " + selectedApp.getDisplayName(), "success");
                    loadApplications();
                } else {
                    showStatus("✗ Alkalmazás törlése sikertelen!", "error");
                }
            }
        });
    }

    @FXML
    private void handleSaveSettings() {
        // Beállítások mentése
        appConfig.setIdleThresholdMinutes(idleThresholdSpinner.getValue());
        appConfig.setDailyTargetHours(dailyTargetSpinner.getValue());
        appConfig.setAutostart(autostartCheckBox.isSelected());
        appConfig.setShowNotifications(notificationsCheckBox.isSelected());
        appConfig.setMinimizeToTray(minimizeToTrayCheckBox.isSelected());

        appConfig.saveConfig();

        showStatus("✓ Beállítások mentve!", "success");

        // Információs dialógus
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Beállítások mentve");
        alert.setHeaderText("A beállítások sikeresen mentve!");
        alert.setContentText("Néhány beállítás csak újraindítás után lép érvénybe.");
        alert.showAndWait();
    }

    @FXML
    private void handleResetSettings() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Beállítások visszaállítása");
        alert.setHeaderText("Biztosan visszaállítod az alapértelmezett beállításokat?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Alapértelmezett értékek
                idleThresholdSpinner.getValueFactory().setValue(5);
                dailyTargetSpinner.getValueFactory().setValue(8);
                autostartCheckBox.setSelected(false);
                notificationsCheckBox.setSelected(true);
                minimizeToTrayCheckBox.setSelected(true);

                showStatus("✓ Beállítások visszaállítva!", "success");
            }
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
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