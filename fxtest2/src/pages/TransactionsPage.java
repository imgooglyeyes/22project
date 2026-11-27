package pages;

import application.FarmSystem;
import application.SaleTransaction;
import application.User;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import ui.Dialogs;
import ui.Searchable;
import ui.SegmentedToggle;
import ui.Ui;
import shell.PageToolbar;
import shell.SceneManager;
import tables.TransactionTable;

/**
 * the page where clerks and managers see every transaction and confirm or void it
 *
 * <p>the rules (stock checks, saving) are in FarmSystem, this page only asks and shows the result.
 * FarmSystem returns null when it worked, or an error message that is shown in an error dialog.
 * a confirm or a void that worked is answered with a toast and the bell of the top bar is counted again
 *
 * <p>a confirm that can't work because the shelf has less than the request needs is stopped before the
 * confirm question, with a warning that says what to do next. a confirm that worked and left the shelf
 * low adds a short note to its toast, so the staff member knows to add more stock
 *
 * <p>voiding a confirmed sale of a product that was deleted can't return the stock, so the confirm dialog
 * and the toast say so, the cash is still taken back. FarmSystem#canReturnStock(SaleTransaction) is the rule
 *
 * <p>the toolbar has the status filter and the confirm and void buttons, the search box of the top bar
 * filters by product and by customer. the page is created again every time it opens
 *
 * @author Shawn Alfred Padilla
 * @version 1.3
 * @since 1.1 toasts for confirm and void, the bell is updated after both
 * @since 1.2 the void messages tell when the stock can't be returned, owner moved to Ui
 * @since 1.3 a warning with a next step before a confirm that can't work, and a low stock note on the toast
 * @see TransactionTable
 * @see Searchable
 * @see Ui
 * @see Dialogs#warning(javafx.stage.Window, String, String)
 * @see FarmSystem#confirm(SaleTransaction, String)
 * @see FarmSystem#voidTransaction(SaleTransaction, String)
 * @see FarmSystem#canReturnStock(SaleTransaction)
 * @see FarmSystem#getShelfStock(int)
 */
public class TransactionsPage extends BorderPane implements Searchable {

    /** the choice that shows every status */
    private static final String SHOW_ALL = "All";

    private final SceneManager manager;
    private final FarmSystem farmSystem;
    private final User currentUser;

    private final PageToolbar toolbar = new PageToolbar("Transactions");
    private final SegmentedToggle statusToggle = new SegmentedToggle(
        SHOW_ALL, SaleTransaction.REQUESTED, SaleTransaction.CONFIRMED, SaleTransaction.VOIDED
    );
    private final TransactionTable table = new TransactionTable(false);

    private final Button confirmButton = new Button("Confirm");
    private final Button voidButton = new Button("Void");

    /** the typed search text in lowercase, empty shows everything */
    private String query = "";

    /**
     * creates the page
     *
     * @param manager the scene manager, gives the transactions and the farm system, and shows toasts
     * @param currentUser the clerk or manager who is logged in, their name is saved on what they confirm or void
     */
    public TransactionsPage(SceneManager manager, User currentUser) {
        this.manager = manager;
        this.farmSystem = manager.getFarmSystem();
        this.currentUser = currentUser;

        setPadding(new Insets(24));

        voidButton.getStyleClass().add("danger");

        // confirm only works on requested transactions
        confirmButton.disableProperty().bind(Bindings.createBooleanBinding(
            () -> table.getSelected() == null || !table.getSelected().canConfirm(),
            table.getSelectionModel().selectedItemProperty()
        ));

        // void works on anything that is not voided yet
        voidButton.disableProperty().bind(Bindings.createBooleanBinding(
            () -> table.getSelected() == null || !table.getSelected().canVoid(),
            table.getSelectionModel().selectedItemProperty()
        ));

        confirmButton.setOnAction(event -> confirmSelected());
        voidButton.setOnAction(event -> voidSelected());

        statusToggle.setOnChange(this::applyFilter);
        toolbar.addFilter(statusToggle);
        toolbar.addAction(confirmButton, voidButton);

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
        return "Search product or customer";
    }

    // ---------- drawing ----------

    /** loads the transactions again and applies the filter */
    private void refresh() {
        table.setTransactions(manager.getTransactions().getTransactions());
        applyFilter();
    }

