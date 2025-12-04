module org.example.digitalisidomero {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.digitalisidomero to javafx.fxml;
    exports org.example.digitalisidomero;
}