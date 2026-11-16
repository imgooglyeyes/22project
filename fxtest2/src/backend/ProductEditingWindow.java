package backend;

// CHANGED: java.awt.TextField IMPORT REMOVED. IT WAS NEVER USED AND IT CAN BREAK THE BUILD IF THE PROJECT HAS NO AWT MODULE.

import application.Product;
import application.ProductContainer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
// CHANGED: TABLEVIEW IMPORT REMOVED. THE NEW SCREENS USE ROWS, NOT A TABLEVIEW, SO THIS WINDOW NO LONGER NEEDS ONE.
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.LabelledTextField;
import ui.Styles;

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

	// CHANGED: THE TABLEVIEW PARAMETER IS NOW A RUNNABLE CALLED refresh (LAST PARAMETER).
	// WHY: THE SCREEN PASSES ITS OWN REFRESH METHOD, SO THE WINDOW CAN TELL THE SCREEN TO REDRAW WITHOUT KNOWING WHAT THE SCREEN LOOKS LIKE.
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

				// CHANGED: WAS table.setItems(...) AND table.refresh(). NOW WE CALL refresh.
				// WHY: THERE IS NO TABLE ANYMORE. THE SCREEN REDRAWS ITSELF.
				if (refresh != null) refresh.run();
				editWindow.close();
			}
		});

        exit.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				editWindow.close();
			}
		});

        editWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
        editWindow.initOwner(stage); // the window below it being the main stage


	}

	public void show() { // just to make things simpler
		// make the new window actually appear
        editWindow.show();
	}
}