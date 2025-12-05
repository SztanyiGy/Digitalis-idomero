module org.example.digitalisidomero {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.digitalisidomero to javafx.fxml;
    exports org.example.digitalisidomero;
    exports org.example.digitalisidomero.ui;
    opens org.example.digitalisidomero.ui to javafx.fxml;
}