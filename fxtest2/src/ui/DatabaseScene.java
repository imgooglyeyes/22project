package ui;

import application.FarmSystem;
import application.Product;
import application.ProductContainer;
import application.User;
import backend.ProductCreationWindow;
import backend.ProductEditingWindow;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * shows every product in the database, managers can create, edit and delete products here
 *
 * <p>every change goes through FarmSystem, this screen only shows the database
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.1
 * @see SceneManager
 * @see FarmSystem
 */
public class DatabaseScene extends Scene {

    private ProductContainer database;

    private ObjectProperty<Product> selectedProduct =
            new SimpleObjectProperty<>();

    private VBox productRows = new VBox(5);

    /**
     * creates the database scene with the buttons that fit the user type
     *
     * @param stage the main window
     * @param database every product and its details
     * @param manager the scene manager, used for the header and the farm system
     * @param currentUser the user who is logged in
     * @since 1.1 the inventoryData and shelfData parameters were removed, FarmSystem handles them
     */
    public DatabaseScene(
            Stage stage,
            ProductContainer database,
            SceneManager manager,
            User currentUser) {

        super(new BorderPane(), 1000, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        this.database = database;

        FarmSystem farmSystem = manager.getFarmSystem();

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        // creates the column headers
        HBox header = new HBox(10);

        header.getChildren().addAll(
        	createLabel("ID", 50),
        	createLabel("Name", 150),
            createLabel("Description", 600),
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

        // opens the product creation window
        createProduct.setOnAction(event -> {

            ProductCreationWindow window =
                new ProductCreationWindow(
                    stage,
                    farmSystem,
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
                        farmSystem,
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
                // FarmSystem returns null when it worked, or an error message
                String error = farmSystem.deleteEverywhere(product);

                if (error != null) {
                    new Alert(AlertType.ERROR, error).showAndWait();
                }

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
        Label description = createLabel(product.getDescription(), 600);
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