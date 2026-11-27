package pages;

import java.util.ArrayList;

import application.SaleTransaction;
import application.User;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import ui.EmptyState;
import shell.PageToolbar;
import shell.SceneManager;
import tables.TransactionTable;

/**
 * the page where a customer sees only their own purchase requests and their status
 *
 * <p>it is read only, customers can't confirm or void anything. when the customer has no request yet,
 * an empty state with a button to the marketplace takes the place of the table.
 * the page is created again every time it opens, so the list is always up to date
 *
 * @author Shawn Alfred Padilla
 * @version 2.0
 * @since 2.0 the page replaces the old my requests scene and uses the shared transaction table
 * @see TransactionTable
 * @see EmptyState
 * @see SceneManager#showMyRequests()
 */
public class MyRequestsPage extends BorderPane {

    /**
     * creates the page
     *
     * @param manager the scene manager, gives the transactions and opens the marketplace
     * @param currentUser the customer who is logged in
     */
    public MyRequestsPage(SceneManager manager, User currentUser) {
        setPadding(new Insets(24));

        ArrayList<SaleTransaction> mine =
            manager.getTransactions().getTransactionsByRequester(currentUser.getUsername());
        int waiting = manager.getFarmSystem().countPendingRequests(currentUser.getUsername());

        PageToolbar toolbar = new PageToolbar("My Requests");
        toolbar.setSubtitle(mine.isEmpty()
            ? "Your requests will show up here"
            : waiting + (waiting == 1 ? " request is" : " requests are") + " waiting for a clerk"
        );
        setTop(toolbar);

        Node center;
        if (mine.isEmpty()) {
            center = new EmptyState(
                "No requests yet",
                "Pick a product from the marketplace and send a request to buy.",
                "Browse the marketplace",
                manager::showShelf
            );
        } else {
            TransactionTable table = new TransactionTable(true); // the short customer view
            table.setTransactions(mine);
            table.setEmptyText("You have no requests yet");
            center = table;
        }

        setCenter(center);
        BorderPane.setMargin(center, new Insets(16, 0, 0, 0));
    }
}