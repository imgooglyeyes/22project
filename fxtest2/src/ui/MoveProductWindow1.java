package ui;

import application.Product;
import application.ProductContainer;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MoveProductWindow1 {

    private Stage window;

    public MoveProductWindow1(
            Stage stage,
            ProductContainer currentDatabase,
            ProductContainer storageDatabase,
            Product targetProduct,
            Runnable refreshInventory) {

        // displays the selected product
        Label productName = new Label(
                "Removing " + targetProduct.getName() + " stocks"
        );

        // creates a spinner for choosing the quantity
        Spinner<Integer> spinner = new Spinner<>();

        IntegerSpinnerValueFactory valueFactory =
                new IntegerSpinnerValueFactory(
                        0,
                        targetProduct.getStock(),
                        0
                );

        spinner.setValueFactory(valueFactory);
        spinner.setEditable(true);

        Button confirm = new Button("Confirm");
        Button cancel = new Button("Cancel");

        // moves or removes the selected quantity
        confirm.setOnAction(event -> {

            try {
                spinner.commitValue();
            } catch (NumberFormatException e) {
                return;
            }

            int quantity = spinner.getValue();

            // checks if the quantity is valid
            if (quantity < 0 || quantity > targetProduct.getStock()) {
                return;
            }

            // moves the product to storage if storage is available
            if (storageDatabase != null) {
                storageDatabase.add(targetProduct, quantity);
            }

            // removes the quantity from the current inventory
            currentDatabase.remove(targetProduct, quantity);

            // refreshes the inventory display
            if (refreshInventory != null) {
                refreshInventory.run();
            }

            window.close();
        });

        // closes the window without making changes
        cancel.setOnAction(event -> window.close());

        // arranges the controls vertically
        HBox buttons = new HBox(10, cancel, confirm);

        VBox root = new VBox(10, productName, spinner, buttons);
        root.setPadding(new Insets(20));

        // creates the window
        window = new Stage();
        window.setTitle("Move Product Stocks");
        window.setScene(new Scene(root, 500, 250));

        // makes the window modal
        window.initModality(Modality.WINDOW_MODAL);
        window.initOwner(stage);
    }

    // displays the window
    public void show() {
        window.show();
    }
}