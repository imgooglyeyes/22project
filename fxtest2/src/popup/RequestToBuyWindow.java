package popup;

import application.FarmSystem;
import application.Product;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.stage.Stage;
import ui.Dialogs;

/**
 * popup window where a customer asks to buy a shelf product
 *
 * <p>the quantity goes from 1 up to the stock on the shelf.
 * the real checks and the saving are done by FarmSystem, this window only shows the result.
 * a request does not change stock, a clerk confirms it later.
 * the window itself (title, buttons, theme) comes from PopupWindow
 *
 * <p>an error stays in a dialog, a request that worked closes the window and the page shows the toast
 * through the refresh code
 *
 * @author Shawn Alfred Padilla
 * @version 1.2
 * @since 1.1 the window extends PopupWindow and uses the themed dialogs
 * @since 1.2 the info dialog was removed, the page shows a toast instead
 * @see PopupWindow
 * @see FarmSystem#requestToBuy(Product, int, String)
 */
public class RequestToBuyWindow extends PopupWindow {

	/**
	 * creates the window, call show() to display it
	 *
	 * @param stage the main window, this window blocks it while open
	 * @param farmSystem the farm system that saves the request
	 * @param product the shelf product the customer wants
	 * @param customer the username of the customer
	 * @param refresh called after the request is sent so the screen redraws and shows its toast, can be null
	 */
	public RequestToBuyWindow(Stage stage, FarmSystem farmSystem, Product product, String customer, Runnable refresh) {
		super(stage, "Request To Buy", "Request to buy", 460, 320);

		Label productName = new Label(product.getName());
		productName.getStyleClass().add("product-name");
		productName.setWrapText(true);

		Label details = new Label(product.getFormattedPrice() + " each, " + product.getStock() + " on the shelf");
		details.getStyleClass().add("caption");

		// quantity spinner, min 1, max is what the shelf has
		Spinner<Integer> spinner = new Spinner<>();
		IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(1, product.getStock(), 1, 1); // min, max, initial value, step
		spinner.setValueFactory(valueFactory);
		spinner.setEditable(true); // allows typing a number, FarmSystem checks it again
		spinner.setMaxWidth(Double.MAX_VALUE);

		Button request = new Button("Send Request");

		form.getChildren().addAll(productName, details, fieldLabel("Quantity"), spinner);
		finish(request);

		request.setOnAction(event -> {
			// typed numbers are not counted until they are committed, stop if the text is not a number
			try {
				spinner.commitValue();
			} catch (NumberFormatException e) {
				return;
			}

			// FarmSystem returns null when it worked, or an error message
			String error = farmSystem.requestToBuy(product, spinner.getValue(), customer);

			if (error != null) {
				Dialogs.error(window, error); // keep the window open so they can fix the amount
				return;
			}

			if (refresh != null) refresh.run();
			window.close();
		});
	}
}