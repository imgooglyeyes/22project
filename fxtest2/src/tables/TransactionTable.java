package tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import application.SaleTransaction;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import pages.HistoryPage;
import pages.MyRequestsPage;
import pages.TransactionsPage;

/**
 * the table of sale transactions, used by the transactions, history and my requests pages
 *
 * <p>the staff view has every column, the customer view only has the columns a customer cares about.
 * the status is shown as a chip: amber while it waits, mint when confirmed and muted when voided.
 * the newest transaction comes first until the person clicks a column to sort
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see DataTable
 * @see TransactionsPage
 * @see HistoryPage
 * @see MyRequestsPage
 */
public class TransactionTable extends DataTable<SaleTransaction> {

    /**
     * creates the table with its columns
     *
     * @param customerView true for the short list of a customer, false for the full staff list
     */
    public TransactionTable(boolean customerView) {
        if (!customerView) addColumn("ID", SaleTransaction::getId, 50);

        addColumn("Product", SaleTransaction::getProductName, 200);
        addColumn("Qty", SaleTransaction::getQuantity, 50);
        addMoneyColumn("Total", SaleTransaction::getTotal, 100);

        if (!customerView) addColumn("Requester", SaleTransaction::getRequester, 110);

        addStatusColumn();
        addColumn("Requested", t -> SaleTransaction.formatTime(t.getRequestedAt()), 140);

        if (!customerView) {
            addColumn("Confirmed By", t -> textOrEmpty(t.getConfirmedBy()), 110);
            addColumn("Voided By", t -> textOrEmpty(t.getVoidedBy()), 110);
        }
    }

    /**
     * shows the given transactions, newest first
     *
     * @param transactions the transactions in the order of the container (oldest first), the list is copied
     */
    public void setTransactions(List<SaleTransaction> transactions) {
        ArrayList<SaleTransaction> newestFirst = new ArrayList<>(transactions);
        Collections.reverse(newestFirst); // the container gives oldest first, this is a copy so it is safe to reverse
        setRows(newestFirst);
    }

    /** adds the status column, its cells are chips so the status can be seen at a glance */
    private void addStatusColumn() {
        TableColumn<SaleTransaction, String> status = addColumn("Status", SaleTransaction::getStatus, 100);

        status.setCellFactory(col -> new TableCell<SaleTransaction, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setGraphic(null);
                    return;
                }

                // a new chip every time, so a reused cell never keeps the color of its old row
                Label chip = new Label(value);
                chip.getStyleClass().add("chip");
                if (value.equals(SaleTransaction.REQUESTED)) chip.getStyleClass().add("chip-amber");
                if (value.equals(SaleTransaction.VOIDED)) chip.getStyleClass().add("chip-muted");
                setGraphic(chip);
            }
        });
    }

    /**
     * gets empty text instead of null, for the confirmer and the voider
     *
     * @param text the text or null
     * @return the text, or an empty text
     */
    private static String textOrEmpty(String text) {
        return text == null ? "" : text;
    }
}