    /** hides the rows that do not match, and updates the empty message and the caption under the title */
    private void applyFilter() {
        table.setFilter(this::matches);
        table.setEmptyText(table.getTotalCount() == 0 ? "There are no transactions yet" : "No transactions match");

        int waiting = farmSystem.countPendingRequests();
        toolbar.setSubtitle(waiting == 0
            ? "No requests are waiting"
            : waiting + (waiting == 1 ? " request is" : " requests are") + " waiting for confirmation"
        );
    }

    /**
     * checks the status filter and the search text
     *
     * @param t the transaction to check
     * @return true if the row should be shown
     */
    private boolean matches(SaleTransaction t) {
        String status = statusToggle.getSelected();
        if (!SHOW_ALL.equals(status) && !t.getStatus().equals(status)) return false;

        if (query.isEmpty()) return true;
        return t.getProductName().toLowerCase().contains(query)
            || t.getRequester().toLowerCase().contains(query);
    }

    // ---------- actions ----------

    /**
     * confirms the selected request, this is when stock leaves the shelf
     * <p>the shelf is checked first, a request that needs more than the shelf has is not confirmed,
     * the staff member gets a warning with the choices instead of a confirm question that would fail
     */
    private void confirmSelected() {
        SaleTransaction t = table.getSelected();
        if (t == null) return;

        // stop here if the shelf can't cover the request, FarmSystem checks it again anyway
        int onShelf = farmSystem.getShelfStock(t.getProductId());
        if (onShelf < t.getQuantity()) {
            String message = onShelf == 0
                ? t.getProductName() + " has no stock on the shelf, the request needs " + t.getQuantity()
                : "Only " + onShelf + " of " + t.getProductName() + " left on the shelf, the request needs " + t.getQuantity();

            Dialogs.warning(Ui.windowOf(this), message,
                "Add stock to the shelf from the inventory, or void this request so the customer can order again");
            return;
        }

        boolean proceed = Dialogs.confirm(Ui.windowOf(this),
            "Confirm " + t.getQuantity() + "x " + t.getProductName() +
            " for " + t.getFormattedTotal() + "?\nThe stock will be taken from the shelf."
        );
        if (!proceed) return;

        // FarmSystem returns null when it worked, or an error message
        String error = farmSystem.confirm(t, currentUser.getUsername());

        refresh();
        manager.refreshBell(); // one request less is waiting

        if (error != null) {
            Dialogs.error(Ui.windowOf(this), error, "The shelf may have changed, check it and try again");
            return;
        }

        // tell the staff member when the sale left the shelf low or empty
        String toast = "Sale confirmed, " + t.getFormattedTotal() + " received";
        int left = farmSystem.getShelfStock(t.getProductId());

        if (left == 0) {
            toast += ", none left on the shelf, add more from the inventory";
        } else if (left <= FarmSystem.LOW_STOCK_THRESHOLD) {
            toast += ", only " + left + " left on the shelf, add more from the inventory";
        }
        manager.toast(toast);
    }

    /**
     * voids the selected transaction, a confirmed sale gets its stock back on the shelf
     * <p>if the product was deleted the stock can't be returned, the dialog and the toast say so
     * and the cash is still taken back
     */
    private void voidSelected() {
        SaleTransaction t = table.getSelected();
        if (t == null) return;

        // both are read before the void, they decide the messages
        boolean wasConfirmed = t.wasConfirmed();
        boolean stockReturns = !wasConfirmed || farmSystem.canReturnStock(t);

        String message = "Void " + t.getQuantity() + "x " + t.getProductName() + "?";
        if (wasConfirmed) { // tell the staff what happens to the stock
            message += stockReturns
                ? "\nThe stock will be returned to the shelf."
                : "\nThe product no longer exists, so the stock can't be returned.\nThe cash will still be taken back.";
        }
        if (!Dialogs.confirm(Ui.windowOf(this), message)) return;

        String error = farmSystem.voidTransaction(t, currentUser.getUsername());

        refresh();
        manager.refreshBell(); // a waiting request that is voided is not waiting anymore

        if (error != null) {
            Dialogs.error(Ui.windowOf(this), error, "The transaction may have changed, check the list and try again");
        } else if (!wasConfirmed) {
            manager.toast("Request voided");
        } else if (stockReturns) {
            manager.toast("Sale voided, stock and cash were restored");
        } else {
            manager.toast("Sale voided, cash was restored, but the product no longer exists so the stock was not returned");
        }
    }
}