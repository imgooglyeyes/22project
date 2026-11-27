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
 * popup window where a customer asks to buy a shelf product
 *
 * <p>the quantity starts at 1 and can be any number, a warning note explains when it is more than
 * the shelf has and says what to do next, and the send button stays disabled until it is fine.
 * the real checks and the saving are done by FarmSystem, this window only shows the result.
 * a request does not change stock, a clerk confirms it later.
 * the window itself (title, buttons, theme) comes from PopupWindow
 *
 * <p>an error stays in a dialog, a request that worked closes the window and the page shows the toast
 * through the refresh code
 *
 * @author Shawn Alfred Padilla
 * @version 1.3
 * @since 1.1 the window extends PopupWindow and uses the themed dialogs
 * @since 1.2 the info dialog was removed, the page shows a toast instead
 * @since 1.3 a warning note explains a quantity that is too big, and the error dialog has a next step
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
		super(stage, "Request To Buy", "Request to buy", 460, 390);

		int stock = product.getStock(); // the window blocks the page, so the stock can't change while it is open

		Label productName = new Label(product.getName());
		productName.getStyleClass().add("product-name");
		productName.setWrapText(true);

		Label details = new Label(product.getFormattedPrice() + " each, " + stock + " on the shelf");
		details.getStyleClass().add("caption");

		Spinner<Integer> spinner = createAmountSpinner();
		IntegerBinding typed = typedAmount(spinner);

		Button request = new Button("Send Request");

		// the button is disabled while the quantity is not a number, is below 1 or is more than the shelf has
		request.disableProperty().bind(typed.lessThan(1).or(typed.greaterThan(stock)));

		// tells what is wrong with the quantity, and what to do about it
		StringBinding message = Bindings.createStringBinding(() -> {
			int amount = typed.get();
			if (stock < 1) return "This product just sold out";
			if (amount < 1) return "Enter a whole number of 1 or more";
			if (amount > stock) return "Only " + stock + " left on the shelf, you asked for " + amount;
			return "";
		}, typed);

		StringBinding nextStep = Bindings.createStringBinding(() -> {
			int amount = typed.get();
			if (stock < 1) return "Close this window and look for another product in the marketplace";
			if (amount < 1) return "Type a number or use the arrows";
			if (amount > stock) return "Lower the quantity to " + stock + " or less, or check back after the farm restocks";
			return "";
		}, typed);

		form.getChildren().addAll(productName, details, fieldLabel("Quantity"), spinner, createWarningNote(message, nextStep));
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
				// keep the window open, the shelf may have changed since it was opened
				Dialogs.error(window, error, "Close this window and check the marketplace for the latest stock");
				return;
			}

			if (refresh != null) refresh.run();
			window.close();
		});
	}
}