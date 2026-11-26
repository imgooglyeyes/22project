package ui;

import java.util.ArrayList;
import java.util.Comparator;

import application.FarmSystem;
import application.Product;
import application.ProductContainer;
import application.TransactionFilesHandler;
import application.User;
import backend.AddProductWindow;
import backend.MoveProductWindow;
import backend.RequestToBuyWindow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * the marketplace, a grid of product cards for everything on the shelf
 *
 * <p>it replaces the old shelf scene. the toolbar has a filter (all, harvest, made from harvest),
 * a sort and, for staff, a toggle between manage shelf and marketplace preview.
 * the search box of the top bar filters the cards by name and by the name of the source product
 *
 * <p>the button of a card follows the permission table in SceneManager:
 * a guest is asked to log in, a customer can request to buy, a manager can move stock back
 * to the inventory in manage mode, and in preview mode the request button is shown disabled
 * so staff can see what customers see. clerks and managers add stock with the toolbar button.
 * every stock change goes through FarmSystem, this page only shows the shelf
 *
 * <p>every action that worked is answered with a toast, and a new request updates the bell,
 * both are done by the code from Ui.afterAction.
 * when the shelf is empty or nothing matches the search and filter, an empty state with a button
 * takes the place of the grid. the page is created again every time it opens
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.2
 * @since 1.1 toasts for the add, move, request and save actions, the bell is updated after a request
 * @since 1.2 show and the after code moved to Ui
 * @see ProductCard
 * @see PageToolbar
 * @see Searchable
 * @see Ui
 * @see SceneManager#showShelf()
 */
public class MarketplacePage extends BorderPane implements Searchable {

    /** filter choices, the made-from filter of the plan */
    private static final String FILTER_ALL = "All products";
    private static final String FILTER_HARVEST = "Harvest";
    private static final String FILTER_MADE = "Made from harvest";

    /** sort choices */
    private static final String SORT_NAME = "Name (A-Z)";
    private static final String SORT_PRICE_LOW = "Price (low to high)";
    private static final String SORT_PRICE_HIGH = "Price (high to low)";
    private static final String SORT_STOCK = "Stock (most first)";

    /** the two views of staff */
    private static final String MODE_MANAGE = "Manage Shelf";
    private static final String MODE_PREVIEW = "Marketplace Preview";

    private final Stage stage;
    private final SceneManager manager;
    private final FarmSystem farmSystem;
    private final ProductContainer inventoryData;
    private final ProductContainer shelfData;
    private final User currentUser;

    /** true for clerks and managers, they get the manage and preview toggle */
    private final boolean staffView;

    private final PageToolbar toolbar = new PageToolbar("Marketplace");
    private final SegmentedToggle filterToggle = new SegmentedToggle(FILTER_ALL, FILTER_HARVEST, FILTER_MADE);
    private final ComboBox<String> sortBox = new ComboBox<>();
    private final SegmentedToggle modeToggle = new SegmentedToggle(MODE_MANAGE, MODE_PREVIEW);

    /** the staff buttons of the toolbar, only shown in manage mode */
    private final Button addButton = new Button("Add From Inventory");
    private final Button saveButton = new Button("Save");

    private final FlowPane grid = new FlowPane(16, 16);
    private final ScrollPane scrollPane = new ScrollPane(grid);
    /** holds the grid, or an empty state in its place */
    private final StackPane body = new StackPane();

    /** the typed search text in lowercase, empty shows everything */
    private String query = "";

