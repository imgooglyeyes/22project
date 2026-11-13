
package ui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

public class ShelfScene extends Scene {

    private ProductContainer database;
    private ProductContainer inventoryData;
    private ProductContainer shelfData;

    private ObjectProperty<Product> selectedProduct =
            new SimpleObjectProperty<>();

    private VBox productRows = new VBox(5);

    public ShelfScene(
            Stage stage,
            SceneManager manager,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            User currentUser) {

        super(new BorderPane(), 900, 500);

        this.database = database;
        this.inventoryData = inventoryData;
        this.shelfData = shelfData;

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
        VBox shelfList = new VBox(10, header, productRows);

        ScrollPane scrollPane = new ScrollPane(shelfList);
        scrollPane.setFitToWidth(true);

        // creates the buttons
        Button addProduct = new Button("Add Product From Inventory");
        Button moveStocks = new Button("Move Stocks To Inventory");
        Button manualSave = new Button("Manual Save");
        Button quit = new Button("Quit");

        HBox buttons = new HBox(10,
            addProduct,
            moveStocks,
            manualSave,
            quit
        );

        // adds a product from inventory
        addProduct.setOnAction(event -> {

            AddProductWindow1 window = new AddProductWindow1(
                stage,
                shelfData,
                inventoryData,
                true,
                this::refreshShelf
            );

            window.show();
        });

        // disables moving stocks when nothing is selected
        moveStocks.disableProperty().bind(
            Bindings.isNull(selectedProduct)
        );

        // moves stocks back to inventory
        moveStocks.setOnAction(event -> {

            Product product = selectedProduct.get();

            if (product != null) {

                MoveProductWindow1 window = new MoveProductWindow1(
                    stage,
                    shelfData,
                    inventoryData,
                    product,
                    this::refreshShelf
                );

                window.show();
            }
        });

        // saves the product data
        manualSave.setOnAction(event -> saveData());

        // exits the application
        quit.setOnAction(event -> Platform.exit());

        // checks the user's permissions
        boolean isManager =
            currentUser.getUserType().equals(User.MANAGER);

        boolean isClerk =
            currentUser.getUserType().equals(User.CLERK);

        // displays the shared header
        root.setTop(manager.createHeader("Shelf"));

        // displays the product list
        root.setCenter(scrollPane);

        // displays the buttons
        if (isManager || isClerk) {
            root.setBottom(buttons);
        }

        // loads the products
        refreshShelf();
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

    // refreshes the shelf display
    public void refreshShelf() {

        productRows.getChildren().clear();
        selectedProduct.set(null);

        for (Product product : shelfData.getProducts()) {
            addProductRow(product);
        }
    }

    // saves the database, shelf, and inventory
    private void saveData() {

        try (
            BufferedWriter databaseWriter =
                Files.newBufferedWriter(Paths.get("src/data/productsDB.txt"));

            BufferedWriter shelfWriter =
                Files.newBufferedWriter(Paths.get("src/data/shelf.txt"));

            BufferedWriter inventoryWriter =
                Files.newBufferedWriter(Paths.get("src/data/inventory.txt"))
        ) {

            // saves all database products
            for (Product p : database.getProducts()) {

                databaseWriter.write(
                    p.getId() + "|" +
                    p.getName() + "|" +
                    p.getDescription() + "|" +
                    p.getPrice()
                );

                databaseWriter.newLine();
            }

            // saves shelf products
            for (Product p : shelfData.getProducts()) {

                shelfWriter.write(
                    p.getId() + "|" + p.getStock()
                );

                shelfWriter.newLine();
            }

            // saves inventory products
            for (Product p : inventoryData.getProducts()) {

                inventoryWriter.write(
                    p.getId() + "|" + p.getStock()
                );

                inventoryWriter.newLine();
            }

            System.out.println("Saving complete.");

        } catch (IOException e) {
            System.out.println("Something went wrong while saving.");
            e.printStackTrace();
        }
    }
}
