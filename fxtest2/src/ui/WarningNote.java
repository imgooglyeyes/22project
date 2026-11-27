package ui;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * a small amber box that tells the person what is wrong and what to do next
 *
 * <p>it is the inline warning of the popup windows, shown while a value is wrong or risky.
 * it takes no space while the message is empty, so a form has no gap when everything is fine.
 * the look comes from the warning-note css classes, there are no inline colors
 *
 * <p>both texts are observable, so the box follows what the person types.
 * the next step line hides itself when it is empty
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see Ui#collapseWhenEmpty(Label)
 * @see popup.PopupWindow#createWarningNote(ObservableValue, ObservableValue)
 */
public class WarningNote extends VBox {

    /**
     * creates the warning box
     *
     * @param message what is wrong, an empty text hides the whole box
     * @param nextStep what the person can do about it, an empty text hides only this line
     */
    public WarningNote(ObservableValue<String> message, ObservableValue<String> nextStep) {
        super(4); // sets the spacing to 4px
        getStyleClass().add("warning-note");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("warning-message");
        messageLabel.setWrapText(true);
        messageLabel.textProperty().bind(message);

        Label nextLabel = new Label();
        nextLabel.getStyleClass().add("warning-next");
        nextLabel.setWrapText(true);
        nextLabel.textProperty().bind(nextStep);
        Ui.collapseWhenEmpty(nextLabel);

        // the whole box follows the message, so an empty message leaves no gap
        managedProperty().bind(messageLabel.textProperty().isNotEmpty());
        visibleProperty().bind(messageLabel.textProperty().isNotEmpty());

        getChildren().addAll(messageLabel, nextLabel);
    }
}