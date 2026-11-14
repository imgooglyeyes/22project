package ui;

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

public class ProductCreationWindow {
	
	private LabelledTextField nameField;
	private LabelledTextField descriptionField;
	private LabelledTextField priceField;
	
	private Button create;
	private Button exit;
	private VBox vbox;
	private HBox buttonsHBox;
	private Scene createScene;
	private Stage createWindow;
	
	public ProductCreationWindow(Stage stage, ProductContainer database, TableView<Product> table) {
		this.nameField = new LabelledTextField("Name:", true);
		this.descriptionField = new LabelledTextField("Description:", true);
		this.priceField = new LabelledTextField("Price:", true);
        this.create = new Button("Create");
        this.exit = new Button("Cancel");
        
        // PLEASE PLEASE MAKE IT SO THAT PRICE FIELD CAN ONLY ACCEPT DOUBLE AS AN INPUT
        // RIGHT NOW IT WILL BREAK IF THE INPUT IS NOT A DOUBLE
        
        // NOTE: It is apparently already given that any future reference to a variable will be "this." if it was initialized with it
        create.disableProperty().bind(nameField.empty.or(descriptionField.empty).or(priceField.empty)); // submit button being disabled is binded to the boolean binding made earlier
        
        this.buttonsHBox = new HBox(10, exit, create);
        this.vbox = new VBox(10, nameField, descriptionField, priceField, buttonsHBox); // creates a vbox with a 10px spacing with these as children
        vbox.setStyle("-fx-padding: 20;");

        this.createScene = new Scene(vbox, 500, 350);

        this.createWindow = new Stage(); // creates a new window for the user creation 
        createWindow.setTitle("Create New Product");
        createWindow.setScene(createScene);
        
        // Handle submission / closing
        create.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				database.add(new Product(nameField.getText(), descriptionField.getText(), Double.parseDouble(priceField.getText()), 0), 0);
				table.setItems(database.getObservableList()); // update table
				createWindow.close();
			}
		});
        
        exit.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				createWindow.close();
			}
		});

        createWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
        createWindow.initOwner(stage); // the window below it being the main stage

        
	}
	
	public void show() { // just to make things simpler
		// make the new window actually appear
        createWindow.show();		
	}
}
