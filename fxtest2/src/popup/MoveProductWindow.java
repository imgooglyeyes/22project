package popup;

import application.FarmSystem;
import application.Product;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import ui.Dialogs;

/**
 * popup window to take stock of a product out of the shelf or the inventory
 *
 * <p>on the shelf the stock moves back to the inventory, in the inventory it is thrown away.
 * the window only picks the amount, FarmSystem does the checks, the stock change and the saving.
 * throwing stock away can't be undone, so its button is a danger button.
 * the window itself (title, buttons, theme) comes from PopupWindow
 *
 * <p>a warning note explains when the amount is more than the stock and says what to do next.
 * taking all of the stock is allowed, but it gets a softer warning because the product leaves the shelf or the inventory
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.4
 * @since 1.3 the window extends PopupWindow and uses the themed dialogs
 * @since 1.4 a warning note explains a wrong amount and taking all the stock, and the error dialog has a next step
 * @see PopupWindow
 * @see AddProductWindow
 * @see FarmSystem#moveToInventory(Product, int)
 * @see FarmSystem#removeFromInventory(Product, int)
 */
public class MoveProductWindow extends PopupWindow {
	private Label productName;
	private Button add;

	/**
	 * creates the window, call show() to display it
	 * <p>the screen passes its own refresh method so the window can tell it to redraw
	 * without knowing what the screen looks like
	 *
	 * @param stage the main window, this window blocks it while open
	 * @param farmSystem the farm system that changes the stock
	 * @param targetProduct the product to take stock from, its stock is the most that can be taken
	 * @param toInventory true if the stock goes from the shelf to the inventory, false if it is deleted from the inventory
	 * @param refresh called after a successful move so the screen redraws, can be null
	 */
	public MoveProductWindow(Stage stage, FarmSystem farmSystem, Product targetProduct, boolean toInventory, Runnable refresh) {
		super(stage, toInventory ? "Move Product Stocks" : "Delete Product Stocks",
			toInventory ? "Move to the inventory" : "Delete stocks", 460, 360);

		int stock = targetProduct.getStock(); // the window blocks the page, so the stock can't change while it is open
		String place = toInventory ? "on the shelf" : "in the inventory";

		this.productName = new Label(
			(toInventory ? "Moving " : "Deleting ") + targetProduct.getName() +
			(toInventory ? " stocks to the inventory" : " stocks from the inventory")
		);
		productName.setWrapText(true);

		Spinner<Integer> spinner = createAmountSpinner();
		IntegerBinding typed = typedAmount(spinner);

		this.add = new Button(toInventory ? "Move" : "Delete");
		if (!toInventory) add.getStyleClass().add("danger"); // throwing stock away can't be undone

		// the button is disabled while the amount is not a number, is below 1 or is more than the stock
		add.disableProperty().bind(typed.lessThan(1).or(typed.greaterThan(stock)));

		// tells what is wrong with the amount, and warns when everything is taken
		StringBinding message = Bindings.createStringBinding(() -> {
			int amount = typed.get();
			if (amount < 1) return "Enter a whole number of 1 or more";
			if (amount > stock) return "Only " + stock + " left " + place + ", you asked for " + amount;
			if (amount == stock) {
				return toInventory
					? "This moves all the stock, the product will leave the shelf"
					: "This deletes all the stock, the product will leave the inventory";
			}
			return "";
		}, typed);

		StringBinding nextStep = Bindings.createStringBinding(() -> {
			int amount = typed.get();
			if (amount < 1) return "Type a number or use the arrows";
			if (amount > stock) return "Lower the amount to " + stock + " or less";
			if (amount == stock) {
				return toInventory
					? "You can put it back later with Add From Inventory"
					: "If this is a mistake, restock it again from the database";
			}
			return "";
		}, typed);

		form.getChildren().addAll(productName, fieldLabel("Amount (up to " + stock + ")"), spinner, createWarningNote(message, nextStep));
		finish(add);

		// asks the farm system to move the stock, stays open if it says no so the amount can be fixed
		add.setOnAction(event -> {
			// the spinner is editable, and typed numbers are not counted until they are committed
			// if the text is not a number we stop
			try {
				spinner.commitValue();
			} catch (NumberFormatException e) {
				return;
			}

			int amount = spinner.getValue();

			// FarmSystem returns null when it worked, or an error message
			String error = toInventory
				? farmSystem.moveToInventory(targetProduct, amount)
				: farmSystem.removeFromInventory(targetProduct, amount);

			if (error != null) {
				Dialogs.error(window, error, "Close this window and check the page for the latest stock");
				return;
			}

			if (refresh != null) refresh.run();
			window.close();
		});
	}
}