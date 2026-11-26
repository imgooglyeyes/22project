package ui;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

/**
 * the themed dialogs of the app, so a page or a popup does not build its own alert
 *
 * <p>a dialog is its own window, so it does not get the stylesheet by itself. theme() copies the stylesheets
 * of the owner and adds the root class, which gives the dialog the color tokens that the stylesheet defines on .root.
 * success messages are toasts, so dialogs are only kept for errors and for confirmations of things that can't be undone
 *
 * @author Shawn Alfred Padilla
 * @version 1.1
 * @since 1.1 the unused info dialog was removed, toasts replaced it
 * @see SceneManager
 * @see Toast
 */
public class Dialogs {

    /**
     * asks the person to confirm an action, used before anything that can't be undone
     *
     * @param owner the window the dialog belongs to
     * @param message what is about to happen
     * @return true if they pressed ok
     */
    public static boolean confirm(Window owner, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        theme(alert, owner, "Please confirm");
        alert.getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("secondary");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * shows an error, for example the message FarmSystem returns when an action is refused
     *
     * @param owner the window the dialog belongs to
     * @param message what went wrong
     */
    public static void error(Window owner, String message) {
        Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
        theme(alert, owner, "Something went wrong");
        alert.showAndWait();
    }

    /**
     * gives a dialog the app theme and removes the parts that are not needed
     *
     * @param alert the dialog to style
     * @param owner the window the dialog belongs to, its stylesheets are copied
     * @param title the title of the dialog window
     */
    public static void theme(Alert alert, Window owner, String title) {
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setGraphic(null);

        DialogPane pane = alert.getDialogPane();
        if (owner != null && owner.getScene() != null) {
            pane.getStylesheets().addAll(owner.getScene().getStylesheets());
        }
        pane.getStyleClass().add("root");
    }
}