package ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;

/**
 * a hbox with a label and a password field
 *
 * <p>the alternate constructor adds an invalid binding, a password is invalid
 * while it is empty or contains | (the separator in the save files)
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.1
 * @see LabelledTextField
 */
public class LabelledPasswordField extends HBox {
	private Label label;
	private PasswordField passwordField;
	protected BooleanBinding invalid;

	/**
	 * creates the field without any validity check
	 *
	 * @param text the text of the label
	 */
	public LabelledPasswordField(String text) {
		super(10); // sets the spacing to 10px
		this.label = new Label(text);
		this.passwordField = new PasswordField();

		// add to the hbox so that they can be displayed
		this.getChildren().addAll(label, passwordField);
	}

	/**
	 * creates the field, and gives it an invalid binding if asked
	 *
	 * @param text the text of the label
	 * @param checkValidity true to create the invalid binding
	 */
	public LabelledPasswordField(String text, boolean checkValidity) { // an alternate constructor that gives the text field a boolean binding
		super(10);
		this.label = new Label(text);
		this.passwordField = new PasswordField();
		this.getChildren().addAll(label, passwordField);

		if (checkValidity) {
			this.invalid = Bindings.createBooleanBinding(() -> {
				if (this.passwordField.getText().trim().contains("|")) return true;
				if (this.passwordField.getText().trim().isEmpty()) return true;
		        else return false;
		    }, this.passwordField.textProperty());
		}
	}

	/** @return the text typed in the field */
	public String getText() { // shortening the code
		return this.passwordField.getText();
	}

	/**
	 * gets the real password field, used to bind to its text property
	 *
	 * @return the password field
	 * @since 1.1
	 */
	public PasswordField getPasswordField() {
		return passwordField;
	}
}