module org.example.digitalisidomero {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.desktop;


    opens org.example.digitalisidomero to javafx.fxml;
    exports org.example.digitalisidomero;
    exports org.example.digitalisidomero.ui;
    opens org.example.digitalisidomero.ui to javafx.fxml;
}