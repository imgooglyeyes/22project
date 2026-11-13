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

public class InventoryScene1 extends Scene {

    // stores the currently selected product
    private final ObjectProperty<Product> selectedProduct =
            new SimpleObjectProperty<>();

    // holds all the product rows
    private final VBox productRows = new VBox(5);

    public InventoryScene1(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            Scene mainScene,
            User currentUser) {

        super(new VBox(10), 900, 500);

        VBox root = (VBox) getRoot();
        root.setPadding(new Insets(12));

        // creates the column headers
        HBox header = new HBox(10);
        header.getChildren().addAll(
                createLabel("Name", 150),
                createLabel("Description", 400),
                createLabel("Price", 100),
                createLabel("Stock", 80)
        );

        // puts the headers and product rows inside a scrollpane
        VBox inventoryList = new VBox(10);
        inventoryList.getChildren().addAll(header, productRows);

        ScrollPane scrollPane = new ScrollPane(inventoryList);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        // displays the products in the inventory
        for (Product product : inventoryData.getProducts()) {
            addProductRow(product);
        }

        // creates the buttons
        Button createProduct = new Button("Add Product From Database");
        Button viewShelf = new Button("View Shelf");
        Button quit = new Button("Quit");

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(createProduct, viewShelf);

        // opens the window for adding a product
        createProduct.setOnAction(event -> {

            AddProductWindow1 addWindow = new AddProductWindow1(
                    stage,
                    inventoryData,
                    database,
                    false,
                    () -> {
                        // clears and reloads the inventory rows
                        productRows.getChildren().clear();

                        for (Product p : inventoryData.getProducts()) {
                            addProductRow(p);
                        }

                        selectedProduct.set(null);
                    }
            );

            addWindow.show();
        });
        // returns to the main scene
        viewShelf.setOnAction(event -> {
            stage.setScene(mainScene);
            stage.setTitle("Proof of Concept");
        });

        // adds manager-only buttons
        if (currentUser.getUserType().equals(User.MANAGER)) {

            Button viewDatabase = new Button("View Database");
            Button removeStocks = new Button("Delete Stocks");

            // disables the delete button when nothing is selected
            removeStocks.disableProperty().bind(
                    Bindings.isNull(selectedProduct)
            );

            viewDatabase.setOnAction(event -> {
                DatabaseScene databaseScene =
                        new DatabaseScene(
                                stage,
                                database,
                                inventoryData,
                                shelfData,
                                mainScene,
                                currentUser
                        );

                stage.setScene(databaseScene);
                stage.setTitle("Database");
            });

            removeStocks.setOnAction(event -> {

                Product product = selectedProduct.get();

                if (product != null) {

                    MoveProductWindow1 moveWindow = new MoveProductWindow1(
                            stage,
                            inventoryData,
                            null,
                            product,
                            () -> {
                                // clears and reloads the inventory rows
                                productRows.getChildren().clear();

                                for (Product p : inventoryData.getProducts()) {
                                    addProductRow(p);
                                }

                                selectedProduct.set(null);
                            }
                    );

                    moveWindow.show();
                }
            });
            buttons.getChildren().addAll(viewDatabase, removeStocks);
        }

        // exits the application
        quit.setOnAction(event -> Platform.exit());

        buttons.getChildren().add(quit);

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

        Label name = createLabel(product.getName(), 150);
        Label description = createLabel(product.getDescription(), 400);
        Label price = createLabel(String.valueOf(product.getPrice()), 100);
        Label stock = createLabel(String.valueOf(product.getStock()), 80);

        row.getChildren().addAll(name, description, price, stock);
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
}