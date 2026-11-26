package ui;

import javafx.scene.Scene;

/**
 * small helper that loads the shared stylesheet into a scene and holds the css class names
 * that more than one screen uses
 *
 * <p>call Styles.apply(scene) once after creating any scene.
 * the class names match the rules in application.css, so a screen never needs an inline color
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.3
 * @since 1.1 the row class names were added
 * @since 1.2 the unused selectRow and ROW_SELECTED were removed, the tables handle selection now
 */
public class Styles {

    /** path is relative to the class path root (bin) */
    private static final String SHEET = "/application/application.css";

    /** css class of one row in a list of hboxes */
    public static final String ROW = "row";

    /**
     * adds the shared stylesheet to a scene
     *
     * @param scene the scene to style
     */
    public static void apply(Scene scene) {
        scene.getStylesheets().add(Styles.class.getResource(SHEET).toExternalForm());
    }
}