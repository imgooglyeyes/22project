package backend;

import application.FarmSystem;
import application.Product;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.LabelledTextField;
import ui.MadeFromPicker;
import ui.Styles;

/**
 * popup window to edit the name, description, price and source of a product
 *
 * <p>the made-from picker starts on the current source and does not list the product itself.
 * it is locked when other products are made from this one, because that product has to stay a harvest item.
 * the confirm button stays disabled while a field is invalid or the name belongs to
 * another product. FarmSystem checks everything again, updates every copy of the product
 * and does the saving
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.3
 * @see MadeFromPicker
 * @see FarmSystem#editProduct(Product, String, String, double, Integer)
 * @see FarmSystem#isNameTaken(String, Integer)
 */
public class ProductEditingWindow {
	private LabelledTextField nameField;
	private LabelledTextField descriptionField;
	private LabelledTextField priceField;
	private MadeFromPicker madeFromPicker;

	private Button confirm;
	private Button exit;
	private VBox vbox;
	private HBox buttonsHBox;
	private Scene editScene;
	private Stage editWindow;

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
		this.nameField = new LabelledTextField("Name:", true);
		this.descriptionField = new LabelledTextField("Description:", true);
		this.priceField = new LabelledTextField("Price:", true);
		this.confirm = new Button("Confirm");
		this.exit = new Button("Cancel");

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

		// true while the name belongs to another product, the product's own name is fine
		BooleanBinding invalidName = Bindings.createBooleanBinding(
			() -> farmSystem.isNameTaken(this.nameField.getTextField().getText(), targetProduct.getId()),
			this.nameField.getTextField().textProperty()
		);

		// the confirm button is disabled while any of the checks fail
		confirm.disableProperty().bind(nameField.invalid.or(descriptionField.invalid).or(priceField.invalid).or(priceField.NaN).or(invalidName));

		this.buttonsHBox = new HBox(10, exit, confirm);
		this.vbox = new VBox(10, nameField, descriptionField, priceField, madeFromPicker); // creates a vbox with a 10px spacing with these as children

		if (hasMadeProducts) { // tells the user why the picker is locked
			Label lockedNote = new Label("Other products are made from this one, so it stays a harvest item");
			lockedNote.setWrapText(true);
			vbox.getChildren().add(lockedNote);
		}

		vbox.getChildren().add(buttonsHBox);
		vbox.setStyle("-fx-padding: 20;");

		this.editScene = new Scene(vbox, 500, 350);
		Styles.apply(editScene); // loads the shared stylesheet

		this.editWindow = new Stage(); // creates a new window for the product editing
		editWindow.setTitle("Edit Product");
		editWindow.setScene(editScene);

		// asks the farm system to apply the edit, stays open if it says no
		confirm.setOnAction(event -> {
			// FarmSystem returns null when it worked, or an error message
			String error = farmSystem.editProduct(
				targetProduct,
				nameField.getText(),
				descriptionField.getText(),
				Double.parseDouble(priceField.getText()), // safe, the button is disabled when the price is not a number
				madeFromPicker.getSelectedId() // null means a harvest item
			);

			if (error != null) {
				new Alert(AlertType.ERROR, error).showAndWait();
				return;
			}

			if (refresh != null) refresh.run();
			editWindow.close();
		});

		exit.setOnAction(event -> editWindow.close());

		editWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
		editWindow.initOwner(stage); // the window below it being the main stage
	}

	/** shows the window */
	public void show() { // just to make things simpler
		// make the new window actually appear
		editWindow.show();
	}
}	