    /**
     * creates the marketplace page
     *
     * @param stage the main window, the popup windows are opened on it
     * @param manager the scene manager, answers the role checks, opens the other pages and shows toasts
     * @param inventoryData the products and stock in the inventory, the source when adding to the shelf
     * @param shelfData the products and stock on the shelf
     * @param currentUser the user who is logged in, or the guest
     */
    public MarketplacePage(
            Stage stage,
            SceneManager manager,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            User currentUser) {

        this.stage = stage;
        this.manager = manager;
        this.farmSystem = manager.getFarmSystem();
        this.inventoryData = inventoryData;
        this.shelfData = shelfData;
        this.currentUser = currentUser;
        this.staffView = manager.canAddToShelf(); // the same people who stock the shelf can manage it

        setPadding(new Insets(24));

        // sort and filter redraw the grid
        sortBox.getItems().addAll(SORT_NAME, SORT_PRICE_LOW, SORT_PRICE_HIGH, SORT_STOCK);
        sortBox.setValue(SORT_NAME);
        sortBox.setOnAction(event -> refresh());
        filterToggle.setOnChange(this::refresh);

        Label sortLabel = new Label("Sort by");
        sortLabel.getStyleClass().add("caption");
        HBox sortRow = new HBox(8, sortLabel, sortBox);
        sortRow.setAlignment(Pos.CENTER_LEFT);

        // staff can switch between managing the shelf and seeing what customers see
        if (staffView) {
            modeToggle.setOnChange(this::refresh);
            toolbar.addFilter(modeToggle);
        }
        toolbar.addFilter(filterToggle, sortRow);

        // the toolbar buttons are shown only in manage mode, see updateActions()
        saveButton.getStyleClass().add("secondary");
        addButton.setOnAction(event -> openAddWindow());
        saveButton.setOnAction(event -> saveData());
        if (manager.canAddToShelf()) toolbar.addAction(addButton);
        if (manager.canManualSave()) toolbar.addAction(saveButton);

        grid.setPadding(new Insets(4)); // room for the card shadows, they would be cut at the edge otherwise
        scrollPane.setFitToWidth(true); // lets the cards wrap instead of scrolling sideways
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

        setTop(toolbar);
        setCenter(body);
        BorderPane.setMargin(body, new Insets(16, 0, 0, 0));

        refresh();
    }

    // ---------- search ----------

    /**
     * filters the cards by the text of the search box
     *
     * @param text what is typed in the search box
     */
    @Override
    public void onSearch(String text) {
        query = text == null ? "" : text.trim().toLowerCase();
        refresh();
    }

    /** @return the faint text of the search box on this page */
    @Override
    public String searchPrompt() {
        return "Search products";
    }

    // ---------- drawing ----------

    /**
     * redraws the grid from the shelf container
     * <p>the cards are built again every time, so the stock and the buttons are always right.
     * the caption under the title shows how many products are visible
     */
    private void refresh() {
        updateActions();
        grid.getChildren().clear();

        int total = shelfData.getProducts().size();
        ArrayList<Product> shown = visibleProducts();

        toolbar.setSubtitle(shown.size() == total
            ? total + (total == 1 ? " product" : " products") + " on the shelf"
            : shown.size() + " of " + total + " products"
        );

        if (total == 0) {
            body.getChildren().setAll(buildShelfEmptyState());
            return;
        }
        if (shown.isEmpty()) {
            body.getChildren().setAll(buildNoMatchState());
            return;
        }

        for (Product product : shown) {
            grid.getChildren().add(new ProductCard(product, farmSystem.getMadeFromName(product), buildAction(product)));
        }
        body.getChildren().setAll(scrollPane);
    }

    /** shows the staff buttons of the toolbar in manage mode only */
    private void updateActions() {
        boolean managing = managing();
        Ui.show(addButton, managing);
        Ui.show(saveButton, managing);
    }

    /** @return true if a clerk or manager is in manage mode, false for customers, guests and the preview */
    private boolean managing() {
        return staffView && MODE_MANAGE.equals(modeToggle.getSelected());
    }

    /**
     * lists the shelf products that pass the filter and the search, in the picked order
     * <p>this is a copy of the list, the shelf itself is never sorted or changed
     *
     * @return the products to show
     */
    private ArrayList<Product> visibleProducts() {
        ArrayList<Product> result = new ArrayList<>();

        for (Product product : shelfData.getProducts()) {
            if (passesFilter(product) && matchesSearch(product)) result.add(product);
        }

        result.sort(sortOrder());
        return result;
    }

    /**
     * checks the made-from filter, a product counts as made when its madeFrom is set
     *
     * @param product the shelf product to check
     * @return true if the product should be shown
     */
    private boolean passesFilter(Product product) {
        String filter = filterToggle.getSelected();

        if (FILTER_HARVEST.equals(filter)) return !product.isMade();
        if (FILTER_MADE.equals(filter)) return product.isMade();
        return true; // "all products" shows everything
    }

    /**
     * checks the search text against the name and the name of the source product
     * <p>this way a search for a harvest item also finds the products made from it
     *
     * @param product the shelf product to check
     * @return true if the search is empty or matches
     */
    private boolean matchesSearch(Product product) {
        if (query.isEmpty()) return true;

        if (product.getName().toLowerCase().contains(query)) return true;

        String source = farmSystem.getMadeFromName(product);
        return source != null && source.toLowerCase().contains(query);
    }

