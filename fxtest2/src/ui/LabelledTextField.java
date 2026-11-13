package ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;


public class LabelledTextField extends HBox { // a hbox with a label and text field
	private Label label;
	private TextField textField;
	public BooleanBinding empty;
	public BooleanBinding NaN;
	
	public LabelledTextField(String text) {
		super(10); // sets the spacing to 10px
		this.label = new Label(text);
		this.textField = new TextField();
		
		// add to the hbox so that they can be displayed
		this.getChildren().addAll(label, textField);
	}
	
	public LabelledTextField(String text, boolean required) { // an alternate constructor that gives the text field a boolean binding
		super(10);
		this.label = new Label(text);
		this.textField = new TextField();
		this.getChildren().addAll(label, textField);
		
		if (required) {
			this.empty = Bindings.createBooleanBinding(() -> { 
		        if (this.textField.getText().trim().isEmpty()) return true;
		        else return false;
		    }, this.textField.textProperty());
			this.NaN = Bindings.createBooleanBinding(() -> { 
				try{
					double val = Double.parseDouble(this.textField.getText());
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
		    }, this.textField.textProperty());
		}
	}
	
	public String getText() { // shortening the code
		return this.textField.getText();
	}
	
	public void setText(String t) {
		textField.setText(t);
	}
}
