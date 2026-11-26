package ui;

import application.FarmSystem;
import application.Product;
import application.ProductContainer;
import backend.ProductCreationWindow;
import backend.ProductEditingWindow;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * the page where a manager creates, edits and deletes products, and puts all product data back to the template data
 *
 * <p>only managers can open this page (see SceneManager#canManageDatabase()), so every button is shown without a role check.
 * every product change goes through FarmSystem, this page only shows the database and answers a change that worked with a toast.
 * the delete and the reset both ask for a confirmation first, because a delete removes the product
 * from the database, inventory and shelf, and the reset replaces all three with the template files.
 * transactions and users are not touched by either
 *
 * <p>the toolbar has a made-from filter like the marketplace, the search box of the top bar filters by name
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 2.3
 * @since 2.0 the page replaces the old database scene and uses the shared product table
 * @since 2.1 toasts replace the info dialog and answer the create, edit and delete actions
 * @since 2.2 the unused stage field was removed, the constructor parameter is still used by the popups
 * @since 2.3 the after code and owner moved to Ui
 * @see ProductTable
 * @see ProductCreationWindow
 * @see ProductEditingWindow
 * @see Ui
 * @see SceneManager#resetToDefaults()
 */
public class ProductsPage extends BorderPane implements Searchable {

    /** filter choices */
    private static final String FILTER_ALL = "All products";
    private static final String FILTER_HARVEST = "Harvest";
    private static final String FILTER_MADE = "Made from harvest";

    private final SceneManager manager;
    private final FarmSystem farmSystem;
    private final ProductContainer database;

    private final PageToolbar toolbar = new PageToolbar("Products");
    private final SegmentedToggle filterToggle = new SegmentedToggle(FILTER_ALL, FILTER_HARVEST, FILTER_MADE);
    private final ProductTable table;

    private final Button createButton = new Button("Create Product");
    private final Button editButton = new Button("Edit");
    private final Button deleteButton = new Button("Delete Everywhere");
    private final Button resetButton = new Button("Reset To Default Data");

    /** the typed search text in lowercase, empty shows everything */
    private String query = "";

    /**
     * creates the page
     *
     * @param stage the main window, the popup windows are opened on it
     * @param manager the scene manager, gives the farm system, does the reset and shows toasts
     * @param database every product and its details
     */
    public ProductsPage(Stage stage, SceneManager manager, ProductContainer database) {
        this.manager = manager;
        this.farmSystem = manager.getFarmSystem();
        this.database = database;
        this.table = new ProductTable(farmSystem, true);

        setPadding(new Insets(24));

        // edit is a secondary action, delete and reset can't be undone so they are danger buttons
        editButton.getStyleClass().add("secondary");
        deleteButton.getStyleClass().add("danger");
        resetButton.getStyleClass().add("danger");

        // edit and delete need a selected product
        editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        createButton.setOnAction(event -> new ProductCreationWindow(
            stage, farmSystem,
            Ui.afterAction(manager, this::refresh, "Product created")
        ).show());

        editButton.setOnAction(event -> {
            Product product = table.getSelected();
            if (product != null) {
                new ProductEditingWindow(
                    stage, farmSystem, product,
                    Ui.afterAction(manager, this::refresh, "Product updated")
                ).show();
            }
        });

        deleteButton.setOnAction(event -> deleteSelected());
        resetButton.setOnAction(event -> resetData());

        filterToggle.setOnChange(this::applyFilter);
        toolbar.addFilter(filterToggle);
        toolbar.addAction(createButton, editButton, deleteButton, resetButton);

        setTop(toolbar);
        setCenter(table);
        BorderPane.setMargin(table, new Insets(16, 0, 0, 0));

        refresh();
    }

    // ---------- search ----------

    /**
     * filters the rows by the text of the search box
     *
     * @param text what is typed in the search box
     */
    @Override
    public void onSearch(String text) {
        query = text == null ? "" : text.trim().toLowerCase();
        applyFilter();
    }

    /** @return the faint text of the search box on this page */
    @Override
    public String searchPrompt() {
        return "Search products";
    }

    // ---------- drawing ----------

    /** loads the database again and applies the filter */
    private void refresh() {
        table.setRows(database.getProducts());
        applyFilter();
    }

    /** hides the rows that do not match, and updates the empty message and the caption under the title */
    private void applyFilter() {
        table.setFilter(this::matches);
        table.setEmptyText(table.getTotalCount() == 0 ? "The database has no products yet" : "No products match");

        int total = table.getTotalCount();
        int shown = table.getShownCount();
        toolbar.setSubtitle(shown == total
            ? total + (total == 1 ? " product" : " products") + " in the database"
            : shown + " of " + total + " products"
        );
    }

    /**
     * checks the made-from filter and the search text, a product counts as made when its madeFrom is set
     *
     * @param product the product to check
     * @return true if the product should be shown
     */
    private boolean matches(Product product) {
        String filter = filterToggle.getSelected();
        if (FILTER_HARVEST.equals(filter) && product.isMade()) return false;
        if (FILTER_MADE.equals(filter) && !product.isMade()) return false;

        return query.isEmpty() || product.getName().toLowerCase().contains(query);
    }

    // ---------- actions ----------

    /** deletes the selected product from the database, inventory and shelf, the manager has to confirm first */
    private void deleteSelected() {
        Product product = table.getSelected();
        if (product == null) return;

        if (!Dialogs.confirm(Ui.windowOf(this),
                "Delete " + product.getName() + " everywhere?\n" +
                "It will be removed from the database, inventory and shelf.\n" +
                "Products made from it will become harvest items.\n" +
                "Old transactions are kept.")) {
            return; // the manager changed their mind, nothing is deleted
        }

        String name = product.getName(); // read before the delete, the toast uses it

        // FarmSystem returns null when it worked, or an error message
        String error = farmSystem.deleteEverywhere(product);

        refresh();

        if (error != null) Dialogs.error(Ui.windowOf(this), error);
        else manager.toast(name + " was deleted everywhere");
    }

    /** puts the database, inventory and shelf back to the template data, the manager has to confirm first */
    private void resetData() {
        if (!Dialogs.confirm(Ui.windowOf(this),
                "Reset the database, inventory and shelf to the template data?\n" +
                "Every product change and stock change will be lost.\n" +
                "Transactions and users are not changed.")) {
            return;
        }

        // SceneManager returns false if the user is not a manager or a file failed
        boolean worked = manager.resetToDefaults();

        refresh();

        if (worked) manager.toast("Product data was reset to the template data");
        else Dialogs.error(Ui.windowOf(this), "The reset failed, nothing was changed");
    }
}