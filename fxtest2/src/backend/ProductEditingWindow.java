package backend;

import java.io.IOException;

import application.FarmSystem;
import application.Product;
import application.ProductImages;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ui.Dialogs;
import ui.LabelledTextField;
import ui.MadeFromPicker;
import ui.ImagePicker;

/**
 * popup window to edit the name, description, price and source of a product
 *
 * <p>the made-from picker starts on the current source and does not list the product itself.
 * it is locked when other products are made from this one, because that product has to stay a harvest item.
 * the save button stays disabled while a field is invalid or the name belongs to
 * another product, and a red note under the fields says why. FarmSystem checks everything again,
 * updates every copy of the product and does the saving.
 * the window itself (title, buttons, theme) comes from PopupWindow
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.4
 * @since 1.4 the window extends PopupWindow and tells why the save button is disabled
 * @see PopupWindow
 * @see MadeFromPicker
 * @see FarmSystem#editProduct(Product, String, String, double, Integer, String)
 * @see FarmSystem#isNameTaken(String, Integer)
 */
public class ProductEditingWindow extends PopupWindow {
	private LabelledTextField nameField;
	private LabelledTextField descriptionField;
	private LabelledTextField priceField;
	private MadeFromPicker madeFromPicker;
	private ImagePicker imagePicker;

	private Button confirm;

	/**
	 * creates the window, call show() to display it
	 * <p>the screen passes its own refresh method so the window can tell it to redraw
	 *
	 * @param stage the main window, this window blocks it while open
	 * @param farmSystem the farm system that edits the product
	 * @param targetProduct the product being edited, its values fill the fields
	 * @param refresh called after the edit so the screen redraws, can be null
	 */
	public ProductEditingWindow(Stage stage, FarmSystem farmSystem, Product targetProduct, Runnable refresh) {
		super(stage, "Edit Product", "Edit " + targetProduct.getName(), 480, 580);

		this.nameField = new LabelledTextField("Name:", true);
		this.descriptionField = new LabelledTextField("Description:", true);
		this.priceField = new LabelledTextField("Price:", true);
		this.confirm = new Button("Save");

		nameField.setText(targetProduct.getName());
		descriptionField.setText(targetProduct.getDescription());
		priceField.setText(Double.toString(targetProduct.getPrice()));

		// the product itself is left out of the list, it can't be made from itself
		this.madeFromPicker = new MadeFromPicker(
			farmSystem.getHarvestProducts(targetProduct.getId()),
			targetProduct.getMadeFrom()
		);

		// a product that others are made from has to stay a harvest item, so the picker is locked
		boolean hasMadeProducts = farmSystem.isSourceOfOthers(targetProduct.getId());
		madeFromPicker.setDisable(hasMadeProducts);
		this.imagePicker = new ImagePicker(targetProduct.getImage());

		// true while the name belongs to another product, the product's own name is fine
		BooleanBinding invalidName = Bindings.createBooleanBinding(
			() -> farmSystem.isNameTaken(this.nameField.getTextField().getText(), targetProduct.getId()),
			this.nameField.getTextField().textProperty()
		);

		// the save button is disabled while any of the checks fail
		confirm.disableProperty().bind(nameField.invalid.or(descriptionField.invalid).or(priceField.invalid).or(priceField.NaN).or(invalidName));

		// tells why the button is disabled
		Label problem = createProblemLabel(Bindings.createStringBinding(
			() -> {
				if (nameField.getText().contains("|") || descriptionField.getText().contains("|")) return "Name and description can't contain |";
				if (!nameField.getText().trim().isEmpty() && invalidName.get()) return "Another product already has this name";
				if (!priceField.getText().trim().isEmpty() && priceField.NaN.get()) return "Price must be a number, 0 or higher";
				return "";
			},
			nameField.getTextField().textProperty(),
			descriptionField.getTextField().textProperty(),
			priceField.getTextField().textProperty()
		));

		form.getChildren().addAll(nameField, descriptionField, priceField, madeFromPicker, imagePicker);

		if (hasMadeProducts) { // tells the user why the picker is locked
			Label lockedNote = new Label("Other products are made from this one, so it stays a harvest item");
			lockedNote.getStyleClass().add("caption");
			lockedNote.setWrapText(true);
			form.getChildren().add(lockedNote);
		}

		form.getChildren().add(problem);
		finish(confirm);

		// asks the farm system to apply the edit, stays open if it says no
		confirm.setOnAction(event -> {
			String original = targetProduct.getImage();
			String image;
			try {
				image = imagePicker.commit();
			} catch (IOException e) {
				Dialogs.error(window, e.getMessage());
				return;
			}

			// FarmSystem returns null when it worked, or an error message
			String error = farmSystem.editProduct(
				targetProduct,
				nameField.getText(),
				descriptionField.getText(),
				Double.parseDouble(priceField.getText()), // safe, the button is disabled when the price is not a number
				madeFromPicker.getSelectedId(), // null means a harvest item
				image
			);

			if (error != null) {
				if (image != null && !image.equals(original)) ProductImages.delete(image);
				Dialogs.error(window, error);
				return;
			}

			if (original != null && !original.equals(image)) ProductImages.delete(original);

			if (refresh != null) refresh.run();
			window.close();
		});
	}
}