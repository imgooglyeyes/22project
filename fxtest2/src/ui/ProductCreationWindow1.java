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

public class ProductCreationWindow1 {

    private Stage window;

    public ProductCreationWindow1(
            Stage stage,
            ProductContainer database,
            Runnable refreshDatabase) {

        // creates the input fields
        LabelledTextField nameField =
                new LabelledTextField("Name:", true);

        LabelledTextField descriptionField =
                new LabelledTextField("Description:", true);

        TextField priceField = new TextField();
        priceField.setPromptText("Enter price");

        // only allows numbers and one decimal point
        TextFormatter<String> priceFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            if (newText.matches("\\d*(\\.\\d*)?")) {
                return change;
            }

            return null;
        });

        priceField.setTextFormatter(priceFormatter);

        // creates the buttons
        Button create = new Button("Create");
        Button cancel = new Button("Cancel");

        // checks if the price is a valid double
        create.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> nameField.empty.get()
                                || descriptionField.empty.get()
                                || !isValidPrice(priceField.getText()),

                        nameField.empty,
                        descriptionField.empty,
                        priceField.textProperty()
                )
        );

        // creates the price field layout
        HBox priceRow = new HBox(10, new Label("Price:"), priceField);

        // arranges the controls vertically
        HBox buttons = new HBox(10, cancel, create);

        VBox root = new VBox(
                10,
                nameField,
                descriptionField,
                priceRow,
                buttons
        );

        root.setPadding(new Insets(20));

        // creates the window
        window = new Stage();
        window.setTitle("Create New Product");
        window.setScene(new Scene(root, 500, 350));

        // creates and adds the product
        create.setOnAction(event -> {

            double price = Double.parseDouble(priceField.getText());

            Product product = new Product(
                    nameField.getText(),
                    descriptionField.getText(),
                    price,
                    0
            );

            database.add(product, 0);

            // refreshes the database display
            if (refreshDatabase != null) {
                refreshDatabase.run();
            }

            window.close();
        });

        // closes the window without creating anything
        cancel.setOnAction(event -> window.close());

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