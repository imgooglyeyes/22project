package ui;

import application.Product;
import application.ProductContainer;
import application.User;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DatabaseScene1 extends Scene {

    // stores the currently selected product
    private final ObjectProperty<Product> selectedProduct =
            new SimpleObjectProperty<>();

    // holds all the product rows
    private final VBox productRows = new VBox(5);

    private final ProductContainer database;

    public DatabaseScene1(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            Scene mainScene,
            User currentUser) {

        super(new VBox(10), 900, 500);

        this.database = database;

        VBox root = (VBox) getRoot();
        root.setPadding(new Insets(12));

        // creates the column headers
        HBox header = new HBox(10);
        header.getChildren().addAll(
                createLabel("Internal ID", 100),
                createLabel("Name", 150),
                createLabel("Description", 400),
                createLabel("Price", 100)
        );

        // puts the headers and product rows inside a scrollpane
        VBox databaseList = new VBox(10);
        databaseList.getChildren().addAll(header, productRows);

        ScrollPane scrollPane = new ScrollPane(databaseList);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        // displays the products in the database
        refreshDatabase();

        // creates the buttons
        Button createProduct = new Button("Create New Product");
        Button viewShelf = new Button("View Shelf");
        Button viewInventory = new Button("View Inventory");
        Button deleteSelected = new Button("Delete Selected");
        Button quit = new Button("Quit");

        HBox buttons = new HBox(
                10,
                createProduct,
                viewShelf,
                viewInventory,
                deleteSelected,
                quit
        );

        // opens the window for creating a product
        createProduct.setOnAction(event -> {

            ProductCreationWindow1 creationWindow =
                    new ProductCreationWindow1(
                            stage,
                            database,
                            this::refreshDatabase
                    );

            creationWindow.show();
        });
        // returns to the shelf
        viewShelf.setOnAction(event -> {
            stage.setScene(mainScene);
            stage.setTitle("The Shelf");
        });

        // opens the inventory scene
        viewInventory.setOnAction(event -> {
            InventoryScene1 inventoryScene =
                    new InventoryScene1(
                            stage,
                            database,
                            inventoryData,
                            shelfData,
                            mainScene,
                            currentUser
                    );

            stage.setScene(inventoryScene);
            stage.setTitle("Inventory");
        });

        // disables delete when nothing is selected
        deleteSelected.disableProperty().bind(
                Bindings.isNull(selectedProduct)
        );

        // handles deleting the selected product
        deleteSelected.setOnAction(event -> {
            Product product = selectedProduct.get();

            if (product != null) {
                System.out.println(
                        "This should delete " + product.getName()
                );
            }
        });

        // exits the application
        quit.setOnAction(event -> Platform.exit());

        root.getChildren().addAll(scrollPane, buttons);
    }

    // creates a label with a fixed width
    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        return label;
    }

    // creates a row for one product
    private void addProductRow(Product product) {

        HBox row = new HBox(10);

        Label id = createLabel(String.valueOf(product.getId()), 100);
        Label name = createLabel(product.getName(), 150);
        Label description = createLabel(product.getDescription(), 400);
        Label price = createLabel(String.valueOf(product.getPrice()), 100);

        row.getChildren().addAll(id, name, description, price);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: lightgray;");

        // selects the product when its row is clicked
        row.setOnMouseClicked(event -> {

            selectedProduct.set(product);

            // removes the selection color from all rows
            for (var node : productRows.getChildren()) {
                node.setStyle("-fx-border-color: lightgray;");
            }

            // highlights the selected row
            row.setStyle(
                    "-fx-background-color: lightblue;" +
                    "-fx-border-color: gray;"
            );
        });

        productRows.getChildren().add(row);
    }

    // clears and reloads the database display
    private void refreshDatabase() {

        productRows.getChildren().clear();
        selectedProduct.set(null);

        for (Product product : database.getProducts()) {
            addProductRow(product);
        }
    }
}