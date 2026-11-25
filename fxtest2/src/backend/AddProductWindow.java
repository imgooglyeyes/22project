package backend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;

import application.FarmSystem;
import application.Product;
import application.ProductContainer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.Styles;

/**
 * popup window to add stock of a product, either from the inventory to the shelf
 * or from the database to the inventory
 *
 * <p>the window only picks the product and the amount, FarmSystem does the checks, the stock
 * change and the saving. the source container is only read, to fill the product list and to
 * cap the amount when stock is taken from it
 *
 * <p>the product list is sorted by name (ignoring letter case), so it always shows in the same order
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.3
 * @see MoveProductWindow
 * @see FarmSystem#addToShelf(Product, int)
 * @see FarmSystem#addToInventory(Product, int)
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
	 * @param farmSystem the farm system that changes the stock
	 * @param sourceContainer the container the products are picked from (inventory for the shelf, database for the inventory)
	 * @param toShelf true to move stock from the inventory to the shelf (amount is capped to the source stock), false to add new stock to the inventory
	 * @param refresh called after a successful add so the screen redraws, can be null
	 * @since 1.3 the product list is sorted by name instead of being in random order
	 */
	public AddProductWindow(Stage stage, FarmSystem farmSystem, ProductContainer sourceContainer, boolean toShelf, Runnable refresh) {
		// sort a copy of the source, so the real container keeps its own order
		ArrayList<Product> sortedProducts = new ArrayList<>(sourceContainer.getProducts());
		sortedProducts.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));

		// a linked map keeps the order the products were put in, so the combo box shows them sorted
		LinkedHashMap<String, Integer> products = new LinkedHashMap<>();
		for (Product p : sortedProducts) { // go through every product in the source, sorted by name
			products.put(p.getName(), p.getId()); // add their name and corresponding internal id
		}

		ObservableList<String> productNames = FXCollections.observableArrayList(products.keySet()); // convert the key set (product names) to an observable list so that the combo box can use it
		this.productBox = new ComboBox<>(productNames);

		// Create an integer Spinner
		Spinner<Integer> spinner = new Spinner<>();
		if (toShelf) { // the stocks are limited to how many are in the source
			IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(0, 0, 0, 1); // min, max, initial value, step
			spinner.setValueFactory(valueFactory);

			productBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal != null) {
					// newVal = name of the chosen product, products.get(newVal) = its id
					int maxStock = sourceContainer.getProductById(products.get(newVal)).getStock();

					// dynamically update the factory max limit
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

		spinner.setEditable(true); // allows the user to put their own number, FarmSystem checks it again

		this.add = new Button("Add");
		this.exit = new Button("Cancel");

		// the add button stays disabled until a product is selected
		BooleanBinding productBoxEmpty = Bindings.createBooleanBinding(
			() -> this.productBox.getValue() == null,
			this.productBox.valueProperty()
		);
		add.disableProperty().bind(productBoxEmpty);

		this.buttonsHBox = new HBox(10, exit, add);
		this.vbox = new VBox(10, productBox, spinner, buttonsHBox); // creates a vbox with a 10px spacing with these as children
		vbox.setStyle("-fx-padding: 20;");

		this.addScene = new Scene(vbox, 500, 350);
		Styles.apply(addScene); // loads the shared stylesheet

		this.addWindow = new Stage(); // creates a new window for the stock add
		addWindow.setTitle("Add Product");
		addWindow.setScene(addScene);

		// asks the farm system to add the stock, stays open if it says no so the amount can be fixed
		add.setOnAction(event -> {
			// the spinner is editable, and typed numbers are not counted until they are committed
			// if the text is not a number we stop
			try {
				spinner.commitValue();
			} catch (NumberFormatException e) {
				return;
			}

			Product chosen = sourceContainer.getProductById(products.get(productBox.getValue())); // reminder: products maps the product name to its id
			int amount = spinner.getValue();

			// FarmSystem returns null when it worked, or an error message
			String error = toShelf
				? farmSystem.addToShelf(chosen, amount)
				: farmSystem.addToInventory(chosen, amount);

			if (error != null) {
				new Alert(AlertType.ERROR, error).showAndWait();
				return;
			}

			if (refresh != null) refresh.run();
			addWindow.close();
		});

		exit.setOnAction(event -> addWindow.close());

		addWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
		addWindow.initOwner(stage); // the window below it being the main stage
	}

	/** shows the window */
	public void show() { // just to make things simpler
		// make the new window actually appear
		addWindow.show();
	}
}