package popup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.function.IntSupplier;

import application.FarmSystem;
import application.Product;
import application.ProductContainer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import ui.Dialogs;

/**
 * popup window to add stock of a product, either from the inventory to the shelf
 * or from the database to the inventory (a restock)
 *
 * <p>the window only picks the product and the amount, FarmSystem does the checks, the stock
 * change and the saving. the source container is only read, to fill the product list and to
 * warn when the amount is more than the source has
 *
 * <p>the amount can be any number, a warning note explains when it is more than the inventory has
 * and says what to do next, and the add button stays disabled until the amount is fine.
 * a restock from the database has no limit
 *
 * <p>the product list is sorted by name (ignoring letter case), so it always shows in the same order.
 * the window itself (title, buttons, theme) comes from PopupWindow
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.5
 * @since 1.3 the product list is sorted by name instead of being in random order
 * @since 1.4 the window extends PopupWindow and uses the themed dialogs
 * @since 1.5 a warning note replaces the spinner limit, and the error dialog has a next step
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
	 * @param toShelf true to move stock from the inventory to the shelf (amount is limited to the source stock), false to add new stock to the inventory
	 * @param refresh called after a successful add so the screen redraws, can be null
	 */
	public AddProductWindow(Stage stage, FarmSystem farmSystem, ProductContainer sourceContainer, boolean toShelf, Runnable refresh) {
		super(stage, "Add Product", toShelf ? "Add stock to the shelf" : "Restock the inventory", 460, 400);

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

		Spinner<Integer> spinner = createAmountSpinner();
		IntegerBinding typed = typedAmount(spinner);

		// the stock of the chosen product in the source, 0 while nothing is chosen
		IntSupplier chosenStock = () -> {
			String picked = productBox.getValue();
			if (picked == null) return 0;

			Product source = sourceContainer.getProductById(products.get(picked));
			return source == null ? 0 : source.getStock();
		};

		this.add = new Button("Add");

		// the add button stays disabled until a product is chosen and the amount is fine
		add.disableProperty().bind(Bindings.createBooleanBinding(
			() -> {
				if (productBox.getValue() == null) return true;

				int amount = typed.get();
				return amount < 1 || (toShelf && amount > chosenStock.getAsInt());
			},
			productBox.valueProperty(), typed
		));

		// tells what is wrong with the amount, and what to do about it
		StringBinding message = Bindings.createStringBinding(() -> {
			if (productBox.getValue() == null) return ""; // the button is disabled, nothing to explain yet

			int amount = typed.get();
			if (amount < 1) return "Enter a whole number of 1 or more";
			if (toShelf && amount > chosenStock.getAsInt()) {
				return "Only " + chosenStock.getAsInt() + " in the inventory, you asked for " + amount;
			}
			return "";
		}, productBox.valueProperty(), typed);

		StringBinding nextStep = Bindings.createStringBinding(() -> {
			if (productBox.getValue() == null) return "";

			int amount = typed.get();
			if (amount < 1) return "Type a number or use the arrows";
			if (toShelf && amount > chosenStock.getAsInt()) {
				return "Lower the amount to " + chosenStock.getAsInt() + " or less, or restock the inventory first";
			}
			return "";
		}, productBox.valueProperty(), typed);

		form.getChildren().addAll(fieldLabel("Product"), productBox, fieldLabel("Amount"), spinner, createWarningNote(message, nextStep));
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
				Dialogs.error(window, error, "Check the amount and the product, then try again");
				return;
			}

			if (refresh != null) refresh.run();
			window.close();
		});
	}
}