    /**
     * gets the order of the picked sort, ties are sorted by name so the order never jumps around
     *
     * @return the comparator of the picked sort
     */
    private Comparator<Product> sortOrder() {
        Comparator<Product> byName = Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
        String sort = sortBox.getValue();

        if (SORT_PRICE_LOW.equals(sort)) {
            return ((Comparator<Product>) (a, b) -> Double.compare(a.getPrice(), b.getPrice())).thenComparing(byName);
        }
        if (SORT_PRICE_HIGH.equals(sort)) {
            return ((Comparator<Product>) (a, b) -> Double.compare(b.getPrice(), a.getPrice())).thenComparing(byName);
        }
        if (SORT_STOCK.equals(sort)) {
            return ((Comparator<Product>) (a, b) -> Integer.compare(b.getStock(), a.getStock())).thenComparing(byName);
        }
        return byName;
    }

    // ---------- card buttons ----------

    /**
     * creates the button of a card, which one depends on who is looking
     * <p>manage mode: only managers get a move button. preview mode: a disabled request button.
     * guests are asked to log in, customers can request to buy
     *
     * @param product the shelf product of the card
     * @return the button, or null if this user gets none
     */
    private Button buildAction(Product product) {
        if (staffView) {
            if (managing()) {
                if (!manager.canMoveToInventory()) return null; // clerks add stock, they can't take it back

                Button move = new Button("Move to Inventory");
                move.getStyleClass().add("secondary");
                move.setOnAction(event -> new MoveProductWindow(
                    stage, farmSystem, product, true,
                    Ui.afterAction(manager, this::refresh, "Stock moved back to the inventory")
                ).show());
                return move;
            }

            // the preview shows what customers see, staff can't buy
            Button preview = new Button("Request to Buy");
            preview.setDisable(true);
            return preview;
        }

        if (currentUser.getUserType().equals(User.GUEST)) {
            Button logIn = new Button("Log In to Request");
            logIn.setOnAction(event -> manager.showLogin());
            return logIn;
        }

        if (manager.canRequestToBuy()) {
            Button request = new Button("Request to Buy");
            request.setOnAction(event -> new RequestToBuyWindow(
                stage, farmSystem, product, currentUser.getUsername(),
                Ui.afterAction(manager, this::refresh, "Request sent, a clerk will confirm it")
            ).show());
            return request;
        }

        return null;
    }

    /** opens the window that moves inventory stock to the shelf */
    private void openAddWindow() {
        new AddProductWindow(
            stage, farmSystem, inventoryData, true,
            Ui.afterAction(manager, this::refresh, "Stock added to the shelf")
        ).show();
    }

    /**
     * manual save, writes every product file and the transactions as a backup
     * <p>the toast tells the staff member that it worked, there is no settings page
     */
    private void saveData() {
        manager.saveProductContainers();
        TransactionFilesHandler.save(manager.getTransactions());
        manager.toast("All data was saved");
    }

    // ---------- empty states ----------

    /**
     * builds the message for a shelf with no products at all
     * <p>staff in manage mode can add stock right away, the other users are sent somewhere else
     *
     * @return the empty state
     */
    private EmptyState buildShelfEmptyState() {
        if (managing()) {
            return new EmptyState(
                "The shelf is empty",
                "Add products from the inventory to put them on sale.",
                "Add From Inventory",
                this::openAddWindow
            );
        }
        if (staffView) { // the preview shows customers an empty shelf
            return new EmptyState(
                "The shelf is empty",
                "Customers see nothing yet. Switch to Manage Shelf to add products.",
                "Manage Shelf",
                () -> modeToggle.select(MODE_MANAGE)
            );
        }
        return new EmptyState(
            "Nothing on the shelf yet",
            "Check back soon, the farm is restocking.",
            "Back to Home",
            manager::showHome
        );
    }

    /**
     * builds the message for a search or filter that matches nothing
     *
     * @return the empty state, its button clears the search and the filter
     */
    private EmptyState buildNoMatchState() {
        return new EmptyState(
            "No products match",
            "Try another search or filter.",
            "Clear search and filters",
            this::clearFilters
        );
    }

    /** empties the search box and goes back to the all products filter */
    private void clearFilters() {
        manager.clearSearch(); // the search box tells this page through onSearch
        filterToggle.select(FILTER_ALL);
        refresh();
    }
}