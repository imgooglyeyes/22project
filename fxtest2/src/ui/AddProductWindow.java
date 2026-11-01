package ui;

import application.Product;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


// TO-DO: Allow the user to specify amount of stock and validate it based on how many of that item is in the inventory

public class AddProductWindow {
	
	private ComboBox<String> productBox;
	
	private Button add;
	private Button exit;
	private VBox vbox;
	private HBox buttonsHBox;
	private Scene addScene;
	private Stage addWindow;
	
	public AddProductWindow(Stage stage, TableView<Product> table, ObservableList<Product> tableData, ObservableList<Product> database) {
		String[] pNames = new String[Product.AVAILABLE_ID]; // available ID here is being used as a way to track the size of the database
		int i = 0;
		for (Product p : database) { // go through every product in the database
			pNames[i] = p.getName(); // add their names to the array
			i++;
		}
		
		ObservableList<String> productNames = FXCollections.observableArrayList(pNames); // convert to an observable list so that the combo box can use it
		
		this.productBox = new ComboBox<>(productNames);
        this.add = new Button("Add");
        this.exit = new Button("Cancel");
        
        
       BooleanBinding productBoxEmpty = Bindings.createBooleanBinding(() -> { // a boolean binding to check if the usertype box has anything selected or not
	        if (this.productBox.getValue() == null) return true;
	        else return false;
	    }, this.productBox.valueProperty());
        
        // NOTE: It is apparently already given that any future reference to a variable will be "this." if it was initialized with it
        add.disableProperty().bind(productBoxEmpty); // submit button being disabled is binded to the boolean binding made earlier
        
        this.buttonsHBox = new HBox(10, exit, add);
        this.vbox = new VBox(10, productBox, buttonsHBox); // creates a vbox with a 10px spacing with these as children
        vbox.setStyle("-fx-padding: 20;");

        this.addScene = new Scene(vbox, 500, 350);

        this.addWindow = new Stage(); // creates a new window for the user creation 
        addWindow.setTitle("Add Product From Inventory");
        addWindow.setScene(addScene);
        
        // Handle submission / closing
        add.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				Product addedProduct = database.get(productBox.getSelectionModel().getSelectedIndex()); // grabs the product from the database
				addedProduct.setStock(100); // temp until I add a way to let the user set their own stock
				tableData.add(addedProduct); 
				table.setItems(tableData);
				addWindow.close();
			}
		});
        
        exit.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				addWindow.close();
			}
		});

        addWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
        addWindow.initOwner(stage); // the window below it being the main stage

        
	}
	
	public void show() { // just to make things simpler
		// make the new window actually appear
        addWindow.show();		
	}
}
