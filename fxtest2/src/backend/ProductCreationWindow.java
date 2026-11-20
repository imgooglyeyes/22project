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
 * popup window to create a new product in the database
 *
 * <p>the create button stays disabled while a field is empty, contains |, the price
 * is not a valid number, or the name already exists. only the database file is saved
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.1
 * @see ProductsFileHandler#saveAll(ProductContainer, ProductContainer, ProductContainer)
 */
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

	/**
	 * creates the window, call show() to display it
	 * <p>the screen passes its own refresh method so the window can tell it to redraw
	 *
	 * @param stage the main window, this window blocks it while open
	 * @param database the database that receives the new product
	 * @param refresh called after the product is created so the screen redraws, can be null
	 */
	public ProductCreationWindow(Stage stage, ProductContainer database, Runnable refresh) {
		this.nameField = new LabelledTextField("Name:", true);
		this.descriptionField = new LabelledTextField("Description:", true);
		this.priceField = new LabelledTextField("Price:", true);
        this.create = new Button("Create");
        this.exit = new Button("Cancel");
        
        BooleanBinding duplicateName = Bindings.createBooleanBinding(() -> { // a boolean binding to check if the product name already exists
	        if (database.getProductByName(this.nameField.getTextField().getText()) == null) return false; // no duplicate found
	        else return true;
	    }, this.nameField.getTextField().textProperty());

        // NOTE: It is apparently already given that any future reference to a variable will be "this." if it was initialized with it
        create.disableProperty().bind(nameField.invalid.or(descriptionField.invalid).or(priceField.invalid).or(priceField.NaN).or(duplicateName)); // submit button being disabled is binded to the boolean binding made earlier

        this.buttonsHBox = new HBox(10, exit, create);
        this.vbox = new VBox(10, nameField, descriptionField, priceField, buttonsHBox); // creates a vbox with a 10px spacing with these as children
        vbox.setStyle("-fx-padding: 20;");

        this.createScene = new Scene(vbox, 500, 350);
        Styles.apply(createScene); // loads the shared stylesheet

        this.createWindow = new Stage(); // creates a new window for the user creation
        createWindow.setTitle("Create New Product");
        createWindow.setScene(createScene);

        // Handle submission / closing
        create.setOnMouseClicked(new EventHandler<MouseEvent>(){
			/**
			 * adds the new product to the database and saves it
			 *
			 * @param event the mouse click on the create button
			 */
			public void handle (MouseEvent event) {
				database.add(new Product(nameField.getText(), descriptionField.getText(), Double.parseDouble(priceField.getText()), 0), 0);
				// the screen redraws itself, there is no table anymore
				if (refresh != null) refresh.run();
				ProductsFileHandler.saveAll(database, null, null); // only the database file is written
				createWindow.close();
			}
		});

        exit.setOnMouseClicked(new EventHandler<MouseEvent>(){
			/**
			 * closes the window without creating anything
			 *
			 * @param event the mouse click on the cancel button
			 */
			public void handle (MouseEvent event) {
				createWindow.close();
			}
		});

        createWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
        createWindow.initOwner(stage); // the window below it being the main stage


	}

	/** shows the window */
	public void show() { // just to make things simpler
		// make the new window actually appear
        createWindow.show();
	}
}