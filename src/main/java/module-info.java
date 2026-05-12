module com.satellitesim {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // JDBC
    requires java.sql;

    // Export packages for JavaFX reflection
    opens com.satellitesim.ui to javafx.fxml, javafx.graphics;
    exports com.satellitesim.ui;
    exports com.satellitesim.core.models;
    exports com.satellitesim.core.constants;
    exports com.satellitesim.core.utils;
    exports com.satellitesim.services;
    exports com.satellitesim.services.routing;
    exports com.satellitesim.data.repository;
    exports com.satellitesim.data.db;
}
