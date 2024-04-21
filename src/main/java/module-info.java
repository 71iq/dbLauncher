module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;
    requires mysql.connector.java;

    opens com.ehab.dbLauncher to javafx.fxml;
    exports com.ehab.dbLauncher;
}