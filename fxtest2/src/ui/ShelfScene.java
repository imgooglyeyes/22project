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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class ShelfScene extends Scene {

    // stores the currently selected product
    private final ObjectProperty<Product> selectedProduct =
            new SimpleObjectProperty<>();

    // holds all the product rows
    private final VBox productRows = new VBox(5);

    private ProductContainer database;
    private ProductContainer inventoryData;
    private ProductContainer shelfData;

    private SceneManager sceneManager;

    public ShelfScene(
            Stage stage,
            SceneManager sceneManager,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            User currentUser) {

        super(new VBox(10), 900, 500);

        this.database = database;
        this.inventoryData = inventoryData;
        this.shelfData = shelfData;
        this.sceneManager = sceneManager;

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
        VBox shelfList = new VBox(10);
        shelfList.getChildren().addAll(header, productRows);

        ScrollPane scrollPane = new ScrollPane(shelfList);
        scrollPane.setFitToWidth(true);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // displays the products on the shelf
        refreshShelf();

        // creates the buttons
        Button addProduct = new Button("Add Product From Inventory");
        Button viewInventory = new Button("View Inventory");
        Button viewDatabase = new Button("View Database");
        Button moveStocks = new Button("Move Stocks To Inventory");
        Button manualSave = new Button("Manual Save");
        Button quit = new Button("Quit");
        Button login = new Button("Login");

        HBox buttons = new HBox(10);

        // checks the user's permissions
        boolean isManager =
                currentUser.getUserType().equals(User.MANAGER);

        boolean canManageStock =
                isManager ||
                currentUser.getUserType().equals(User.CLERK);

        // adds a product from inventory to the shelf
        addProduct.setOnAction(event -> {

            AddProductWindow1 addWindow = new AddProductWindow1(
                    stage,
                    shelfData,
                    inventoryData,
                    true,
                    this::refreshShelf
            );

            addWindow.show();
        });

        // opens the inventory
        viewInventory.setOnAction(event -> {
            sceneManager.showInventory();
        });

        // opens the database
        viewDatabase.setOnAction(event -> {
            sceneManager.showDatabase();
        });

        // disables moving stocks when nothing is selected
        moveStocks.disableProperty().bind(
                Bindings.isNull(selectedProduct)
        );

        // moves stocks from the shelf to inventory
        moveStocks.setOnAction(event -> {

            Product product = selectedProduct.get();

            if (product != null) {

                MoveProductWindow1 moveWindow = new MoveProductWindow1(
                        stage,
                        shelfData,
                        inventoryData,
                        product,
                        this::refreshShelf
                );

                moveWindow.show();
            }
        });

        // saves all product data
        manualSave.setOnAction(event -> saveData());

        // exits the application
        quit.setOnAction(event -> Platform.exit());

        // displays a temporary login message
        login.setOnAction(event -> {
            System.out.println("login is not implemented yet.");
        });

        // adds buttons based on the user's role
        if (canManageStock) {
            buttons.getChildren().addAll(
                    addProduct,
                    viewInventory
            );
        }

        if (isManager) {
            buttons.getChildren().add(viewDatabase);
        }

        if (canManageStock) {
            buttons.getChildren().addAll(
                    moveStocks,
                    manualSave,
                    quit
            );
        }

        if (currentUser.getUserType().equals(User.GUEST)) {
            buttons.getChildren().add(login);
        }

        // displays the shelf and its buttons
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

    // clears and reloads the shelf display
    private void refreshShelf() {

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

            // saves all products in the database
            for (Product p : database.getProducts()) {
                databaseWriter.write(
                        p.getId() + "|" +
                        p.getName() + "|" +
                        p.getDescription() + "|" +
                        p.getPrice()
                );

                databaseWriter.newLine();
            }

            // saves all products on the shelf
            for (Product p : shelfData.getProducts()) {
                shelfWriter.write(
                        p.getId() + "|" + p.getStock()
                );

                shelfWriter.newLine();
            }

            // saves all products in inventory
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