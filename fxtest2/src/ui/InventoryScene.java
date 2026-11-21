package ui;

import application.Product;
import application.ProductContainer;
import application.User;
import backend.AddProductWindow;
import backend.MoveProductWindow;

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

/**
 * shows the products in the inventory
 *
 * <p>clerks and managers can look at the inventory, but only managers get the buttons
 * to add stock from the database and to delete stock (see SceneManager#canManageInventory()).
 * every stock change goes through FarmSystem, this screen only shows the inventory
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.2
 * @see SceneManager
 * @see application.FarmSystem
 */
public class InventoryScene extends Scene {

    private ProductContainer inventoryData;

    private ObjectProperty<Product> selectedProduct =
            new SimpleObjectProperty<>();

    private VBox productRows = new VBox(5);

    /**
     * creates the inventory scene with the buttons that fit the user
     *
     * @param stage the main window
     * @param database every product and its details, the source when adding to the inventory
     * @param inventoryData the products and stock in the inventory
     * @param manager the scene manager, used for the header, the farm system and the role checks
     * @param currentUser the user who is logged in
     * @since 1.1 the shelfData parameter was removed, this screen no longer needs it
     */
    public InventoryScene(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            SceneManager manager,
            User currentUser) {

        super(new BorderPane(), 1000, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        this.inventoryData = inventoryData;

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        // creates the column headers
        HBox header = new HBox(10);

        header.getChildren().addAll(
            createLabel("Name", 150),
            createLabel("Description", 600),
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
        Button logout = new Button("Log Out");
        Button quit = new Button("Quit");

        HBox buttons = new HBox(10);

        // adds new stock of a product from the database
        addProduct.setOnAction(event -> {

            AddProductWindow window = new AddProductWindow(
                stage,
                manager.getFarmSystem(),
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

                MoveProductWindow window = new MoveProductWindow(
                    stage,
                    manager.getFarmSystem(),
                    product,
                    false,
                    this::refreshInventory
                );

                window.show();
            }
        });

        logout.setOnAction(event -> {
        	currentUser.logOut();
        	manager.showShelf();
        });
        
        // exits the application
        quit.setOnAction(event -> Platform.exit());

        // displays the shared header
        root.setTop(manager.createHeader("Inventory"));

        // displays the product list
        root.setCenter(scrollPane);

        // only managers change the inventory stock, clerks just look at it
        if (manager.canManageInventory()) {
            buttons.getChildren().addAll(addProduct, deleteStocks);
        }

        buttons.getChildren().addAll(logout, quit);
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
        Label description = createLabel(product.getDescription(), 600);
        Label price = createLabel(product.getFormattedPrice(), 100); // formatted as ₱0.00
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
