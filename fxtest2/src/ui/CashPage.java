package ui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

import application.Money;
import application.SaleTransaction;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * the page for clerks and managers to see the cash balance and every cash entry
 *
 * <p>there is no cash file or model, the entries are worked out from the transactions.
 * a confirmed sale is money in, a voided confirmed sale is money out,
 * and a request voided before it was confirmed never moved any money, so it has no entry.
 * every entry shows the customer, the staff member who handled it, the amount and the time
 *
 * <p>the balance card comes from FarmSystem, so it is the same number as the staff home tile.
 * the search box of the top bar filters by product, customer and staff.
 * the page is created again every time it opens, so it is always up to date
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 2.2
 * @since 2.0 the page replaces the old cash scene, the entries got a customer column
 * @since 2.1 the garbled peso sign in the comments was replaced with plain words
 * @since 2.2 the balance text comes from Money
 * @see application.FarmSystem#getCashBalance()
 * @see SceneManager#showCash()
 */
public class CashPage extends BorderPane implements Searchable {

    /** one cash movement, only used inside this page */
    private static class CashEntry {
        final LocalDateTime time;
        final String type; // "Sale" or "Void"
        final SaleTransaction transaction;
        final String staff; // who confirmed or voided it
        final double amount; // positive for money in, negative for money out

        CashEntry(LocalDateTime time, String type, SaleTransaction transaction, String staff, double amount) {
            this.time = time;
            this.type = type;
            this.transaction = transaction;
            this.staff = staff;
            this.amount = amount;
        }
    }

    /** the table of cash entries */
    private static class CashTable extends DataTable<CashEntry> {
        CashTable() {
            addColumn("Time", e -> SaleTransaction.formatTime(e.time), 140);
            addColumn("Type", e -> e.type, 70);
            addColumn("Product", e -> e.transaction.getProductName(), 200);
            addColumn("Qty", e -> e.transaction.getQuantity(), 50);
            addColumn("Customer", e -> e.transaction.getRequester(), 110);
            addColumn("By", e -> e.staff == null ? "" : e.staff, 110);
            addMoneyColumn("Amount", e -> e.amount, 110);
        }
    }

    private final CashTable table = new CashTable();

    /** the typed search text in lowercase, empty shows everything */
    private String query = "";

    /**
     * creates the page
     *
     * @param manager the scene manager, gives the transactions and the farm system
     */
    public CashPage(SceneManager manager) {
        setPadding(new Insets(24));

        PageToolbar toolbar = new PageToolbar("Cash");
        toolbar.setSubtitle("Every sale and void, newest first");

        // the balance card on top of the list
        Label caption = new Label("Cash balance");
        caption.getStyleClass().add("caption");

        Label balance = new Label(Money.format(manager.getFarmSystem().getCashBalance()));
        balance.getStyleClass().add("page-title");

        Label note = new Label("from confirmed sales");
        note.getStyleClass().add("caption");

        VBox card = new VBox(4, caption, balance, note);
        card.getStyleClass().add("card");
        card.setMaxWidth(280); // a small card on the left, not a bar across the page

        setTop(new VBox(16, toolbar, card));

        table.setRows(buildEntries(manager));
        applyFilter();

        setCenter(table);
        BorderPane.setMargin(table, new Insets(16, 0, 0, 0));
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
        return "Search product, customer or staff";
    }

    /** hides the rows that do not match, and picks the right empty message */
    private void applyFilter() {
        table.setFilter(this::matches);
        table.setEmptyText(table.getTotalCount() == 0 ? "There are no cash entries yet" : "No entries match");
    }

    /**
     * checks the search text against the product, the customer and the staff
     *
     * @param entry the entry to check
     * @return true if the search is empty or matches
     */
    private boolean matches(CashEntry entry) {
        if (query.isEmpty()) return true;

        String staff = entry.staff == null ? "" : entry.staff.toLowerCase();
        return entry.transaction.getProductName().toLowerCase().contains(query)
            || entry.transaction.getRequester().toLowerCase().contains(query)
            || staff.contains(query);
    }

    // ---------- entries ----------

    /**
     * works out the cash entries from the transactions, newest first
     *
     * @param manager gives the transactions
     * @return one entry for every confirmed sale, and one more for every confirmed sale that was voided
     */
    private ArrayList<CashEntry> buildEntries(SceneManager manager) {
        ArrayList<CashEntry> entries = new ArrayList<>();

        for (SaleTransaction t : manager.getTransactions().getTransactions()) {
            if (!t.wasConfirmed()) continue; // never confirmed, no money moved

            // money in when it was confirmed
            entries.add(new CashEntry(
                timeOrFallback(t.getConfirmedAt(), t),
                "Sale", t, t.getConfirmedBy(), t.getTotal()
            ));

            // money out when a confirmed sale was voided
            if (t.getStatus().equals(SaleTransaction.VOIDED)) {
                entries.add(new CashEntry(
                    timeOrFallback(t.getVoidedAt(), t),
                    "Void", t, t.getVoidedBy(), -t.getTotal()
                ));
            }
        }

        entries.sort(Comparator.comparing((CashEntry e) -> e.time).reversed());
        return entries;
    }

    /**
     * uses the request time if a saved time is missing (for example a hand edited file)
     *
     * @param time the saved time, can be null
     * @param t the transaction the time belongs to
     * @return a time that is never null
     */
    private LocalDateTime timeOrFallback(LocalDateTime time, SaleTransaction t) {
        return time != null ? time : t.getRequestedAt();
    }
}