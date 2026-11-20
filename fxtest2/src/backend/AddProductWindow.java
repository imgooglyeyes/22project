package backend;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.SceneManager;
import ui.Styles;

/**
 * popup window to add stock of a product from a source container to a target container
 *
 * <p>if limited is true the stock is taken from the source, so the amount can't go above
 * what the source has. saving goes through manager.saveProductContainers()
 * (the ProductFilesHandler import was removed)
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.1
 * @see MoveProductWindow
 * @see SceneManager#saveProductContainers()
 */
public class AddProductWindow {

	private ComboBox<String> productBox;

	private Button add;
	private Button exit;
	private VBox vbox;
	private HBox buttonsHBox;
	private Scene addScene;
	private Stage addWindow;


	/**
	 * creates the window, call show() to display it
	 * <p>the screen passes its own refresh method so the window can tell it to redraw
	 * without knowing what the screen looks like
	 *
	 * @param stage the main window, this window blocks it while open
	 * @param manager the scene manager, used to save the product containers
	 * @param targetDatabase the container that receives the stock
	 * @param sourceDatabase the container the products are picked from
	 * @param limited true if the stock is taken from the source and capped to its stock
	 * @param refresh called after a successful add so the screen redraws, can be null
	 */
	public AddProductWindow(Stage stage, SceneManager manager, ProductContainer targetDatabase, ProductContainer sourceDatabase, Boolean limited, Runnable refresh) {
		HashMap<String, Integer> products = new HashMap<>();
		for (Product p : sourceDatabase.getProducts()) { // go through every product in the database
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
        Styles.apply(addScene); // loads the shared stylesheet

        this.addWindow = new Stage(); // creates a new window for the user creation
        addWindow.setTitle("Add Product");
        addWindow.setScene(addScene);

        // Handle submission / closing
        add.setOnMouseClicked(new EventHandler<MouseEvent>(){
			/**
			 * adds the chosen amount to the target, takes it from the source if limited, then saves
			 *
			 * @param event the mouse click on the add button
			 */
			public void handle (MouseEvent event) {
				// the spinner is editable, and typed numbers are not counted until they are committed
				// if the text is not a number we stop
				try {
					spinner.commitValue();
				} catch (NumberFormatException e) {
					return;
				}
				int addAmount = spinner.getValue();
				// adding nothing would create an empty row, so the amount must be at least 1
				if (addAmount < 1) return;
				int addId = products.get(productBox.getValue()); // reminder: products is a HashMap<String, Integer> where string is the product name and integer is the id
				System.out.println("Attempting to add product with id: " + addId);
				Product addedProduct = sourceDatabase.getProductById(addId); // grabs the product from the database
				System.out.println("Product with that id in the source database is: " + addedProduct.getName());
				if (limited) {
					// stop if the user typed more than the source has, or the stock would go negative
					if (addAmount > addedProduct.getStock()) return;
					sourceDatabase.remove(addedProduct, addAmount);
					// since we're taking stock from the source, we need to remove stocks from the source
				}
				targetDatabase.add(addedProduct, addAmount); // add the product with its specific data
				// the screen redraws itself, there is no table anymore
				if (refresh != null) refresh.run();
				manager.saveProductContainers();
				addWindow.close();
			}
		});

        exit.setOnMouseClicked(new EventHandler<MouseEvent>(){
			/**
			 * closes the window without changing anything
			 *
			 * @param event the mouse click on the cancel button
			 */
			public void handle (MouseEvent event) {
				addWindow.close();
			}
		});

        addWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
        addWindow.initOwner(stage); // the window below it being the main stage


	}

	/** shows the window */
	public void show() { // just to make things simpler
		// make the new window actually appear
        addWindow.show();
	}
}
