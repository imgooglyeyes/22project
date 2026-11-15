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

// popup window where a customer asks to buy a shelf product
// the quantity goes from 1 up to the stock on the shelf
// the real checks and the saving are done by FarmSystem, this window only shows the result
// a request does not change stock, a clerk confirms it later

public class RequestToBuyWindow {

	private Stage requestWindow;

	public RequestToBuyWindow(Stage stage, FarmSystem farmSystem, Product product, String customer, Runnable refresh) {

		Label productName = new Label("Request to buy: " + product.getName());
		Label details = new Label("Price: " + product.getFormattedPrice() + " each, " + product.getStock() + " on the shelf");

		// quantity spinner, min 1, max is what the shelf has
		Spinner<Integer> spinner = new Spinner<>();
		IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(1, product.getStock(), 1, 1); // min, max, initial value, step
		spinner.setValueFactory(valueFactory);
		spinner.setEditable(true); // allows typing a number, FarmSystem checks it again

		Button request = new Button("Send Request");
		Button exit = new Button("Cancel");

		HBox buttonsHBox = new HBox(10, exit, request);
		VBox vbox = new VBox(10, productName, details, spinner, buttonsHBox);
		vbox.setStyle("-fx-padding: 20;");

		Scene requestScene = new Scene(vbox, 500, 350);
		Styles.apply(requestScene); // loads the shared stylesheet

		requestWindow = new Stage();
		requestWindow.setTitle("Request To Buy");
		requestWindow.setScene(requestScene);

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
				new Alert(AlertType.ERROR, error).showAndWait(); // keep the window open so they can fix the amount
				return;
			}

			new Alert(AlertType.INFORMATION, "Request sent. A clerk will confirm it.").showAndWait();
			if (refresh != null) refresh.run();
			requestWindow.close();
		});

		exit.setOnAction(event -> requestWindow.close());

		requestWindow.initModality(Modality.WINDOW_MODAL); // blocks the window below it
		requestWindow.initOwner(stage);
	}

	public void show() {
		requestWindow.show();
	}
}