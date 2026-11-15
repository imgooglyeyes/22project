package ui;

import javafx.scene.Scene;

// small helper that loads the shared stylesheet into a scene
// call Styles.apply(scene) once after creating any scene

public class Styles {

    // path is relative to the class path root (bin)
    private static final String SHEET = "/application/application.css";

    public static void apply(Scene scene) {
        scene.getStylesheets().add(Styles.class.getResource(SHEET).toExternalForm());
    }
}