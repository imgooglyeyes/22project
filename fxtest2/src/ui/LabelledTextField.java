package ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * a hbox with a label and a text field, used by the product popup windows
 *
 * <p>the alternate constructor adds two bindings: invalid is true while the text is empty or contains |
 * (the separator in the save files), and NaN is true while the text is not a number of 0 or higher.
 * the label has a fixed width and the field fills the rest, so stacked fields line up with each other
 * and with the made-from picker
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.2
 * @since 1.1 the label has the field-label style and a fixed width, the field grows
 * @since 1.2 the comments were turned into javadoc
 * @see MadeFromPicker
 */
public class LabelledTextField extends HBox {

	/** the width of every label, so the fields of different rows start at the same place */
	public static final double LABEL_WIDTH = 90;

	private Label label;
	private TextField textField;

	/** true while the text is empty or contains |, null if the field was created without the validity check */
	public BooleanBinding invalid;
	/** true while the text is not a number of 0 or higher, the popups use it for the price field */
	public BooleanBinding NaN;

	/**
	 * creates the field without any validity check
	 *
	 * @param text the text of the label
	 */
	public LabelledTextField(String text) {
		super(10); // sets the spacing to 10px
		this.label = new Label(text);
		this.textField = new TextField();

		// add to the hbox so that they can be displayed
		this.getChildren().addAll(label, textField);
		style();
	}

	/**
	 * creates the field, and gives it the invalid binding if asked
	 * <p>the NaN binding is always created, it is only read by the price fields
	 *
	 * @param text the text of the label
	 * @param checkValidity true to create the invalid binding
	 */
	public LabelledTextField(String text, boolean checkValidity) {
		super(10);
		this.label = new Label(text);
		this.textField = new TextField();
		this.getChildren().addAll(label, textField);
		style();

		if (checkValidity) {
			this.invalid = Bindings.createBooleanBinding(() -> {
				if (this.textField.getText().trim().contains("|")) return true;
				if (this.textField.getText().trim().isEmpty()) return true;
				else return false;
			}, this.textField.textProperty());
		}

		this.NaN = Bindings.createBooleanBinding(() -> {
			try {
				double value = Double.parseDouble(this.textField.getText());
				return !Double.isFinite(value) || value < 0;
			} catch (NumberFormatException e) {
				return true;
			}
		}, this.textField.textProperty());
	}

	/** gives the label its look and width, and lets the field fill the rest of the row */
	private void style() {
		label.getStyleClass().add("field-label");
		label.setMinWidth(LABEL_WIDTH);
		HBox.setHgrow(textField, Priority.ALWAYS);
		setAlignment(Pos.CENTER_LEFT);
	}

	/** @return the text typed in the field */
	public String getText() {
		return this.textField.getText();
	}

	/** @param text the new text of the field */
	public void setText(String text) {
		this.textField.setText(text);
	}

	/**
	 * gets the real text field, used to bind to its text property or to edit it from outside this class
	 *
	 * @return the text field
	 */
	public TextField getTextField() {
		return textField;
	}
}