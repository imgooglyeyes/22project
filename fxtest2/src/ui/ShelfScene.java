package ui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import application.Product;
import application.ProductContainer;
import application.ProductFilesHandler;
import application.TransactionContainer;
import application.TransactionFilesHandler;
import application.User;
import backend.AddProductWindow;
import backend.MoveProductWindow;
import backend.RequestToBuyWindow;

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
    private TransactionContainer transactionData;

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

        // loads the shared stylesheet
        Styles.apply(this);

        this.database = database;
        this.inventoryData = inventoryData;
        this.shelfData = shelfData;
        this.transactionData = manager.getTransactions(); // used by manual save

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        HBox header = new HBox(10);
        header.getChildren().addAll(
            createLabel("Name", 150),
            createLabel("Description", 400),
            createLabel("Price", 100),
            createLabel("Stock", 80)
        );

        VBox shelfList = new VBox(10, header, productRows);
        ScrollPane scrollPane = new ScrollPane(shelfList);
        scrollPane.setFitToWidth(true);

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

        // customers get their own buttons, they can only request to buy and quit
        Button requestToBuy = new Button("Request To Buy");
        Button customerQuit = new Button("Quit");
        HBox customerButtons = new HBox(10, requestToBuy, customerQuit);

        addProduct.setOnAction(event -> {
            AddProductWindow window = new AddProductWindow(
                stage,
                shelfData,
                inventoryData,
                true,
                this::refreshShelf
            );
            window.show();
        });

        moveStocks.disableProperty().bind(Bindings.isNull(selectedProduct));

        moveStocks.setOnAction(event -> {
            Product product = selectedProduct.get();
            if (product != null) {
                MoveProductWindow window = new MoveProductWindow(
                    stage,
                    shelfData,
                    inventoryData,
                    product,
                    this::refreshShelf
                );
                window.show();
            }
        });

        // the request button needs a selected product
        requestToBuy.disableProperty().bind(Bindings.isNull(selectedProduct));

        // opens the request window, the sales rules are in FarmSystem
        requestToBuy.setOnAction(event -> {
            Product product = selectedProduct.get();
            if (product != null) {
                RequestToBuyWindow window = new RequestToBuyWindow(
                    stage,
                    manager.getFarmSystem(),
                    product,
                    currentUser.getUsername(),
                    this::refreshShelf
                );
                window.show();
            }
        });

        manualSave.setOnAction(event -> saveData());
        quit.setOnAction(event -> Platform.exit());
        customerQuit.setOnAction(event -> Platform.exit());

        boolean isManager = currentUser.getUserType().equals(User.MANAGER);
        boolean isClerk = currentUser.getUserType().equals(User.CLERK);
        boolean isCustomer = currentUser.getUserType().equals(User.CUSTOMER);

        root.setTop(manager.createHeader("Shelf"));
        root.setCenter(scrollPane);
        if (isManager || isClerk) {
            root.setBottom(buttons);
        } else if (isCustomer) {
            root.setBottom(customerButtons);
        }

        refreshShelf();
    }

    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        return label;
    }

    private void addProductRow(Product product) {
        HBox row = new HBox(10);
        Label name = createLabel(product.getName(), 150);
        Label description = createLabel(product.getDescription(), 400);
        Label price = createLabel(product.getFormattedPrice(), 100); // formatted as ₱0.00
        Label stock = createLabel(String.valueOf(product.getStock()), 80);

        row.getChildren().addAll(name, description, price, stock);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: lightgray;");

        row.setOnMouseClicked(event -> {
            selectedProduct.set(product);
            for (Node node : productRows.getChildren()) {
                node.setStyle("-fx-border-color: lightgray;");
            }
            row.setStyle(
                "-fx-background-color: lightblue;" +
                "-fx-border-color: gray;"
            );
        });

        productRows.getChildren().add(row);
    }

    public void refreshShelf() {
        productRows.getChildren().clear();
        selectedProduct.set(null);
        for (Product product : shelfData.getProducts()) {
            addProductRow(product);
        }
    }

    private void saveData() {
    	ProductFilesHandler.save(database, inventoryData, shelfData);
        TransactionFilesHandler.save(transactionData);
    }
}