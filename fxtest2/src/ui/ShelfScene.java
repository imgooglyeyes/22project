package ui;

import application.FarmSystem;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * the main screen, it shows the products on the shelf
 *
 * <p>each row shows the name, description, where the product comes from, price and stock.
 * the made-from column shows the name of the harvest product it is made from,
 * or "Harvest item" if it is not made from anything
 *
 * <p>a filter above the list can show every product or only the products made from a harvest item.
 * a product counts as made when its madeFrom is set. the filter only hides rows,
 * it never changes the shelf, and it goes back to "all" when the screen is opened again
 *
 * <p>which buttons appear follows the permission table in SceneManager:
 * clerks and managers can add stock from the inventory and save by hand, only managers can move
 * stock back to the inventory, and only customers can send a request to buy.
 * every stock change goes through FarmSystem, this screen only shows the shelf
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.5
 * @see SceneManager
 * @see application.FarmSystem
 */
public class ShelfScene extends Scene {

    /** text shown in the made-from column for a product that is not made from anything */
    private static final String HARVEST_TEXT = "Harvest item";

    /** filter choice that shows every product on the shelf */
    private static final String FILTER_ALL = "All products";
    /** filter choice that shows only the products made from a harvest item */
    private static final String FILTER_MADE = "Made from harvest";

    private ProductContainer inventoryData;
    private ProductContainer shelfData;
    private TransactionContainer transactionData;
    /** used to look up the name of the source product of a made product */
    private FarmSystem farmSystem;

    /** the filter choice, refreshShelf reads it every time the list is drawn */
    private ComboBox<String> filterBox;

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
        this.farmSystem = manager.getFarmSystem();

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        HBox header = new HBox(10);
        header.getChildren().addAll(
            createLabel("Name", 150),
            createLabel("Description", 360),
            createLabel("Made From", 200),
            createLabel("Price", 100),
            createLabel("Stock", 80)
        );

        VBox shelfList = new VBox(10, header, productRows);
        ScrollPane scrollPane = new ScrollPane(shelfList);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS); // the list takes the space left under the filter

        // the filter starts on "all", picking another choice redraws the list
        this.filterBox = new ComboBox<>();
        filterBox.getItems().addAll(FILTER_ALL, FILTER_MADE);
        filterBox.setValue(FILTER_ALL);
        filterBox.setOnAction(event -> refreshShelf());

        HBox filterRow = new HBox(10, new Label("Show:"), filterBox);
        VBox center = new VBox(10, filterRow, scrollPane);

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
        root.setCenter(center);
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
     * gets the text for the made-from column of a product
     * <p>a made product whose source can't be found shows "Unknown" instead of crashing
     *
     * @param product the shelf product to describe
     * @return the name of the source product, "Harvest item" if it has none, or "Unknown" if the source is missing
     * @since 1.4
     */
    private String madeFromText(Product product) {
        if (!product.isMade()) return HARVEST_TEXT;

        String sourceName = farmSystem.getMadeFromName(product);
        return sourceName == null ? "Unknown" : sourceName;
    }

    /**
     * checks if a product passes the filter that is picked right now
     * <p>a product counts as made when its madeFrom is set
     *
     * @param product the shelf product to check
     * @return true if the product should be shown
     * @since 1.5
     */
    private boolean passesFilter(Product product) {
        if (FILTER_MADE.equals(filterBox.getValue())) return product.isMade();
        return true; // "all" shows everything
    }

    /**
     * creates a row for one product, clicking the row selects the product
     *
     * @param product the shelf product to show
     */
    private void addProductRow(Product product) {
        HBox row = new HBox(10);
        Label name = createLabel(product.getName(), 150);
        Label description = createLabel(product.getDescription(), 360);
        Label madeFrom = createLabel(madeFromText(product), 200);
        Label price = createLabel(product.getFormattedPrice(), 100); // formatted as ₱0.00
        Label stock = createLabel(String.valueOf(product.getStock()), 80);

        row.getChildren().addAll(name, description, madeFrom, price, stock);
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

    /**
     * redraws the shelf from the shelf container and clears the selection
     * <p>only the products that pass the filter get a row, and a note is shown
     * when the shelf has products but none of them match
     */
    public void refreshShelf() {
        productRows.getChildren().clear();
        selectedProduct.set(null); // a hidden product must not stay selected

        for (Product product : shelfData.getProducts()) {
            if (passesFilter(product)) addProductRow(product);
        }

        if (productRows.getChildren().isEmpty() && !shelfData.getProducts().isEmpty()) {
            productRows.getChildren().add(new Label("No products on the shelf match this filter."));
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