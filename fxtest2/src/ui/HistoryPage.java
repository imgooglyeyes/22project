package ui;

import application.SaleTransaction;
import application.User;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

/**
 * the page where a clerk sees the requests they took action on, a manager can open the history of any staff member
 *
 * <p>a request counts as taken when the staff member confirmed it or voided it.
 * the page is read only, nothing can be confirmed or voided from here.
 * a manager gets a button back to the staff page, because that is where the history is opened from.
 * the page is created again every time it opens, so the list is always up to date
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 2.0
 * @since 2.0 the page replaces the old history scene and uses the shared transaction table
 * @see TransactionTable
 * @see SceneManager#showHistory(User)
 */
public class HistoryPage extends BorderPane implements Searchable {

    private final TransactionTable table = new TransactionTable(false);

    /** the message shown when the staff member has no history at all */
    private final String noHistoryText;

    /** the typed search text in lowercase, empty shows everything */
    private String query = "";

    /**
     * creates the page
     *
     * @param manager the scene manager, gives the transactions and opens the staff page
     * @param currentUser the user who is logged in
     * @param targetUser the staff member whose history is shown
     */
    public HistoryPage(SceneManager manager, User currentUser, User targetUser) {
        // compared by username, the logged in user is a copy of the account in the user list
        boolean own = currentUser.getUsername().equals(targetUser.getUsername());

        this.noHistoryText = own
            ? "You have taken no requests yet"
            : targetUser.getUsername() + " has taken no requests yet";

        setPadding(new Insets(24));

        PageToolbar toolbar = new PageToolbar(own ? "My History" : targetUser.getUsername() + "'s History");
        toolbar.setSubtitle("Requests that " + (own ? "you" : targetUser.getUsername()) + " confirmed or voided");

        // a manager came from the staff page, so there is a way back
        if (manager.canViewStaffList()) {
            Button back = new Button("Back to Staff");
            back.getStyleClass().add("secondary");
            back.setOnAction(event -> manager.showStaffList());
            toolbar.addAction(back);
        }

        table.setTransactions(manager.getTransactions().getTransactionsByActor(targetUser.getUsername()));
        applyFilter();

        setTop(toolbar);
        setCenter(table);
        BorderPane.setMargin(table, new Insets(16, 0, 0, 0));
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
        return "Search product or customer";
    }

    /** hides the rows that do not match the search, and picks the right empty message */
    private void applyFilter() {
        table.setFilter(this::matches);
        table.setEmptyText(table.getTotalCount() == 0 ? noHistoryText : "No transactions match");
    }

    /**
     * checks the search text against the product and the customer
     *
     * @param t the transaction to check
     * @return true if the search is empty or matches
     */
    private boolean matches(SaleTransaction t) {
        if (query.isEmpty()) return true;
        return t.getProductName().toLowerCase().contains(query)
            || t.getRequester().toLowerCase().contains(query);
    }
}