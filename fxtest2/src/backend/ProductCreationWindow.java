package backend;

import application.FarmSystem;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ui.Dialogs;
import ui.LabelledTextField;
import ui.MadeFromPicker;

/**
 * popup window to create a new product in the database
 *
 * <p>the product can be a harvest item or made from one harvest item, picked with the made-from picker.
 * the create button stays disabled while a field is empty, contains |, the price
 * is not a valid number, or the name already exists, and a red note under the fields says why.
 * FarmSystem checks everything again and does the saving.
 * the window itself (title, buttons, theme) comes from PopupWindow
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.4
 * @since 1.4 the window extends PopupWindow and tells why the create button is disabled
 * @see PopupWindow
 * @see MadeFromPicker
 * @see FarmSystem#createProduct(String, String, double, Integer)
 * @see FarmSystem#isNameTaken(String, Integer)
 */
public class ProductCreationWindow extends PopupWindow {

	private LabelledTextField nameField;
	private LabelledTextField descriptionField;
	private LabelledTextField priceField;
	private MadeFromPicker madeFromPicker;

	private Button create;

	/**
	 * creates the window, call show() to display it
	 * <p>the screen passes its own refresh method so the window can tell it to redraw
	 *
	 * @param stage the main window, this window blocks it while open
	 * @param farmSystem the farm system that creates the product
	 * @param refresh called after the product is created so the screen redraws, can be null
	 */
	public ProductCreationWindow(Stage stage, FarmSystem farmSystem, Runnable refresh) {
		super(stage, "Create New Product", "Create a new product", 480, 440);

		this.nameField = new LabelledTextField("Name:", true);
		this.descriptionField = new LabelledTextField("Description:", true);
		this.priceField = new LabelledTextField("Price:", true);
		priceField.getTextField().setPromptText("0.00");

		// every harvest product can be the source, a new product starts as a harvest item
		this.madeFromPicker = new MadeFromPicker(farmSystem.getHarvestProducts(null), null);

		this.create = new Button("Create");

		// true while the name is already used by another product
		BooleanBinding duplicateName = Bindings.createBooleanBinding(
			() -> farmSystem.isNameTaken(this.nameField.getTextField().getText(), null),
			this.nameField.getTextField().textProperty()
		);

		// the create button is disabled while any of the checks fail
		create.disableProperty().bind(nameField.invalid.or(descriptionField.invalid).or(priceField.invalid).or(priceField.NaN).or(duplicateName));

		// tells why the button is disabled, only after the person typed in the field
		Label problem = createProblemLabel(Bindings.createStringBinding(
			() -> {
				if (nameField.getText().contains("|") || descriptionField.getText().contains("|")) return "Name and description can't contain |";
				if (!nameField.getText().trim().isEmpty() && duplicateName.get()) return "A product with this name already exists";
				if (!priceField.getText().trim().isEmpty() && priceField.NaN.get()) return "Price must be a number, 0 or higher";
				return "";
			},
			nameField.getTextField().textProperty(),
			descriptionField.getTextField().textProperty(),
			priceField.getTextField().textProperty()
		));

		form.getChildren().addAll(nameField, descriptionField, priceField, madeFromPicker, problem);
		finish(create);

		// asks the farm system to create the product, stays open if it says no
		create.setOnAction(event -> {
			// FarmSystem returns null when it worked, or an error message
			String error = farmSystem.createProduct(
				nameField.getText(),
				descriptionField.getText(),
				Double.parseDouble(priceField.getText()), // safe, the button is disabled when the price is not a number
				madeFromPicker.getSelectedId() // null means a harvest item
			);

			if (error != null) {
				Dialogs.error(window, error);
				return;
			}

			if (refresh != null) refresh.run();
			window.close();
		});
	}
}