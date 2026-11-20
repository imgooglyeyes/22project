package ui;

import application.Product;
import application.ProductContainer;
import application.User;
import backend.ProductCreationWindow;
import backend.ProductEditingWindow;
import backend.UniversalDeleter;

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

public class DatabaseScene extends Scene {

    private ProductContainer database;

    private ObjectProperty<Product> selectedProduct =
            new SimpleObjectProperty<>();

    private VBox productRows = new VBox(5);

    public DatabaseScene(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            SceneManager manager,
            User currentUser) {

        super(new BorderPane(), 900, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        this.database = database;

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        // creates the column headers
        HBox header = new HBox(10);

        header.getChildren().addAll(
        	createLabel("ID", 50),
        	createLabel("Name", 150),
            createLabel("Description", 400),
            createLabel("Price", 100)
        );

        // creates the product list
        VBox databaseList = new VBox(10, header, productRows);

        ScrollPane scrollPane = new ScrollPane(databaseList);
        scrollPane.setFitToWidth(true);

        // creates the buttons
        Button createProduct = new Button("Create New Product");
        Button editSelected = new Button("Edit Product Data");
        Button deleteSelected = new Button("Delete All Instances");
        Button logout = new Button("Log Out");
        Button quit = new Button("Quit");

        HBox buttons = new HBox(10);

        // creates the deleter used to remove a product everywhere
        UniversalDeleter deleter =
            new UniversalDeleter(database, inventoryData, shelfData);

        // opens the product creation window
        createProduct.setOnAction(event -> {

            ProductCreationWindow window =
                new ProductCreationWindow(
                    stage,
                    database,
                    this::refreshDatabase
                );

            window.show();
        });

        // disables edit and delete when no product is selected
        editSelected.disableProperty().bind(
            Bindings.isNull(selectedProduct)
        );

        deleteSelected.disableProperty().bind(
            Bindings.isNull(selectedProduct)
        );

        // opens the product editing window
        editSelected.setOnAction(event -> {

            Product product = selectedProduct.get();

            if (product != null) {

                ProductEditingWindow window =
                    new ProductEditingWindow(
                        stage,
                        database,
                        inventoryData,
                        shelfData,
                        product,
                        this::refreshDatabase
                    );

                window.show();
            }
        });

        // deletes the selected product from the database, inventory, and shelf
        deleteSelected.setOnAction(event -> {

            Product product = selectedProduct.get();

            if (product != null) {
                deleter.delete(product);
                refreshDatabase();
            }
        });

        logout.setOnAction(event -> {
        	currentUser.logOut();
        	manager.showShelf();
        });
        
        // exits the application
        quit.setOnAction(event -> Platform.exit());

        // displays the shared header
        root.setTop(manager.createHeader("Database"));

        // displays the product list
        root.setCenter(scrollPane);

        // displays the buttons
        if (currentUser.getUserType().equals(User.MANAGER)) {
            buttons.getChildren().addAll(
                createProduct,
                editSelected,
                deleteSelected
            );
        }

        buttons.getChildren().addAll(logout, quit);
        root.setBottom(buttons);

        // loads the products
        refreshDatabase();
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

        Label id = createLabel(String.valueOf(product.getId()), 50);
        Label name = createLabel(product.getName(), 150);
        Label description = createLabel(product.getDescription(), 400);
        Label price = createLabel(product.getFormattedPrice(), 100); // formatted as ₱0.00
        

        row.getChildren().addAll(id, name, description, price);
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

    // refreshes the database display
    private void refreshDatabase() {

        productRows.getChildren().clear();
        selectedProduct.set(null);

        for (Product product : database.getProducts()) {
            addProductRow(product);
        }
    }
}