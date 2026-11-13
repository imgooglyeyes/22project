package backend;

import java.awt.TextField;

import application.Product;
import application.ProductContainer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.LabelledTextField;

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
	
	public ProductEditingWindow(Stage stage, TableView<Product> table, ProductContainer database, ProductContainer inventoryData, ProductContainer shelfData, Product targetProduct) {
		this.nameField = new LabelledTextField("Name:", true);
		this.descriptionField = new LabelledTextField("Description:", true);
		this.priceField = new LabelledTextField("Price:", true);
        this.confirm = new Button("Confirm");
        this.exit = new Button("Cancel");
        
        nameField.setText(targetProduct.getName());
        descriptionField.setText(targetProduct.getDescription());
        priceField.setText(Double.toString(targetProduct.getPrice()));
        
        // PLEASE PLEASE MAKE IT SO THAT PRICE FIELD CAN ONLY ACCEPT DOUBLE AS AN INPUT
        // RIGHT NOW IT WILL BREAK IF THE INPUT IS NOT A DOUBLE
        
        // NOTE: It is apparently already given that any future reference to a variable will be "this." if it was initialized with it
        confirm.disableProperty().bind(nameField.empty.or(descriptionField.empty).or(priceField.empty)); // submit button being disabled is binded to the boolean binding made earlier
        
        this.buttonsHBox = new HBox(10, exit, confirm);
        this.vbox = new VBox(10, nameField, descriptionField, priceField, buttonsHBox); // creates a vbox with a 10px spacing with these as children
        vbox.setStyle("-fx-padding: 20;");

        this.editScene = new Scene(vbox, 500, 350);

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
				
				table.setItems(database.getObservableList()); // update table
				table.refresh();
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
