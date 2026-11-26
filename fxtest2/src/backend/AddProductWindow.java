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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.stage.Stage;
import ui.Dialogs;

/**
 * popup window to add stock of a product, either from the inventory to the shelf
 * or from the database to the inventory (a restock)
 *
 * <p>the window only picks the product and the amount, FarmSystem does the checks, the stock
 * change and the saving. the source container is only read, to fill the product list and to
 * cap the amount when stock is taken from it
 *
 * <p>the product list is sorted by name (ignoring letter case), so it always shows in the same order.
 * the window itself (title, buttons, theme) comes from PopupWindow
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.4
 * @since 1.3 the product list is sorted by name instead of being in random order
 * @since 1.4 the window extends PopupWindow and uses the themed dialogs
 * @see PopupWindow
 * @see MoveProductWindow
 * @see FarmSystem#addToShelf(Product, int)
 * @see FarmSystem#addToInventory(Product, int)
 */
public class AddProductWindow extends PopupWindow {

	private ComboBox<String> productBox;

	private Button add;

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
	 */
	public AddProductWindow(Stage stage, FarmSystem farmSystem, ProductContainer sourceContainer, boolean toShelf, Runnable refresh) {
		super(stage, "Add Product", toShelf ? "Add stock to the shelf" : "Restock the inventory", 460, 320);

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
		productBox.setPromptText("Choose a product");
		productBox.setMaxWidth(Double.MAX_VALUE); // fills the width of the form

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
		spinner.setMaxWidth(Double.MAX_VALUE);

		this.add = new Button("Add");

		// the add button stays disabled until a product is selected
		BooleanBinding productBoxEmpty = Bindings.createBooleanBinding(
			() -> this.productBox.getValue() == null,
			this.productBox.valueProperty()
		);
		add.disableProperty().bind(productBoxEmpty);

		form.getChildren().addAll(fieldLabel("Product"), productBox, fieldLabel("Amount"), spinner);
		finish(add);

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
				Dialogs.error(window, error);
				return;
			}

			if (refresh != null) refresh.run();
			window.close();
		});
	}
}