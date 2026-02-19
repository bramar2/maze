module me.bramar.mazebfs {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;


    opens me.bramar.maze to javafx.fxml;
    exports me.bramar.maze;
}