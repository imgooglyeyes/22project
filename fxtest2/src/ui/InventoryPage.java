package ui;

import application.FarmSystem;
import application.Product;
import application.ProductContainer;
import backend.AddProductWindow;
import backend.MoveProductWindow;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * the page that shows the products in the inventory
 *
 * <p>clerks and managers can look at the inventory, but only managers get the restock and delete stocks buttons
 * (see SceneManager#canManageInventory()). every stock change goes through FarmSystem, this page only shows the inventory
 * and answers a change that worked with a toast.
 * the search box of the top bar filters the rows by name
 *
 * <p>the page is created again every time it opens, and the popup windows tell it to redraw after a change,
 * the code they run comes from Ui.afterAction
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 2.3
 * @since 2.0 the page replaces the old inventory scene and uses the shared product table
 * @since 2.1 toasts for the restock and the delete stocks actions
 * @since 2.2 the unused stage field was removed, the constructor parameter is still used by the popups
 * @since 2.3 the after code moved to Ui, so the manager field was removed too
 * @see ProductTable
 * @see AddProductWindow
 * @see MoveProductWindow
 * @see Ui
 */
public class InventoryPage extends BorderPane implements Searchable {

    private final ProductContainer inventoryData;

    private final PageToolbar toolbar = new PageToolbar("Inventory");
    private final ProductTable table;

    private final Button restockButton = new Button("Restock");
    private final Button deleteButton = new Button("Delete Stocks");

    /** the typed search text in lowercase, empty shows everything */
    private String query = "";

    /**
     * creates the page with the buttons that fit the user
     *
     * @param stage the main window, the popup windows are opened on it
     * @param manager the scene manager, answers the role checks, gives the farm system and shows toasts
     * @param database every product, the source when restocking the inventory
     * @param inventoryData the products and stock in the inventory
     */
    public InventoryPage(Stage stage, SceneManager manager, ProductContainer database, ProductContainer inventoryData) {
        this.inventoryData = inventoryData;
        this.table = new ProductTable(manager.getFarmSystem(), false);

        setPadding(new Insets(24));

        // throwing stock away can't be undone, so it is a danger button
        deleteButton.getStyleClass().add("danger");
        deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        FarmSystem farmSystem = manager.getFarmSystem();

        // adds new stock of a product from the database
        restockButton.setOnAction(event -> new AddProductWindow(
            stage, farmSystem, database, false,
            Ui.afterAction(manager, this::refresh, "Stock added to the inventory")
        ).show());

        // removes stocks from the inventory
        deleteButton.setOnAction(event -> {
            Product product = table.getSelected();
            if (product != null) {
                new MoveProductWindow(
                    stage, farmSystem, product, false,
                    Ui.afterAction(manager, this::refresh, "Stocks deleted from the inventory")
                ).show();
            }
        });

        // only managers change the inventory stock, clerks just look at it
        if (manager.canManageInventory()) toolbar.addAction(restockButton, deleteButton);

        setTop(toolbar);
        setCenter(table);
        BorderPane.setMargin(table, new Insets(16, 0, 0, 0));

        refresh();
    }

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
        return "Search inventory";
    }

    /** loads the inventory again and applies the filter */
    private void refresh() {
        table.setRows(inventoryData.getProducts());
        applyFilter();
    }

    /** hides the rows that do not match, and updates the empty message and the caption under the title */
    private void applyFilter() {
        table.setFilter(this::matches);
        table.setEmptyText(table.getTotalCount() == 0 ? "The inventory is empty" : "No products match");

        int total = table.getTotalCount();
        int shown = table.getShownCount();
        toolbar.setSubtitle(shown == total
            ? total + (total == 1 ? " product" : " products") + " in the inventory"
            : shown + " of " + total + " products"
        );
    }

    /**
     * checks the search text against the name
     *
     * @param product the product to check
     * @return true if the search is empty or matches
     */
    private boolean matches(Product product) {
        return query.isEmpty() || product.getName().toLowerCase().contains(query);
    }
}