package ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;

public class LabelledPasswordField extends HBox { // a hbox with a label and text field
	private Label label;
	private PasswordField passwordField;
	protected BooleanBinding empty;
	
	public LabelledPasswordField(String text) {
		super(10); // sets the spacing to 10px
		this.label = new Label(text);
		this.passwordField = new PasswordField();
		
		// add to the hbox so that they can be displayed
		this.getChildren().addAll(label, passwordField);
	}
	
	public LabelledPasswordField(String text, boolean required) { // an alternate constructor that gives the text field a boolean binding
		super(10);
		this.label = new Label(text);
		this.passwordField = new PasswordField();
		this.getChildren().addAll(label, passwordField);
		
		if (required) {
			this.empty = Bindings.createBooleanBinding(() -> { 
		        if (this.passwordField.getText().trim().isEmpty()) return true;
		        else return false;
		    }, this.passwordField.textProperty());
		}
	}
	
	public String getText() { // shortening the code
		return this.passwordField.getText();
	}
}
