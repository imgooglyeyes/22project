package backend;

import application.FarmSystem;
import application.Product;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.Styles;

/**
 * popup window to take stock of a product out of the shelf or the inventory
 *
 * <p>on the shelf the stock moves back to the inventory, in the inventory it is thrown away.
 * the window only picks the amount, FarmSystem does the checks, the stock change and the saving
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.2
 * @see AddProductWindow
 * @see FarmSystem#moveToInventory(Product, int)
 * @see FarmSystem#removeFromInventory(Product, int)
 */
public class MoveProductWindow {
	private Label productName;
	private Button add;
	private Button exit;
	private VBox vbox;
	private HBox buttonsHBox;
	private Scene moveScene;
	private Stage moveWindow;

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

		this.productName = new Label(
			(toInventory ? "Moving " : "Deleting ") + targetProduct.getName() +
			(toInventory ? " stocks to the inventory" : " stocks from the inventory")
		);

		// Create an integer Spinner
		Spinner<Integer> spinner = new Spinner<>();
		IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(0, targetProduct.getStock(), 0, 1); // min, max, initial value, step
		spinner.setValueFactory(valueFactory);

		spinner.setEditable(true); // allows the user to put their own number, FarmSystem checks it again

		this.add = new Button("Confirm");
		this.exit = new Button("Cancel");

		this.buttonsHBox = new HBox(10, exit, add);
		this.vbox = new VBox(10, productName, spinner, buttonsHBox); // creates a vbox with a 10px spacing with these as children
		vbox.setStyle("-fx-padding: 20;");

		this.moveScene = new Scene(vbox, 500, 350);
		Styles.apply(moveScene); // loads the shared stylesheet

		this.moveWindow = new Stage(); // creates a new window for the stock move
		moveWindow.setTitle(toInventory ? "Move Product Stocks" : "Delete Product Stocks");
		moveWindow.setScene(moveScene);

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
				new Alert(AlertType.ERROR, error).showAndWait();
				return;
			}

			if (refresh != null) refresh.run();
			moveWindow.close();
		});

		exit.setOnAction(event -> moveWindow.close());

		moveWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
		moveWindow.initOwner(stage); // the window below it being the main stage
	}

	/** shows the window */
	public void show() { // just to make things simpler
		// make the new window actually appear
		moveWindow.show();
	}
}