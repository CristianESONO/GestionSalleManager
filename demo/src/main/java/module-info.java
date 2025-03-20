module com {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jakarta.persistence; // Remplace java.persistence par jakarta.persistence

    // Nécessaire pour Hibernate et Spring
    requires org.hibernate.orm.core;
    requires javafx.graphics;
    requires java.persistence;
    requires org.apache.pdfbox;
    requires javafx.base;

    // Ouvre les packages pour la réflexion (Hibernate, Spring)
    opens com to javafx.fxml, org.hibernate.orm.core;
    opens com.entities to org.hibernate.orm.core; // Si tes modèles sont dans ce package
    opens com.controllers to javafx.fxml; // Si tes contrôleurs sont ici
    

    // Exporte ton package principal
    exports com;
    exports com.controllers;
    exports com.entities;
}
