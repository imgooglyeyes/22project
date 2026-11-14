package ui;

import java.util.HashMap;

import application.Product;
import application.ProductContainer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MoveProductWindow {
	private Label productName;
	private Button add;
	private Button exit;
	private VBox vbox;
	private HBox buttonsHBox;
	private Scene addScene;
	private Stage addWindow;
	
	public MoveProductWindow(Stage stage, TableView<Product> table, ProductContainer currentDatabase, ProductContainer storageDatabase, Product targetProduct) {
		
		this.productName = new Label("Removing " + targetProduct.getName() + " stocks");
		//productBox.getSelectionModel().selectFirst(); // to make sure something is already selected
        
        
		// Create an integer Spinner
		Spinner<Integer> spinner = new Spinner<>();
		IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(0, targetProduct.getStock(), 0, 1); // min, max, initial value, step
		spinner.setValueFactory(valueFactory);
			
		spinner.setEditable(true); // allows the user to put their own number (does not go over max)		
		
        this.add = new Button("Confirm");
        this.exit = new Button("Cancel");
        
        
        this.buttonsHBox = new HBox(10, exit, add);
        this.vbox = new VBox(10, productName, spinner, buttonsHBox); // creates a vbox with a 10px spacing with these as children
        vbox.setStyle("-fx-padding: 20;");

        this.addScene = new Scene(vbox, 500, 350);

        this.addWindow = new Stage(); // creates a new window for the user creation 
        addWindow.setTitle("Move Product Stocks");
        addWindow.setScene(addScene);
        
        // Handle submission / closing
        add.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				int moveAmount = spinner.getValue();
				if (storageDatabase != null) {
					storageDatabase.add(targetProduct, moveAmount);
					// since there is an assigned storage for the moved products to go to, we need to add it there
				}
				currentDatabase.remove(targetProduct, moveAmount); // add the product with its specific data
				table.setItems(currentDatabase.getObservableList());
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
