package ui;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

/**
 * a password field with an eye button that shows or hides the typed text
 *
 * <p>it holds a password field and a plain text field on top of each other, only one is visible at a time.
 * their text is bound both ways, so switching never loses what was typed.
 * the eye button sits on the right edge, the reveal-field css class adds the padding
 * that keeps the text from going under it
 *
 * <p>the eye button is not in the tab order, so tab goes from this field straight to the next one
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see AuthPage
 */
public class RevealPasswordField extends StackPane {

    /** the field that hides the text with dots */
    private final PasswordField hidden = new PasswordField();
    /** the field that shows the same text as plain text */
    private final TextField shown = new TextField();
    /** the eye button that swaps the two fields */
    private final Button eye = new Button();

    /**
     * creates the field, it starts with the text hidden
     *
     * @param prompt the faint text shown while the field is empty
     */
    public RevealPasswordField(String prompt) {
        getStyleClass().add("reveal-field");

        hidden.setPromptText(prompt);
        shown.setPromptText(prompt);

        // both fields always hold the same text
        shown.textProperty().bindBidirectional(hidden.textProperty());

        // managed false takes the field out of the layout so it does not change the size
        shown.setVisible(false);
        shown.setManaged(false);

        eye.getStyleClass().add("icon-button");
        eye.setGraphic(Icons.of(Icons.EYE, 18));
        eye.setFocusTraversable(false);
        eye.setAccessibleText("Show or hide the password");
        eye.setTooltip(new Tooltip("Show or hide the password"));
        eye.setOnAction(event -> setRevealed(!shown.isVisible()));

        StackPane.setAlignment(eye, Pos.CENTER_RIGHT);
        StackPane.setMargin(eye, new Insets(0, 6, 0, 0));

        getChildren().addAll(hidden, shown, eye);
    }

    /**
     * shows or hides the typed text and keeps the cursor at the end
     *
     * @param reveal true to show the plain text, false to hide it with dots
     */
    private void setRevealed(boolean reveal) {
        shown.setVisible(reveal);
        shown.setManaged(reveal);
        hidden.setVisible(!reveal);
        hidden.setManaged(!reveal);

        // the eye is tinted while the text is shown
        eye.getStyleClass().remove("eye-active");
        if (reveal) eye.getStyleClass().add("eye-active");

        TextField now = reveal ? shown : hidden;
        now.requestFocus();
        now.positionCaret(now.getLength());
    }

    /** @return the text typed in the field */
    public String getText() {
        return hidden.getText();
    }

    /** @return the text property, bind a check to it or listen for changes */
    public StringProperty textProperty() {
        return hidden.textProperty();
    }

    /** empties the field */
    public void clear() {
        hidden.clear();
    }

    /**
     * sets what happens when enter is pressed in the field, whether the text is shown or hidden
     *
     * @param action the code to run
     */
    public void setOnSubmit(Runnable action) {
        hidden.setOnAction(event -> action.run());
        shown.setOnAction(event -> action.run());
    }

    /** gives the focus to the field that is visible right now */
    @Override
    public void requestFocus() {
        (shown.isVisible() ? shown : hidden).requestFocus();
    }
}