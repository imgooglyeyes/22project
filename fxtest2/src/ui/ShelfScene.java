package ui;

import application.Product;
import application.ProductContainer;
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
 * <p>which buttons appear follows the permission table in SceneManager:
 * clerks and managers can add stock from the inventory and save by hand, only managers can move
 * stock back to the inventory, and only customers can send a request to buy.
 * every stock change goes through FarmSystem, this screen only shows the shelf
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.3
 * @see SceneManager
 * @see application.FarmSystem
 */
public class ShelfScene extends Scene {

    private ProductContainer inventoryData;
    private ProductContainer shelfData;
    private TransactionContainer transactionData;

    private ObjectProperty<Product> selectedProduct =
            new SimpleObjectProperty<>();

    private VBox productRows = new VBox(5);

    /**
     * creates the shelf scene with the buttons that fit the user
     *
     * @param stage the main window
     * @param manager the scene manager, used for the header, saving, the farm system and the role checks
     * @param inventoryData the products and stock in the inventory, the source when adding to the shelf
     * @param shelfData the products and stock on the shelf
     * @param currentUser the user who is logged in
     * @since 1.2 the database parameter was removed, this screen no longer needs it
     */
    public ShelfScene(
            Stage stage,
            SceneManager manager,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            User currentUser) {

        super(new BorderPane(), 1000, 500);

        // loads the shared stylesheet
        Styles.apply(this);

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

        // customers get their own button, they can only request to buy
        Button requestToBuy = new Button("Request To Buy");

        // moves stock from the inventory to the shelf
        addProduct.setOnAction(event -> {
            AddProductWindow window = new AddProductWindow(
                stage,
                manager.getFarmSystem(),
                inventoryData,
                true,
                this::refreshShelf
            );
            window.show();
        });

        moveStocks.disableProperty().bind(Bindings.isNull(selectedProduct));

        // moves stock from the shelf back to the inventory
        moveStocks.setOnAction(event -> {
            Product product = selectedProduct.get();
            if (product != null) {
                MoveProductWindow window = new MoveProductWindow(
                    stage,
                    manager.getFarmSystem(),
                    product,
                    true,
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

        manualSave.setOnAction(event -> saveData(manager));
        logout.setOnAction(event -> {
        	currentUser.logOut();
        	manager.showShelf();
        });
        quit.setOnAction(event -> Platform.exit());

        // each button is added only if the permission table allows it for this user
        HBox buttons = new HBox(10);

        if (manager.canRequestToBuy()) buttons.getChildren().add(requestToBuy);
        if (manager.canAddToShelf()) buttons.getChildren().add(addProduct);
        if (manager.canMoveToInventory()) buttons.getChildren().add(moveStocks);
        if (manager.canManualSave()) buttons.getChildren().add(manualSave);

        // a guest has no account, so there is nothing to log out of
        boolean isGuest = currentUser.getUserType().equals(User.GUEST);
        if (!isGuest) buttons.getChildren().add(logout);

        buttons.getChildren().add(quit);

        root.setTop(manager.createHeader("Shelf"));
        root.setCenter(scrollPane);
        root.setBottom(buttons);

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

    /**
     * manual save, writes every product file and the transactions as a backup
     *
     * @param manager the scene manager that knows the product containers
     */
    private void saveData(SceneManager manager) {
        manager.saveProductContainers();
        TransactionFilesHandler.save(transactionData);
    }
}