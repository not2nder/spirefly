module gui.spirefly {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires fontawesomefx;
    requires java.sql;

    opens gui.spirefly to javafx.fxml;
    exports gui.spirefly;
}