package ui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import application.Product;
import application.ProductContainer;
import application.TransactionContainer;
import application.TransactionStore;
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

        manualSave.setOnAction(event -> saveData());
        quit.setOnAction(event -> Platform.exit());

        boolean isManager = currentUser.getUserType().equals(User.MANAGER);
        boolean isClerk = currentUser.getUserType().equals(User.CLERK);

        root.setTop(manager.createHeader("Shelf"));
        root.setCenter(scrollPane);
        if (isManager || isClerk) {
            root.setBottom(buttons);
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
        try (
            BufferedWriter databaseWriter =
                Files.newBufferedWriter(Paths.get("src/data/productsDB.txt"));
            BufferedWriter shelfWriter =
                Files.newBufferedWriter(Paths.get("src/data/shelf.txt"));
            BufferedWriter inventoryWriter =
                Files.newBufferedWriter(Paths.get("src/data/inventory.txt"))
        ) {
            for (Product p : database.getProducts()) {
                databaseWriter.write(
                    p.getId() + "|" +
                    p.getName() + "|" +
                    p.getDescription() + "|" +
                    p.getPrice()
                );
                databaseWriter.newLine();
            }

            for (Product p : shelfData.getProducts()) {
                shelfWriter.write(p.getId() + "|" + p.getStock());
                shelfWriter.newLine();
            }

            for (Product p : inventoryData.getProducts()) {
                inventoryWriter.write(p.getId() + "|" + p.getStock());
                inventoryWriter.newLine();
            }

            System.out.println("Saving complete.");
        } catch (IOException e) {
            System.out.println("Something went wrong while saving.");
            e.printStackTrace();
        }

        // transactions have their own save, it handles its own errors
        TransactionStore.save(transactionData);
    }
}