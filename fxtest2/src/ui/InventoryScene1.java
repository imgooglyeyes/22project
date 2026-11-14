
package ui;

import application.Product;
import application.ProductContainer;
import application.User;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InventoryScene1 extends Scene {

    private ProductContainer inventoryData;

    private ObjectProperty<Product> selectedProduct =
            new SimpleObjectProperty<>();

    private VBox productRows = new VBox(5);

    public InventoryScene1(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            SceneManager manager,
            User currentUser) {

        super(new BorderPane(), 900, 500);

        this.inventoryData = inventoryData;

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        // creates the column headers
        HBox header = new HBox(10);

        header.getChildren().addAll(
            createLabel("Name", 150),
            createLabel("Description", 400),
            createLabel("Price", 100),
            createLabel("Stock", 80)
        );

        // creates the product list
        VBox inventoryList = new VBox(10, header, productRows);

        ScrollPane scrollPane = new ScrollPane(inventoryList);
        scrollPane.setFitToWidth(true);

        // creates the buttons
        Button addProduct = new Button("Add Product From Database");
        Button deleteStocks = new Button("Delete Stocks");
        Button quit = new Button("Quit");

        HBox buttons = new HBox(10);

        // adds a product from the database
        addProduct.setOnAction(event -> {

            AddProductWindow1 window = new AddProductWindow1(
                stage,
                inventoryData,
                database,
                false,
                this::refreshInventory
            );

            window.show();
        });

        // disables delete when no product is selected
        deleteStocks.disableProperty().bind(
            Bindings.isNull(selectedProduct)
        );

        // removes stocks from inventory
        deleteStocks.setOnAction(event -> {

            Product product = selectedProduct.get();

            if (product != null) {

                MoveProductWindow1 window = new MoveProductWindow1(
                    stage,
                    inventoryData,
                    null,
                    product,
                    this::refreshInventory
                );

                window.show();
            }
        });

        // exits the application
        quit.setOnAction(event -> Platform.exit());

        // displays the shared header
        root.setTop(manager.createHeader("Inventory"));

        // displays the product list
        root.setCenter(scrollPane);

        // displays the buttons
        boolean canManageInventory =
            currentUser.getUserType().equals(User.MANAGER) ||
            currentUser.getUserType().equals(User.CLERK);

        if (canManageInventory) {
            buttons.getChildren().addAll(addProduct, deleteStocks);
        }

        buttons.getChildren().add(quit);
        root.setBottom(buttons);

        // loads the products
        refreshInventory();
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

            // resets all row styles
            for (Node node : productRows.getChildren()) {
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

    // refreshes the inventory display
    private void refreshInventory() {

        productRows.getChildren().clear();
        selectedProduct.set(null);

        for (Product product : inventoryData.getProducts()) {
            addProductRow(product);
        }
    }
}
