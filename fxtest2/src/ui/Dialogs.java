package ui;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;
import shell.SceneManager;

/**
 * the themed dialogs of the app, so a page or a popup does not build its own alert
 *
 * <p>a dialog is its own window, so it does not get the stylesheet by itself. theme() copies the stylesheets
 * of the owner and adds the root class, which gives the dialog the color tokens that the stylesheet defines on .root.
 * success messages are toasts, so dialogs are only kept for errors, warnings and for confirmations of things that can't be undone
 *
 * <p>a warning or an error can come with a next step, a short line that tells the person what to do about the problem
 *
 * @author Shawn Alfred Padilla
 * @version 1.2
 * @since 1.1 the unused info dialog was removed, toasts replaced it
 * @since 1.2 warning and the next step line were added
 * @see SceneManager
 * @see Toast
 * @see WarningNote
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
        error(owner, message, null);
    }

    /**
     * shows an error with a line that tells the person what to do next
     *
     * @param owner the window the dialog belongs to
     * @param message what went wrong
     * @param nextStep what the person can do about it, null or empty for none
     * @since 1.2
     */
    public static void error(Window owner, String message, String nextStep) {
        Alert alert = new Alert(AlertType.ERROR, withNextStep(message, nextStep), ButtonType.OK);
        theme(alert, owner, "Something went wrong");
        alert.showAndWait();
    }

    /**
     * shows a warning for an action that can't go on, with a line that tells the person what to do next
     * <p>use it when nothing is broken but the action is not possible right now, like an empty inventory
     *
     * @param owner the window the dialog belongs to
     * @param message why the action can't go on
     * @param nextStep what the person can do about it, null or empty for none
     * @since 1.2
     */
    public static void warning(Window owner, String message, String nextStep) {
        Alert alert = new Alert(AlertType.WARNING, withNextStep(message, nextStep), ButtonType.OK);
        theme(alert, owner, "Heads up");
        alert.showAndWait();
    }

    /**
     * puts the next step under the message
     *
     * @param message the problem
     * @param nextStep what to do about it, can be null
     * @return the message alone if there is no next step, or both with a blank line between
     */
    private static String withNextStep(String message, String nextStep) {
        if (nextStep == null || nextStep.isEmpty()) return message;
        return message + "\n\nWhat to do next: " + nextStep;
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