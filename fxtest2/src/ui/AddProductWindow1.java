package ui;

import java.util.HashMap;

import application.Product;
import application.ProductContainer;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddProductWindow1 {

    private Stage window;

    public AddProductWindow1(
            Stage stage,
            ProductContainer targetDatabase,
            ProductContainer sourceDatabase,
            boolean limited,
            Runnable refreshInventory) {

        // stores product names and their corresponding ids
        HashMap<String, Integer> products = new HashMap<>();

        for (Product p : sourceDatabase.getProducts()) {
            products.put(p.getName(), p.getId());
        }

        // creates a dropdown containing the available products
        ComboBox<String> productBox = new ComboBox<>(
                FXCollections.observableArrayList(products.keySet())
        );

        // creates a spinner for choosing the quantity
        Spinner<Integer> spinner = new Spinner<>();

        IntegerSpinnerValueFactory valueFactory =
                new IntegerSpinnerValueFactory(
                        0,
                        limited ? 0 : Integer.MAX_VALUE,
                        0
                );

        spinner.setValueFactory(valueFactory);
        spinner.setEditable(true);

        // updates the maximum quantity when a product is selected
        if (limited) {
            productBox.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldValue, newValue) -> {

                        if (newValue != null) {
                            Product product = sourceDatabase.getProductById(
                                    products.get(newValue)
                            );

                            int maxStock = product.getStock();

                            valueFactory.setMax(maxStock);

                            // makes sure the quantity does not exceed the new limit
                            if (spinner.getValue() > maxStock) {
                                valueFactory.setValue(maxStock);
                            }
                        }
                    });
        }

        Button add = new Button("Add");
        Button cancel = new Button("Cancel");

        // disables add when no product is selected
        add.disableProperty().bind(
                Bindings.isNull(productBox.valueProperty())
        );

        // adds the selected product to the target database
        add.setOnAction(event -> {

            try {
                spinner.commitValue();
            } catch (NumberFormatException e) {
                return;
            }

            int quantity = spinner.getValue();
            int productId = products.get(productBox.getValue());

            Product product = sourceDatabase.getProductById(productId);

            if (product == null) {
                return;
            }

            // removes the stock from the source when the quantity is limited
            if (limited) {
                if (quantity > product.getStock()) {
                    return;
                }

                sourceDatabase.remove(product, quantity);
            }

            targetDatabase.add(product, quantity);

            // refreshes the inventory display
            if (refreshInventory != null) {
                refreshInventory.run();
            }

            window.close();
        });

        // closes the window without adding anything
        cancel.setOnAction(event -> window.close());

        // arranges the controls vertically
        HBox buttons = new HBox(10, cancel, add);

        VBox root = new VBox(10, productBox, spinner, buttons);
        root.setPadding(new Insets(20));

        // creates the window
        window = new Stage();
        window.setTitle("Add Product");
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