package ui;

import java.util.HashMap;

import application.Product;
import application.ProductContainer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddProductWindow {
	
	private ComboBox<String> productBox;
	
	private Button add;
	private Button exit;
	private VBox vbox;
	private HBox buttonsHBox;
	private Scene addScene;
	private Stage addWindow;
	
	public AddProductWindow(Stage stage, TableView<Product> table, ProductContainer targetDatabase, ProductContainer sourceDatabase, Boolean limited) {
		HashMap<String, Integer> products = new HashMap<>();
		for (Product p : sourceDatabase.getList()) { // go through every product in the database
			products.put(p.getName(), p.getId()); // add their name and corresponding internal id
		}
		
		ObservableList<String> productNames = FXCollections.observableArrayList(products.keySet()); // convert the key set (product names) to an observable list so that the combo box can use it
		this.productBox = new ComboBox<>(productNames);
		
		//productBox.getSelectionModel().selectFirst(); // to make sure something is already selected
        
        
		// Create an integer Spinner
		Spinner<Integer> spinner = new Spinner<>();
		if(limited) { // the stocks are limited to how many are in the database
			IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(0, 0, 0, 1); // min, max, initial value, step
			spinner.setValueFactory(valueFactory);
			
			productBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
	            if (newVal != null) {
	            	int maxStock = sourceDatabase.getProductById(products.get(newVal)).getStock();
	                // to explain piece by piece:
	                // newVal = name of the product the user just chose
	                // products.get(newVal) = the internal ID of the chosen product
	                // database.getProductById(...) = the full product information of the chosen product
	                // (...).getStock() = the stock of the chosen product, taken from the full information

	                // Dynamically update the factory max limit
	                valueFactory.setMax(maxStock);

	                
	                if (spinner.getValue() > maxStock) { // current value of the spinner is greater than the new max
	                    valueFactory.setValue(maxStock); // cap the current value to the new max
	                }
	            }
	        });
			
		} else {
			SpinnerValueFactory<Integer> valueFactory = 
					new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0, 1); // min, max, initial value, step
			spinner.setValueFactory(valueFactory);
		}
        
		spinner.setEditable(true); // allows the user to put their own number (does not go over max)		
		
        this.add = new Button("Add");
        this.exit = new Button("Cancel");
        
        
       BooleanBinding productBoxEmpty = Bindings.createBooleanBinding(() -> { // a boolean binding to check if the usertype box has anything selected or not
	        if (this.productBox.getValue() == null) return true;
	        else return false;
	    }, this.productBox.valueProperty());
        
        add.disableProperty().bind(productBoxEmpty); // submit button being disabled is binded to the boolean binding made earlier
        
        this.buttonsHBox = new HBox(10, exit, add);
        this.vbox = new VBox(10, productBox, spinner, buttonsHBox); // creates a vbox with a 10px spacing with these as children
        vbox.setStyle("-fx-padding: 20;");

        this.addScene = new Scene(vbox, 500, 350);

        this.addWindow = new Stage(); // creates a new window for the user creation 
        addWindow.setTitle("Add Product");
        addWindow.setScene(addScene);
        
        // Handle submission / closing
        add.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				int addAmount = spinner.getValue();
				int addId = products.get(productBox.getValue()); // reminder: products is a HashMap<String, Integer> where string is the product name and integer is the id
				System.out.println("Attempting to add product with id: " + addId);
				Product addedProduct = sourceDatabase.getProductById(addId); // grabs the product from the database
				System.out.println("Product with that id in the source database is: " + addedProduct.getName());
				if (limited) {
					sourceDatabase.remove(addedProduct, addAmount);
					// since we're taking stock from the source, we need to remove stocks from the source
				}
				targetDatabase.add(addedProduct, addAmount); // add the product with its specific data
				table.setItems(targetDatabase.getObservableList());
				table.refresh();
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
