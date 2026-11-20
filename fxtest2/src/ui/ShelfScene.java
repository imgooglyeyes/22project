package ui;

import application.Product;
import application.ProductContainer;
import application.ProductsFileHandler;
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

/**
 * the main screen, it shows the products on the shelf
 *
 * <p>managers and clerks can move stock between the shelf and the inventory and save by hand.
 * customers can only select a product and send a request to buy it
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.1
 * @see SceneManager
 * @see ProductsFileHandler
 */
public class ShelfScene extends Scene {

    private ProductContainer database;
    private ProductContainer inventoryData;
    private ProductContainer shelfData;
    private TransactionContainer transactionData;

    private ObjectProperty<Product> selectedProduct =
            new SimpleObjectProperty<>();

    private VBox productRows = new VBox(5);

    /**
     * creates the shelf scene with the buttons that fit the user type
     *
     * @param stage the main window
     * @param manager the scene manager, used for the header, saving and the farm system
     * @param database every product and its details
     * @param inventoryData the products and stock in the inventory
     * @param shelfData the products and stock on the shelf
     * @param currentUser the user who is logged in
     */
    public ShelfScene(
            Stage stage,
            SceneManager manager,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            User currentUser) {

        super(new BorderPane(), 1000, 500);

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
            createLabel("Description", 600),
            createLabel("Price", 100),
            createLabel("Stock", 80)
        );

        VBox shelfList = new VBox(10, header, productRows);
        ScrollPane scrollPane = new ScrollPane(shelfList);
        scrollPane.setFitToWidth(true);

        Button addProduct = new Button("Add Product From Inventory");
        Button moveStocks = new Button("Move Stocks To Inventory");
        Button manualSave = new Button("Manual Save");
        Button logout = new Button("Log Out");
        Button quit = new Button("Quit");

        // customers get their own buttons, they can only request to buy and quit
        Button requestToBuy = new Button("Request To Buy");

        addProduct.setOnAction(event -> {
            AddProductWindow window = new AddProductWindow(
                stage,
                manager,
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
                    manager,
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
        logout.setOnAction(event -> {
        	currentUser.logOut();
        	manager.showShelf();
        });
        quit.setOnAction(event -> Platform.exit());

        boolean isManager = currentUser.getUserType().equals(User.MANAGER);
        boolean isClerk = currentUser.getUserType().equals(User.CLERK);
        boolean isCustomer = currentUser.getUserType().equals(User.CUSTOMER);

        root.setTop(manager.createHeader("Shelf"));
        root.setCenter(scrollPane);
        if (isManager || isClerk) {
            root.setBottom(new HBox(10, addProduct, moveStocks, manualSave, logout, quit));
        } else if (isCustomer) {
            root.setBottom(new HBox(10, requestToBuy, logout, quit));
        } else { // guest
        	root.setBottom(quit);
        }

        refreshShelf();
    }

    /**
     * creates a label with a fixed width
     *
     * @param text the text of the label
     * @param width the preferred width in pixels
     * @return the label
     */
    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        return label;
    }

    /**
     * creates a row for one product, clicking the row selects the product
     *
     * @param product the shelf product to show
     */
    private void addProductRow(Product product) {
        HBox row = new HBox(10);
        Label name = createLabel(product.getName(), 150);
        Label description = createLabel(product.getDescription(), 600);
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

    /** redraws the shelf from the shelf container and clears the selection */
    public void refreshShelf() {
        productRows.getChildren().clear();
        selectedProduct.set(null);
        for (Product product : shelfData.getProducts()) {
            addProductRow(product);
        }
    }

    /** manual save, writes every product file and the transactions as a backup */
    private void saveData() {
    	ProductsFileHandler.saveAll(database, inventoryData, shelfData);
        TransactionFilesHandler.save(transactionData);
    }
}