package backend;

import application.Product;
import application.ProductContainer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
// CHANGED: TABLEVIEW IMPORT REMOVED. THE NEW SCREENS USE ROWS, NOT A TABLEVIEW, SO THIS WINDOW NO LONGER NEEDS ONE.
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MoveProductWindow {
	private Label productName;
	private Button add;
	private Button exit;
	private VBox vbox;
	private HBox buttonsHBox;
	private Scene addScene;
	private Stage addWindow;

	// CHANGED: THE TABLEVIEW PARAMETER IS NOW A RUNNABLE CALLED refresh (LAST PARAMETER).
	// WHY: THE SCREEN PASSES ITS OWN REFRESH METHOD, SO THE WINDOW CAN TELL THE SCREEN TO REDRAW WITHOUT KNOWING WHAT THE SCREEN LOOKS LIKE.
	public MoveProductWindow(Stage stage, ProductContainer currentDatabase, ProductContainer storageDatabase, Product targetProduct, Runnable refresh) {

		this.productName = new Label("Removing " + targetProduct.getName() + " stocks");
		//productBox.getSelectionModel().selectFirst(); // to make sure something is already selected


		// Create an integer Spinner
		Spinner<Integer> spinner = new Spinner<>();
		IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(0, targetProduct.getStock(), 0, 1); // min, max, initial value, step
		spinner.setValueFactory(valueFactory);

		spinner.setEditable(true); // allows the user to put their own number (does not go over max)

        this.add = new Button("Confirm");
        this.exit = new Button("Cancel");


        this.buttonsHBox = new HBox(10, exit, add);
        this.vbox = new VBox(10, productName, spinner, buttonsHBox); // creates a vbox with a 10px spacing with these as children
        vbox.setStyle("-fx-padding: 20;");

        this.addScene = new Scene(vbox, 500, 350);

        this.addWindow = new Stage(); // creates a new window for the user creation
        addWindow.setTitle("Move Product Stocks");
        addWindow.setScene(addScene);

        // Handle submission / closing
        add.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				// ADDED: READ WHAT THE USER TYPED INTO THE SPINNER.
				// WHY: THE SPINNER IS EDITABLE, AND TYPED NUMBERS ARE NOT COUNTED UNTIL THEY ARE COMMITTED. IF THE TEXT IS NOT A NUMBER WE STOP.
				try {
					spinner.commitValue();
				} catch (NumberFormatException e) {
					return;
				}
				int moveAmount = spinner.getValue();
				// ADDED: STOP IF THE AMOUNT IS NEGATIVE OR MORE THAN WHAT WE HAVE.
				// WHY: A TYPED NUMBER CAN GO OUTSIDE THE SPINNER LIMITS.
				if (moveAmount < 0 || moveAmount > targetProduct.getStock()) return;
				if (storageDatabase != null) {
					storageDatabase.add(targetProduct, moveAmount);
					// since there is an assigned storage for the moved products to go to, we need to add it there
				}
				currentDatabase.remove(targetProduct, moveAmount); // add the product with its specific data
				// CHANGED: WAS table.setItems(...) AND table.refresh(). NOW WE CALL refresh.
				// WHY: THERE IS NO TABLE ANYMORE. THE SCREEN REDRAWS ITSELF.
				if (refresh != null) refresh.run();
				addWindow.close();
			}
		});

        exit.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				addWindow.close();
			}
		});

        addWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
        addWindow.initOwner(stage); // the window below it being the main stage


	}

	public void show() { // just to make things simpler
		// make the new window actually appear
        addWindow.show();
	}
}
