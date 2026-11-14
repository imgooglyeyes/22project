package ui;

import application.Product;
import application.ProductContainer;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProductEditingWindow1 {

    private Stage window;

    public ProductEditingWindow1(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            Product targetProduct,
            Runnable refreshDatabase) {

        // creates the input fields filled with the current values
        TextField nameField = new TextField(targetProduct.getName());
        TextField descriptionField = new TextField(targetProduct.getDescription());
        TextField priceField = new TextField(String.valueOf(targetProduct.getPrice()));

        // only allows numbers and one decimal point in the price
        priceField.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*(\\.\\d*)?") ? change : null
        ));

        Button confirm = new Button("Confirm");
        Button cancel = new Button("Cancel");

        // disables confirm when a field is empty or the price is invalid
        confirm.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> nameField.getText().trim().isEmpty()
                                || descriptionField.getText().trim().isEmpty()
                                || !isValidPrice(priceField.getText()),

                        nameField.textProperty(),
                        descriptionField.textProperty(),
                        priceField.textProperty()
                )
        );

        // applies the changes to every container that has the product
        confirm.setOnAction(event -> {

            ProductContainer[] containers = { database, inventoryData, shelfData };

            for (ProductContainer container : containers) {

                Product p = container.getProductById(targetProduct.getId());

                // skips containers that do not have the product
                if (p == null) continue;

                p.setName(nameField.getText());
                p.setDescription(descriptionField.getText());
                p.setPrice(Double.parseDouble(priceField.getText()));
            }

            // refreshes the database display
            if (refreshDatabase != null) {
                refreshDatabase.run();
            }

            window.close();
        });

        // closes the window without changing anything
        cancel.setOnAction(event -> window.close());

        // arranges the controls vertically
        VBox root = new VBox(
                10,
                new HBox(10, new Label("Name:"), nameField),
                new HBox(10, new Label("Description:"), descriptionField),
                new HBox(10, new Label("Price:"), priceField),
                new HBox(10, cancel, confirm)
        );

        root.setPadding(new Insets(20));

        // creates the window
        window = new Stage();
        window.setTitle("Edit Product");
        window.setScene(new Scene(root, 500, 250));

        // makes the window modal
        window.initModality(Modality.WINDOW_MODAL);
        window.initOwner(stage);
    }

    // checks if the input is a valid non-negative double
    private boolean isValidPrice(String text) {
        try {
            double price = Double.parseDouble(text);
            return Double.isFinite(price) && price >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // displays the window
    public void show() {
        window.show();
    }
}