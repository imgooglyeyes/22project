package backend;

import application.FarmSystem;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.LabelledTextField;
import ui.MadeFromPicker;
import ui.Styles;

/**
 * popup window to create a new product in the database
 *
 * <p>the product can be a harvest item or made from one harvest item, picked with the made-from picker.
 * the create button stays disabled while a field is empty, contains |, the price
 * is not a valid number, or the name already exists. FarmSystem checks everything again
 * and does the saving
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.3
 * @see MadeFromPicker
 * @see FarmSystem#createProduct(String, String, double, Integer)
 * @see FarmSystem#isNameTaken(String, Integer)
 */
public class ProductCreationWindow {

	private LabelledTextField nameField;
	private LabelledTextField descriptionField;
	private LabelledTextField priceField;
	private MadeFromPicker madeFromPicker;

	private Button create;
	private Button exit;
	private VBox vbox;
	private HBox buttonsHBox;
	private Scene createScene;
	private Stage createWindow;

	/**
	 * creates the window, call show() to display it
	 * <p>the screen passes its own refresh method so the window can tell it to redraw
	 *
	 * @param stage the main window, this window blocks it while open
	 * @param farmSystem the farm system that creates the product
	 * @param refresh called after the product is created so the screen redraws, can be null
	 */
	public ProductCreationWindow(Stage stage, FarmSystem farmSystem, Runnable refresh) {
		this.nameField = new LabelledTextField("Name:", true);
		this.descriptionField = new LabelledTextField("Description:", true);
		this.priceField = new LabelledTextField("Price:", true);

		// every harvest product can be the source, a new product starts as a harvest item
		this.madeFromPicker = new MadeFromPicker(farmSystem.getHarvestProducts(null), null);

		this.create = new Button("Create");
		this.exit = new Button("Cancel");

		// true while the name is already used by another product
		BooleanBinding duplicateName = Bindings.createBooleanBinding(
			() -> farmSystem.isNameTaken(this.nameField.getTextField().getText(), null),
			this.nameField.getTextField().textProperty()
		);

		// the create button is disabled while any of the checks fail
		create.disableProperty().bind(nameField.invalid.or(descriptionField.invalid).or(priceField.invalid).or(priceField.NaN).or(duplicateName));

		this.buttonsHBox = new HBox(10, exit, create);
		this.vbox = new VBox(10, nameField, descriptionField, priceField, madeFromPicker, buttonsHBox); // creates a vbox with a 10px spacing with these as children
		vbox.setStyle("-fx-padding: 20;");

		this.createScene = new Scene(vbox, 500, 350);
		Styles.apply(createScene); // loads the shared stylesheet

		this.createWindow = new Stage(); // creates a new window for the product creation
		createWindow.setTitle("Create New Product");
		createWindow.setScene(createScene);

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
				new Alert(AlertType.ERROR, error).showAndWait();
				return;
			}

			if (refresh != null) refresh.run();
			createWindow.close();
		});

		exit.setOnAction(event -> createWindow.close());

		createWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
		createWindow.initOwner(stage); // the window below it being the main stage
	}

	/** shows the window */
	public void show() { // just to make things simpler
		// make the new window actually appear
		createWindow.show();
	}
}