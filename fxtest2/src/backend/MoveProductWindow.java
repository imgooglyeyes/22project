package backend;

import application.FarmSystem;
import application.Product;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
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
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.3
 * @since 1.3 the window extends PopupWindow and uses the themed dialogs
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
			toInventory ? "Move to the inventory" : "Delete stocks", 460, 280);

		this.productName = new Label(
			(toInventory ? "Moving " : "Deleting ") + targetProduct.getName() +
			(toInventory ? " stocks to the inventory" : " stocks from the inventory")
		);
		productName.setWrapText(true);

		// Create an integer Spinner
		Spinner<Integer> spinner = new Spinner<>();
		IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(0, targetProduct.getStock(), 0, 1); // min, max, initial value, step
		spinner.setValueFactory(valueFactory);

		spinner.setEditable(true); // allows the user to put their own number, FarmSystem checks it again
		spinner.setMaxWidth(Double.MAX_VALUE);

		this.add = new Button(toInventory ? "Move" : "Delete");
		if (!toInventory) add.getStyleClass().add("danger"); // throwing stock away can't be undone

		form.getChildren().addAll(productName, fieldLabel("Amount (up to " + targetProduct.getStock() + ")"), spinner);
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
				Dialogs.error(window, error);
				return;
			}

			if (refresh != null) refresh.run();
			window.close();
		});
	}
}