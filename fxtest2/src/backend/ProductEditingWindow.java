package backend;

import application.Product;
import application.ProductContainer;
import application.ProductsFileHandler;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.LabelledTextField;
import ui.Styles;

/**
 * popup window to edit the name, description and price of a product
 *
 * <p>the change is applied to the database, inventory and shelf copies of the product,
 * then all three files are saved. the confirm button stays disabled while a field is
 * invalid or the name belongs to another product
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.1
 * @see ProductsFileHandler#saveAll(ProductContainer, ProductContainer, ProductContainer)
 */
public class ProductEditingWindow {
	private LabelledTextField nameField;
	private LabelledTextField descriptionField;
	private LabelledTextField priceField;

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
	 * @param database every product and its details
	 * @param inventoryData the products and stock in the inventory
	 * @param shelfData the products and stock on the shelf
	 * @param targetProduct the product being edited, its values fill the fields
	 * @param refresh called after the edit so the screen redraws, can be null
	 */
	public ProductEditingWindow(Stage stage, ProductContainer database, ProductContainer inventoryData, ProductContainer shelfData, Product targetProduct, Runnable refresh) {
		this.nameField = new LabelledTextField("Name:", true);
		this.descriptionField = new LabelledTextField("Description:", true);
		this.priceField = new LabelledTextField("Price:", true);
        this.confirm = new Button("Confirm");
        this.exit = new Button("Cancel");

        nameField.setText(targetProduct.getName());
        descriptionField.setText(targetProduct.getDescription());
        priceField.setText(Double.toString(targetProduct.getPrice()));
        
        BooleanBinding invalidName = Bindings.createBooleanBinding(() -> { // a boolean binding to check if the name is invalid (a duplicate of anything but itself)
	        if (this.nameField.getTextField().getText().equals(targetProduct.getName())) return false; // it's okay if the name hasn't been edited
        	if (database.getProductByName(this.nameField.getTextField().getText()) == null) return false; // no duplicate found
	        else return true;
	    }, this.nameField.getTextField().textProperty());

        // NOTE: It is apparently already given that any future reference to a variable will be "this." if it was initialized with it
        confirm.disableProperty().bind(nameField.invalid.or(descriptionField.invalid).or(priceField.invalid).or(priceField.NaN).or(invalidName)); // submit button being disabled is binded to the boolean binding made earlier

        this.buttonsHBox = new HBox(10, exit, confirm);
        this.vbox = new VBox(10, nameField, descriptionField, priceField, buttonsHBox); // creates a vbox with a 10px spacing with these as children
        vbox.setStyle("-fx-padding: 20;");

        this.editScene = new Scene(vbox, 500, 350);
        Styles.apply(editScene); // loads the shared stylesheet

        this.editWindow = new Stage(); // creates a new window for the user creation
        editWindow.setTitle("Edit Product");
        editWindow.setScene(editScene);

        // Handle submission / closing
        confirm.setOnMouseClicked(new EventHandler<MouseEvent>(){
			/**
			 * applies the new values to every copy of the product, then saves all files
			 *
			 * @param event the mouse click on the confirm button
			 */
			public void handle (MouseEvent event) {
				Product[] editTargets = new Product[3]; // 3 because that's how many databases we have
				editTargets[0] = database.getProductById(targetProduct.getId());
				editTargets[1] = inventoryData.getProductById(targetProduct.getId());
				editTargets[2] = shelfData.getProductById(targetProduct.getId());
				String newName = nameField.getText();
				String newDesc = descriptionField.getText();
				double newPrice = Double.parseDouble(priceField.getText());

				for (Product p : editTargets) {
					if (p == null) continue; // skip if it doesn't exist
					p.setName(newName);
					p.setDescription(newDesc);
					p.setPrice(newPrice);
				}

				// the screen redraws itself, there is no table anymore
				if (refresh != null) refresh.run();
				ProductsFileHandler.saveAll(database, inventoryData, shelfData);
				editWindow.close();
			}
		});

        exit.setOnMouseClicked(new EventHandler<MouseEvent>(){
			/**
			 * closes the window without changing anything
			 *
			 * @param event the mouse click on the cancel button
			 */
			public void handle (MouseEvent event) {
				editWindow.close();
			}
		});

        editWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
        editWindow.initOwner(stage); // the window below it being the main stage


	}

	/** shows the window */
	public void show() { // just to make things simpler
		// make the new window actually appear
        editWindow.show();
	}
}